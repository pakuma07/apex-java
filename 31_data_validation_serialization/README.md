# Chapter 31: Data Validation & Serialization -- Java

## What This Chapter Covers
At every boundary — HTTP bodies, config, message queues, database rows — data
arrives **untrusted and untyped**. This chapter covers turning it into validated,
typed Java objects and back: **Jackson** (the dominant JSON library), **Jakarta
Bean Validation** (declarative constraints), and binary/interchange formats
(**Protobuf**, **Avro**) with their schema-evolution rules. The theme: **parse,
don't validate** — convert input into a well-typed object once, at the edge, then
trust it everywhere inside.

> **Version note:** Java 21, Jackson 2.x, **Jakarta** Bean Validation 3.x
> (Hibernate Validator; note the `jakarta.validation` namespace in Spring Boot 3).
> Records (Java 16+) make excellent immutable DTOs.

> **C++ contrast:** C++ has no standard reflection, so JSON libraries
> (nlohmann/json, RapidJSON) require manual or macro-based field mapping. Java's
> runtime reflection lets Jackson map JSON to objects automatically — convenient,
> but a historical security liability (31.7).

---

## 31.1 "Parse, Don't Validate"

Instead of re-checking the same raw map throughout your code, **parse** it once
into a typed object whose existence guarantees validity. Downstream code receives a
`User`, not a `Map<String,Object>`, and never re-checks fields.

```text
   untrusted input            parse at boundary           trusted domain object
  JSON / form / queue  ──▶  deserialize + validate  ──▶  User(id, email, ...)
                            (reject if invalid)          typed, guaranteed valid
```

Two directions: **deserialization** (bytes/JSON → object, reject invalid) and
**serialization** (object → bytes/JSON for transport/storage).

---

## 31.2 Jackson: JSON Binding

Jackson's `ObjectMapper` maps between JSON and Java objects via reflection. With
**records** it's nearly boilerplate-free.

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public record User(long id, String name, String email) {}

ObjectMapper mapper = new ObjectMapper();           // reuse this — it's thread-safe & expensive
String json = mapper.writeValueAsString(new User(42, "Ada", "ada@x.io"));  // serialize
User u = mapper.readValue(json, User.class);        // deserialize (validates types)
```

Key annotations and behaviors:

```java
public record OrderDto(
    @JsonProperty("order_id") long id,              // map snake_case wire -> camelCase
    @JsonFormat(shape = STRING) Instant createdAt,  // control date rendering
    @JsonInclude(NON_NULL) String note              // omit nulls from output
) {}
```

- **Reuse the `ObjectMapper`** (or Spring's configured bean) — constructing one per
  call is a real performance bug; it's thread-safe once configured.
- **Modules**: register `JavaTimeModule` for `java.time` (Chapter 20); `ParameterNamesModule`
  (auto in Boot) lets Jackson bind records/constructors without `@JsonCreator`.
- **Tolerant reader** (Chapter 38/40): `FAIL_ON_UNKNOWN_PROPERTIES=false` so adding
  fields upstream doesn't break you (forward compatibility) — but keep it strict for
  inputs where unexpected fields signal a bug.

### Streaming for large payloads

`readValue` builds the whole object graph in memory — an OOM/DoS risk on large
input. For big documents, use the **streaming API** (`JsonParser`) to process
tokens incrementally at constant memory, and always bound input size before parsing.

---

## 31.3 Jakarta Bean Validation

Declarative constraints on fields, checked with one call (or automatically by Spring
MVC via `@Valid`, Chapter 28). Hibernate Validator is the reference implementation.

```java
import jakarta.validation.constraints.*;

public record CreateUser(
    @NotBlank String name,
    @Email String email,
    @Min(0) @Max(130) int age,
    @Pattern(regexp = "\\+?[0-9]{7,15}") String phone
) {}

