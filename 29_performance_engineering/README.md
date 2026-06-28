# Chapter 29: Performance Engineering -- Java

## What This Chapter Covers
The JVM is a remarkable performance platform — a profile-guided optimizing
compiler (Chapter 19) on top of a tunable garbage collector (Chapter 12). But it
also has behaviors that trip up the unwary: warmup, GC pauses, and benchmarks that
lie. This chapter is the disciplined workflow for making Java fast: **measure
first**, benchmark *correctly* (JMH), profile in production (JFR/async-profiler),
understand GC and allocation, and optimize for the **tail latency** that matters at
scale.

> **The golden rule:** *measure, don't guess.* On the JVM this is doubly true — the
> JIT, inlining, and GC make intuition unreliable, and a microbenchmark without
> warmup measures the interpreter, not your code.

> **C++ contrast:** C++ performance is mostly *static* — you reason about the
> machine code the compiler emits ahead of time. Java performance is *dynamic*: the
> same bytecode runs slowly (interpreted), then fast (C2-compiled), with GC pauses
> threaded through. You optimize a *running system*, not a binary.

---

## 29.1 The Optimization Workflow

```text
1. Set a goal        ── "p99 latency < 50 ms" / "sustain 10k req/s"
2. Measure baseline  ── realistic load, AFTER warmup
3. Profile           ── find the ONE hotspot or the GC/lock cost
4. Hypothesize & fix ── the cheapest change that targets it
5. Re-measure        ── kept only if it moved the goal metric
6. Repeat or stop    ── stop at the goal; don't over-optimize
```

- **Amdahl's law:** optimizing code that's 5% of runtime caps your gain at 5%.
  Attack the dominant cost.
- **The JVM optimizes for you.** Before hand-optimizing, confirm the JIT *isn't*
  already handling it — escape analysis, inlining, and lock elision often make
  "clever" source-level tricks pointless or counterproductive.

---

## 29.2 Microbenchmarking with JMH (Never `System.nanoTime` Loops)

Hand-rolled benchmarks on the JVM are almost always wrong: they measure interpreter
warmup, get dead-code-eliminated by C2, or fold constants. **JMH** (Java
Microbenchmark Harness) is the only credible tool — it forks JVMs, warms up,
iterates, and defeats the optimizer with `Blackhole` and state objects.

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)            // let C2 compile the hot path first
@Measurement(iterations = 5)
@Fork(2)                          // separate JVMs -> isolate run-to-run variance
@State(Scope.Thread)
public class HashBench {
    private Map<Integer,Integer> map;
    @Setup public void init() { map = new HashMap<>(); for (int i=0;i<1000;i++) map.put(i,i); }

    @Benchmark
    public Integer lookup(Blackhole bh) {     // return/consume so it isn't DCE'd
        return map.get(500);
    }
}
```

> **Why a raw loop lies:** `long t=nanoTime(); for(...) work(); print(nanoTime()-t)`
> measures the *interpreter* for the first thousands of iterations, then C2 may
> delete `work()` entirely if its result is unused. JMH exists precisely to make
> these mistakes impossible. Report mean **and** percentiles, not one number.

---

## 29.3 Why the JVM Is (Sometimes) Slow: The Cost Model

- **Warmup.** Code starts interpreted, gets profiled, then C1- then C2-compiled
  (Chapter 19, tiered compilation). The first thousands of calls are slow; a
  process that restarts constantly never reaches peak. (Mitigations: CDS, and AOT/
  native image for startup-sensitive workloads.)
- **Boxing.** `Integer`/`Long` are heap objects with header overhead; autoboxing in
  a hot loop or a `List<Integer>` allocates millions of objects. Use primitive
  arrays / `IntStream` / specialized collections (Eclipse Collections, fastutil)
  for numeric hot paths.
- **Object headers & indirection.** Every object carries a 12–16 byte header;
  pointer-chasing through objects is cache-unfriendly versus contiguous arrays.
- **Megamorphic call sites.** A virtual call site that sees many concrete types
  can't be inlined and de-optimizes; monomorphic/bimorphic sites inline and fly.
- **GC pressure.** High allocation churn means frequent GC — which shows up in the
  *tail* (29.6).

---

## 29.4 Profiling: JFR and async-profiler

- **Java Flight Recorder (JFR)** — a built-in, ~1%-overhead event recorder. Capture
  allocation, GC, locks, I/O, and method profiles from a *running* process and
  analyze in JDK Mission Control. Safe in production.

```bash
# Record 60s from a live process, no restart, low overhead:
jcmd <pid> JFR.start duration=60s filename=rec.jfr settings=profile
# Or start at launch:
java -XX:StartFlightRecording=duration=120s,filename=rec.jfr -jar app.jar
```

- **async-profiler** — a sampling profiler that, unlike many, avoids the
  *safepoint bias* that distorts traditional Java profilers, and produces **flame
  graphs** for CPU, allocations, and lock contention — including native frames.

```bash
asprof -d 30 -e cpu -f flame.html <pid>          # 30s CPU flame graph of a live JVM
asprof -d 30 -e alloc -f alloc.html <pid>        # where allocations come from
```

- **Deterministic vs sampling:** instrumenting profilers (some IDE profilers) record
  every call (accurate counts, heavy, distorting); samplers (JFR, async-profiler)
  snapshot stacks (low overhead, statistical) — prefer samplers for realistic and
  production profiling.

---

## 29.5 Garbage Collection and Tuning

The GC choice and heap sizing dominate latency-sensitive services. Modern HotSpot
collectors:

| GC | Character | Use when |
|---|---|---|
| **G1** (default) | balanced, region-based, ~tens of ms pauses | most services |
| **ZGC** | concurrent, **sub-millisecond** pauses, huge heaps | low-latency, large-heap apps |
| **Shenandoah** | concurrent, low-pause (Red Hat) | similar niche to ZGC |
| **Parallel** | max throughput, longer pauses | batch/throughput jobs, not latency |
| **Serial / Epsilon** | tiny apps / no-op (testing) | constrained or benchmarking |

```bash
# Always enable GC logging in prod — you cannot tune what you can't see:
java -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m -jar app.jar
# Low-latency service: switch collector + size the heap as a % of the container:
java -XX:+UseZGC -XX:MaxRAMPercentage=75 -jar app.jar
```

Principles:

- **Set `-Xms == -Xmx`** (or `MaxRAMPercentage`) for servers — avoid heap resizing
  pauses and let the JVM commit memory up front.
- **Allocation rate drives pause frequency.** The cheapest GC tuning is *allocating
  less* (29.3) — fewer temporary objects means fewer/shorter collections.
- **Container awareness:** modern JVMs read cgroup limits, but verify
  `MaxRAMPercentage` and processor counts — an OOMKilled container (Chapter 30) is
  usually heap + non-heap exceeding the limit, not "a leak."

---

## 29.6 Tail Latency

At scale the number users feel is a **high percentile**, not the average. On the
JVM the tail is shaped by *non-application* factors:

```text
A request's p99 is dominated by: GC pauses, lock contention, JIT de-opt,
   thread-pool queueing, and connection-pool waits — rarely raw CPU.
