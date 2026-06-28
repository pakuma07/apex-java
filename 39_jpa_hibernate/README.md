# 39 — JPA & Hibernate in Depth

An ORM is the most productive abstraction in enterprise Java — and the most dangerous to use without understanding what it does underneath. The first time `repository.findAll()` returns a tidy `List<Order>`, the database feels like it disappeared. It did not. When that code ships and a page that "loaded one order" issues **5,000 SQL statements**, or throws `LazyInitializationException` only under load, or a `SELECT` drags back 40 columns to render two — you discover the ORM never removed the database; it only hid it. Chapter [24_database_jdbc](../24_database_jdbc/README.md) covered the raw `java.sql` layer every ORM ultimately calls. This is the layer on top: **JPA** (the *Jakarta Persistence API* — the spec: `EntityManager`, `@Entity`, `@Id`) and **Hibernate** (the dominant *implementation*). Spring Data JPA (Chapter [28_web_frameworks](../28_web_frameworks/README.md)) is repositories layered over this.

This is the authoritative ORM chapter for the engineer who must *debug* an ORM on large datasets: the impedance mismatch, the persistence context and entity lifecycle, dirty checking and flush timing, N+1 and its fixes, lazy vs eager loading, transactions and locking, the second-level cache, bulk operations, DTO projections, and when to drop the ORM entirely. The recurring discipline: **turn on SQL logging and count the statements** — an ORM bug is almost always visible in the SQL it generates.

> **The ORM premise:** map rows ↔ objects so business code manipulates a graph of Java objects, and the framework translates graph mutations into `INSERT`/`UPDATE`/`DELETE`. It excels at CRUD on an object graph and is poor at set-based queries — *"ORM for CRUD, SQL for queries."*

---

## 39.1 The Impedance Mismatch — and JPA vs Hibernate

The **object-relational impedance mismatch** is the structural gap between how objects model data and how relational databases do. Objects use *identity* (reference equality), *inheritance*, *navigable associations*, and *encapsulation*; tables use *primary keys*, *flat rows*, *foreign keys*, and *joins*. Mapping is never lossless:

- **Identity**: a row is identified by its primary key, an object by its JVM reference — two objects from the same row must be reconciled.
- **Inheritance**: SQL has no `extends`; the ORM fakes it (single-table, joined, table-per-class), each a tradeoff.
- **Associations**: an object reference is navigable in code; a foreign key is a separate value. A bidirectional `@OneToMany`/`@ManyToOne` must keep both sides consistent and know which side owns the FK.
- **Graph granularity**: one logical save can touch many tables; one navigation (`order.getCustomer().getAddress()`) can trigger several queries.

**JPA is the specification, Hibernate is the implementation.** You program to JPA interfaces (`EntityManager`, `@Entity`, JPQL) so code is portable; Hibernate (or EclipseLink) supplies the engine. In practice you still reach for Hibernate-specific features (`@BatchSize`, `@NaturalId`, statistics), so portability is real but partial.

| ORM **helps** when | ORM **hurts** when |
|--------------------|--------------------|
| CRUD on a rich object graph | Complex reporting / analytics (joins, windows, aggregates) |
| Domain logic on managed entities | Bulk ETL (millions of rows) |
| Caching & dirty tracking add value | Fine-tuned, hand-optimized queries |
| Many small, similar transactions | Set-based updates over large tables |

---

## 39.2 Entities & the Persistence Context (the First-Level Cache)

A class becomes an **entity** with `@Entity` and an `@Id`. The central runtime object is the **`EntityManager`** (`Session` in native Hibernate), which owns a **persistence context**: a *unit of work* and a **first-level cache** holding every managed entity, keyed by type + primary key. Within one context, **a given row maps to exactly one object instance** — load the same id twice and you get `==`-equal references.

```java
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)   // *-to-one defaults to EAGER; override it
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<LineItem> items = new ArrayList<>();   // LAZY by default
}
```

```java
Order a = em.find(Order.class, 1L);   // SELECT ... FROM orders WHERE id=1
Order b = em.find(Order.class, 1L);   // NO SQL — served from the 1st-level cache
assert a == b;                         // same managed instance: guaranteed identity
```

**`equals`/`hashCode` pitfall.** Default `Object` identity works *within* one context but breaks across contexts and in `Set`s when an entity goes from transient (no id) to managed (id assigned). Two rules: **never** base `equals`/`hashCode` on a DB-generated `@Id` (it is `null` before persist, so the hash changes after insert and the entity is lost in a `HashSet`); prefer a **business/natural key** (`@NaturalId` email, order number) or a constructor-assigned UUID.

