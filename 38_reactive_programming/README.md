# 38 — Reactive Programming (Reactor, RxJava & Backpressure)

Reactive programming builds **asynchronous, non-blocking, composable streams of data** with **backpressure** — a way for a slow consumer to tell a fast producer "slow down, I can only take N more items." It rose to prominence for high-throughput, I/O-bound services serving many concurrent connections on a small, fixed pool of threads. This is the book's authoritative treatment of reactive: the Reactive Streams JVM standard, Project Reactor (the engine under Spring WebFlux), a tour of RxJava, error handling, testing, the famous pitfalls — and an honest verdict now that **Java 21 virtual threads** ([16_concurrency](../16_concurrency/README.md)) cover most of the same ground with far simpler code.

> **C++ vs Java at a glance.** C++ has no standard reactive framework; the closest idioms are callback chains, `std::future`/`.then`, C++20 coroutines, and libraries like RxCpp. Java's reactive story is a published interface contract (`java.util.concurrent.Flow`) with interoperable implementations (Reactor, RxJava, Akka Streams). Where C++ reaches for executors and futures, reactive Java composes **declarative operator pipelines that don't run until subscribed**.

---

## 38.1 The Problem Reactive Solves

Classic Java servers use **thread-per-request** with blocking I/O: a thread calls the database, *blocks* until rows return — parked, holding ~1 MB of stack — for the entire wait. Under 10,000 concurrent slow requests you need ~10,000 threads, which the OS cannot sustain.

Reactive flips this. Work is a **pipeline of transformations over an asynchronous stream**. A handful of event-loop threads (often one per core) drive thousands of in-flight requests; whenever one waits on I/O the thread advances another. Three properties define a reactive stream:

- **Asynchronous & non-blocking** — calls return immediately; results arrive later via framework-managed callbacks.
- **Composable** — `map`, `filter`, `flatMap`, `zip`, retry, timeout chain into a declarative dataflow.
- **Backpressure** — the consumer signals *demand* (`request(n)`); the producer emits no more than `n`. This is what `CompletableFuture` ([16_concurrency](../16_concurrency/README.md)) and plain callbacks lack — with no way for a slow sink to throttle a fast source, an unbounded queue grows until `OutOfMemoryError`.

---

## 38.2 The Reactive Streams Specification

Reactive Streams is the **interoperability standard** that lets Reactor, RxJava, and Akka talk to each other. It was absorbed into the JDK as `java.util.concurrent.Flow` (Java 9). It is just **four interfaces** plus a rulebook.

```java
import java.util.concurrent.Flow.*;   // JDK 9+ : the standard reactive interfaces

interface Publisher<T> { void subscribe(Subscriber<? super T> s); }  // a source

interface Subscriber<T> {              // a sink; called in a strict order
    void onSubscribe(Subscription s);  // exactly once, first
    void onNext(T item);               // 0..N times — never more than requested
    void onError(Throwable t);         // terminal, at most once
    void onComplete();                 // terminal, at most once
}

interface Subscription {               // the link — where backpressure lives
    void request(long n);              // "I can handle n more items" = DEMAND
    void cancel();
}

interface Processor<T, R> extends Subscriber<T>, Publisher<R> {}  // an in-stream transform
```

The protocol *is* backpressure: a `Subscriber` calls `request(n)`, and the `Publisher` may emit **at most `n`** `onNext` calls before waiting for more demand. The contract fixes the sequence — `onSubscribe` first, then any number of `onNext`, terminated by **at most one** of `onError`/`onComplete`, with calls serialized (no concurrent `onNext`).

> You almost never implement these by hand — that is what Reactor and RxJava do — but the four interfaces explain *every* reactive library; they are all sugar over `Publisher`/`Subscriber`/`Subscription`.

---

## 38.3 Project Reactor: `Mono` and `Flux`