```

- Report and target **p50/p95/p99/p999**, never averages — averages hide GC pauses
  and queueing entirely.
- **Fan-out amplifies the tail:** a request hitting 100 backends waits on the
  *slowest*; each backend's p99 becomes the typical end-to-end latency. Reducing tail
  variance (GC, hedged requests) beats shaving the mean.
- **Coordinated omission:** load tools that wait for a slow response before sending
  the next under-count the tail — use an open-loop generator.
- ZGC/Shenandoah exist precisely to attack GC-driven tail latency.

---

## 29.7 Concurrency for Throughput

Pick the model by bottleneck (Chapters 16, 28):

| Workload | Tool |
|---|---|
| **I/O-bound** (DB, HTTP, files) | **virtual threads** (Java 21) or async — block cheaply, scale to 100k+ |
| **CPU-bound** parallelizable | parallel streams / `ForkJoinPool` / fixed pool ≈ cores |
| **High-fan-out I/O** | `CompletableFuture` composition or structured concurrency |

```java
// Virtual threads: one cheap thread per task; blocking I/O no longer caps you.
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = ids.stream()
        .map(id -> executor.submit(() -> fetchFromDb(id)))    // blocks cheaply
        .toList();
    for (var f : futures) process(f.get());
}
```

> **Virtual threads change the throughput equation:** the old "thread pool ≈ cores
> and never block" wisdom was a workaround for *expensive platform threads*. Virtual
> threads make blocking I/O cheap, so straightforward blocking code now scales —
> but CPU-bound work still needs a bounded pool (don't fan a CPU loop across 100k
> virtual threads).

---

## 29.8 Memory and Data-Structure Performance

- **Right-size collections:** `new HashMap<>(expectedSize)` /
  `new ArrayList<>(n)` avoids repeated resize-and-copy on growth.
- **Primitive collections** (fastutil, Eclipse Collections) avoid boxing for large
  numeric maps/lists — major memory and cache wins.
- **Streams vs loops:** streams are expressive and usually fast enough, but for the
  hottest inner loops a plain `for` can be faster (no lambda/iterator overhead).
  *Measure* — don't assume either way.
- **Mechanical sympathy:** contiguous arrays beat linked structures for traversal
  (cache locality); beware **false sharing** (two threads writing adjacent fields in
  one cache line) — `@Contended` or padding fixes it for hot counters.
- **Off-heap / direct buffers** (`ByteBuffer.allocateDirect`, the Panama FFM API)
  for large I/O buffers avoid GC and enable zero-copy.

---

## Summary

- **Measure first.** Benchmark with **JMH** (never raw `nanoTime` loops — warmup and
  DCE will fool you); profile live with **JFR** and **async-profiler** flame graphs.
- The JVM cost model is **warmup + boxing + GC + megamorphic dispatch**; let the JIT
  do its job before hand-optimizing.
- **GC is the main latency lever:** know G1 vs ZGC vs Parallel, enable GC logging,
  set `Xms==Xmx`/`MaxRAMPercentage`, and reduce **allocation rate** to reduce pauses.
- **Optimize the tail** (p99/p999), not the average — GC, contention, and queueing
  dominate it, and fan-out amplifies it.
- Use **virtual threads** (Java 21) for I/O-bound throughput; bounded pools for
  CPU-bound work.
- Mind **memory layout**: right-sized and primitive collections, cache locality,
  false sharing, and off-heap buffers for hot data.

## Next Steps

- Write a JMH benchmark comparing `HashMap` vs an `int`-keyed primitive map; observe
  the boxing cost.
- Capture a 60s JFR recording of a running service and open it in JDK Mission
  Control; find the top allocation site.
- Switch a latency-sensitive service to ZGC and compare p99 from the GC logs.
- Revisit **[Chapter 19: JVM Internals](../19_jvm_internals/README.md)** (JIT/warmup)
  and **[Chapter 12: Memory Management](../12_memory_management/README.md)** (GC).
- Continue to **[Chapter 30: Production & Operational Concerns](../30_production_operational/README.md)**.
