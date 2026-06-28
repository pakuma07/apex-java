# Chapter 34: Data-Intensive Systems -- Java

## What This Chapter Covers
Chapter 24 taught you to *use* a database with JDBC and JPA. This chapter explains
what's happening *underneath* and *around* it — the knowledge that lets a
staff/principal engineer choose a data store, design a schema that survives growth,
write queries that don't fall over at 100×, and wire databases, caches, and
streams into a coherent data platform. The theme: **most large systems are
bottlenecked on data, not CPU** — so data architecture *is* system architecture.

> **Version note:** Java 21. Examples use JDBC/JPA idioms from Chapter 24 plus
> conceptual SQL, Kafka, and Redis patterns. Principles are engine-agnostic.

> **C++ contrast:** these are database and data-platform concerns, independent of
> the application language. A C++ service faces the same index, transaction, and
> streaming trade-offs; only the client libraries differ.

---

## 34.1 Storage Engines: How Databases Actually Store Data

Two families dominate, and the choice shows up in everything from write throughput
to read latency:

- **B-tree (read-optimized):** the default for relational databases (PostgreSQL,
  MySQL/InnoDB). Data lives in a balanced tree of fixed-size pages; lookups and
  range scans are `O(log n)` with few disk seeks. Writes update pages in place (with
  a write-ahead log for durability). Predictable reads; writes amplify.
- **LSM-tree (write-optimized):** Cassandra, RocksDB, ScyllaDB. Writes go to an
  in-memory **memtable** + append-only log, then flush to immutable sorted files
  (**SSTables**) that are periodically **compacted**. Reads may check several
  SSTables (mitigated by **Bloom filters**). Excellent write throughput; read/space
  amplification from compaction.

```text
B-tree:   read O(log n), in-place writes      ── choose for read-heavy, range scans
LSM-tree: sequential writes, compaction       ── choose for write-heavy, high ingest
```

> The rule: **write-heavy ingest (time series, event logs, metrics) → LSM**;
> **read-heavy with range queries and updates (most OLTP) → B-tree.** Know which
> your store uses — it explains its latency profile under load.

---

## 34.2 Indexes: The Most Important Performance Lever

An index turns an `O(n)` table scan into an `O(log n)` lookup — at the cost of
slower writes and storage. Indexing is where most "the database is slow" problems
are won or lost.

```sql
-- Without an index this scans every row (a "seq scan"):
SELECT * FROM orders WHERE customer_id = 42;
CREATE INDEX idx_orders_customer ON orders(customer_id);   -- now an index lookup
```

Concepts a staff engineer must hold:

- **Composite indexes are order-sensitive.** `INDEX(a, b)` serves `WHERE a=?` and
  `WHERE a=? AND b=?`, but **not** `WHERE b=?` alone (the "leftmost prefix" rule).
- **Covering index:** if the index contains *all* columns a query needs, the engine
  answers from the index without touching the table ("index-only scan").
- **Selectivity:** an index on a low-cardinality column (e.g. `status` with 3
  values) may be *ignored* — scanning is cheaper.
- **Write cost:** each index slows inserts/updates and consumes space. Unused
  indexes are pure overhead — audit and drop them.

```sql
-- READ THE PLAN. The single most useful database skill.
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 42 AND status = 'paid';
-- Look for: Seq Scan (bad on big tables), Index Scan (good), estimated vs actual
-- rows (bad stats -> bad plans), nested-loop joins over large sets.
```

> In JPA-land (Chapter 28), the ORM hides the SQL — but the index rules still
> govern performance. Log generated SQL in dev and run `EXPLAIN ANALYZE` on the
> queries your repositories actually emit.

---

## 34.3 Transactions and Isolation

A **transaction** groups operations so they are **ACID**: **A**tomic (all or
nothing), **C**onsistent (invariants preserved), **I**solated, **D**urable. The
subtle one is **isolation**, governed by levels that trade correctness for
concurrency:

| Level | Prevents | Still allows |
|---|---|---|
| **Read Uncommitted** | — | dirty reads |
| **Read Committed** (PG default) | dirty reads | non-repeatable reads, phantoms |
| **Repeatable Read** (MySQL default) | non-repeatable reads | phantoms (mostly) |
| **Serializable** | everything | nothing — as if txns ran one at a time |

The anomalies: **dirty read** (see uncommitted data), **non-repeatable read**
(re-read gives a different value), **phantom** (re-run range query returns new
rows), **lost update / write skew** (two txns read-modify-write; one clobbers the
other or both pass a check that should be exclusive).

Most databases use **MVCC (multi-version concurrency control)**: readers see a
consistent snapshot without blocking writers, and writers create new row versions.
This is why "readers don't block writers" — and why long transactions bloat the DB
with old versions.