[Project Reactor](https://projectreactor.io) is the dominant implementation and the foundation of **Spring WebFlux** ([28_web_frameworks](../28_web_frameworks/README.md)). It offers two `Publisher` types:

| Type | Emits | Analogy | Typical use |
|---|---|---|---|
| `Mono<T>` | 0 or 1 item, then completes/errors | `CompletableFuture<T>` / `Optional<T>` | one DB row, one HTTP response |
| `Flux<T>` | 0..N items, then completes/errors | an async `Stream<T>` | rows, server-sent events, a Kafka topic |

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

Mono<String> one  = Mono.just("hello");                  // a single value
Flux<Integer> nums = Flux.just(1, 2, 3, 4, 5);           // five values
Flux<Integer> range = Flux.range(1, 100);                // 1..100
Mono<String> fut  = Mono.fromFuture(someCompletableFuture);
Flux<String> list = Flux.fromIterable(List.of("a", "b", "c"));
```

### 38.3.1 Operator categories

Reactor ships hundreds of operators; learn the categories, then look up specifics.

| Category | Operators | Purpose |
|---|---|---|
| Transform | `map`, `flatMap`, `concatMap`, `cast` | reshape elements (`flatMap` for async sub-streams) |
| Filter | `filter`, `take`, `skip`, `distinct` | drop elements |
| Combine | `zip`, `merge`, `concat`, `combineLatest` | join multiple publishers |
| Reduce | `reduce`, `collectList`, `count` | fold a `Flux` into a `Mono` |
| Error | `onErrorResume`, `onErrorReturn`, `retry`, `retryWhen` | recover or retry |
| Time | `timeout`, `delayElements`, `interval`, `window` | time-based behavior |

```java
Flux<String> pipeline = Flux.range(1, 10)
    .filter(n -> n % 2 == 0)                 // 2,4,6,8,10
    .map(n -> n * n)                         // sync 1:1: T -> R
    .flatMap(sq -> lookupNameAsync(sq))      // async: T -> Publisher<R>, merged, order NOT kept
    .onErrorResume(ex -> Flux.just("fallback")); // recover from any upstream error
// flatMap calls another reactive service per element; concatMap is the order-preserving variant.
```

### 38.3.2 Assembly vs subscription time — "nothing happens until you subscribe"

The most surprising rule. Building a pipeline does **no work** — it assembles a recipe. Work begins **only when something subscribes**.

```java
Flux<Integer> recipe = Flux.range(1, 3)
    .map(n -> { System.out.println("mapping " + n); return n * 2; });
// Nothing printed yet — COLD and lazy.
recipe.subscribe(v -> System.out.println("got " + v));   // NOW it runs
// Subscribing twice runs the whole pipeline TWICE (cold = per-subscriber).
```

> **C++ contrast.** A `std::future` is already running when you hold it; `.get()` just waits. A Reactor pipeline is the opposite — an inert description, like a lambda you haven't called yet.

### 38.3.3 Hot vs cold publishers

- **Cold**: each subscriber gets its own execution from the start (HTTP request, DB query); re-subscribing replays. Most `Flux`/`Mono` are cold.
- **Hot**: emits regardless of subscribers; late subscribers miss earlier items (live price feed, Kafka). Created with `Sinks`, `.share()`, `.publish().refCount()`.

```java
Sinks.Many<String> hot = Sinks.many().multicast().onBackpressureBuffer();
hot.tryEmitNext("event-1");                 // emitted whether or not anyone listens
Flux<String> live = hot.asFlux();           // subscribers see only events after they join
```

### 38.3.4 Schedulers and the threading model

A pipeline runs on the subscribing thread by default. Two operators move work between thread pools (`Scheduler`s):

- **`subscribeOn(scheduler)`** — chooses the thread the *source* runs on; affects the **whole** chain wherever it appears.
- **`publishOn(scheduler)`** — switches the thread for operators *downstream* of it; may appear multiple times.

```java
import reactor.core.scheduler.Schedulers;
Flux.range(1, 10)
    .publishOn(Schedulers.parallel())         // downstream maps run on parallel pool
    .map(this::cpuWork)
    .subscribeOn(Schedulers.boundedElastic())  // source runs on blocking-safe pool
    .subscribe();
```

| Scheduler | Backing | Use for |
|---|---|---|
| `parallel()` | fixed pool, ~N CPUs | CPU-bound, non-blocking work |
| `boundedElastic()` | growable, capped | wrapping **blocking** calls (JDBC, legacy APIs) |
| `single()` | one reusable thread | low-volume, ordered tasks |
| `immediate()` | caller thread | no thread switch |

### 38.3.5 `Context` — propagation without `ThreadLocal`

Reactive operators hop threads, so `ThreadLocal` ([16_concurrency](../16_concurrency/README.md)) is unreliable for request-scoped data (trace IDs, security principal). Reactor's immutable `Context` rides **along the subscription**, downstream-to-upstream.

```java
Mono.deferContextual(ctx -> Mono.just("user=" + ctx.get("userId")))
    .contextWrite(ctx -> ctx.put("userId", "u-42"))   // write flows UP to deferContextual
    .subscribe(System.out::println);                  // prints: user=u-42
```

---

## 38.4 RxJava — The Other Major Implementation

[RxJava](https://github.com/ReactiveX/RxJava) predates Reactor and popularized the model on the JVM (and Android, where it remains common). Its concepts map closely to Reactor's, but it splits "stream" by **backpressure support**:

| RxJava type | Backpressure? | Reactor analog |
|---|---|---|
| `Observable<T>` | **No** | (none — a hot `Flux` without demand) |
| `Flowable<T>` | **Yes** (Reactive Streams) | `Flux<T>` |
| `Single<T>` | exactly 1 | non-empty `Mono<T>` |
| `Maybe<T>` | 0 or 1 | `Mono<T>` |
| `Completable` | 0 (done/error only) | `Mono<Void>` |

```java
io.reactivex.rxjava3.core.Flowable.range(1, 5)   // Flowable = backpressured stream
    .map(n -> n * n)
    .filter(n -> n > 4)
    .subscribe(System.out::println);              // 9, 16, 25
```

Key takeaway: **use `Flowable` when the source can outpace the consumer** (network, files); `Observable` is fine only for small, fully-buffered sequences (UI events) where backpressure is irrelevant. Reactor avoids the fork — `Flux` always supports backpressure. New JVM backends usually pick Reactor (Spring alignment); RxJava dominates Android.

---

## 38.5 Error Handling in Reactive Pipelines

In a reactive stream an error is a **terminal signal** (`onError`) — it travels down the pipeline and stops it, like an exception unwinding a stack but asynchronously. You handle it with operators, not `try/catch` (which only catches *assembly*-time errors, not runtime signals).

```java
Flux<Integer> safe = Flux.just(1, 2, 0, 4)
    .map(n -> 10 / n)                              // 10/0 -> ArithmeticException as onError
    .onErrorReturn(-1)                             // emit a fallback value, then complete
    // .onErrorResume(ex -> Flux.just(99))         // switch to an alternative publisher
    // .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // re-subscribe with backoff
    ;
safe.subscribe(System.out::println, err -> System.out.println("failed: " + err));
```

- `onErrorReturn(value)` — substitute a single fallback value.
- `onErrorResume(fn)` — switch to a whole fallback `Publisher` (e.g. a cache).
- `retry(n)` / `retryWhen(...)` — re-subscribe to the source; pair with backoff to avoid hammering a failing dependency (Resilience4j, [30_production_operational](../30_production_operational/README.md)).
- `doOnError(...)` — side-effect (log) without handling.

---

## 38.6 Testing with `StepVerifier`

You cannot assert on a reactive stream with `.get()` — there is no value yet. Reactor Test's `StepVerifier` subscribes, drives demand, and asserts each signal in order, including virtual time for delays.

```java
import reactor.test.StepVerifier;

StepVerifier.create(Flux.just("a", "b", "c"))
    .expectNext("a").expectNext("b", "c")
    .verifyComplete();                       // asserts onComplete with no error

StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofHours(1)).take(2))
    .thenAwait(Duration.ofHours(2))          // a 2-hour delay tested in microseconds
    .expectNext(0L, 1L)
    .verifyComplete();
