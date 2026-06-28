# Chapter 33: System Design & Distributed Systems -- Java

## What This Chapter Covers
The previous chapters made a *single JVM* fast, correct, and operable. This
chapter is about what happens when one process is not enough — when you split work
across machines that fail independently, communicate over an unreliable network,
and must still present a coherent system to users. This is the material a
staff/principal engineer reasons about every day: **latency vs consistency,
partitioning, replication, consensus, caching, and the failure modes that emerge
only at scale.** It is language-agnostic in principle; the examples are Java.

> **Why this is in a Java book:** most "Java at scale" is really distributed
> systems wearing a Spring costume — a fleet of stateless services (Chapter 30) in
> front of databases, caches, and queues. The hard problems live in the *spaces
> between* the JVMs, not inside them.

> **C++ contrast:** the concerns here are language-independent — a C++ service hits
> the same CAP, partitioning, and consensus problems. The JVM only changes the
> per-node story (GC pauses show up in tail latency, Chapter 29); the distributed
> story is identical.

---

## 33.1 The Fallacies and the Latency Numbers

Every distributed-systems bug traces back to forgetting one of the **eight
fallacies of distributed computing**: the network is *not* reliable, *not* zero-
latency, *not* infinite-bandwidth, *not* secure, *not* free, topology *changes*,
there is *not* one administrator, and it is *not* homogeneous. Design as if every
remote call can be slow, fail, or be duplicated — because it will.

Calibrate with the **latency numbers every engineer should know**
(order-of-magnitude, modern hardware):

```text
L1 cache reference ........................   ~1 ns
Main memory reference .....................  ~100 ns
SSD random read ...........................  ~16 µs
Round trip within same datacenter .........  ~500 µs
Disk (HDD) seek ...........................  ~2 ms
Packet round trip CA <-> Netherlands ......  ~150 ms
```

The ratios that drive most designs: **memory is ~100× faster than SSD, SSD is
~100× faster than a network round trip, and a cross-continent round trip is
~300× a same-DC one.** This is *why* we cache, batch, co-locate data with compute,
and never treat "just add a network call" as free. On the JVM, remember a GC pause
(Chapter 29) can add tens of milliseconds *on top* of any of these.

---

## 33.2 Scaling: Vertical, Horizontal, and Statelessness

- **Vertical scaling** (a bigger machine) is simplest and often the right first
  move — no distributed-systems tax. It hits a ceiling and a price cliff.
- **Horizontal scaling** (more machines) is unbounded but buys you this whole
  chapter's problem set.

Horizontal scaling *requires* **stateless** services (Chapter 30): any instance can
serve any request because no request-affecting state lives on the heap. State goes
to shared backing services (databases, Redis, object stores).

```text
            ┌── instance 1 ──┐
client ─▶ LB ┼── instance 2 ──┼─▶ shared state (DB / cache / queue / blob)
            └── instance N ──┘
   stateless: any instance handles any request; scale N up or down at will
```

> **Sticky sessions are a smell.** Routing a user to "their" JVM reintroduces state
> and breaks elasticity, failover, and rolling deploys. Put session state in a
> shared store (Redis via Spring Session, or a signed cookie) and keep instances
> fungible.

---

## 33.3 The CAP Theorem (and Why PACELC Is More Useful)

**CAP**: when a network **P**artition happens, a distributed store must choose
between **C**onsistency (every read sees the latest write) and **A**vailability
(every request gets a non-error response). You cannot have both *during a
partition* — and CAP says nothing the rest of the time.

That's why **PACELC** is sharper: **if P**artitioned, choose **A** or **C**;
**E**lse (normal operation), choose **L**atency or **C**onsistency. Every real
store sits somewhere on this map:

| System | Partition | Else | Character |
|---|---|---|---|
| Classic RDBMS (single primary) | CP | EC | consistent, lower availability on failover |
| DynamoDB / Cassandra (tunable) | AP or CP | EL or EC | tunable per query |
| ZooKeeper / etcd | CP | EC | consistency-first coordination |
| DNS | AP | EL | availability-first, eventually consistent |

The practical takeaway: **consistency is a per-operation choice, not a per-system
one.** "Add to cart" can be eventually consistent; "charge the card" cannot. Pick
the model per data flow.

---

## 33.4 Consistency Models

Consistency is a *spectrum*, strongest (most intuitive, most expensive) to weakest:

- **Linearizable (strong):** every operation appears atomic at a single instant;
  once a write returns, all later reads see it. The single-machine mental model —
  expensive across nodes.
- **Sequential / serializable:** all nodes agree on *an* order (serializable is the
  DB-transaction analogue, Chapter 34).
- **Causal:** causally related operations are seen in order by everyone; concurrent
  ones may differ. Often strong enough and much cheaper.
- **Read-your-writes / monotonic reads (session guarantees):** a client sees its own
  writes and never goes backward — usually the *minimum* users notice.
- **Eventual:** if writes stop, replicas converge — eventually. Says nothing about
  *when*.

```java
// The classic eventual-consistency footgun: write to primary, then immediately
// read from a replica that hasn't caught up -> the user "loses" their own update.
profileRepo.save(profile);                 // -> primary
var name = profileReadReplica.find(id);    // -> a read replica, still stale!
// Fix: read-your-writes — route the just-written user's reads to the primary for a
// short window, or read from primary when freshness matters.
```

> Eventual consistency isn't "wrong," it's a *contract*. The bug is using it where
> users expect read-your-writes, or where two systems must agree (inventory,
> balances). Name the guarantee each path needs and pick storage to match.

---

## 33.5 Partitioning (Sharding)

When data or load outgrows one node, **partition** it across many:

- **Range partitioning** — contiguous key ranges per shard. Great for range scans;
  prone to **hot spots** (partitioning by timestamp sends all "now" writes to one
  shard).
- **Hash partitioning** — `hash(key) % N`. Even load; kills range scans. Naive `% N`
  is a trap: changing `N` remaps almost every key.
- **Consistent hashing** — keys and nodes sit on a hash ring; a key belongs to the
  next node clockwise. Adding/removing a node remaps only `~1/N` of keys. **Virtual
  nodes** smooth imbalance.

```java
import java.util.*;

public class HashRing {                        // consistent hashing with virtual nodes
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int vnodes;

    public HashRing(Collection<String> nodes, int vnodes) {
        this.vnodes = vnodes;
        nodes.forEach(this::add);
    }
    public void add(String node) {
        for (int v = 0; v < vnodes; v++) ring.put(hash(node + "#" + v), node);
    }
    public String nodeFor(String key) {
        if (ring.isEmpty()) return null;
        long h = hash(key);
        SortedMap<Long, String> tail = ring.tailMap(h);          // next node clockwise
        Long k = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(k);
    }
    private long hash(String s) {                                  // (use a good hash in prod)
        return s.hashCode() & 0xffffffffL;
    }
}
```

The hard part isn't choosing a key — it's **rebalancing** (moving data when shards
change) and **cross-shard operations** (a join or transaction spanning shards is
expensive and usually avoided by denormalizing, Chapter 34).

---

## 33.6 Replication

Replication copies data to multiple nodes for **availability** (survive node loss)
and **read scaling**:

- **Single-leader (primary/replica):** all writes go to the leader, which streams a
  log to followers. Reads fan out to followers (stale by the *replication lag*).
  Simple, the RDBMS default; leader failure needs **failover** (the dangerous
  moment — split-brain, lost writes).
- **Multi-leader:** multiple write nodes (e.g. per region) replicating to each
  other — great for multi-region write latency, but introduces **write conflicts**
  (last-write-wins is lossy; CRDTs converge correctly).
- **Leaderless (Dynamo-style):** clients write to *several* replicas and read from
  *several*; correctness comes from **quorums**.

**Quorum math:** with `N` replicas, `W` write acks and `R` read acks give strong
consistency for a key when **W + R > N** (read and write sets overlap):

```text
N=3:  W=3,R=1 -> fast reads, fragile writes
      W=1,R=3 -> fast writes, slow reads
      W=2,R=2 -> balanced, W+R=4 > 3 (strong)  ◀ common default
```

> **Replication lag is a feature you design around**, not a bug to ignore.
> Read-your-writes, monotonic reads, and "read from primary after write" all exist
> to paper over the window where a follower trails the leader (33.4).

---

## 33.7 Consensus and Coordination

Some decisions require *all* nodes to agree exactly once: who is leader, is this
lock held, what is the committed order. This is **consensus** — provably hard, but
solved by **Paxos** and **Raft**. You almost never implement these; you *use* a
system that did.

- **Raft/Paxos** give a replicated, linearizable log as long as a **majority**
  (quorum) is alive. A 5-node cluster tolerates 2 failures; a 3-node cluster
  tolerates 1.
