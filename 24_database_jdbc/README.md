# Chapter 24: Database Access with JDBC -- Java

Most non-trivial programs eventually need to persist structured data in a relational database. Java's standard answer is **JDBC** (Java Database Connectivity) — a vendor-neutral API in the **`java.sql`** package (since Java 1.1) that lets your code talk to *any* SQL database through a uniform set of interfaces. JDBC itself is just the API (interfaces like `Connection`, `Statement`, `ResultSet`); the concrete implementation is supplied by a **driver** — a JAR for your specific database (PostgreSQL, MySQL, Oracle, H2, SQLite, ...). You write to the interfaces, drop the right driver on the classpath, and the same code runs against different engines. C++ has no equivalent standard: you reach for a vendor library (libpq, MySQL Connector/C++), ODBC, or a third-party layer such as SOCI/ODB.

This chapter covers the full JDBC workflow: connection URLs and the modern auto-registering driver model, `DriverManager.getConnection`, `try`-with-resources for guaranteed cleanup, the critical `Statement` vs `PreparedStatement` distinction (and the SQL-injection bug that makes `PreparedStatement` non-negotiable), running queries and updates, walking a `ResultSet`, transactions and isolation levels, batch updates, generated keys, `RowSet`, connection pooling with a `DataSource`, the DAO pattern, and where ORMs (JPA/Hibernate) take over. The cardinal rule throughout: **close every resource** (use try-with-resources) and **never build SQL by string concatenation**.

> **C++ database access → Java/JDBC — at a glance**
> - ODBC `SQLConnect` / connection string → `DriverManager.getConnection(url, user, pass)`
> - libpq `PQexec` / MySQL `mysql_query` → `Statement.executeQuery` / `executeUpdate`
> - Prepared statements (`PQprepare`, `mysql_stmt_prepare`) → `PreparedStatement` with `?` placeholders
> - Result cursor (`PQgetvalue`, `mysql_fetch_row`) → `ResultSet.next()` + typed getters
> - Manual `BEGIN`/`COMMIT`/`ROLLBACK` → `setAutoCommit(false)` / `commit()` / `rollback()`
> - RAII (destructor closes the handle) → **try-with-resources** (`AutoCloseable`)
> - Custom connection pool / `ODB` / SOCI ORM → HikariCP `DataSource`, JPA/Hibernate

## 24.1 What JDBC Is — The API and Driver Model

JDBC separates the **API** (what you program against) from the **driver** (how it actually reaches the database). The API lives in `java.sql` (core) and `javax.sql` (pooling, `DataSource`, `RowSet`). The driver is a third-party JAR implementing those interfaces for one database engine. Since JDBC 4.0 (Java 6), drivers **auto-register** via the `ServiceLoader` mechanism: a driver JAR ships a `META-INF/services/java.sql.Driver` file, and `DriverManager` discovers it automatically when the JAR is on the classpath. The old incantation `Class.forName("...")` is therefore obsolete for modern drivers — you no longer load the driver by hand.

```java
// MODERN (JDBC 4.0+): driver auto-registers via ServiceLoader.
// Just have the driver JAR on the classpath and connect:
Connection conn = DriverManager.getConnection(url, user, password);

// LEGACY (pre-JDBC 4.0): explicit driver class load. NOT needed anymore.
// Class.forName("org.postgresql.Driver");   // historical only
```

A **JDBC URL** identifies the driver and the target database. It always begins `jdbc:`, followed by the *subprotocol* (the engine) and an engine-specific locator:

| Database | Example JDBC URL |
|----------|------------------|
| H2 (in-memory) | `jdbc:h2:mem:testdb` |
| H2 (file) | `jdbc:h2:./data/mydb` |
| SQLite | `jdbc:sqlite:app.db` |
| PostgreSQL | `jdbc:postgresql://localhost:5432/mydb` |
| MySQL | `jdbc:mysql://localhost:3306/mydb` |
| Oracle | `jdbc:oracle:thin:@host:1521:orcl` |

