# Chapter 30: Production & Operational Concerns -- Java

## What This Chapter Covers
Code that compiles is half the job; running it reliably for others is the rest.
This chapter covers the operational layer of a Java service: **twelve-factor
configuration**, **structured logging**, **observability** (metrics, traces,
health) via Micrometer/OpenTelemetry and Spring Boot **Actuator**, **graceful
shutdown**, **containerizing the JVM**, **worker/heap sizing**, and **resilience**
(timeouts, retries, circuit breakers via Resilience4j). It ties together
concurrency (16), the JVM (19), performance (29), and Spring (28).

> **Version note:** Java 21, Spring Boot 3.x, Micrometer + OpenTelemetry. Container
> examples use a JVM that respects cgroup limits (Java 17+ is fully container-aware).

> **C++ contrast:** a C++ service ships as a native binary with no runtime VM to
> tune. The JVM adds heap/GC sizing, warmup, and rich built-in telemetry (JMX, JFR,
> Actuator) — more to operate, but also far more *introspectable* in production.

---

## 30.1 The Twelve-Factor App

A checklist for services that scale and deploy cleanly:

| Factor | Rule of thumb (Java specifics) |
|---|---|
| **Config** | in the environment; bind via `@ConfigurationProperties`, not hard-coded |
| **Dependencies** | declared in Maven/Gradle, isolated in the artifact (fat JAR) |
| **Backing services** | DBs/queues/caches as attachable URLs |
| **Processes** | **stateless**; session state in Redis/DB, not the heap |
| **Port binding** | embedded server (Boot, Chapter 28) — the app *is* the server |
| **Concurrency** | scale out with more instances; virtual threads within (Ch 29) |
| **Disposability** | fast startup (mind JVM warmup), **graceful shutdown** (30.5) |
| **Logs** | event stream to **stdout**; the platform routes them (30.3) |

The two with the biggest day-to-day impact: **config from the environment** and
**logs to stdout**. Everything below builds on them.

---

## 30.2 Configuration

Bind external config into a typed, validated object once at startup — the Java
analogue of pydantic-settings:

```java
@ConfigurationProperties(prefix = "app")
@Validated
public record AppConfig(
        @NotBlank String databaseUrl,        // required: APP_DATABASEURL / app.database-url
        @Min(1) @Max(65535) int port,
        Duration requestTimeout) {           // Spring parses "5s" -> Duration
}
```

- **Precedence** (Chapter 28.7): CLI args > env vars > profile YAML > defaults — one
  artifact, many environments (dev/prod parity).
- **Fail fast:** `@Validated` config rejects a misconfigured deploy at startup, not
  at the first request.
- **Secrets are config too** (Chapter 32): inject via env/secret managers (Vault,
  cloud secret stores, K8s secrets) — never commit them.

---

## 30.3 Structured Logging

Plain text logs don't scale to query. Emit **structured (JSON)** logs to **stdout**
with a correlation id so one request is traceable across lines and services. Java's
stack is **SLF4J** (the facade) over **Logback** or **Log4j 2**.

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

void place(Order o) {
    MDC.put("orderId", String.valueOf(o.id()));   // MDC -> appears on every log line
    try {
        log.info("placing order");                // use parameterized logging:
        log.debug("cart total {} for {} items", o.total(), o.size());  // lazy, no concat
    } finally {
        MDC.clear();                               // ALWAYS clear (thread reuse!)
    }
}
```

- **Use the SLF4J `{}` placeholders**, not string concatenation — the message is
  only formatted if the level is enabled (lazy, avoids wasted work).
- **MDC (Mapped Diagnostic Context)** attaches a request/trace id to every line on
  that thread; a JSON encoder (logstash-logback-encoder) emits it as a field.
  **Caveat:** MDC is thread-local — clear it, and propagate it explicitly across
  thread/async boundaries (and virtual threads).
- **Never log secrets/PII**; log business events at `INFO`, recoverable issues at
  `WARN`, failures with the exception object (`log.error("msg", ex)` — not
  `ex.getMessage()`, which loses the stack trace).

---

## 30.4 Observability: Metrics, Traces, Health

Logs say *what happened*; **metrics** say *how much/how fast*; **traces** say
*where the time went across services*. Spring Boot **Actuator** + **Micrometer**
provide all three; Micrometer is a vendor-neutral facade (the "SLF4J for metrics")
that exports to Prometheus, and OpenTelemetry handles traces.

```java
// Micrometer: a timer + counter, exported to /actuator/prometheus for scraping.
@Timed("orders.place")                    // records latency distribution + percentiles
public Order place(Order o) {
    meterRegistry.counter("orders.placed", "sku", o.sku()).increment();
    ...
}
```

```yaml
management:
  endpoints.web.exposure.include: health,prometheus,info
  endpoint.health.probes.enabled: true    # liveness + readiness groups
  tracing.sampling.probability: 0.1       # sample 10% of traces