// Manual validation:
var violations = validator.validate(new CreateUser("", "nope", 200, "x"));
violations.forEach(v -> System.out.println(v.getPropertyPath() + " " + v.getMessage()));
```

- In Spring MVC, `@Valid @RequestBody CreateUser body` validates automatically and
  returns a 400 with the violations — the rich, structured errors clients need.
- **Custom constraints**: define an annotation + `ConstraintValidator` for domain
  rules (e.g. `@ValidSku`). **Cross-field** rules go on the type with a class-level
  constraint or `@AssertTrue` on a derived method.
- **Validation groups** let one object validate differently per context (e.g.
  `OnCreate` vs `OnUpdate`).

---

## 31.4 Immutability and DTOs vs Entities

- **Records** (Java 21) are ideal **DTOs**: immutable, transparent, with
  `equals`/`hashCode`/`toString` generated (Chapter 8). Parse external data into a
  record; pass records around internally.
- **Separate DTOs from JPA entities** (Chapter 28). Exposing entities directly
  leaks your schema, invites lazy-loading exceptions outside a transaction, and
  couples your API to your database. Map entity ↔ DTO explicitly (or with MapStruct).
- A validated record at the boundary is the Java embodiment of "parse, don't
  validate" — once constructed, it's trusted everywhere inside.

---

## 31.5 Binary and Interchange Formats

JSON is human-readable but verbose and schema-less. For performance, size, or
strong cross-language contracts, choose a binary format:

| Format | Schema | Notes |
|---|---|---|
| **JSON** | no | universal, human-readable, slow/large |
| **Protocol Buffers** | yes (`.proto`) | typed, compact, fast; gRPC default |
| **Avro** | yes | schema travels/evolves with data; big-data/Kafka pipelines |
| **Java native serialization** | no | **avoid** — slow, brittle, and a security hole (31.7) |

```protobuf
// user.proto — compiled to Java classes by protoc.
syntax = "proto3";
message User {
  int32  id   = 1;          // field NUMBERS, not names, are the contract
  string name = 2;
  repeated string tags = 3;
}
```

**gRPC** (Chapter 28/40) is Protobuf over HTTP/2 with generated stubs — the common
choice for typed, high-throughput service-to-service calls.

---

## 31.6 Schema Evolution

Data outlives code; design wire formats to change without breaking either side.
Precise definitions:

- **Backward compatible**: new code reads old data (add fields with defaults).
- **Forward compatible**: old code reads new data (ignore unknown fields).
- **Full**: both — the target for independently-deployed producers and consumers.

Format specifics:

- **Protobuf**: fields are identified by **number**; *never reuse or renumber*
  fields — mark removed ones `reserved`. Unknown fields are preserved on round-trip.
  A safe change adds an optional field; renaming or retyping is breaking.
- **Avro**: data is written with a **writer schema** and read with a **reader
  schema**; Avro resolves differences by **name** using reader defaults — so the
  writer schema must be available to the reader, which is why Avro pairs with a
  **Schema Registry** (Confluent).
- **Schema Registry** (for Kafka): stores versioned schemas, prefixes each message
  with a schema id, and **enforces a compatibility level at publish time** —
  rejecting a producer that would break existing consumers. Wire compatibility
  becomes a CI-enforced contract instead of a hope.

---

## 31.7 Deserialization Security

Deserialization is an attack surface — historically Java's worst one:

- **Never use Java native serialization (`ObjectInputStream`) on untrusted data.**
  It can instantiate arbitrary classes and trigger **gadget chains** that achieve
  remote code execution. This is a top-tier Java CVE class; prefer JSON/Protobuf for
  any data crossing a trust boundary (Chapter 32).
- **Bound and limit inputs:** enforce max payload size and nesting depth before
  parsing; deeply nested JSON can exhaust memory/stack (a billion-laughs analog).
- **Jackson polymorphic typing** (`@JsonTypeInfo` / default typing) has had its own
  RCE history — never enable default typing on untrusted input; use an allow-list of
  permitted subtypes.

```java
// Determinism for signing/caching/dedup: stable, sorted-key output.
ObjectMapper canonical = JsonMapper.builder()
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .build();   // byte-identical output for equal data
```

---

## 31.8 Performance

- **Reuse `ObjectMapper`** and pre-resolve `ObjectReader`/`ObjectWriter` for hot
  paths (thread-safe, avoids per-call setup).
- **Records + `afterburner`/`blackbird` modules** speed up Jackson by avoiding
  reflection on the hot path.
- **Validation isn't free**: it's mandatory at trust boundaries, but skip it on
  internal hops over an already-typed wire format.
- **Pick the format for the audience**: JSON for public/debuggable APIs, Protobuf
  for internal high-throughput RPC, Avro for Kafka/big-data — and *benchmark* size
  and speed on your data.

---

## Summary

- **Parse, don't validate**: turn untrusted input into a validated, typed object
  (ideally a **record**) once at the boundary, then trust it inside.
- **Jackson** binds JSON to objects via a reused thread-safe `ObjectMapper`; control
  the wire with annotations, register `JavaTimeModule`, and stream large payloads.
- **Jakarta Bean Validation** (`@NotBlank`, `@Email`, `@Valid`, custom validators)
  gives declarative, structured validation, automatic in Spring MVC.
- Separate **DTOs from JPA entities**; choose **Protobuf/Avro** for typed
  cross-language contracts and design for **schema evolution** (numbers/reserved,
  reader/writer schemas, a **Schema Registry**).
- **Deserialization is dangerous**: never native-deserialize untrusted data, bound
  input size/depth, and avoid unrestricted polymorphic typing.

## Next Steps

- Define a record DTO, validate it with `@Valid` in a controller, and observe the
  400 + violation details.
- Compile a `.proto` and compare the serialized size/speed vs Jackson JSON for the
  same object (JMH, Chapter 29).
- Add `FAIL_ON_UNKNOWN_PROPERTIES=false` and evolve a producer's schema to confirm
  forward compatibility.
- Revisit **[Chapter 8: Operators & Equality](../08_operator_overloading/README.md)**
  (records' `equals`/`hashCode`) and **[Chapter 28: Web Frameworks](../28_web_frameworks/README.md)**.
- Continue to **[Chapter 32: Security & Supply Chain](../32_security_supply_chain/README.md)**.