> **Contrast with C++:** There is no portable C++ standard like JDBC. The closest is ODBC (a C API with driver managers and DSNs), but it is lower-level and platform-flavored. Each database vendor also ships a native C/C++ client library with its own incompatible API. JDBC's value is exactly this uniformity: swap the URL and the driver JAR, keep the code.

---

## 24.2 Connecting: DriverManager, Connection, and try-with-resources

`DriverManager.getConnection(url, user, password)` returns a **`Connection`** — your session with the database, and the factory for statements. A `Connection` is a scarce, expensive OS-level resource (a socket plus server-side state), so it must always be closed. Because `Connection`, `Statement`, and `ResultSet` all implement `AutoCloseable`, the idiomatic pattern is **try-with-resources**, which closes them in reverse order even when an exception is thrown — Java's analogue of C++ RAII. Every JDBC call can throw the checked `SQLException`, so you must handle or declare it.

```java
import java.sql.*;

String url  = "jdbc:h2:mem:testdb";
String user = "sa";
String pass = "";

// Connection, Statement, and ResultSet are all AutoCloseable.
// try-with-resources closes them in reverse order, even on exception.
try (Connection conn = DriverManager.getConnection(url, user, pass);
     Statement stmt  = conn.createStatement();
     ResultSet rs    = stmt.executeQuery("SELECT 1 AS one")) {

    if (rs.next()) {
        System.out.println("Connected; got " + rs.getInt("one"));
    }
} catch (SQLException e) {
    // SQLException carries the SQL state and vendor error code
    System.err.println("DB error: " + e.getMessage()
                       + " [SQLState=" + e.getSQLState()
                       + ", code=" + e.getErrorCode() + "]");
}
```

> **Contrast with C++:** In C++ you would `PQfinish(conn)` / `mysql_close(conn)` manually, or wrap the handle in an RAII guard whose destructor closes it. JDBC's try-with-resources is that RAII guard, built into the language: declare the resource in the `try (...)` header and the JVM guarantees `close()` runs.

---

## 24.3 Statement vs PreparedStatement — and SQL Injection

JDBC offers two ways to send SQL. A plain **`Statement`** sends a literal SQL string — and if you build that string by concatenating user input, you have created a **SQL injection** vulnerability, one of the most damaging and common security bugs in software. A **`PreparedStatement`** instead sends a SQL *template* with `?` placeholders, then binds the values separately via `setString`, `setInt`, etc. The driver and database treat bound values strictly as *data*, never as SQL syntax, so injection is impossible. `PreparedStatement` is also faster on repeated execution (the database can cache the parsed plan) and handles type conversion and escaping for you.

```java
// ❌ VULNERABLE — string concatenation with a plain Statement.
// If name is:  '; DROP TABLE users; --
// the executed SQL becomes a second, malicious statement.
String name = userSuppliedInput;
String sql  = "SELECT * FROM users WHERE name = '" + name + "'";   // DANGER
try (Statement stmt = conn.createStatement();
     ResultSet rs   = stmt.executeQuery(sql)) {
    // ...
}

// ✅ SAFE — PreparedStatement with a bound parameter.
// The value is sent separately and can never be interpreted as SQL.
String safe = "SELECT * FROM users WHERE name = ?";
try (PreparedStatement ps = conn.prepareStatement(safe)) {
    ps.setString(1, name);              // 1-based parameter index
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ": " + rs.getString("name"));
        }
    }
}
```

> **The rule:** Use `PreparedStatement` with `?` parameters for **every** query that includes a value, especially anything derived from user input. Reserve plain `Statement` for fixed, parameter-free DDL/DML you wrote yourself (e.g. `CREATE TABLE`). Parameter indices are **1-based**, a frequent off-by-one trap for C/C++ programmers used to 0-based arrays.

> **Contrast with C++:** The injection risk is identical in C++ if you `sprintf` SQL together — libpq and Connector/C++ both provide parameterized queries (`PQexecParams`, `prepared statements`) for exactly this reason. JDBC's `PreparedStatement` is the same defense, surfaced as a first-class, type-safe object.

---

