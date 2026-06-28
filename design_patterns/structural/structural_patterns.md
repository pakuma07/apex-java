# Adapter Pattern

## Overview

The Adapter pattern converts the interface of a class into another interface clients expect, allowing incompatible interfaces to work together.

## Intent

- Make incompatible interfaces compatible
- Reuse existing classes with incompatible interfaces
- Act as middleware between two interfaces
- Convert data between different formats

## Problem

You have two classes with incompatible interfaces that need to work together:
- New class needs old implementation
- Third-party library with incompatible interface
- Different data formats that need translation
- Legacy code integration

## Solution

Create an Adapter class that:
1. Inherits from the interface clients expect
2. Wraps the incompatible class
3. Translates between interfaces

## Structure (Class Adapter)

```
┌──────────────┐         ┌─────────────────┐
│   Target     │         │   Adaptee       │
├──────────────┤         ├─────────────────┤
│+request()    │         │+specificRequest │
└──────────────┘         └─────────────────┘
       △                         △
       │                         │
       └──────────┬──────────────┘
                  │
              ┌───────────┐
              │  Adapter  │
              ├───────────┤
              │+request() │
              └───────────┘
```

## Java 21 Implementation

### Class Adapter

Java has no multiple inheritance of classes, so the "class adapter" form
implements the Target interface and extends the Adaptee (single base class).

```java
// Target interface (what client expects)
interface Target {
    void request();
}

// Adaptee (incompatible interface)
class Adaptee {
    public void specificRequest() {
        System.out.println("Adaptee specific request");
    }
}

// Adapter
class Adapter extends Adaptee implements Target {
    @Override
    public void request() {
        specificRequest();  // Call adaptee's method
    }
}

// Usage
Target target = new Adapter();
target.request();
```

### Object Adapter

```java
// Target
interface MediaPlayer {
    void play(String filename);
}

// Adaptee
class VLCPlayer {
    public void playVLC(String file) {
        System.out.println("Playing VLC: " + file);
    }
}

// Adapter
class VLCAdapter implements MediaPlayer {
    private final VLCPlayer vlc;

    public VLCAdapter() {
        this.vlc = new VLCPlayer();
    }

    @Override
    public void play(String filename) {
        vlc.playVLC(filename);
    }
}

// Usage
List<MediaPlayer> players = new ArrayList<>();
players.add(new VLCAdapter());

for (var player : players) {
    player.play("video.mp4");
}
```

### Data Format Adapter

```java
// Expected interface
interface PaymentProcessor {
    void processPayment(double amount);
}

// Legacy payment system
class LegacyPaymentGateway {
    public void handleTransaction(String amount, String currency) {
        System.out.println("Processing: " + amount + " " + currency);
    }
}

// Adapter
class LegacyPaymentAdapter implements PaymentProcessor {
    private final LegacyPaymentGateway legacy;

    public LegacyPaymentAdapter() {
        this.legacy = new LegacyPaymentGateway();
    }

    @Override
    public void processPayment(double amount) {
        legacy.handleTransaction(String.valueOf(amount), "USD");
    }
}
```

## Advantages

✅ **Reusability** - Use incompatible classes together
✅ **Single Responsibility** - Conversion logic isolated
✅ **Flexibility** - Adapter can be swapped
✅ **No modification** - Don't modify existing classes
✅ **Late binding** - Choose adapter at runtime

## Disadvantages

❌ **Complexity** - Extra layer of indirection
❌ **Performance** - Overhead from wrapping
❌ **Debugging** - Harder to trace through adapter

## When to Use

✅ Integrating incompatible interfaces
✅ Third-party library integration
✅ Legacycode modernization
✅ Data format conversion
✅ API compatibility

## Related Patterns

- **Bridge** - Similar structure, different intent
- **Decorator** - Similar wrapping approach
- **Proxy** - Similar structure for control

---

# Decorator Pattern

## Overview

The Decorator pattern attaches additional responsibilities to an object dynamically, providing a flexible alternative to subclassing.

## Intent

- Add responsibilities to objects dynamically
- Avoid rigid subclass hierarchies
- Combine behaviors flexibly
- Keep single responsibility principle

## Problem

You need to add features to objects, but:
- Subclassing leads to class explosion
- Feature combinations are many
- Features need runtime composition
- Inheritance tree becomes unmanageable

## Solution

Create Decorator classes that:
1. Wrap target object
2. Implement same interface
3. Add new behavior
4. Delegate to wrapped object

