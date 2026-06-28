# 37 — Garbage Collection: Algorithms & Tuning

## What This Chapter Covers

[Chapter 12: Memory Management](../12_memory_management/README.md) introduced the
garbage-collected heap; [Chapter 19: JVM Internals](../19_jvm_internals/README.md)
showed *where* GC lives and *named* the collectors. This chapter explains **how
they actually work** and **how to tune them on large heaps** — the difference
between a service that pauses for 3 seconds and one that pauses for 300 µs.

- **Foundations:** reachability & GC roots, why the JVM **traces**, the
  **generational hypothesis**, **TLAB** bump-pointer allocation, tenuring
- **Phases & barriers:** mark/sweep/compact/evacuate, **write barriers**,
  **card tables**/**remembered sets**, **SATB** vs incremental-update, **safepoints**
- **The collectors:** Serial, Parallel, **G1**, **ZGC**, **Shenandoah**, **Epsilon**
- **Tuning:** choosing by goal, heap sizing, allocation rate, humongous-object pitfalls
- **Reading the evidence:** `-Xlog:gc*`, JFR, container-awareness
- A **Symptom / Cause / Fix** catalogue

> **C++ contrast:** in C++ you reclaim memory **manually** — `delete`, or
> deterministically via **RAII** (`~T()` runs the instant a scope exits or a
> `unique_ptr`/`shared_ptr` drops to zero). Reclamation is *prompt*, *predictable*,
> and *your responsibility* — leak it and it leaks forever; free it twice and you
> corrupt the heap. Java **traces** the live graph on a background schedule: you
> never `free` and never double-free, but reclamation is *non-deterministic*. GC
> trades C++'s zero-pause determinism for safety and allocation throughput.

---

## 37.1 Foundations: Reachability and GC Roots

The collector finds the **live** objects and reclaims the rest. "Live" means
**reachable from a GC root** — not "your program will use it again" (the JVM can't
know that). GC roots are the graph's entry points: **local variables & operand
stacks** of live threads, **static fields** (in Metaspace), **JNI references**,
active threads, and system loaders.

```java
Object a = new Object();   // reachable from a stack local (a GC root) -> LIVE
a = null;                  // now unreachable -> eligible for GC
                           // (not necessarily collected yet — only at the next GC)
```

An object survives if *any* path of references reaches it from *any* root.

### Tracing vs reference counting — why the JVM traces

A reference-counting GC frees an object when its per-object counter hits zero —
prompt, but two fatal flaws for a general runtime: **it cannot reclaim cycles**,
and **counter maintenance is racy and expensive** (every reference store must
atomically bump counters, murdering multi-threaded throughput).

```java
// A reference cycle. Ref-counting leaks this; tracing collects it, because once
// a and b are unreachable from roots the whole island is never marked.
class Node { Node next; }
Node a = new Node(), b = new Node();
a.next = b; b.next = a;     // counts: a=1 (from b), b=1 (from a)
a = b = null;               // unreachable island — tracing GC reclaims both
```

The JVM **traces**: start at roots, walk the graph, mark the reachable set,
reclaim the rest. Cycles vanish naturally; no per-store overhead.

> **C++ contrast:** `std::shared_ptr` *is* reference counting — and it *does* leak
> cycles, which is why C++ also provides `std::weak_ptr` to break them. Java's
> tracing GC needs no such workaround for ordinary references (it still offers
> `Weak`/`Soft`/`PhantomReference` for caches — see [Chapter 12](../12_memory_management/README.md)).

---

## 37.2 The Generational Hypothesis

The dominant observation across real programs — the **weak generational hypothesis**:

> **Most objects die young.** Loop temporaries, request-scoped buffers, and stream
> intermediates become garbage almost immediately; the few that survive their first
> collection tend to live a long time.

So **focusing effort on the youngest objects** reclaims the most memory for the
least work: scan a small region, find 95–99% already dead, copy out the survivors.

```
┌──────────────────────────── Java Heap ─────────────────────────────┐
│  YOUNG GENERATION                          │  OLD GENERATION         │
│  ┌──────────┐ ┌──────┐ ┌──────┐            │  ┌───────────────────┐  │
│  │   Eden    │ │  S0  │ │  S1  │            │  │  Tenured           │  │
│  │ (new      │ │ surv │ │ surv │            │  │  (long-lived       │  │
│  │  objects) │ │      │ │      │            │  │   survivors)       │  │
│  └──────────┘ └──────┘ └──────┘            │  └───────────────────┘  │
│  Minor (Young) GC: frequent, cheap, copies  │  Major/Full GC: rarer,  │
│  survivors                                  │  costlier                │
└─────────────────────────────────────────────────────────────────────┘
```

- **Eden** — almost all new objects are born here.
- **Survivors S0/S1** — two equal spaces; each young GC **copies** live objects
  into the empty one (one is always empty). Objects ping-pong, **age** incrementing.
- **Old/Tenured** — objects surviving enough young GCs are **promoted** here.

**Consequences:** young GC is cheap (work ∝ *survivors*, not garbage); copying
**compacts for free**, keeping Eden contiguous; old GC is rarer but costlier — that
is where concurrent collectors earn their keep.

---

## 37.3 Allocation: TLABs and Bump-Pointer

In a contiguous Eden, allocation is just **bumping a pointer**:

```
allocate(size):  ptr = top;  top = top + size;  return ptr   // one add + bounds check
```

But Eden is shared, so a global `top` would need an atomic CAS per allocation. The
JVM uses **Thread-Local Allocation Buffers (TLABs)**: each thread gets a private
chunk of Eden and bump-allocates within it **lock-free**, grabbing a fresh TLAB
only when one fills.

```java
for (int i = 0; i < 1_000_000; i++) {
    var buf = new byte[64];   // ~lock-free bump allocation; no global contention
    process(buf);             // ...and buf dies almost immediately (young!)
}
```

> This is why "allocation is cheap in Java" is *true* — often faster than C
> `malloc`. The cost is paid later, at collection, ∝ **survivors**.

**Promotion / tenuring:** a survivor's **age** rises each young GC; at
`-XX:MaxTenuringThreshold` (or survivor overflow) it is **promoted** to old.
**Premature promotion** — medium-lived objects tenured before they die — fills the
old gen with soon-dead objects and triggers expensive old-gen GCs; undersized
survivor spaces are the usual culprit.

---

## 37.4 The Phases and the Barriers

Every tracing collector composes a few primitives:

- **Mark** — trace from roots, mark every reachable object.
- **Sweep** — reclaim the unmarked (leaves **fragmentation**).
- **Compact** — slide live objects together in place (defragments; updates refs).
- **Evacuate (copy)** — move live objects to a fresh region; free the old wholesale.
  Cheap when survivors are few — this is what young GC does.

### The cross-generational problem and the write barrier

To collect young **without** scanning the huge old gen, the collector must know
which old-gen objects point **into** young. It tracks these via a **write
barrier** — a snippet the JIT injects after every reference-field store:

```java
obj.field = other;   // JIT emits, conceptually:
                      //   obj.field = other; writeBarrier(obj, other);
```

- **Card table** — splits the heap into ~512-byte "cards"; the barrier just dirties
  the card holding `obj`. At young GC, only **dirty cards** are scanned. Cheap, coarse.
- **Remembered sets (RSets)** — per-region "who points into me," used by **G1**.
  More precise (collect one region at a time), more bookkeeping.

### Concurrent marking: SATB vs incremental-update

A concurrent marker runs while the app ("mutator") keeps mutating refs:

- **SATB (Snapshot-At-The-Beginning)** — logically marks everything live *when
  marking began*; the barrier records the **overwritten** reference so a deleted
  edge can't hide a live object. May retain "floating garbage" (collected next
  cycle). Used by **G1** and **Shenandoah**.
- **Incremental-update** — the barrier records **newly added** references. Used by
  the retired CMS.

### Safepoints and "time to safepoint"

STW GC phases, deoptimization, etc. require every app thread paused at a known,
consistent point — a **safepoint** — where stack maps and reference locations are
trustworthy. The JVM requests a pause and waits for all threads.

> **Time-to-safepoint (TTSP)** matters as much as pause time. A thread stuck in a
> long **counted loop** (no poll) or a slow native call makes *everyone* wait. Your
> log may say "2 ms pause" while the real stall was 200 ms of waiting for one
> thread. Diagnose with `-Xlog:safepoint`.

**STW** phases pause all mutators (roots must be scanned at a safepoint — even
"concurrent" collectors do brief STW root scans); **concurrent** phases run
alongside the app, trading CPU and barrier overhead for tiny pauses.

---

## 37.5 The Collectors

### Serial — `-XX:+UseSerialGC`
One thread, world fully stopped for the whole collection. Minimal overhead. Ideal
for **small heaps** (< ~100 MB), single-core machines, and tiny containers.

### Parallel — `-XX:+UseParallelGC`
The **throughput collector**: still fully **STW**, but many threads mark/copy/
compact in parallel. Maximizes work per CPU-second, accepting long pauses. Perfect
for **batch jobs/analytics** where total runtime matters and pauses don't.

### G1 — `-XX:+UseG1GC` (default since Java 9)
**Garbage-First** splits the heap into ~2048 equal **regions** tagged Eden,
Survivor, Old, or **Humongous** at runtime — generations are *logical* sets of
regions.

- **Incremental & region-based:** collects a *subset* of regions (a **collection
  set**) per pause, prioritizing the most-garbage ones (hence "garbage first").
- **Pause-time target:** `-XX:MaxGCPauseMillis=200` (default 200 ms) sizes the
  collection set to *try* to hit it — a **soft** goal, not a guarantee.
- **Mixed collections** reclaim young *and* selected old regions after a concurrent
  **SATB** mark cycle.
- **Remembered sets** per region let G1 collect a region without scanning the heap.
- **Humongous objects** — allocations larger than **half a region** get their own
  contiguous Humongous regions in old (see §37.7).

The balanced default: good latency *and* throughput on multi-GB heaps.

### ZGC — `-XX:+UseZGC` (generational in JDK 21+: add `-XX:+ZGenerational`)
**Concurrent, region-based**, engineered for **sub-millisecond pauses** on heaps
from hundreds of MB to **multiple terabytes** — pause time is **independent of heap
size**. Even **compaction** is concurrent.

- **Colored pointers** — GC metadata bits live *inside the 64-bit reference*
  (marked, remapped…), so ZGC reasons about an object's state from the pointer.
- **Load barriers** — a barrier on reference *loads*: when the app reads a ref, it
  checks the color bits and, if the object moved, fixes the pointer **on the spot**
  ("self-healing"). This lets ZGC relocate objects without an STW fix-up.
- **Generational ZGC** (JDK 21+) adds a young gen, slashing CPU/memory overhead.
  Prefer it.

### Shenandoah — `-XX:+UseShenandoahGC`
Also **concurrent** with the same low-pause goal, via **concurrent evacuation**
using a **Brooks / load-reference barrier**: each object carries a forwarding word
so readers/writers always reach the *current* copy while objects relocate
concurrently. Very low pauses across a wide range of heap sizes.

### Epsilon — `-XX:+UseEpsilonGC` (needs `-XX:+UnlockExperimentalVMOptions`)
A **no-op** collector: allocates, **never reclaims**; OOMs when the heap fills.
For **performance testing** (measure allocation with zero GC noise) and
**extremely short-lived jobs**. **Never** in a long-running service.

| Collector | STW behavior | Heap sweet spot | Optimizes for | Mechanism highlight |
|-----------|-------------|------------------|---------------|---------------------|
| **Serial** | Full STW, 1 thread | < ~100 MB | Footprint | Single-threaded copy/compact |
| **Parallel** | Full STW, N threads | Any (batch) | **Throughput** | Parallel STW copy/compact |
| **G1** (default) | Short incremental STW | ~1 GB – tens of GB | **Balance** | Regions, RSets, SATB, `MaxGCPauseMillis` |
| **ZGC** | Sub-ms, mostly concurrent | ~100s MB – **multi-TB** | **Latency** | Colored pointers + load barriers |
| **Shenandoah** | Sub-ms, mostly concurrent | Small – very large | **Latency** | Brooks barrier, concurrent evacuation |
| **Epsilon** | None | N/A | Measurement | No-op; OOM when full |

---

## 37.6 Choosing a Collector by Goal

Three competing objectives; you can't maximize all three.

- **Throughput** → **Parallel** (batch); **G1** (general services).
- **Latency** (tail pauses for request/response, trading) → **ZGC** or **Shenandoah**.
- **Footprint** (small containers, CLIs) → **Serial**.

```bash
# General-purpose service (the default — usually leave it alone):
java -XX:+UseG1GC -Xms4g -Xmx4g -jar app.jar

# Latency-critical service on a big heap (P99 pause budget in microseconds):
java -XX:+UseZGC -XX:+ZGenerational -Xms32g -Xmx32g -jar app.jar

# Nightly batch ETL — total runtime is all that matters:
java -XX:+UseParallelGC -Xmx16g -jar etl.jar
```

> **Don't tune blindly.** For 90% of services the best "tuning" is: pick the right
> collector, size the heap sensibly, then **measure before touching anything else**.
> Most production flag-soup is cargo-cult copy that *hurts*. See
> [Chapter 29: Performance Engineering](../29_performance_engineering/README.md).

---

## 37.7 Tuning in Practice

### Heap sizing — why set `-Xms` equal to `-Xmx`

```bash
java -Xms8g -Xmx8g -jar app.jar     # initial == maximum: commit it all up front
```

When `-Xms` < `-Xmx`, the JVM grows the heap on demand — each growth pauses, and a
server reaches `-Xmx` anyway. Setting them **equal** on a long-running service
avoids resize pauses, stabilizes the size the collector reasons about, and makes
behavior predictable.

### Allocation rate — the key metric

The most predictive GC-pressure metric is **allocation rate** (bytes/s of new
objects), *not* heap size. High rate → Eden fills fast → frequent young GCs → more
GC CPU and premature promotion.

```java
// WRONG — boxes every element and builds a list per call; high churn.
List<Integer> hot(int n) {
    List<Integer> out = new ArrayList<>();
    for (int i = 0; i < n; i++) out.add(Integer.valueOf(i * 2));  // boxing!
    return out;
}

// RIGHT — primitive stream, no boxing, no per-element object; young GCs rarer.
int[] hot(int n) {
    return java.util.stream.IntStream.range(0, n).map(i -> i * 2).toArray();
}
```

> Reducing allocation rate (avoid boxing, reuse buffers, let **escape analysis**
> stack-allocate — [Chapter 19 §19.6](../19_jvm_internals/README.md)) is almost
> always more effective than tuning flags. Fewer objects = less to collect.

### Humongous-object pitfalls (G1)

An object larger than **half a region** is "humongous": allocated into dedicated
contiguous old-gen regions, bypassing Eden. Many such allocations **fragment** the
heap (freeing one leaves a region-sized hole only another humongous object fills)
and trigger more collections.

```bash
# Fix A: enlarge regions so the object is no longer "humongous":
java -XX:+UseG1GC -XX:G1HeapRegionSize=32m -jar app.jar
# Fix B (better): stop allocating multi-MB arrays/buffers; stream or chunk instead.
```

---

## 37.8 Reading the Evidence

### Unified GC logging — `-Xlog:gc*`

```bash
java -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=20m -jar app.jar
```

A young-collection line reads roughly:

```
[12.345s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause)
                     512M->48M(2048M) 6.231ms
#  ^cause                              ^before->after(total heap)  ^pause
```

- **Pause time** — your latency budget; watch **P99/P99.9**, not the mean.
- **Cause** — `G1 Evacuation Pause` (normal), `Allocation Failure`, **`Full GC`**
  (the alarm bell — §37.9).
- **before→after(total)** — `after` creeping up across GCs = **leak**; `after` near
  `total` = heap too small.
- **Frequency** — back-to-back young GCs = high allocation rate.

### JFR and allocation profiling

```bash
java -XX:StartFlightRecording=duration=120s,filename=rec.jfr -jar app.jar
jfr print --events jdk.GCPhasePause,jdk.ObjectAllocationSample rec.jfr
```

Allocation-sample events show **which call sites** allocate most — point churn
reduction there. Open `rec.jfr` in JDK Mission Control; async-profiler `alloc` mode
gives allocation flame graphs ([Chapter 29](../29_performance_engineering/README.md)).

### Container awareness

A modern JVM reads **cgroup** limits, sizing GC ergonomics to the *container*.
Prefer a percentage over a hard `-Xmx`:

```bash
java -XX:MaxRAMPercentage=75.0 -jar app.jar    # 75% of the cgroup memory limit
```

> Leave headroom — Metaspace, thread stacks, code cache, and direct buffers don't
> count against `-Xmx`. Set heap to ~70–80% of the limit, never 100%, or the kernel
> **OOMKills** the process though the Java heap "had room." Ties to
> [Chapter 19 §19.11](../19_jvm_internals/README.md).

---

## 37.9 Common Problems — Symptom / Cause / Fix

**Long STW pauses on a big heap**
- *Symptom:* P99 spikes to hundreds of ms/seconds; long `Pause` lines that grow with heap.
- *Cause:* an STW-heavy collector (Parallel, or G1 stretched too far) on too large a heap.
- *Fix:* switch to a **concurrent, pause-independent** collector —
  `-XX:+UseZGC -XX:+ZGenerational` (or Shenandoah); re-measure tail latency.

**High allocation rate → frequent young GC**
- *Symptom:* back-to-back young collections; high GC CPU% though each pause is short.
- *Cause:* churn of short-lived objects (boxing, per-request buffers, intermediate collections).
- *Fix:* **reduce churn** — primitive streams/arrays, reuse buffers, escape analysis
  ([Chapter 19](../19_jvm_internals/README.md)). Enlarge young gen (`-Xmn`) only after.

**Humongous allocations fragment G1**
- *Symptom:* `humongous allocation` log lines; old-gen pressure and Full GCs despite a large heap.
- *Cause:* objects larger than half a G1 region.
- *Fix:* raise `-XX:G1HeapRegionSize`, or — better — stop allocating giant contiguous objects.

**Full GC / OutOfMemoryError**
- *Symptom:* `Pause Full` stalls, or `OutOfMemoryError: Java heap space` and death.
- *Cause:* heap genuinely too small, **or** a **memory leak** (live set grows without
  bound — lingering refs, unbounded caches, un-deregistered listeners).
- *Fix:* if the working set is truly large, raise `-Xmx`. Else **find the leak**:
  `-XX:+HeapDumpOnOutOfMemoryError`, analyze the `.hprof` in Eclipse MAT, chase the
  GC-root retention path. See [Chapter 12](../12_memory_management/README.md).

**Pauses longer than the GC log claims**
- *Symptom:* clients see stalls far longer than reported GC pauses.
- *Cause:* **time-to-safepoint** — one thread (counted loop, slow JNI) is slow to reach a safepoint.
- *Fix:* `-Xlog:safepoint`; break up long counted loops; check native calls.
  ([Chapter 29](../29_performance_engineering/README.md)).

---

## 37.10 Best Practices

- **Pick the collector by goal first**; the default **G1** suits most services.
- **Set `-Xms == -Xmx`** on servers; use `-XX:MaxRAMPercentage` in containers with headroom.
- **Drive down allocation rate** before tuning flags — fewer objects beats faster collection.
- **Always log GC** (`-Xlog:gc*` to a rotating file) in production.
- **Read tail latency**, not averages — GC pain lives in P99/P99.9.
- **Change one thing, measure, repeat.** Resist flag soup.
- **Sub-ms pauses on big heaps** → **generational ZGC**. **Never** run Epsilon in production.

---

## Summary

- GC keeps anything **reachable from a GC root**; the JVM **traces** the live graph
  (not reference-counting), reclaiming cycles with no per-write overhead.
- The **generational hypothesis** shapes the Eden + survivors + old layout, makes
  **TLAB bump-pointer** allocation nearly free, and makes **young GC cheap** (∝ survivors).
- Collectors compose **mark/sweep/compact/evacuate**, track cross-generational refs
  via **write barriers + card tables / remembered sets**, mark concurrently via
  **SATB** or **incremental-update**, and pause at **safepoints**.
- Choose **Serial** (footprint), **Parallel** (throughput), **G1** (balanced
  default), **ZGC**/**Shenandoah** (low latency, large heaps), **Epsilon** (testing).
- Tune by **goal**, size the heap (`-Xms == -Xmx`, `MaxRAMPercentage`), watch
  **allocation rate** and humongous objects, **read `-Xlog:gc*` + JFR** before any flag.

> **C++ contrast in one line:** C++ reclaims memory *deterministically and manually*
> (RAII, `delete`) with zero pause but every leak/double-free on you; the JVM
> reclaims *automatically* by tracing on a schedule, trading determinism and brief
> (often sub-millisecond) pauses for memory safety and high allocation throughput.

---

> **Next:** [38 — Data Structures](../data_structures/README.md)
>
> **Related:** [12 — Memory Management](../12_memory_management/README.md) ·
> [18 — Memory Model](../18_memory_model/README.md) ·
> [19 — JVM Internals](../19_jvm_internals/README.md) ·
> [29 — Performance Engineering](../29_performance_engineering/README.md) ·
> [30 — Production & Operational Concerns](../30_production_operational/README.md)