## 24.4 Executing Queries and Updates; Reading a ResultSet

JDBC splits execution by intent. **`executeQuery`** runs a `SELECT` and returns a **`ResultSet`** — a forward-only cursor over the rows. **`executeUpdate`** runs an `INSERT`/`UPDATE`/`DELETE` (or DDL) and returns the **row count** affected. (A generic `execute` returns a `boolean` and is used when you do not know which kind of statement you have.) You walk a `ResultSet` with `next()`, which advances the cursor and returns `false` past the last row, then read columns by **name** or **1-based index** with typed getters: `getInt`, `getLong`, `getString`, `getDouble`, `getBoolean`, `getBigDecimal`, `getTimestamp`, `getObject`, and so on.

```java
// --- DDL + INSERT (executeUpdate returns affected row count) ---
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("""
        CREATE TABLE person (
            id   INT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            age  INT
        )""");
}

try (PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO person (id, name, age) VALUES (?, ?, ?)")) {
    ps.setInt(1, 1);
    ps.setString(2, "Alice");
    ps.setInt(3, 30);
    int rows = ps.executeUpdate();          // 1
    System.out.println(rows + " row inserted");
}

// --- SELECT + walk the ResultSet ---
try (PreparedStatement ps = conn.prepareStatement(
        "SELECT id, name, age FROM person WHERE age >= ?")) {
    ps.setInt(1, 18);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {                 // false past the last row
            int    id   = rs.getInt("id");          // by column name
            String name = rs.getString(2);          // or by 1-based index
            int    age  = rs.getInt("age");
            // SQL NULL: a numeric getter returns 0/false — check wasNull() if it matters
            if (rs.wasNull()) age = -1;
            System.out.printf("%d: %s (%d)%n", id, name, age);
        }
    }
}
```

> **Note on NULL:** a primitive getter like `getInt` returns `0` for a SQL `NULL`. Call `rs.wasNull()` immediately afterward to distinguish a real `0` from a `NULL`, or use `getObject(col, Integer.class)` which returns `null`.

---

## 24.5 Transactions: commit, rollback, isolation levels

A **transaction** groups several statements so they succeed or fail as a unit — the database's all-or-nothing guarantee (the *A* in ACID). By default a JDBC `Connection` is in **auto-commit** mode: every statement commits immediately. To group statements, call `setAutoCommit(false)`, run them, then `commit()` on success or `rollback()` on failure. This is the classic transfer-money example: debit and credit must both happen or neither.

```java
try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    conn.setAutoCommit(false);              // begin a manual transaction
    try (PreparedStatement debit  = conn.prepareStatement(
             "UPDATE account SET balance = balance - ? WHERE id = ?");
         PreparedStatement credit = conn.prepareStatement(
             "UPDATE account SET balance = balance + ? WHERE id = ?")) {

        debit.setBigDecimal(1, new java.math.BigDecimal("100.00"));
        debit.setInt(2, 1);
        debit.executeUpdate();

        credit.setBigDecimal(1, new java.math.BigDecimal("100.00"));
        credit.setInt(2, 2);
        credit.executeUpdate();

        conn.commit();                      // both succeeded — make it permanent
    } catch (SQLException e) {
        conn.rollback();                    // any failure — undo everything
        throw e;
    } finally {
        conn.setAutoCommit(true);           // restore default if reusing the connection
    }
}
```

**Isolation levels** control how concurrent transactions see each other's uncommitted/committed work, trading consistency against concurrency. Set them with `conn.setTransactionIsolation(...)`:

| Constant | Prevents | Notes |
|----------|----------|-------|
| `TRANSACTION_READ_UNCOMMITTED` | (nothing) | Allows dirty reads; rarely used |
| `TRANSACTION_READ_COMMITTED` | dirty reads | Common default (PostgreSQL, Oracle) |
| `TRANSACTION_REPEATABLE_READ` | dirty + non-repeatable reads | MySQL/InnoDB default |
| `TRANSACTION_SERIALIZABLE` | dirty + non-repeatable + phantom reads | Strongest; most contention |

