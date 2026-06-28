# Chapter 35: API & Interface Design -- Java

## What This Chapter Covers
An API is a **contract** — the most expensive thing you will ever ship, because
once others depend on it you can almost never take it back. This chapter is about
designing interfaces that are correct, evolvable, and pleasant: Java library APIs
(the types and methods you publish) and network APIs (REST, gRPC, GraphQL) between
services. The staff/principal skill is **designing for change** — shipping a v1
that grows to v5 without breaking every caller.

> **Why it matters disproportionately:** internal code you refactor freely; a
> published API you cannot. **Hyrum's Law:** *with enough users, every observable
> behavior of your interface will be depended upon — including behaviors you never
> promised.* Design the surface deliberately and keep it small.

> **C++ contrast:** C++ adds **ABI stability** to the API problem — changing a class
> layout breaks compiled callers, not just source. Java's JIT and reflection avoid
> binary-layout fragility, but **source/behavioral** compatibility still rules, and
> the JDK's own deprecation discipline is the model to emulate.

---

## 35.1 Principles of Good Interface Design

These apply equally to a method signature and a microservice endpoint:

- **Easy to use correctly, hard to use incorrectly.** Make the common case the
  default; make the dangerous case require effort or be impossible.
- **Small surface area.** Every public type/method is a forever-commitment. Expose
  the minimum; keep internals package-private or in non-exported modules (JPMS,
  Chapter 15).
- **Least astonishment.** Behavior should match the name and ecosystem conventions.
- **Consistency** over local cleverness — same concepts, names, and argument order
  across the whole API.
- **Favor immutability and clear types** — return a typed object, not a `Map`, so
  callers can't depend on incidental structure.

```java
// Hard to misuse: a builder makes invalid combinations unrepresentable and reads
// self-documenting at the call site.
HttpRequest req = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/orders"))
    .timeout(Duration.ofSeconds(5))
    .header("Idempotency-Key", key)
    .POST(BodyPublishers.ofString(json))
    .build();
// vs a 6-positional-arg constructor where nobody remembers the order.
```

---

## 35.2 Designing Java Library APIs

For the types you publish to other code (Chapter 15 modules):

- **Program to interfaces.** Accept and return the most general type that works
  (`List`, not `ArrayList`; `Collection`, not your concrete class) so you can change
  implementations freely.
- **Don't break call sites.** Adding a method to an interface breaks implementers —
  use a **`default` method** (Chapter 7) to extend an interface compatibly. Adding
  an overload must not make existing calls ambiguous.
- **Return types are the contract.** Return `Optional<T>` instead of `null` for
  "maybe absent"; return immutable collections (`List.copyOf`) so callers can't
  mutate your internals.
- **Use the type system to prevent errors:** `enum`s over magic strings/ints,
  **records** for value objects, **sealed** hierarchies (Chapter 7) so callers
  exhaustively handle every case.
- **Throw meaningful exceptions** (Chapter 13) from a documented hierarchy; prefer
  unchecked for programming errors, checked only where the caller can recover.

```java
public sealed interface PaymentResult permits Approved, Declined, Pending {}
// Callers switch exhaustively (pattern matching, Chapter 2); adding a permitted
// type becomes a compile error everywhere that must handle it -> safe evolution.
```