- **Coordination services** — **etcd**, **ZooKeeper** (Apache Curator is the Java
  client), **Consul** — expose leader election, distributed locks, config, and
  service discovery. Reach for them; don't build a lock out of a database row under
  load.

```java
// Distributed lock via a coordination store (conceptual). The non-negotiables:
var lock = client.lock("inventory:sku-123", Duration.ofSeconds(10));  // TTL: dead holder auto-releases
if (lock.acquire(Duration.ofSeconds(2))) {
    try {
        doCriticalSection();        // MUST finish well within the TTL, or fence it
    } finally {
        lock.release();
    }
}
```

> **Distributed locks are dangerous.** A JVM can acquire a lock, pause (a long GC,
> Chapter 12/29, or VM migration), have its TTL expire and the lock reassigned,
> then wake and act — two holders at once. The fix is **fencing tokens**: the lock
> service issues a monotonically increasing token, and the protected resource
> rejects any write with a lower token than the highest it has seen. A lock without
> fencing reduces contention but is *not* safe for correctness. (Note: a stop-the-
> world GC pause makes this failure mode very real on the JVM.)

---

## 33.8 Time, Ordering, and Idempotency

There is **no global clock**. Wall-clock time across machines drifts and jumps
(NTP, leap seconds) — never use `System.currentTimeMillis()` to order distributed
events or expire leases precisely. (Use `System.nanoTime()` only for local
durations; it's not comparable across machines.)

- **Logical clocks (Lamport):** a counter bumped on each event and on receive
  (`max(local, received) + 1`) gives a consistent *happens-before* order.
- **Vector clocks:** detect *concurrency* (two events, neither before the other) —
  what conflict resolution needs.
- **Hybrid logical clocks / TrueTime:** combine physical and logical time with
  bounded uncertainty (Spanner's approach).

Because the network duplicates and retries (Chapter 30), **idempotency** is what
makes a distributed system survivable: doing an operation twice equals once.

```java
// Idempotency key: the client sends a unique key; the server dedupes.
public Charge charge(String idempotencyKey, Money amount, Card card) {
    Charge prior = store.get(idempotencyKey);
    if (prior != null) return prior;            // already done — return the SAME result
    Charge result = gateway.charge(amount, card);
    store.put(idempotencyKey, result, Duration.ofHours(24));
    return result;                              // a client retry can't double-charge
}
```

> Design writes to be **idempotent or idempotency-keyed** from day one. It's the
> single most leveraged habit for surviving retries, at-least-once queues, and
> reprocessing — and nearly impossible to retrofit.

---

## 33.9 Communication Patterns: Sync vs Async

- **Synchronous (request/response):** REST, gRPC (Chapter 35). Simple model;
  couples caller and callee in *time* (both up) and *latency* (caller waits).
  Failure is immediate and visible.
- **Asynchronous (messaging):** the caller drops a message on a **queue** or **log**
  and moves on; a consumer processes it later (Chapter 34). Decouples services,
  absorbs spikes, enables retries — at the cost of eventual consistency and harder
  tracing.

```text
sync  :  service A ──REST/gRPC──▶ service B     (B must be up; A waits; tight coupling)
async :  service A ──▶ [ queue / log ] ──▶ service B   (B can be down/slow; A doesn't wait)
```

Delivery guarantees, weakest to strongest:

- **At-most-once:** fire and forget; may lose messages. Fine for metrics.
- **At-least-once:** retried until acked; may *duplicate*. The common default — pair
  with idempotent consumers (33.8).
- **Exactly-once:** achievable only as *effectively*-once via idempotency + dedup or
  a transactional outbox. Treat vendor "exactly-once" as "at-least-once + dedup
  under specific conditions."

---

## 33.10 Caching at the System Level

Caching (Chapter 29 covered in-JVM, e.g. Caffeine) is the highest-leverage
distributed performance tool — and a top source of subtle bugs. Where caches live:

```text
browser ─▶ CDN ─▶ reverse-proxy ─▶ in-JVM cache (Caffeine) ─▶ distributed cache (Redis) ─▶ DB
   each layer trades freshness for latency/offload; invalidation gets harder going right
```

Write/refresh strategies:

| Strategy | How | Trade-off |
|---|---|---|
| **Cache-aside** (lazy) | app reads cache; on miss, loads DB and populates | simple default; first request slow, stampede risk |
| **Write-through** | write to cache + DB together | always warm; slower writes |
| **Write-behind** | write to cache, async flush to DB | fast writes; risk of loss on crash |

The two classic failures:

- **Stampede / thundering herd** — a hot key expires and thousands of requests hit
  the DB at once. Mitigate with single-flight (Chapter 30), early/probabilistic
  refresh, or jittered TTLs.
- **Invalidation** — prefer short TTLs and event-driven invalidation over manual
  deletes; accept bounded staleness where you can.

---

## 33.11 Designing for Failure

At scale, *something is always broken*. Keep partial failure partial:

- **Redundancy & no SPOF:** N+1 replicas, multi-AZ, and for the top tiers
  multi-region. Every singleton (a lone primary, one queue) is an outage waiting.
- **Blast-radius reduction:** **bulkheads** (isolate resource pools — Resilience4j,
  Chapter 30), **cell-based architecture** (independent cells so a bad deploy or
  poison input hits one cell), and **graceful degradation** (serve stale/partial
  results when a dependency is down).
- **Backpressure & load shedding** (Chapters 29, 30): bound queues, shed excess load
  early rather than collapsing.
- **Failure injection:** you don't know the system survives failure until you
  *cause* it — chaos engineering in staging and, carefully, prod.

> **The metastable-failure trap:** under retry-amplification or cache stampede a
> system can get *stuck* failed even after the trigger is gone, because the load it
> generates sustains the failure. Retry budgets, circuit breakers, and load
> shedding (Chapter 30) break the loop.

---

## 33.12 A Worked Sketch: A URL Shortener at Scale

```text
Requirements: 100:1 read:write; billions of links; <50 ms p99 redirect; never
lose a mapping; short codes are permanent.

Write path  (POST /shorten):
  generate id ──▶ base62-encode ──▶ store {code: url} ──▶ return short URL
    - ID generation: range-allocated counter blocks per host, or a Snowflake id —
      NOT a single DB autoincrement bottleneck.
    - Store: a partitioned key-value store (33.5), key = code. No joins needed.

Read path   (GET /<code>):
  edge/CDN ─▶ Redis (code->url) ─▶ KV store ─▶ 301/302 redirect
    - 100:1 reads -> cache is load-bearing; codes are immutable so caching is easy
      (no invalidation problem — the best kind of cache).

Cross-cutting:
  - Idempotency: same long URL -> same code (dedup by hashing the URL), so retries
    don't create duplicates (33.8).
  - Analytics: emit a click event to a log (33.9), aggregate offline (Ch 34) —
    never make the redirect wait on analytics.
  - Capacity: estimate QPS, storage/yr, and cache working set from 33.1 BEFORE
    choosing instance counts.
```

The staff-level move is reasoning quantitatively (33.1), naming the consistency
needs per path (33.4), and pushing the 100:1 read skew into an immutable cache —
not reaching for a fancier database.

---

## Summary

- Distributed systems are defined by **partial failure over an unreliable
  network**; internalize the fallacies and the **latency numbers** (33.1).
- Scale **horizontally with stateless services**; push state to backing services.
- **CAP/PACELC** make consistency a *per-operation* choice; pick the weakest
  **consistency model** users won't notice, and design around **replication lag**.
- **Partition** for scale (consistent hashing), **replicate** for availability
  (quorums, `W+R>N`), and use **consensus systems** (Raft/etcd) for the few
  decisions needing agreement — with **fencing tokens** for locks (GC pauses make
  this real on the JVM).
- There is **no global clock**: order with logical/vector clocks and make writes
  **idempotent**; prefer **at-least-once + idempotent consumers**.
- **Cache** aggressively but treat **invalidation and stampedes** as first-class;
  **design for failure** with redundancy, bulkheads, backpressure, and chaos
  testing — and beware **metastable** failure loops.

## Next Steps

- Sketch the URL-shortener (33.12) end to end and label the consistency model of
  each path.
- Implement the `HashRing` and verify only ~1/N keys move when you add a node.
- Add idempotency keys to one write endpoint and prove a duplicate request is a
  no-op.
- Revisit **[Chapter 30: Production & Operational Concerns](../30_production_operational/README.md)**
  (resilience, stateless services) and **[Chapter 29: Performance Engineering](../29_performance_engineering/README.md)** (GC and tail latency).
- Continue to **[Chapter 34: Data-Intensive Systems](../34_data_intensive_systems/README.md)**.