> **Contrast with C++:** With libpq/Connector you issue raw `BEGIN`, `COMMIT`, `ROLLBACK`, and `SET TRANSACTION ISOLATION LEVEL` SQL yourself. JDBC promotes these to method calls on `Connection`, so the transaction boundary is expressed in Java control flow (the `try/catch` above) rather than embedded SQL strings.

---

## 24.6 Batch Updates

Executing one statement per row is slow when inserting or updating thousands of rows, because each `executeUpdate` is a network round trip. **Batching** queues many parameter sets with `addBatch()` and ships them in one round trip with `executeBatch()`, which returns an `int[]` of per-statement row counts. Batch inside a manual transaction for both speed and atomicity.

```java
conn.setAutoCommit(false);
try (PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO person (id, name, age) VALUES (?, ?, ?)")) {
    for (int i = 1; i <= 1000; i++) {
        ps.setInt(1, i);
        ps.setString(2, "User" + i);
        ps.setInt(3, 20 + (i % 50));
        ps.addBatch();                      // queue this row
        if (i % 100 == 0) {
            ps.executeBatch();              // flush every 100 to bound memory
        }
    }
    ps.executeBatch();                      // flush the remainder
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
}
```

---

## 24.7 Generated Keys and RowSet

When a table has an auto-generated primary key (identity/serial column), you often need the key the database just assigned. Ask for it by passing **`Statement.RETURN_GENERATED_KEYS`** when preparing, then read it from `getGeneratedKeys()`.

```java
String sql = "INSERT INTO person (name, age) VALUES (?, ?)";   // id is auto-generated
try (PreparedStatement ps =
         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, "Bob");
    ps.setInt(2, 42);
    ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
            long newId = keys.getLong(1);   // the id the DB assigned
            System.out.println("Inserted with id " + newId);
        }
    }
}
```

A **`RowSet`** (`javax.sql.rowset`) is a JavaBean wrapper around result data. The most useful variant, **`CachedRowSet`**, fetches rows then *disconnects* from the database, so you can pass it around or iterate it without holding a `Connection` open — handy for detached, serializable result sets.

```java
import javax.sql.rowset.*;

try (CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet()) {
    crs.setUrl(url);
    crs.setUsername(user);
    crs.setPassword(pass);
    crs.setCommand("SELECT id, name FROM person");
    crs.execute();                          // fetches, then disconnects
    while (crs.next()) {
        System.out.println(crs.getInt("id") + ": " + crs.getString("name"));
    }
}   // no live Connection was held while iterating
```

---

## 24.8 Connection Pooling — DataSource, not per-request Connections

Opening a `Connection` is expensive: a TCP handshake, authentication, and server-side session setup, often tens of milliseconds. A server that opens and closes a fresh connection per request wastes most of its time on connection overhead and can exhaust the database under load. The fix is a **connection pool**: a fixed set of physical connections, created once and *reused*. You borrow one with `getConnection()` and "close" it — but `close()` on a pooled connection returns it to the pool rather than tearing it down. Pools are exposed through the **`javax.sql.DataSource`** interface; the de-facto standard implementation is **HikariCP** (the default pool in Spring Boot).

```java
import com.zaxxer.hikari.*;
import javax.sql.DataSource;

HikariConfig cfg = new HikariConfig();
cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
cfg.setUsername("app");
cfg.setPassword("secret");
cfg.setMaximumPoolSize(10);                 // cap concurrent physical connections

DataSource pool = new HikariDataSource(cfg);   // create ONCE at startup, share it

// Per request: borrow and return via try-with-resources.
try (Connection conn = pool.getConnection();        // borrows from the pool
     PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM person");
     ResultSet rs = ps.executeQuery()) {
    if (rs.next()) System.out.println("rows: " + rs.getInt(1));
}   // conn.close() RETURNS it to the pool (does not actually disconnect)
```

> **Rule of thumb:** use `DriverManager` only for quick scripts and examples. Any long-running application or server should obtain connections from a pooled `DataSource`, created once at startup and shared. The `try`-with-resources idiom is identical — only the *source* of the connection changes.