```java
// Lost update — the bug isolation levels exist to prevent.
// txn A: balance = read(); write(balance - 100)
// txn B: balance = read(); write(balance - 50)   // both read 1000 -> one update lost
// Fixes: SELECT ... FOR UPDATE (pessimistic), an atomic
// UPDATE acct SET balance = balance - 100 WHERE id=? (let the DB serialize it),
// or optimistic concurrency with a version column (34.4).
```

In Spring, `@Transactional` controls the boundary; set `isolation =` deliberately
when the default Read Committed isn't enough — and keep transactions **short**.

---

## 34.4 Optimistic vs Pessimistic Concurrency

- **Pessimistic locking:** lock before reading data you intend to write
  (`SELECT ... FOR UPDATE`, JPA `LockModeType.PESSIMISTIC_WRITE`). Correct and
  simple; serializes contended rows and risks deadlocks. Good when conflicts are
  frequent.
- **Optimistic concurrency (OCC):** don't lock; detect conflicts at write time via a
  **version** column, and retry if someone changed the row first. Better under low
  contention and across stateless services — and JPA has it built in.

```java
@Entity
class Account {
    @Id long id;
    long balance;
    @Version long version;        // JPA bumps + checks this on every update
}
// On commit, JPA issues UPDATE ... WHERE id=? AND version=?; a mismatch throws
// OptimisticLockException -> the caller retries the whole transaction (bounded).
```

This is the database analogue of the compare-and-swap from Chapter 18 and the
fencing/idempotency ideas from Chapter 33.

---

## 34.5 Normalization vs Denormalization

- **Normalization** (3NF) eliminates redundancy: each fact lives in exactly one
  place, so updates are cheap and anomalies impossible. The cost is **joins** —
  fine at moderate scale, expensive across shards.
- **Denormalization** duplicates data to avoid joins: store data the way it's
  *read*. Reads get fast and shard-friendly; writes must update every copy and risk
  inconsistency.

The staff trade-off: **normalize until joins hurt, then denormalize deliberately**
for specific read paths — and own the consistency burden (often via async updates,
Chapter 33). NoSQL stores (34.6) push you toward modeling per-query from the start.

---

## 34.6 SQL vs NoSQL — Choosing a Data Model

"NoSQL" is four different things; choose by access pattern, not hype:

| Family | Examples | Model | Sweet spot |
|---|---|---|---|
| **Relational** | PostgreSQL, MySQL | tables + joins, ACID | the default; complex queries, strong consistency |
| **Key-value** | Redis, DynamoDB | `key -> blob` | caches, sessions, simple lookups at scale |
| **Document** | MongoDB | JSON docs | flexible schema, data read as a unit |
| **Wide-column** | Cassandra, Bigtable | partitioned rows, tunable | massive write throughput, time series |
| **Graph** | Neo4j | nodes + edges | relationship-heavy traversals (social, fraud) |

Heuristics:

- **Start relational.** Postgres with JSONB covers an enormous range and keeps
  transactions and ad-hoc queries. Migrate *off* it only when a measured need
  forces it.
- NoSQL trades query flexibility for scale/shape: you model for the queries you
  know; ad-hoc queries become hard.
- "Polyglot persistence" is normal: relational for core entities, Redis for cache,
  a search engine for full-text, an OLAP store for analytics.

---

## 34.7 OLTP vs OLAP — Two Different Worlds

- **OLTP (transactional):** many small, low-latency reads/writes of current state
  (place an order, fetch a profile). Row-oriented, heavily indexed, normalized —
  your application database.
- **OLAP (analytical):** few huge queries scanning billions of rows to aggregate
  (revenue by region by month). **Column-oriented** storage, compression,
  denormalized star schemas.

You do **not** run analytics on your OLTP database — a full-table aggregation locks
up the store your users depend on. Move data via **ETL/ELT** into a **warehouse**
(Snowflake, BigQuery, Redshift) or **lakehouse** (data lake + Iceberg/Delta).

```text
OLTP (Postgres) ──CDC / batch ETL──▶ Warehouse (columnar) ──▶ dashboards / ML
   serves users, row-store               serves analysts, column-store
```

> **Change Data Capture (CDC)** — streaming the database's replication log (e.g. via
> **Debezium**) into the analytics/eventing world — keeps the warehouse and
> downstream consumers fresh without dual-writes. The **transactional-outbox**
> pattern (write the event to an outbox table in the same DB transaction, then ship
> it) avoids the dual-write race entirely.

---

## 34.8 Messaging and Streaming

Asynchronous data movement (Chapter 33.9) splits into two models:

- **Message queues (RabbitMQ, SQS):** a message goes to *a* consumer and is removed
  after ack. Work distribution; the queue drains as work completes.
- **Event logs (Kafka, Kinesis, Redpanda):** an append-only, partitioned, *retained*
  log. Consumers track their own **offset** and can replay history; many independent
  consumer groups read the same stream. The log *is* the source of truth.

```text
Queue:  producer ─▶ [ msg msg msg ] ─▶ one consumer per msg, then gone
Log:    producer ─▶ [ 0 1 2 3 4 5 ... ] ─▶ group A at offset 5, group B at offset 2
                     (retained; replayable; ordered within a partition)
```

