# Proxy Pattern

## Overview

The Proxy pattern provides a surrogate or placeholder for another object to control access to it.

## Intent

- Control access to another object
- Add functionality before/after real object
- Lazy initialization
- Logging and security
- Remote object access
- Resource management

## Problem

You need to control or manage access to an object:
- Object is expensive to create (lazy loading)
- Need access control/security
- Want to log access
- Object is remote (network)
- Need caching
- Multiple references to control

## Solution

Create a Proxy object that:
1. Implements the same interface as real object
2. Holds reference to real object
3. Adds extra behavior before/after delegation
4. Appears identical to client

## Structure

```
┌──────────────┐         ┌──────────────┐
│  Subject     │         │   Proxy      │
├──────────────┤         ├──────────────┤
│+request()    │         │+request()    │
└──────────────┘         └──────────────┘
       △                      △
       │                      │
       │           ┌──────────┴─────────┐
       │           │                    │
       │    ┌──────▼────────┐    ┌──────▼────────┐
       └────│ RealSubject   │    │ Virtual/Access│
            │ +request()    │    │ Proxy         │
            └───────────────┘    └───────────────┘
```

## Java 21 Implementation

### Virtual Proxy (Lazy Loading)

```java
import java.util.ArrayList;
import java.util.List;

interface Image {
    void display();
}

class RealImage implements Image {
    private final String filename;

    public RealImage(String filename) {
        this.filename = filename;
        loadImage();
    }

    private void loadImage() {
        System.out.println("Loading image: " + filename);
        // Expensive operation
    }

    @Override
    public void display() {
        System.out.println("Displaying: " + filename);
    }
}

class ProxyImage implements Image {
    private final String filename;
    private RealImage realImage;

    public ProxyImage(String filename) {
        this.filename = filename;
        this.realImage = null;
    }

    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        List<Image> images = new ArrayList<>();

        // Only create proxy, real image not loaded yet
        images.add(new ProxyImage("large_image_1.jpg"));
        images.add(new ProxyImage("large_image_2.jpg"));
        images.add(new ProxyImage("large_image_3.jpg"));

        System.out.println("Images created but not loaded");
        System.out.println();

        // Real images loaded only when displayed
        System.out.println("Displaying images:");
        for (Image img : images) {
            img.display();
        }
    }
}
```

### Protection Proxy

```java
interface BankAccount {
    void withdraw(double amount);
    void deposit(double amount);
    double balance();
}

class RealBankAccount implements BankAccount {
    private double balance;

    public RealBankAccount(double initial) {
        this.balance = initial;
    }

    @Override
    public void withdraw(double amount) {
        balance -= amount;
        System.out.println("Withdrawn: $" + amount);
    }

    @Override
    public void deposit(double amount) {
        balance += amount;
        System.out.println("Deposited: $" + amount);
    }

    @Override
    public double balance() {
        return balance;
    }
}

class AccountProxy implements BankAccount {
    private final RealBankAccount realAccount;
    private final String userPassword;
    private String providedPassword;

    public AccountProxy(String password, double initial) {
        this.userPassword = password;
        this.providedPassword = "";
        this.realAccount = new RealBankAccount(initial);
    }

    public void setPassword(String pwd) {
        this.providedPassword = pwd;
    }

    public boolean authenticate() {
        return providedPassword.equals(userPassword);
    }

    @Override
    public void withdraw(double amount) {
        if (!authenticate()) {
            System.out.println("Access denied!");
            return;
        }
        realAccount.withdraw(amount);
    }

    @Override
    public void deposit(double amount) {
        if (!authenticate()) {
            System.out.println("Access denied!");
            return;
        }
        realAccount.deposit(amount);
    }

    @Override
    public double balance() {
        if (!authenticate()) {
            System.out.println("Access denied!");
            return -1;
        }
        return realAccount.balance();
    }
}
```

### Logging Proxy