---

## 24.9 The DAO Pattern and ORMs

Scattering SQL throughout your business logic is hard to maintain and test. The **DAO** (Data Access Object) pattern confines all persistence for one entity behind an interface, so the rest of the program works with plain objects and methods (`findById`, `save`) rather than SQL. This isolates the database, makes the data layer mockable in tests (see Chapter 25), and lets you swap the implementation.

```java
// Domain object (a record is ideal for an immutable row).
record Person(long id, String name, int age) {}

// DAO interface — the contract the rest of the app depends on.
interface PersonDao {
    java.util.Optional<Person> findById(long id) throws SQLException;
    long save(Person p) throws SQLException;
}

// JDBC implementation — all SQL lives here, behind the interface.
class JdbcPersonDao implements PersonDao {
    private final DataSource ds;
    JdbcPersonDao(DataSource ds) { this.ds = ds; }

    public java.util.Optional<Person> findById(long id) throws SQLException {
        String sql = "SELECT id, name, age FROM person WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(
                        new Person(rs.getLong("id"), rs.getString("name"), rs.getInt("age")));
                }
                return java.util.Optional.empty();
            }
        }
    }

    public long save(Person p) throws SQLException {
        String sql = "INSERT INTO person (name, age) VALUES (?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.name());
            ps.setInt(2, p.age());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }
}
```

For larger applications, an **ORM** (Object-Relational Mapper) automates the object↔table mapping entirely. The Java standard is **JPA** (Jakarta Persistence API), most commonly implemented by **Hibernate**; **Spring Data JPA** layers repositories on top. You annotate a class with `@Entity`/`@Id` and the ORM generates the SQL, manages a persistence context, lazy-loads associations, and handles dirty-checking — at the cost of a leaky abstraction you must still understand (the N+1 query problem, fetch strategies, the first-level cache). Lighter alternatives such as **jOOQ** (type-safe SQL DSL) and **MyBatis** (SQL mapper) sit between raw JDBC and a full ORM. Under the hood, every one of these still uses JDBC — which is why understanding this chapter matters even if you ultimately use Hibernate.

---

## 24.10 A Complete Runnable Example (H2 in-memory)

JDBC needs a driver on the classpath to do anything, so unlike most chapters in this book there is **no separate `.java` file under `code_examples/`** — a bare `javac`/`java` with no driver could not connect to a database. Instead, here is a complete, self-contained program that uses the **H2** database in **in-memory** mode (`jdbc:h2:mem:...`): it creates a table, inserts rows, queries them, and runs a transaction, then the in-memory database vanishes when the JVM exits — no files, no server. To run it you only need the single H2 driver JAR on the classpath.

**Maven** (`pom.xml`):

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
</dependency>
```

**Gradle** (`build.gradle`):

```groovy
dependencies {
    implementation 'com.h2database:h2:2.3.232'
}
```

The full program:

```java
import java.sql.*;

public class JdbcH2Demo {
    public static void main(String[] args) throws SQLException {
        // In-memory H2: DB_CLOSE_DELAY=-1 keeps it alive while the JVM runs.
        String url = "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1";

        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {

            // 1. Create schema (plain Statement is fine for fixed DDL).
            try (Statement st = conn.createStatement()) {
                st.execute("""
                    CREATE TABLE person (
                        id   IDENTITY PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        age  INT
                    )""");
            }

            // 2. Insert with a PreparedStatement, capturing generated keys.
            String insert = "INSERT INTO person (name, age) VALUES (?, ?)";
            try (PreparedStatement ps =
                     conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                String[][] people = {{"Alice","30"},{"Bob","25"},{"Carol","41"}};
                for (String[] p : people) {
                    ps.setString(1, p[0]);
                    ps.setInt(2, Integer.parseInt(p[1]));
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (k.next()) System.out.println("Inserted id " + k.getLong(1));
                    }
                }
            }

            // 3. Query with a bound parameter.
            try (PreparedStatement ps =
                     conn.prepareStatement("SELECT id, name, age FROM person WHERE age >= ?")) {
                ps.setInt(1, 30);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("--- age >= 30 ---");
                    while (rs.next()) {
                        System.out.printf("%d: %s (%d)%n",
                            rs.getLong("id"), rs.getString("name"), rs.getInt("age"));
                    }
                }
            }

