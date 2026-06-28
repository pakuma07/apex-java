# Chapter 28: Web Frameworks & Spring -- Java

## What This Chapter Covers
Java's dominance in the backend is, in practice, **Spring's** dominance. This
chapter explains how Java serves the web: the **Servlet** foundation, the
**Spring Framework**'s core idea (**inversion of control / dependency
injection**), and **Spring Boot**, the convention-over-configuration layer that
makes a production service a few annotations away. We cover request handling
(Spring MVC and reactive WebFlux), data access (Spring Data JPA), configuration,
and the auto-instrumentation (**Actuator**) that turns an app into an operable
service. This is the framework a staff Java engineer lives in daily.

> **Version note:** Java 21, **Spring Boot 3.x** / **Spring Framework 6.x**. Note
> the **Jakarta EE namespace migration**: `javax.*` became `jakarta.*` in Spring 6
> — old `javax.servlet`/`javax.persistence` imports won't compile against Boot 3.

> **C++ contrast:** C++ has no equivalent ecosystem default — web work means
> picking a library (Drogon, Crow, Pistache) and wiring everything by hand. Java's
> answer is an *opinionated platform*: Spring Boot supplies the server, DI
> container, data layer, security, and observability as a coherent whole.

---

## 28.1 The Foundation: Servlets and the Container

Before Spring, Java web apps were **Servlets**: classes whose `doGet`/`doPost`
methods a **servlet container** (Tomcat, Jetty) invokes per HTTP request. The
container owns the socket, the thread pool, and the request lifecycle; your code
fills in handlers.

```java
// The raw model Spring hides — one request, one container-managed thread.
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("Hello");
    }
}
```

The classic Java server model is **thread-per-request**: each request occupies a
platform thread for its whole duration, blocking on I/O. This is simple and
debuggable but caps concurrency at the thread-pool size — the problem **virtual
threads** (Chapter 16, Java 21) and **WebFlux** (28.6) each solve differently.

---

## 28.2 Inversion of Control and Dependency Injection

Spring's core is the **IoC container**: instead of objects constructing their own
dependencies, the container *injects* them. Your code declares *what* it needs;
the framework decides *how* to provide it. This decouples components, makes them
testable (inject a fake), and centralizes lifecycle.

```java
@Service                                  // a Spring-managed bean (singleton by default)
public class OrderService {
    private final PaymentGateway gateway; // declare the dependency...

    public OrderService(PaymentGateway gateway) {   // ...constructor injection
        this.gateway = gateway;           // Spring supplies the bean here
    }
}
```

- **Prefer constructor injection** (shown) over field injection: it makes
  dependencies explicit, allows `final` fields, and works without Spring in tests.
- **Beans** are objects the container manages; their default **scope** is
  `singleton` (one per container) — be careful putting mutable per-request state in
  one (Chapter 18). `@Component`/`@Service`/`@Repository`/`@Controller` all register
  beans; `@Configuration` + `@Bean` methods define them explicitly.

> **The trade-off:** DI removes wiring boilerplate and coupling, but the
> indirection ("where does this bean come from?") and classpath "magic" can
> obscure control flow. The payoff — swappable, testable components — is why it
> dominates large codebases.

---

## 28.3 Spring Boot: Convention Over Configuration

**Spring Boot** turns Spring from a powerful-but-fiddly framework into a batteries-
included platform: an embedded server, sensible defaults, and **auto-configuration**
that wires beans based on what's on the classpath.

```java
@SpringBootApplication                    // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);   // starts embedded Tomcat on :8080
    }
}
```

- **Starters** (`spring-boot-starter-web`, `-data-jpa`, `-security`) are curated
  dependency bundles — add one and the relevant features auto-configure.
- **Auto-configuration** backs off when you define your own bean, so defaults are
  overridable. `--debug` prints the auto-config report (what was applied and why).
- **Embedded server**: the app *is* the server (Chapter 30's port-binding /
  12-factor model) — `java -jar app.jar`, no external Tomcat to deploy into.

---

## 28.4 Building REST APIs with Spring MVC