## Java 21 Implementation

```java
// Component
interface Coffee {
    double getCost();
    String getDescription();
}

// Concrete component
class SimpleCoffee implements Coffee {
    @Override
    public double getCost() { return 2.0; }

    @Override
    public String getDescription() { return "Simple Coffee"; }
}

// Decorator
abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee;

    protected CoffeeDecorator(Coffee c) {
        this.coffee = c;
    }
}

// Concrete decorators
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee c) { super(c); }

    @Override
    public double getCost() { return coffee.getCost() + 0.5; }

    @Override
    public String getDescription() { return coffee.getDescription() + ", Milk"; }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee c) { super(c); }

    @Override
    public double getCost() { return coffee.getCost() + 0.25; }

    @Override
    public String getDescription() { return coffee.getDescription() + ", Sugar"; }
}

// Usage
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);

System.out.println(coffee.getDescription() + ": $" + coffee.getCost());
// Output: Simple Coffee, Milk, Sugar: $2.75
```

## Advantages

✅ **Flexible combination** - Combine behaviors at runtime
✅ **Single Responsibility** - Each decorator has one job
✅ **Open/Closed** - Open for extension, closed for modification
✅ **Dynamic** - Add/remove features without subclassing
✅ **Transparent** - Same interface as wrapped object

## Disadvantages

❌ **Complexity** - Multiple wrapper objects
❌ **Order matters** - Decorator order affects behavior
❌ **Debugging** - Hard to trace through layers
❌ **Performance** - Overhead from wrapping

## When to Use

✅ Dynamic feature addition
✅ Avoid subclass explosion
✅ Features can be combined
✅ Runtime configuration needed
✅ Single Responsibility Principle important

---

# Composite Pattern

## Overview

The Composite pattern composes objects into tree structures to represent part-whole hierarchies, letting clients treat individual objects and compositions uniformly.

## Intent

- Treat individual and composite objects uniformly
- Create tree structures
- Recursive hierarchies
- Client simplification

## Java 21 Implementation

```java
// Component
interface FileSystemItem {
    void display(int indent);
    int getSize();
}

// Leaf
class FileItem implements FileSystemItem {
    private final String name;
    private final int size;

    public FileItem(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public void display(int indent) {
        System.out.println(" ".repeat(indent) + "File: " + name + " (" + size + "B)");
    }

    @Override
    public int getSize() { return size; }
}

// Composite
class Directory implements FileSystemItem {
    private final String name;
    private final List<FileSystemItem> items = new ArrayList<>();

    public Directory(String name) {
        this.name = name;
    }

    public void add(FileSystemItem item) {
        items.add(item);
    }

    @Override
    public void display(int indent) {
        System.out.println(" ".repeat(indent) + "Dir: " + name);
        for (var item : items) {
            item.display(indent + 2);
        }
    }

    @Override
    public int getSize() {
        int total = 0;
        for (var item : items) {
            total += item.getSize();
        }
        return total;
    }
}

// Usage
var root = new Directory("root");
root.add(new FileItem("file1.txt", 100));
root.add(new FileItem("file2.txt", 200));

var subdir = new Directory("subdir");
subdir.add(new FileItem("file3.txt", 150));
root.add(subdir);

root.display(0);
System.out.println("Total size: " + root.getSize());
```

---

# Facade Pattern

## Overview

The Facade pattern provides a unified, simplified interface to a set of interfaces in a subsystem.

## Java 21 Implementation

```java
// Complex subsystem
class DatabaseConnection {
    public void query(String sql) { /* ... */ }
}

class CacheManager {
    public boolean has(int key) { return false; }
    public void set(int key, String value) { /* ... */ }
}

class Logger {
    public void log(String message) { /* ... */ }
}

// Facade
class ApplicationFacade {
    private final DatabaseConnection db;
    private final CacheManager cache;
    private final Logger logger;

    public ApplicationFacade() {
        db = new DatabaseConnection();
        cache = new CacheManager();
        logger = new Logger();
    }

    public void getUserData(int userId) {
        logger.log("Fetching user " + userId);
        if (cache.has(userId)) {
            logger.log("Cache hit");
            return;
        }
        logger.log("Querying database");
        db.query("SELECT * FROM users WHERE id = " + userId);
        cache.set(userId, "data");
    }
}

// Client only sees simple interface
var app = new ApplicationFacade();
app.getUserData(123);
```

---

All structural patterns follow similar principles of composition and delegation to provide flexible, maintainable designs.