            // 4. A transaction: bump everyone's age, all-or-nothing.
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("UPDATE person SET age = age + 1");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            System.out.println("Transaction committed; everyone is one year older.");
        }
    }
}
```

Compile and run with the driver on the classpath (`;` separator on Windows, `:` on Unix):

```bash
# Assuming h2-2.3.232.jar sits next to the source file:
javac -cp h2-2.3.232.jar JdbcH2Demo.java
java  -cp ".;h2-2.3.232.jar" JdbcH2Demo     # Windows
java  -cp ".:h2-2.3.232.jar" JdbcH2Demo     # macOS / Linux
```

---

## 24.11 Best Practices

```java
// ✅ Always use try-with-resources for Connection, Statement, ResultSet.
try (Connection c = ds.getConnection();
     PreparedStatement ps = c.prepareStatement(sql)) { /* ... */ }

// ✅ Always use PreparedStatement with ? for any value — never concatenate SQL.
ps.setString(1, userInput);              // injection-proof

// ✅ Use a pooled DataSource (HikariCP) in real apps, not DriverManager per request.

// ✅ Wrap multi-statement work in an explicit transaction; rollback on failure.
conn.setAutoCommit(false); /* ... */ conn.commit();

// ✅ Batch bulk inserts/updates (addBatch/executeBatch) inside a transaction.

// ✅ Read columns by name for clarity; handle SQL NULL with wasNull()/getObject.

// ✅ Confine SQL behind a DAO; consider JPA/Hibernate for large domains.

// ❌ Don't ignore SQLException — log SQLState + vendor code, or wrap and rethrow.

// ❌ Don't leave auto-commit off on a pooled connection you return to the pool.
```

The recurring themes: close everything with try-with-resources, parameterize every query with `PreparedStatement`, pool connections through a `DataSource`, make transaction boundaries explicit, and keep persistence behind a DAO so the rest of the app never sees raw SQL.

---

## Summary

| Concept | JDBC API |
|---------|----------|
| **Connect** | `DriverManager.getConnection(url, user, pass)` → `Connection` |
| **Driver loading** | Auto-registered via `ServiceLoader` (JDBC 4.0+); no `Class.forName` |
| **Run a SELECT** | `Statement`/`PreparedStatement.executeQuery()` → `ResultSet` |
| **Run INSERT/UPDATE/DELETE** | `executeUpdate()` → affected row count |
| **Parameterized & safe SQL** | `PreparedStatement` with `?` + `setString`/`setInt`/... |
| **Read rows** | `ResultSet.next()` + `getInt`/`getString`/`getObject`, `wasNull()` |
| **Transactions** | `setAutoCommit(false)`, `commit()`, `rollback()` |
| **Isolation** | `setTransactionIsolation(TRANSACTION_*)` |
| **Bulk DML** | `addBatch()` / `executeBatch()` |
| **Auto-generated keys** | `prepareStatement(sql, RETURN_GENERATED_KEYS)` + `getGeneratedKeys()` |
| **Detached results** | `CachedRowSet` (`javax.sql.rowset`) |
| **Pooling** | `javax.sql.DataSource`, HikariCP |
| **Structure** | DAO pattern; JPA/Hibernate, jOOQ, MyBatis as higher layers |
| **Cleanup** | try-with-resources (`AutoCloseable`) — Java's RAII |

---

## Next Steps
- Stand up the H2 example above (add the one driver dependency) and run all four steps
- Rewrite every query that touches a variable as a `PreparedStatement`
- Wrap a multi-step operation in a transaction with proper `rollback`
- Introduce a pooled `DataSource` (HikariCP) and a DAO for one entity
- Explore JPA/Hibernate once raw JDBC feels routine
- Move to [Chapter 25: Testing](../25_testing/README.md)