> **Symptom:** an entity added to a `HashSet` before save "vanishes" after save. **Cause:** `hashCode` uses the auto-generated id, which changed from `null` to a value. **Fix:** base equality on an immutable natural key, never the generated id.

---

## 39.3 The Entity Lifecycle

Every entity is in one of four states relative to a persistence context. Knowing transitions is the key to predicting what SQL fires and when.

| State | Tracked? | In DB? | How you get there |
|-------|----------|--------|-------------------|
| **Transient** (new) | No | No | `new Order()` |
| **Managed** | Yes — dirty-checked | Yes (at flush) | `em.persist(o)`, `em.find(...)`, JPQL result |
| **Detached** | No | Yes | context closed, `em.detach(o)`, `em.clear()` |
| **Removed** | Yes (scheduled) | Until flush/commit | `em.remove(o)` |

```java
Order o = new Order();          // TRANSIENT — not tracked, no SQL
em.persist(o);                  // MANAGED — INSERT queued (not necessarily sent yet)
o.setStatus("PAID");            // dirty-checked: UPDATE generated at flush
em.detach(o);                   // DETACHED — further changes NOT tracked
o.setStatus("SHIPPED");         // ignored by the ORM (no UPDATE)
Order m = em.merge(o);          // copies detached state into a MANAGED instance
em.remove(m);                   // REMOVED — DELETE queued
```

**`merge` returns a *new* managed instance** — it does not make the argument managed. Keep using the return value, not the original detached object (a classic bug: editing `o` after `merge(o)` and seeing nothing persist).

---

## 39.4 Dirty Checking & Flushing

You rarely call an explicit "update". Hibernate **dirty-checks**: at flush it compares each managed entity's current values against a snapshot taken at load and emits an `UPDATE` for every changed entity. Hence the classic surprise:

```java
@Transactional
public void markPaid(Long id) {
    Order o = em.find(Order.class, id);  // managed
    o.setStatus("PAID");
    // No save()/update()/persist() — yet an UPDATE fires at commit:
    // UPDATE orders SET status='PAID', ... WHERE id=?
}
```

> **Symptom:** "I never called `save()` but the row changed anyway." **Cause:** the entity is **managed**, so dirty checking persists any mutation at flush. **Fix:** this is correct — if you do *not* want it persisted, work with a detached entity or a read-only DTO (§39.10).

**Flush timing** — when queued SQL actually hits the DB — is controlled by `FlushMode`:

| `FlushMode` | Flush happens... |
|-------------|------------------|
| `AUTO` (default) | before a query that could be affected by pending changes, and at commit |
| `COMMIT` | only at transaction commit |
| `ALWAYS` | before every query |
| `MANUAL` | only when you call `em.flush()` |

The default `AUTO` triggers a **flush-before-query**: mutate an entity then run an overlapping JPQL query, and Hibernate flushes first so the query sees your changes. Flushing is *not* committing — flushed SQL can still roll back. Manual `em.flush()` surfaces constraint violations early but does not commit.

---

## 39.5 The N+1 SELECT Problem — the #1 ORM Performance Bug

The single most important ORM pathology. You load **N** parent rows in one query, iterate them touching a lazy association, and each touch fires **one more** query: N parents → **1 + N** queries. At N = 5,000 that is 5,001 round trips for what should be one join.

```java
// ❌ WRONG — N+1. One query for orders, then one per order for its customer.
List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class)
                       .getResultList();                  // 1 SELECT
for (Order o : orders) System.out.println(o.getCustomer().getName());  // 1 SELECT each
```

```sql
SELECT * FROM orders;                  -- 1
SELECT * FROM customers WHERE id = ?;  -- N times (once per order)  => 1 + N
```

**Detect it by counting statements** — never trust "feels slow":

```properties
# DEV ONLY — noisy + slow
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
spring.jpa.properties.hibernate.generate_statistics=true  # query/entity-load counts
```

For production-grade counting use **datasource-proxy** or **p6spy**, or assert `Statistics.getQueryExecutionCount()` in a test. A test that fails when the statement count exceeds a threshold is the only durable guard against N+1 regressions.

**The fixes** — pick by use case:

```java
// ✅ FIX 1 — JOIN FETCH: parent + association in ONE query.
em.createQuery("SELECT o FROM Order o JOIN FETCH o.customer", Order.class).getResultList();
// SQL: SELECT o.*, c.* FROM orders o JOIN customers c ON o.customer_id = c.id;  -- 1 query

// ✅ FIX 2 — @EntityGraph (declarative, reusable; great with Spring Data).
@EntityGraph(attributePaths = {"customer", "items"})
List<Order> findByStatus(String status);

// ✅ FIX 3 — batch fetching: N selects become N/size IN-queries.
@BatchSize(size = 50) @OneToMany(mappedBy = "order") List<LineItem> items;
// or globally: hibernate.default_batch_fetch_size = 50
// SQL: SELECT * FROM customers WHERE id IN (?,?,...,?)  -- batched

// ✅ FIX 4 — DTO projection: don't load entities at all for read views (§39.10).
em.createQuery("SELECT new com.app.OrderView(o.id, c.name) FROM Order o JOIN o.customer c",
    OrderView.class).getResultList();    // 1 query, only the 2 columns you need
```

> **Caution:** a single `JOIN FETCH` on a **collection** multiplies rows and breaks pagination — Hibernate warns "applying in memory" and paginates the whole result in the JVM. For one-to-many use `@EntityGraph` + batch fetch, or fetch the collection in a second query. Never `JOIN FETCH` two collections at once.

---

## 39.6 Lazy vs Eager, Proxies & `LazyInitializationException`

JPA defaults: `@OneToMany`/`@ManyToMany` are **LAZY**, `@ManyToOne`/`@OneToOne` are **EAGER**. Eager `*-to-one` is a frequent silent N+1 source, so most teams override every association to LAZY and fetch explicitly.

| Strategy | Default for | Loads | Risk |
|----------|-------------|-------|------|
| `FetchType.LAZY` | collections | a **proxy**; real query on first access | `LazyInitializationException` outside the session |
| `FetchType.EAGER` | `*-to-one` | immediately, always | over-fetching; hidden N+1 |

A lazy association is a **proxy** — a Hibernate-generated subclass standing in for the real object. Accessing it triggers the query *if the context is still open*. Once the transaction/session closed:

```java
// ❌ WRONG — service returns the entity; session closes; the view touches a lazy field.
@Transactional public Order getOrder(Long id) { return em.find(Order.class, id); } // ends here
// later, in the web layer (no session):
order.getItems().size();   // -> org.hibernate.LazyInitializationException
```

> **Symptom:** `LazyInitializationException: could not initialize proxy — no Session`, often only on certain navigation paths. **Cause:** a lazy association touched after the context closed (entity now **detached**). **Fix (proper):** fetch what the caller needs *inside* the transaction — `JOIN FETCH`, `@EntityGraph`, or map to a DTO before returning. The transaction boundary owns all loading.

**The open-session-in-view (OSIV) anti-pattern.** Spring's default keeps the session open for the whole HTTP request so the view can lazily load anything. It *suppresses* the exception but holds DB connections across rendering, hides N+1 in templates, and leaks persistence into the view. Set `spring.jpa.open-in-view=false` and fetch explicitly — the exception then becomes a *useful* dev-time signal that you under-fetched, not a production mask.

---

## 39.7 Transactions, Isolation & Locking

In JPA the **transaction is the persistence-context boundary**: it opens, you load and mutate managed entities, dirty checking flushes at commit, the context closes. With Spring, `@Transactional` declares the boundary; **propagation** controls nesting:

```java
@Transactional                                          // REQUIRED (default): join or start one
public void placeOrder(Order o) { ... }

@Transactional(propagation = Propagation.REQUIRES_NEW)  // suspend caller, own tx (e.g. audit)
public void audit(String msg) { ... }
```

`REQUIRED` reuses an existing transaction (the common case); `REQUIRES_NEW` always starts a fresh one. Isolation levels (`READ_COMMITTED`, `REPEATABLE_READ`, ...) are set on the transaction and behave exactly as in [24_database_jdbc](../24_database_jdbc/README.md) §24.5 and [34_data_intensive_systems](../34_data_intensive_systems/README.md).

**Optimistic locking** (`@Version`) is the default high-concurrency strategy: add a version column; Hibernate appends `WHERE id=? AND version=?` and bumps the version. A lost-update conflict matches no rows and throws `OptimisticLockException`, so you retry rather than block.

```java
@Entity class Account {
    @Id Long id;
    @Version int version;       // UPDATE ... WHERE id=? AND version=?
    BigDecimal balance;
}
```

**Pessimistic locking** takes a DB lock up front (`SELECT ... FOR UPDATE`) when conflicts are frequent and retries costly:

```java
Account a = em.find(Account.class, id, LockModeType.PESSIMISTIC_WRITE);  // FOR UPDATE
```

Prefer optimistic (no held locks, scales better) unless contention is high. See [34_data_intensive_systems](../34_data_intensive_systems/README.md) for the MVCC/isolation theory.

---

## 39.8 The Second-Level Cache & Query Cache

The first-level cache (§39.2) lives for one context. The **second-level cache (L2)** is *optional and shared across sessions* — a process- or cluster-wide cache (Ehcache, Caffeine, Infinispan) keyed by entity id. It helps for **read-mostly, frequently loaded reference data** (countries, catalog) and hurts for write-heavy data, where invalidation costs more than it saves.

```java
@Entity @Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Country { @Id String code; String name; }
```

The **query cache** is separate: it caches *query result id lists*, not entities. Its caveat — any write to a referenced table evicts it, and at low hit rates it *adds* queries. Enable it only for genuinely repeated, parameter-stable queries, and measure.

> **Symptom:** updates to a table don't appear, or appear intermittently across nodes. **Cause:** L2 / query cache serving stale entries (often after a bulk update bypassed it). **Fix:** scope caching to immutable reference data, pick the right `CacheConcurrencyStrategy`, and evict explicitly after bulk DML.

---

## 39.9 Bulk Operations — Where the ORM Is Weakest

The persistence context is built for *small* units of work. Loading a million entities to update a field means a million snapshots in the first-level cache → `OutOfMemoryError` and a flood of individual `UPDATE`s.

```java
// ❌ WRONG — loads everything into the context, dirty-checks each, OOMs at scale.
em.createQuery("SELECT o FROM Order o WHERE o.status='NEW'", Order.class)
  .getResultList().forEach(o -> o.setStatus("ARCHIVED"));

// ✅ RIGHT — a single set-based bulk UPDATE via JPQL (or native SQL).
int n = em.createQuery("UPDATE Order o SET o.status='ARCHIVED' WHERE o.status='NEW'")
          .executeUpdate();   // UPDATE orders SET status='ARCHIVED' WHERE status='NEW';
```

**The critical caveat:** bulk `UPDATE`/`DELETE` execute **directly in the database**, bypassing the persistence context entirely. They **do not** run lifecycle callbacks (`@PreUpdate`, `@PreRemove`) or cascade, **do not** update entities already in the first-level cache (now stale), and **do not** invalidate the L2 cache.

> **Symptom:** a bulk delete "worked" but soft-delete callbacks/audit never fired, and previously loaded entities still show old values. **Cause:** bulk JPQL/native DML bypasses the context, callbacks, and cache. **Fix:** run bulk DML on a fresh context, then `em.clear()` (and evict the L2 region) so later reads reload from the DB; if you *need* callbacks, process per-entity in paginated batches. For very large jobs, drop to plain JDBC batches ([24_database_jdbc](../24_database_jdbc/README.md) §24.6).

For mass inserts, set `hibernate.jdbc.batch_size`, then flush and `clear()` every N entities — or bypass the ORM with JDBC batch inserts.

---

## 39.10 Projections & DTOs — Don't Load Entities for Reads

A managed entity is expensive: every column, a dirty-check snapshot, lazy proxies, cache entries. For a **read-only view** needing two columns, loading the full entity is pure waste.

```java
// ❌ WRONG — loads 40 columns + builds managed entities to render id + customer name.
List<Order> orders = repo.findAll();
orders.forEach(o -> render(o.getId(), o.getCustomer().getName()));   // + N+1!
```

> **Symptom:** "a `SELECT` pulls 40 columns I don't need" (and a heap full of managed entities for a read-only screen). **Cause:** loading entities for a view-only path. **Fix:** project exactly the columns into a DTO — no managed state, no dirty checking, no lazy proxies, often one query.

```java
// ✅ RIGHT — JPQL constructor expression into an immutable record DTO.
public record OrderView(Long id, String customerName) {}

List<OrderView> views = em.createQuery(
    "SELECT new com.app.OrderView(o.id, o.customer.name) FROM Order o", OrderView.class)
    .getResultList();
// SQL: SELECT o.id, c.name FROM orders o JOIN customers c ...   -- only 2 columns
```

Spring Data supports the same via **interface/record projections**. DTO projections are read-only by definition — they sidestep flush, the cache, and `LazyInitializationException` in one move. **Read paths should rarely return entities.**

---

## 39.11 Reading the Generated SQL

You cannot debug an ORM you cannot see. Make the SQL visible and count it:

```properties
spring.jpa.show-sql=true                                  # echo SQL to console
spring.jpa.properties.hibernate.format_sql=true           # pretty-print
logging.level.org.hibernate.SQL=DEBUG                     # logger-routed SQL
logging.level.org.hibernate.orm.jdbc.bind=TRACE           # bind parameter values
spring.jpa.properties.hibernate.generate_statistics=true  # per-session query counts
```

- `show_sql` / the `org.hibernate.SQL` logger: *what* runs.
- `generate_statistics` / `SessionFactory.getStatistics()`: *how many* queries, cache hits/misses, entity loads — the fastest way to spot N+1 in a test.
- **datasource-proxy** / **p6spy**: production-grade interception with real bind values, timing, and **statement-count assertions** in tests.

The workflow mirrors [29_performance_engineering](../29_performance_engineering/README.md): measure first. Reproduce the slow path, count statements, read the plan ([34_data_intensive_systems](../34_data_intensive_systems/README.md)), then apply a fetch fix — never guess.

---

## 39.12 When *Not* to Use an ORM

The ORM earns its keep on transactional CRUD over an object graph; it fights you elsewhere.

| Use the **ORM** for | Drop to **SQL / alternatives** for |
|---------------------|-----------------------------------|
| CRUD on entities, domain logic | Reporting, analytics (joins, windows, `GROUP BY`) |
| Small, frequent transactions | Bulk ETL / millions of rows |
| Caching & dirty tracking help | Hand-tuned, plan-sensitive queries |
| Object-graph navigation | Set-based mass updates |

Alternatives, lighter to heavier:

- **plain JDBC** ([24_database_jdbc](../24_database_jdbc/README.md)) — full control, for batch/ETL and the hottest paths.
- **jOOQ** — type-safe SQL DSL; the schema generates Java so queries are compile-checked. Ideal for complex reads.
- **Spring Data JDBC** — a simpler model (no lazy loading, no dirty-checking proxies; aggregates persisted explicitly) when JPA's machinery is overkill.
- **MyBatis** — SQL mapper: you write the SQL, it maps results.

> **The rule:** *ORM for CRUD, SQL for queries.* A healthy system commonly runs **both** — JPA for the write/domain model, jOOQ or native SQL for reporting — against one database. Mixing is not a failure; insisting the ORM do everything is.

---

## Summary

| Concept | What to remember |
|---------|------------------|
| **JPA vs Hibernate** | Spec (interfaces) vs implementation (engine) |
| **Persistence context** | 1st-level cache + unit of work; one row → one managed instance |
| **Lifecycle** | transient → managed → detached → removed; `merge` returns a *new* managed object |
| **Dirty checking** | mutating a managed entity = `UPDATE` at flush, no `save()` needed |
| **Flush** | `AUTO` flushes before queries & at commit; flush ≠ commit |
| **N+1** | 1 parent query + N association queries; fix with `JOIN FETCH` / `@EntityGraph` / `@BatchSize` / DTOs |
| **Lazy/eager** | collections LAZY, `*-to-one` EAGER; `LazyInitializationException` = touched a proxy after the session closed |
| **OSIV** | anti-pattern; set `open-in-view=false`, fetch explicitly |
| **Locking** | optimistic `@Version` by default; pessimistic `FOR UPDATE` under contention |
| **L2 cache** | shared, optional; read-mostly reference data only |
| **Bulk DML** | bypasses context/callbacks/cache — `clear()` and evict afterward |
| **Projections** | DTO/record for read views — no entities, no proxies, fewer columns |
| **Visibility** | `show_sql` + statistics + datasource-proxy; **count the statements** |
| **When not to** | reporting/ETL/tuned queries → jOOQ, Spring Data JDBC, plain SQL |

---

## Next Steps
- Turn on `show_sql` + `generate_statistics` and count the statements on your slowest endpoint
- Find one N+1 and fix it with `JOIN FETCH` or `@EntityGraph`; add a statement-count test
- Set `spring.jpa.open-in-view=false` and resolve the `LazyInitializationException`s it surfaces
- Convert one read-only screen to a DTO/record projection
- Rewrite one bulk update as JPQL `UPDATE` and handle the stale-cache fallout

> **Next:** [Chapter 40 — NeetCode Patterns](../neet_code/README.md)
> **Related:** [24_database_jdbc](../24_database_jdbc/README.md) · [28_web_frameworks](../28_web_frameworks/README.md) · [29_performance_engineering](../29_performance_engineering/README.md) · [34_data_intensive_systems](../34_data_intensive_systems/README.md)