```java
interface Document {
    void open();
    void close();
    void read();
}

class RealDocument implements Document {
    private final String filename;

    public RealDocument(String filename) {
        this.filename = filename;
    }

    @Override
    public void open() {
        System.out.println("Opening: " + filename);
    }

    @Override
    public void close() {
        System.out.println("Closing: " + filename);
    }

    @Override
    public void read() {
        System.out.println("Reading: " + filename);
    }
}

class LoggingDocumentProxy implements Document {
    private final RealDocument document;
    private final String username;

    public LoggingDocumentProxy(String filename, String user) {
        this.document = new RealDocument(filename);
        this.username = user;
    }

    @Override
    public void open() {
        System.out.println("[LOG] " + username + " opened document");
        document.open();
    }

    @Override
    public void close() {
        System.out.println("[LOG] " + username + " closed document");
        document.close();
    }

    @Override
    public void read() {
        System.out.println("[LOG] " + username + " read document");
        document.read();
    }
}
```

### Caching Proxy

```java
import java.util.HashMap;
import java.util.Map;

interface DataService {
    String getData(int id);
}

class RealDataService implements DataService {
    @Override
    public String getData(int id) {
        System.out.println("Fetching from database...");
        return "Data for ID: " + id;
    }
}

class CachingDataService implements DataService {
    private final RealDataService service;
    private final Map<Integer, String> cache;

    public CachingDataService() {
        this.service = new RealDataService();
        this.cache = new HashMap<>();
    }

    @Override
    public String getData(int id) {
        if (cache.containsKey(id)) {
            System.out.println("Cache hit for ID: " + id);
            return cache.get(id);
        }

        System.out.println("Cache miss for ID: " + id);
        String data = service.getData(id);
        cache.put(id, data);
        return data;
    }
}
```

## Advantages

✅ **Lazy initialization** - Create expensive objects only when needed
✅ **Access control** - Regulate who can access object
✅ **Logging/audit** - Track object access
✅ **Caching** - Avoid repeated expensive operations
✅ **Remote access** - Access objects across network
✅ **Same interface** - Transparent to clients
✅ **Flexible** - Add behavior without modifying real object

## Disadvantages

❌ **Complexity** - Extra layer of indirection
❌ **Performance** - Overhead from proxy operations
❌ **Thread safety** - Need synchronization for shared objects
❌ **Debugging** - Harder to trace through proxy

## Variations

| Type | Purpose | Example |
|------|---------|---------|
| **Virtual** | Lazy initialization | Load expensive images on demand |
| **Protection** | Access control | Bank account access |
| **Remote** | Network access | RPC stubs, web services |
| **Logging** | Track operations | Audit trails |
| **Caching** | Performance | Database result caching |
| **Smart** | Reference management | References (garbage-collected) |

## When to Use

✅ Object creation is expensive
✅ Need access control/security
✅ Want to log/audit operations
✅ Accessing remote objects
✅ Need caching layer
✅ Lazy initialization needed
✅ Multiple references need coordination
✅ Want to add behavior transparently

## When NOT to Use

❌ Simple, cheap objects
❌ Direct access preferred
❌ Performance critical (overhead)
❌ Single simple operation
❌ No additional behavior needed

## Related Patterns

- **Decorator** - Similar structure, different intent (adds behavior)
- **Adapter** - Converts interface (Proxy maintains interface)
- **Facade** - Simplifies complex subsystem (Proxy controls access)

---

## Comparison: Proxy vs Decorator

| Aspect | Proxy | Decorator |
|--------|-------|-----------|
| **Intent** | Control access | Add behavior |
| **Responsibility** | Replace object | Enhance object |
| **Lifetime** | Same as real object | Wraps real object |
| **Interface** | Same as subject | Same as component |
| **Purpose** | Access control | Feature addition |

---

Proxy pattern is essential for lazy loading, access control, and adding operations transparently. Use it when you need to manage how clients access objects.
