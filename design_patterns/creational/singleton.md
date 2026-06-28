# Singleton Pattern

## Overview

The Singleton pattern ensures that a class has only one instance and provides a global point of access to it.

## Intent

- Ensure a class has only one instance
- Provide global access to that instance
- Lazy initialization (create when needed)
- Thread-safe in multi-threaded environments

## Problem

Some classes should have only one instance:
- Database connections
- Logger
- Configuration manager
- Thread pools
- Window managers

Creating multiple instances wastes resources and can cause problems.

## Solution

Create a class with:
1. Private constructor (prevent instantiation)
2. Static instance variable
3. Static getter method to access instance
4. In Java, prefer an `enum` singleton, which the JVM guarantees to be a single instance and is serialization-safe

## Structure

```
┌─────────────┐
│  Singleton  │
├─────────────┤
│ - instance  │ (static)
├─────────────┤
│ + getInstance() │ (static)
│ - Singleton()   │ (private)
└─────────────┘
```

## Java 21 Implementation

### Enum Singleton (Preferred)

In Java the simplest and safest singleton is an `enum`. The JVM guarantees exactly one instance, and it is thread-safe and serialization-safe out of the box.

```java
public enum Singleton {
    INSTANCE;

    // Public methods
    public void doSomething() {
        System.out.println("Singleton operation");
    }
}

// Usage
Singleton.INSTANCE.doSomething();
```

### Eager-Initialized Singleton

```java
public final class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    // Private constructor
    private EagerSingleton() {}

    // Get singleton instance
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }

    public void doSomething() {
        System.out.println("Eager singleton operation");
    }
}
```

### Thread-Safe Lazy Singleton (Initialization-on-Demand Holder)

The holder idiom gives lazy, thread-safe initialization without synchronization overhead. The holder class is not loaded until `getInstance()` is first called.

```java
public final class ThreadSafeSingleton {
    private ThreadSafeSingleton() {}

    private static class Holder {
        private static final ThreadSafeSingleton INSTANCE = new ThreadSafeSingleton();
    }

    public static ThreadSafeSingleton getInstance() {
        return Holder.INSTANCE;  // Created once, thread-safe
    }

    public void doSomething() {
        System.out.println("Thread-safe singleton operation");
    }
}
```

### Double-Checked Locking Singleton

When you need lazy initialization with explicit control, use a `volatile` field with double-checked locking.

```java
public final class LazySingleton {
    private static volatile LazySingleton instance;

    private LazySingleton() {}

    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }

    public void doSomething() {
        System.out.println("Lazy singleton");
    }
}
```

## Usage Examples

### Logger Singleton

```java
public enum Logger {
    INSTANCE;

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

// Usage
Logger.INSTANCE.log("Application started");
Logger.INSTANCE.log("Processing data");
Logger.INSTANCE.log("Application ended");
```

### Configuration Manager

```java
import java.util.HashMap;
import java.util.Map;

public enum ConfigManager {
    INSTANCE;

    private final Map<String, String> configs = new HashMap<>();

    ConfigManager() {
        // Load configuration
        configs.put("db_host", "localhost");
        configs.put("db_port", "5432");
        configs.put("timeout", "30");
    }

    public String get(String key) {
        return configs.get(key);
    }

    public void set(String key, String value) {
        configs.put(key, value);
    }
}

// Usage
System.out.println(ConfigManager.INSTANCE.get("db_host"));
ConfigManager.INSTANCE.set("timeout", "60");
```

### Database Connection Pool

```java
import java.util.ArrayList;
import java.util.List;

public enum DatabasePool {
    INSTANCE;

    private final List<Connection> connections = new ArrayList<>();

    DatabasePool() {
        // Initialize connection pool
        for (int i = 0; i < 5; i++) {
            connections.add(new Connection());
        }
    }

    public Connection getConnection() {
        if (!connections.isEmpty()) {
            return connections.remove(connections.size() - 1);
        }
        return null;
    }

    public void releaseConnection(Connection conn) {
        connections.add(conn);
    }
}
```

## Advantages

✅ **Controlled access** - Single, global access point
✅ **Lazy initialization** - Created only when needed (holder idiom / double-checked locking)
✅ **Resource efficiency** - Only one instance exists
✅ **Thread safety** - Enum and holder idioms are thread-safe
✅ **Easy to use** - Simple static getter method

## Disadvantages

❌ **Global state** - Hides dependencies
❌ **Testing difficulties** - Hard to mock for unit tests
❌ **Hidden coupling** - All code depends on singleton
❌ **Thread safety complexity** - Must handle multi-threading
❌ **Performance** - Global access point can be bottleneck

## When to Use

✅ **Logger** - Single logging instance for entire app
✅ **Configuration Manager** - One config source
✅ **Database Connection Pool** - Manage single pool
✅ **Cache** - Single cache for application
✅ **Thread Pool** - Single thread manager
✅ **File System** - Single file system interface

## When NOT to Use

❌ Replacing global variables without good reason
❌ When you need multiple instances
❌ In highly concurrent systems (complex synchronization)
❌ For dependency injection scenarios
❌ When testability is critical

## Related Patterns

- **Factory Method** - Can use Singleton to manage instances
- **Abstract Factory** - Often used with Singleton
- **Facade** - Often implemented as Singleton

## Best Practices

1. **Use an enum singleton** - Easiest thread-safe and serialization-safe approach
2. **Avoid accessing globally** - Pass as dependency when possible
3. **Use the holder idiom for lazy init** - Thread-safe without locking
4. **Make the constructor private** - Prevent accidental instantiation
5. **Rely on garbage collection** - No manual cleanup needed
6. **Document the purpose** - Why this class needs to be singleton
7. **Consider alternatives** - Dependency injection might be better

## Common Pitfalls

⚠️ **Too many singletons** - "Singleton creep"
⚠️ **Hidden dependencies** - Hard to see what's needed
⚠️ **Testing nightmare** - Difficult to unit test
⚠️ **Thread safety issues** - Improper synchronization (forgetting `volatile`)
⚠️ **Serialization breaking the singleton** - Use enum to avoid this

## Implementation Checklist

- [ ] Prefer an `enum` singleton when possible
- [ ] Make constructor private
- [ ] Provide a static getInstance() (or use the enum INSTANCE)
- [ ] Ensure thread safety (enum, holder idiom, or volatile + double-checked locking)
- [ ] Rely on garbage collection for cleanup
- [ ] Document why it's a singleton
- [ ] Unit test the singleton behavior
- [ ] Consider testing alternatives (fakes, mocks)