Properties to reason about:

- **Ordering** is guaranteed only *within a partition*. Pick a partition key (e.g.
  `userId`) so related events stay ordered; cross-partition order is undefined.
- **Delivery** is **at-least-once** by default — consumers must be **idempotent**
  (Chapter 33.8). "Exactly-once" needs transactional producers + idempotent
  consumers, within the broker's boundary.
- **Consumer lag** (how far behind the latest offset a group is) is the key health
  metric — rising lag means consumers can't keep up; alert on it (Chapter 30).
- **Poison messages:** route repeatedly-failing messages to a **dead-letter queue**
  rather than blocking the partition forever.

```java
// Idempotent consumer over an at-least-once stream (conceptual Kafka loop).
for (var rec : consumer.poll(timeout)) {          // may redeliver after a crash/rebalance
    if (seen.contains(rec.key(), rec.offset())) continue;   // dedup -> effectively-once
    process(rec.value());
    seen.add(rec.key(), rec.offset());
    consumer.commitSync();                         // commit AFTER processing (at-least-once)
}
// Commit-after-process = at-least-once (safe + idempotent).
// Commit-before-process = at-most-once (lossy).
```

---

## 34.9 Data Modeling for Access Patterns

The relational instinct ("model the entities, query later") breaks at scale and in
NoSQL. The staff approach inverts it: **start from the queries.**

1. List the read/write patterns and their frequencies/latency targets.
2. Choose partition keys so each common query hits **one** partition (Chapter 33.5).
3. Denormalize/duplicate so hot reads need no joins or scatter-gather.
4. Accept and manage the resulting write fan-out and eventual consistency.

```text
Bad (relational habit in a KV store): "users" and "orders" tables, then a query
   that scatters across every partition to find a user's orders.
Good (access-pattern first): partition by userId; store a user's orders under
   their partition so "get my orders" is a single-partition read.
```

This is why **single-table design** is idiomatic in DynamoDB and why time-series
stores partition by `(seriesId, time-bucket)` — the model is shaped by the read
path, not by entity purity.

---

## 34.10 Connection Pooling and the Operational Edges

Even a perfect schema fails on operational basics:

- **Connection pools** (HikariCP, Chapters 28/30): databases handle a *bounded*
  number of connections; each is expensive (a backend process in Postgres). A fleet
  of stateless instances × a big per-instance pool can exhaust the DB. Size pools
  deliberately and front Postgres with **PgBouncer** when instance count is high.
- **The N+1 query problem:** JPA lazily loads a related row *per* parent row — 1
  query becomes 1+N. Eager-load with `JOIN FETCH` / `@EntityGraph` (Chapter 28).
- **Hot partitions / hot rows:** a celebrity key or a monotonic write key
  (timestamp, autoincrement) concentrates load on one shard — design keys to spread
  (Chapter 33.5).
- **Migrations at scale** (Chapter 36): adding an index or `NOT NULL` column can lock
  a huge table; use online/concurrent migrations and the expand/contract pattern so
  a schema change doesn't take an outage. (Flyway/Liquibase manage versioned
  migrations.)

---

## Summary

- Know your **storage engine** (B-tree vs LSM) — it predicts your read/write latency
  profile and the right workload fit.
- **Indexes** are the top performance lever: composite leftmost-prefix, covering,
  selectivity, write cost — and always **read the query plan** (`EXPLAIN ANALYZE`).
- **Transactions** trade isolation for concurrency; understand the anomalies and
  **MVCC**, and resolve contention with pessimistic locks or JPA **`@Version`**
  optimistic checks.
- **Normalize until joins hurt, then denormalize deliberately**; pick SQL vs NoSQL
  by **access pattern**, starting relational unless a measured need says otherwise.
- Separate **OLTP from OLAP** (never analyze on the transactional store); move data
  with **ETL/CDC** (Debezium, outbox) into a columnar warehouse.
- Use **queues** for work distribution and **event logs** (Kafka) for replayable,
  ordered streams; assume **at-least-once** and make consumers **idempotent**,
  watching **consumer lag**.
- Model **from the queries**, and respect the operational edges: connection pools,
  N+1, hot partitions, and online migrations.

## Next Steps

- Turn on SQL logging for a JPA repository and run `EXPLAIN ANALYZE` on its query;
  add a composite index and compare.
- Add `@Version` to an entity and force an `OptimisticLockException` with two
  concurrent updates.
- Sketch a Kafka topic's partition key and consumer-group layout for a feature you
  know, and identify where idempotency is required.
- Revisit **[Chapter 24: Database Access with JDBC](../24_database_jdbc/README.md)**
  and **[Chapter 28: Web Frameworks](../28_web_frameworks/README.md)** (JPA, pools).
- Continue to **[Chapter 35: API & Interface Design](../35_api_design/README.md)**.