The bread and butter: annotate a class as a controller, map methods to routes, and
let Spring handle (de)serialization (Jackson, Chapter 31), status codes, and
content negotiation. (API design principles are in [Chapter 40 of the Python
edition's arc]; here is the Java mechanism.)

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable long id) {
        return service.find(id).orElseThrow(() -> new NotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)               // 201, not default 200
    public OrderDto create(@Valid @RequestBody CreateOrder body) {   // @Valid -> Ch 31
        return service.create(body);
    }
}
```

- `@RestController` = `@Controller` + `@ResponseBody` (return values become the JSON
  body). `@RequestBody` deserializes the request; `@PathVariable`/`@RequestParam`
  bind URL parts.
- **Centralize error handling** with `@RestControllerAdvice` + `@ExceptionHandler`
  so every endpoint returns a consistent error shape and correct status code:

```java
@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)             // map domain exception -> 404
    ProblemDetail handle(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
```

`ProblemDetail` (Spring 6) implements RFC 9457 problem-details — a standard
machine-readable error envelope.

---

## 28.5 Data Access with Spring Data JPA

Spring Data generates repository implementations from interfaces: declare the
queries, get the code. It sits on **JPA/Hibernate** (the ORM, Chapter 24) over
JDBC.

```java
public record Order(long id, String sku, int qty) {}

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findBySku(String sku);                // query DERIVED from the name
    @Query("select o from Order o where o.qty > :min") // or explicit JPQL
    List<Order> bigOrders(int min);
}
```

The staff-level caveats (Chapter 39 of the Python arc covers the theory):

- **The N+1 problem is the #1 JPA performance bug**: lazily loading a relation per
  parent row turns 1 query into 1+N. Use `@EntityGraph` or `JOIN FETCH` to eager-
  load what you'll use.
- **Transactions**: annotate service methods with `@Transactional`; understand that
  it works via a proxy, so self-invocation within the same bean bypasses it.
- **Connection pooling**: Boot ships **HikariCP**; size the pool deliberately
  (Chapter 30) — a big pool × many instances can exhaust the database.
- **Know when to drop to SQL**: for complex reporting queries, JPQL/Criteria fights
  you — use `JdbcTemplate` or jOOQ instead of contorting the ORM.

---

## 28.6 Servlet (MVC) vs Reactive (WebFlux) vs Virtual Threads

Spring offers two stacks, and Java 21 changes the calculus:

| Model | Concurrency | Style | Best for |
|---|---|---|---|
| **Spring MVC** (Servlet) | thread-per-request (blocking) | imperative, simple | most services; pairs with virtual threads |
| **Spring WebFlux** (Reactor) | event-loop, non-blocking | reactive (`Mono`/`Flux`) | very high concurrency, streaming, backpressure |
| **MVC + virtual threads** (21) | one virtual thread per request | imperative, *scales* | the new default for I/O-bound services |

WebFlux scales to huge connection counts on few threads but imposes the **reactive
programming model** — `Mono`/`Flux` pipelines, no blocking calls anywhere, and
notoriously hard debugging (stack traces don't reflect logical flow).

> **Java 21's pivot:** virtual threads (`spring.threads.virtual.enabled=true`) give
> blocking MVC code the scalability that previously required WebFlux — keep the
> simple imperative model *and* handle tens of thousands of concurrent I/O-bound
> requests. For most teams this removes the main reason to adopt reactive.

---

## 28.7 Configuration and Profiles

Externalized config (Chapter 30's 12-factor) is first-class:

```yaml
# application.yml — defaults, overridable by env vars and profiles
server:
  port: 8080
spring:
  datasource:
    url: ${DATABASE_URL}            # bound from the environment, not hard-coded
---
spring:
  config:
    activate:
      on-profile: prod              # prod-only overrides
  jpa:
    show-sql: false
```

- **Profiles** (`dev`, `prod`, `test`) select environment-specific beans/config via
  `@Profile` and `--spring.profiles.active`.
- **`@ConfigurationProperties`** binds a config tree to a typed, validated record —
  the Java analogue of pydantic-settings: fail fast at startup on bad config.
- **Order of precedence**: command-line args > env vars > profile YAML > default
  YAML — so the same artifact runs in every environment (dev/prod parity).

---

## 28.8 The Broader Ecosystem

Spring isn't the only option, and knowing the landscape is part of the staff remit:

- **Quarkus / Micronaut** — build-time DI and **GraalVM native image** (Chapter 19)
  for millisecond startup and tiny memory — strong for serverless/containers where
  Spring's reflection-heavy startup is a tax (Spring's AOT/native support is
  catching up).
- **Helidon**, **Javalin**, **Spark** — lighter-weight web frameworks.
- **Jakarta EE** (formerly Java EE) — the standards-based application-server world
  Spring largely displaced.

Spring remains the default for breadth and ecosystem; the alternatives win on
startup/footprint for cloud-native and serverless workloads.

---

## Summary

- Java web apps rest on the **Servlet/container** model (thread-per-request);
  **Spring** is the de-facto platform on top of it.
- Spring's core is **IoC/DI** — declare dependencies, let the container inject them;
  prefer **constructor injection** and mind **singleton** bean state.
- **Spring Boot** adds auto-configuration, starters, and an embedded server, so a
  production service is `java -jar` away.
- Build REST with **Spring MVC** (`@RestController`, `@RequestMapping`, centralized
  `@RestControllerAdvice` + `ProblemDetail`); access data with **Spring Data JPA**
  while watching the **N+1**, transactions, and pool sizing.
- Choose **MVC + virtual threads** (Java 21) for most I/O-bound services; reach for
  **WebFlux** only when you genuinely need reactive streaming/backpressure.
- Externalize config with **profiles** and **`@ConfigurationProperties`**; know the
  **Quarkus/Micronaut** native-image alternatives for cloud-native footprints.

## Next Steps

- Build a `@RestController` + `@Service` + `JpaRepository` slice and observe
  constructor injection wiring it together.
- Turn on `spring.threads.virtual.enabled=true` and load-test it against a blocking
  downstream call; compare to the default thread pool.
- Move all config to `application.yml` + env vars and add a `prod` profile.
- Revisit **[Chapter 16: Concurrency](../16_concurrency/README.md)** for virtual
  threads and **[Chapter 24: Database Access with JDBC](../24_database_jdbc/README.md)**
  for the JDBC layer JPA sits on.
- Continue to **[Chapter 29: Performance Engineering](../29_performance_engineering/README.md)**.