> **Deprecate, don't delete.** Mark removed APIs `@Deprecated(since="2.5",
> forRemoval=true)`, document the replacement, keep them working for at least one
> release, and remove only on a major version (35.6). The JDK does exactly this.

---

## 35.3 REST API Design

REST models your domain as **resources** (nouns) manipulated with HTTP **methods**
(verbs). Done well it's discoverable and cache-friendly; done badly it's RPC in a
trench coat.

```text
GET    /orders            list orders        (safe, idempotent, cacheable)
POST   /orders            create an order     (not idempotent -> use idempotency key)
GET    /orders/{id}       fetch one
PUT    /orders/{id}       replace             (idempotent)
PATCH  /orders/{id}       partial update
DELETE /orders/{id}       delete              (idempotent)
```

Get these right and most REST debates disappear:

- **Resource-oriented URLs** (plural nouns): `/users/42/orders`, not
  `/getUserOrders?id=42`. Verbs live in the HTTP method.
- **Correct status codes:** `200/201/204` success; `400` bad request, `401`
  unauthenticated, `403` unauthorized, `404` not found, `409` conflict, `422`
  validation, `429` rate-limited; `500`/`503` server. Don't return `200` with an
  error body — callers and proxies rely on the code.
- **Method semantics:** `GET` is **safe** (no side effects) and cacheable;
  `PUT`/`DELETE` **idempotent**; `POST` is neither — protect it with an
  **idempotency key** (Chapter 33.8).
- **Consistent error shape:** Spring's `ProblemDetail` (RFC 9457) gives a standard
  machine-readable envelope with a code and message.

```java
@RestController
@RequestMapping("/orders")
class OrderController {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)               // 201, not default 200
    OrderDto create(@Valid @RequestBody CreateOrder body) {   // @Valid -> Chapter 31
        return service.create(body);
    }

    @GetMapping("/{id}")
    OrderDto get(@PathVariable long id) {
        return service.find(id).orElseThrow(() -> new NotFoundException(id));  // -> 404
    }
}
```

### Pagination and field selection

- **Never return an unbounded list.** Prefer **cursor-based** pagination (opaque
  cursor pointing at the last item) over offset-based — offsets are slow on large
  tables and skip/duplicate rows under concurrent writes.
- Support **filtering, sorting, and field selection** via query params so clients
  fetch exactly what they need (and you can cache by them).

---

## 35.4 RPC and gRPC

When the relationship is **action-oriented service-to-service** (not resource CRUD),
RPC fits better than REST. **gRPC** is the common choice: Protobuf (Chapter 31)
over HTTP/2 with generated Java stubs, streaming, and small fast payloads.

```protobuf
service OrderService {
  rpc CreateOrder (CreateOrderRequest) returns (Order);
  rpc WatchOrders (WatchRequest) returns (stream Order);   // server streaming
}
```

REST vs gRPC, honestly:

| | REST/JSON | gRPC/Protobuf |
|---|---|---|
| Payload | text, large, human-readable | binary, small, fast |
| Browser-native | yes | no (needs grpc-web/proxy) |
| Streaming | limited (SSE/WebSocket) | first-class (bi-directional) |
| Contract | OpenAPI (often after the fact) | `.proto` (contract-first, codegen) |
| Best for | public/partner APIs, browsers | internal high-throughput services |

The staff pattern is often **both**: gRPC between internal services for speed and a
typed contract; REST/JSON (or GraphQL) at the public edge for reach.

---

## 35.5 GraphQL — and When Not To

GraphQL lets clients request *exactly* the fields they need in one round trip,
solving REST's over-/under-fetching for rich, client-driven UIs (mobile especially).
Spring for GraphQL integrates it cleanly.

The costs to weigh before adopting it:

- **Server complexity** moves up: you own query planning, depth/complexity limits (a
  malicious deep query is a DoS), and the **N+1 problem** in resolvers — mitigated
  with batching (DataLoader).
- **Caching is harder:** you lose HTTP/CDN caching by URL (every query is a `POST`
  to one endpoint); you cache at the field/resolver level instead.
- **Use it** to aggregate many backends behind one flexible graph for diverse
  clients; **skip it** for simple CRUD or a single first-party client where REST is
  less machinery.

---

## 35.6 Versioning and Backward Compatibility

The heart of API design: **how do you change a contract without breaking callers?**
First classify the change:

- **Backward-compatible (safe):** add an optional field/endpoint, add an enum value
  consumers tolerate, relax a constraint. Old clients keep working.
- **Breaking:** remove/rename a field, change a type, make optional required, change
  semantics or error behavior, tighten validation.

Strategies:

```text
URL versioning      /v1/orders, /v2/orders     explicit, cache-friendly, common
Header versioning   Accept: application/vnd.acme.v2+json   clean URLs, less visible
No versioning       evolve additively forever (the "never break" discipline)
```

- **Additive evolution is the goal** (the Protobuf philosophy, Chapter 31): design so
  most changes are backward-compatible and you rarely cut a new version.
- **Tolerant reader** (Postel's Law, carefully): ignore unknown fields (Jackson
  `FAIL_ON_UNKNOWN_PROPERTIES=false`, Chapter 31) so a server can add them without
  breaking old clients. Be strict in what you *require*, liberal in what you
  *accept* — but not so liberal you mask real errors.
- When you must break: **run v1 and v2 in parallel**, announce a **deprecation
  timeline**, instrument v1 usage to know when it's safe to retire (Chapter 30), and
  provide a migration guide.

> **Consumer-driven contract tests** (Chapter 36): with **Spring Cloud Contract** or
> **Pact**, the consumer publishes the shape it depends on and the provider's CI
> verifies it never breaks that shape. This catches breaking changes *before* deploy,
> across team boundaries — the scalable alternative to "hope no one depended on
> that."

---

## 35.7 Authentication, Authorization, and Rate Limiting

The cross-cutting contract every network API needs (Chapter 32 for the security
depth):

- **AuthN (who are you):** API keys for service-to-service, OAuth2/OIDC for users,
  short-lived **JWTs** validated with explicit algorithms and claims (Chapter 32).
  Spring Security wires this up.
- **AuthZ (what may you do):** enforce **server-side on every request** — never trust
  the client. RBAC/ABAC; check at the resource, not just the route
  (`@PreAuthorize`).
- **Rate limiting & quotas:** protect the service and ensure fairness. Token-bucket
  per client (Resilience4j or Bucket4j); return `429` with `Retry-After` so
  well-behaved clients back off (Chapter 33 load shedding).

```java
// Token-bucket limiter (per client). Refills continuously; allows controlled bursts.
final class TokenBucket {
    private final double ratePerSec, capacity;
    private double tokens; private long ts = System.nanoTime();
    TokenBucket(double ratePerSec, double capacity) {
        this.ratePerSec = ratePerSec; this.capacity = capacity; this.tokens = capacity;
    }
    synchronized boolean allow(double cost) {
        long now = System.nanoTime();
        tokens = Math.min(capacity, tokens + (now - ts) / 1e9 * ratePerSec);
        ts = now;
        if (tokens >= cost) { tokens -= cost; return true; }
        return false;                                   // -> respond 429 Retry-After
    }
}
```

---

## 35.8 Documentation and Discoverability

An undocumented API doesn't exist. Make the contract **machine-readable** and
generate the rest:

- **OpenAPI/Swagger** for REST (springdoc-openapi generates it from your controllers
  and Bean Validation annotations, Chapter 28/31) and **`.proto`** for gRPC are the
  source of truth — humans and codegen both consume them.
- Document **errors, idempotency, pagination, rate limits, and auth**, not just the
  happy path — that's where integrators get stuck.
- Provide **examples and a changelog**; version the docs with the API.

---

## Summary

- An API is a **forever contract**; design the surface to be **small, consistent,
  hard to misuse**, and above all **evolvable** (Hyrum's Law guarantees people depend
  on everything observable).
- For **Java library APIs**: program to interfaces, extend with `default` methods,
  return `Optional`/immutable types, use **records/enums/sealed** types for safety,
  and stage **deprecation** (`forRemoval`).
- For **REST**: resource URLs, correct status codes, honest method semantics
  (safe/idempotent), `ProblemDetail` errors, and **cursor pagination**.
- Choose **gRPC** for fast internal contract-first services, **GraphQL** for flexible
  client-driven aggregation (mind complexity/caching), **REST** for the public edge.
- **Versioning is the core skill:** evolve additively, be a tolerant reader, run
  versions in parallel with a deprecation timeline, and guard cross-team contracts
  with **consumer-driven contract tests** (Spring Cloud Contract / Pact).
- Bake in **authN/authZ server-side** and **rate limiting** (`429` + `Retry-After`),
  and ship a **machine-readable spec** (OpenAPI/`.proto`) as the source of truth.

## Next Steps

- Add springdoc-openapi to a service and review the generated spec; document one
  endpoint's errors and pagination.
- Add `@Deprecated(forRemoval=true)` to a method and observe the compiler warnings
  callers get.
- Introduce a Spring Cloud Contract / Pact test between two services and break the
  provider to watch CI catch it.
- Revisit **[Chapter 31: Data Validation & Serialization](../31_data_validation_serialization/README.md)**
  and **[Chapter 28: Web Frameworks](../28_web_frameworks/README.md)**.
- Continue to **[Chapter 36: Engineering Practice at Scale](../36_engineering_practice/README.md)**.