```

- **Three pillars correlated by a trace id:** metrics (Micrometer→Prometheus),
  traces (OpenTelemetry→Jaeger/Tempo), logs (stdout→Loki/ELK).
- **SLOs over raw metrics** (define p99/error-rate objectives + error budgets);
  measure the **golden signals** (latency, traffic, errors, saturation).
- **Metric cardinality** is a real cost: never put unbounded values (`userId`,
  `requestId`) in metric tags — they explode the time-series count. High-cardinality
  data belongs in traces/logs.

### Health checks

Actuator distinguishes **liveness** (is the process healthy — restart if not) from
**readiness** (can it serve traffic — stop routing if not). A failing DB makes you
*not ready* (drain traffic), not *not alive* (don't restart into the same failure).

---

## 30.5 Graceful Shutdown

An orchestrator sends `SIGTERM`, waits a grace period, then `SIGKILL`. A disposable
process must **drain in-flight work** in between:

```yaml
server.shutdown: graceful                 # stop accepting new requests, finish in-flight
spring.lifecycle.timeout-per-shutdown-phase: 25s
```

The correct sequence in a container (Chapter 28's embedded server handles much of
it):

```text
1. SIGTERM received
2. readiness flips to DOWN  -> load balancer stops sending NEW requests
3. in-flight requests finish (within the shutdown timeout)
4. @PreDestroy / DisposableBean: close pools, flush metrics/logs
5. JVM exits 0  (before the orchestrator's grace period -> SIGKILL)
```

- Set the app's drain timeout **shorter** than the orchestrator grace period.
- The JVM forwards `SIGTERM` to shutdown hooks — but if you launch via a shell
  wrapper, ensure signals reach the `java` process (exec form / `tini`, Chapter
  30.6).

---

## 30.6 Containerizing the JVM

Containers give dev/prod parity. The JVM-specific concerns:

```dockerfile
# Multi-stage: build a layered, jlink-trimmed image; run as non-root on a slim base.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r appuser                     # don't run as root
COPY --from=build /app/target/app.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]   # exec form -> signals reach the JVM
```

- **Container awareness:** Java 17+ reads cgroup CPU/memory limits. Size the heap
  with `-XX:MaxRAMPercentage` (a % of the *container* limit), not a fixed `-Xmx`
  guessed from the host — leave room for non-heap (metaspace, thread stacks, direct
  buffers) so you don't get **OOMKilled** (which is RSS > limit, *not* a Java
  `OutOfMemoryError`).
- **Startup cost:** JVM warmup (Chapter 19/29) makes the first requests slow; for
  serverless/scale-to-zero consider **CDS**/**AppCDS** or **GraalVM native image**
  (Quarkus/Micronaut/Spring AOT) for millisecond startup.
- **Layered/jib images** keep rebuilds fast (deps in a lower layer than your code).

---

## 30.7 Running and Sizing

- **Instances scale out**; within an instance, **virtual threads** (Chapter 29)
  handle I/O concurrency, so you no longer size a huge platform-thread pool.
- **Connection pools** (HikariCP, Chapter 28): instances × pool size must stay under
  the database's connection limit — front Postgres with a pooler if instance count
  is high.
- **Reverse proxy / ingress** in front for TLS, buffering, and timeouts; set
  **timeouts everywhere** (client, server, DB, HTTP client) so one slow dependency
  can't exhaust threads/connections.
- **Resource limits + autoscaling:** set container CPU/memory requests and limits;
  scale on a meaningful signal (latency, queue depth), not just CPU.

---

## 30.8 Resilience Patterns

Dependencies fail; design for it. **Resilience4j** is the modern (Hystrix-
successor) library, integrating with Spring:

```java
@Retry(name = "pricing")                  // bounded retries with backoff
@CircuitBreaker(name = "pricing", fallbackMethod = "cachedPrice")
public Price fetchPrice(String sku) {
    return pricingClient.get(sku);        // remote call
}
Price cachedPrice(String sku, Throwable t) {   // graceful degradation when open
    return cache.lastKnown(sku);
}
```

| Pattern | Purpose |
|---|---|
| **Timeouts** | bound every remote call — never wait forever |
| **Retries + backoff + jitter** | survive transient blips without a thundering herd |
| **Circuit breaker** | stop hammering a dead dependency; fail fast, recover later |
| **Bulkhead** | isolate resource pools so one dependency can't drown the rest |
| **Rate limiter** | cap load; shed excess early under overload |
| **Idempotency** | make retried writes safe (idempotency keys) |

> Only retry **idempotent** or idempotency-keyed operations — blindly retrying a
> payment double-charges. And use a **retry budget**: layered retries amplify a
> partial outage into a self-sustaining overload (Chapter 38 of the Python arc).

---

## 30.9 Production Readiness Checklist

```text
[ ] Config from env (@ConfigurationProperties, @Validated); secrets from a manager
[ ] Structured JSON logs to stdout with trace/MDC ids; no secrets/PII
[ ] /actuator/health liveness + readiness; metrics (Micrometer/Prometheus) + traces
[ ] SLOs with error budgets; alert on burn rate; metric tags bounded (no ids)
[ ] server.shutdown=graceful; drain timeout < orchestrator grace period
[ ] Heap sized via MaxRAMPercentage under the container limit; GC logging on (Ch 29)
[ ] Timeouts on every outbound call; Resilience4j retries/circuit breakers where safe
[ ] Slim, non-root, layered image; signals reach the JVM (exec form)
[ ] HikariCP pool sized under the DB connection limit
[ ] CI gate (Ch 25) + dependency/vuln scan (Ch 32); rollback plan
```

---

## Summary

- Treat config as **environment data** bound to a **validated** object; keep
  processes **stateless** and disposable (12-factor).
- Emit **structured logs to stdout** (SLF4J `{}` + MDC, cleared per thread) and
  instrument the **three pillars** via **Actuator + Micrometer + OpenTelemetry**,
  correlated by trace id and governed by **SLOs**.
- Provide **liveness/readiness** and handle **SIGTERM** for **graceful shutdown**
  that drains in-flight work.
- Containerize with **cgroup-aware heap sizing** (`MaxRAMPercentage`), non-root slim
  images, and signal-safe launch; mind **JVM warmup** and **OOMKilled** vs
  `OutOfMemoryError`.
- Build in **resilience** (Resilience4j: timeouts, retries+backoff, circuit
  breakers, bulkheads) and only retry **idempotent** operations.

## Next Steps

- Add Actuator + Micrometer to a service and scrape `/actuator/prometheus`; build a
  latency-percentile dashboard.
- Set `server.shutdown=graceful` and verify in-flight requests complete on SIGTERM.
- Containerize with `MaxRAMPercentage` and load it until OOMKilled to learn your
  real memory ceiling.
- Revisit **[Chapter 28: Web Frameworks](../28_web_frameworks/README.md)** and
  **[Chapter 29: Performance Engineering](../29_performance_engineering/README.md)**.
- Continue to **[Chapter 31: Data Validation & Serialization](../31_data_validation_serialization/README.md)**.