```

See [25_testing](../25_testing/README.md) for the broader JUnit 5 / AssertJ workflow `StepVerifier` slots into.

---

## 38.7 Pitfalls (Symptom / Cause / Fix)

### Blocking inside a reactive pipeline — the cardinal sin

A reactive app runs on a *tiny* pool of event-loop threads. Blocking one (JDBC, `sleep`, `future.get()`, a blocking HTTP client) parks a thread meant to serve thousands of requests; a few blocked calls and the whole server stalls.

```java
// WRONG — blocks an event-loop thread; starves every other in-flight request
Flux.range(1, 100)
    .map(id -> jdbcTemplate.queryForObject(SQL, id))   // synchronous JDBC = BLOCKING
    .subscribe();

// RIGHT (A) — isolate the blocking call on the blocking-safe elastic pool
Flux.range(1, 100)
    .flatMap(id -> Mono.fromCallable(() -> jdbcTemplate.queryForObject(SQL, id))
                       .subscribeOn(Schedulers.boundedElastic()))
    .subscribe();

// RIGHT (B) — don't block at all: use a reactive driver (R2DBC, reactive WebClient)
Flux.range(1, 100).flatMap(id -> r2dbcRepository.findById(id)).subscribe();
```

- **Symptom:** latency spikes, throughput collapses; thread dumps show event-loop threads stuck in `socketRead`/`jdbc`.
- **Cause:** a blocking call on a `parallel`/Netty event-loop thread.
- **Fix:** wrap unavoidable blocking work in `Mono.fromCallable(...).subscribeOn(boundedElastic())`, or switch to non-blocking drivers. Run **BlockHound** in tests to detect blocking calls automatically.

### Unreadable stack traces

- **Symptom:** an exception trace is full of Reactor internals (`Operators`, `FluxMap$MapSubscriber`) and never names *which operator* failed.
- **Cause:** async assembly puts the throwing frame far from your code; the stack is the scheduler's, not yours.
- **Fix:** add `.checkpoint("after parsing")` at suspect points, or enable `ReactorDebugAgent.init()` / `Hooks.onOperatorDebug()` in dev to capture assembly stack traces.

### Accidental eager / double subscription

- **Symptom:** a side effect (HTTP POST, DB write) runs twice or zero times.
- **Cause:** subscribing twice re-runs a cold pipeline; forgetting to subscribe runs it never; `.block()` inside a `map` is a hidden second subscription.
- **Fix:** subscribe exactly once at the edge (let WebFlux do it); use `.cache()` to share one execution; never `.block()` inside an operator.

### The learning curve

- **Symptom:** correct-looking pipelines that deadlock, leak, or silently do nothing.
- **Cause:** lazy assembly, demand, scheduler hopping, and terminal signals are a genuinely different mental model — and one blocking call poisons everything.
- **Fix:** budget real ramp-up, enforce BlockHound in CI, and question whether you need reactive at all on Java 21 (next section).

---

## 38.8 The Honest Modern Take: Reactive vs Virtual Threads

For a decade, reactive's headline justification was **thread efficiency**: blocking I/O wastes threads, so go non-blocking. **Java 21 virtual threads** ([16_concurrency](../16_concurrency/README.md) §16.15, Project Loom / JEP 444) demolish that specific argument. A virtual thread that blocks on I/O *unmounts* from its carrier OS thread, so you can run **millions** of blocking tasks on a handful of OS threads — the same scaling reactive gives you — while writing **plain, sequential, debuggable** code.

```java
// Virtual threads — scales like reactive, reads like a 2005 servlet, SANE stack traces
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var id : ids) {
        executor.submit(() -> {
            var user   = userClient.fetch(id);    // blocking call — and that's FINE
            var orders = orderClient.fetch(id);   // blocks a cheap virtual thread, not an OS one
            return enrich(user, orders);
        });
    }
}
```

| Concern | Reactive (Reactor/Rx) | Virtual threads (Java 21) |
|---|---|---|
| Thread efficiency for I/O | Excellent | Excellent (now equal) |
| Code style | Declarative pipelines, callbacks | Plain blocking, sequential |
| Debuggability / stack traces | Poor (needs checkpoints/agent) | Normal traces, normal breakpoints |
| Learning curve | Steep | Near-zero (it's just threads) |
| **Backpressure** | Built-in (`request(n)`) | **Not provided** — add semaphores/queues yourself |
| Stream composition (`zip`, `merge`, windowing) | First-class | Manual / awkward |
| Time operators, hot streams, SSE/event flows | First-class | DIY |
| Blocking a task | Catastrophic (starves event loop) | Cheap and expected |

### Decision guidance

- **Reach for virtual threads** for **request-per-task** I/O concurrency: REST endpoints, microservice fan-out, "call three services and combine." That is the *majority* of server code, and on Java 21 it is simpler, easier to debug, and just as scalable. Spring MVC on virtual threads ([28_web_frameworks](../28_web_frameworks/README.md)) is now a first-class choice.
- **Reach for reactive** when you need its *semantics*, not just thread thrift: **backpressure** over a stream whose producer can outrun the consumer (Kafka, SSE, file/network streaming); **complex async dataflow** (merging/zipping/windowing live streams, `combineLatest`, debouncing, time operators); or because you are **already** in a reactive stack (WebFlux + R2DBC).
- **Do not** adopt reactive *solely* for thread efficiency on new Java 21 code — that trade (large complexity for scaling you now get for free) no longer pays off.

> Reactive is a *programming model* (composable streams + backpressure); virtual threads are a *concurrency mechanism* (cheap blocking). For the use case that drove most reactive adoption — scalable blocking I/O — virtual threads win on ergonomics. See [29_performance_engineering](../29_performance_engineering/README.md) for measuring which wins for *your* latency/throughput profile.

---

## 38.9 Best Practices and Common Mistakes

**Do:**
- Default to **virtual threads + blocking code** for ordinary request/response I/O on Java 21; reserve reactive for streaming, backpressure, and complex async composition.
- Go non-blocking *end to end* (reactive driver), or isolate unavoidable blocking on `boundedElastic()`; enforce **BlockHound** in tests.
- Prefer `flatMap` for async per-element calls, `concatMap` when order matters, `map` for sync transforms.
- Handle errors with `onErrorResume`/`retryWhen` + backoff (treat `onError` as terminal); add `.checkpoint(...)`/debug agent early.
- Subscribe **once**, at the framework edge; use `.cache()` to share a cold pipeline. Test with `StepVerifier`, not `.block()`.

**Avoid:**
- Blocking (JDBC, `sleep`, `future.get()`, `.block()`) inside an operator — starves the event loop.
- Building a pipeline and forgetting to subscribe (nothing runs), or subscribing twice and re-running side effects.
- RxJava `Observable` where the source can outpace the consumer (no backpressure → OOM) — use `Flowable`.
- `ThreadLocal` across operators (use Reactor `Context`); `try/catch` around assembly instead of `onError*` operators.
- Adopting reactive purely for thread efficiency on Java 21 when virtual threads would be far simpler.

---

## 38.11 Compile and Run

Reactor is not part of the JDK; add it via Maven/Gradle ([26_build_tools](../26_build_tools/README.md)):

```xml
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-core</artifactId>
  <version>3.6.0</version>
</dependency>
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-test</artifactId>
  <scope>test</scope>
</dependency>
```

The `java.util.concurrent.Flow` interfaces (§38.2) are built in from Java 9 and need no dependency. Virtual threads (§38.8) require **Java 21**.

---

> **Next:** [39_design_patterns](../design_patterns/README.md) — the Gang-of-Four patterns and their idiomatic Java forms (the Observer pattern is the conceptual ancestor of reactive streams).
>
> **Related:** [16_concurrency](../16_concurrency/README.md) for threads, `CompletableFuture`, and virtual threads · [28_web_frameworks](../28_web_frameworks/README.md) for Spring WebFlux vs MVC vs virtual threads · [29_performance_engineering](../29_performance_engineering/README.md) for measuring reactive vs virtual threads · [30_production_operational](../30_production_operational/README.md) for resilience and backpressure in production.
