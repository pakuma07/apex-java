# Prototype Pattern

## Overview

The Prototype pattern creates new objects by copying an existing object (prototype) rather than creating from scratch.

## Intent

- Create objects by cloning prototypes
- Avoid expensive object creation
- Hide creation complexity
- Create variations of existing objects
- Specify new object's class at runtime

## Problem

Creating objects from scratch is expensive:
- Complex initialization logic
- Deep hierarchies to navigate
- Resource-intensive setup
- Similar objects with slight variations
- Need to clone at runtime without knowing type

## Solution

Define a Prototype interface with clone() method. Concrete prototypes implement clone() to create copies of themselves. Client clones prototypes instead of creating new objects.

## Structure

```
┌──────────────────┐
│   Prototype      │
├──────────────────┤
│+clone()          │ (abstract)
└──────────────────┘
        △
        │
   ┌────┴─────┐
   │           │
┌──────────┐ ┌──────────┐
│ConcreteA │ │ConcreteB │
├──────────┤ ├──────────┤
│+clone()  │ │+clone()  │
└──────────┘ └──────────┘
```

## Java 21 Implementation

### Basic Prototype

```java
// Abstract prototype
interface Prototype {
    Prototype clone();
    void show();
}

// Concrete prototype
class ConcretePrototype implements Prototype {
    private final String name;
    private final int value;

    public ConcretePrototype(String name, int value) {
        this.name = name;
        this.value = value;
    }

    // Copy constructor used by clone()
    public ConcretePrototype(ConcretePrototype other) {
        this.name = other.name;
        this.value = other.value;
    }

    @Override
    public Prototype clone() {
        return new ConcretePrototype(this);
    }

    @Override
    public void show() {
        System.out.println("Name: " + name + ", Value: " + value);
    }
}

// Usage
ConcretePrototype original = new ConcretePrototype("Original", 10);
Prototype clone1 = original.clone();
Prototype clone2 = original.clone();

original.show();  // Name: Original, Value: 10
clone1.show();    // Name: Original, Value: 10
clone2.show();    // Name: Original, Value: 10
```

### Deep Copy Prototype

```java
import java.util.ArrayList;
import java.util.List;

interface Shape {
    Shape clone();
    void draw();
}

class Circle implements Shape {
    private final int radius;
    private final List<Integer> points;  // Complex data

    public Circle(int radius) {
        this.radius = radius;
        this.points = new ArrayList<>();
        // Expensive initialization
        for (int i = 0; i < 1000; i++) {
            points.add(i);
        }
    }

    // Copy constructor performing a deep copy
    public Circle(Circle other) {
        this.radius = other.radius;
        this.points = new ArrayList<>(other.points);  // Deep copy of the list
    }

    @Override
    public Shape clone() {
        return new Circle(this);  // Deep copy
    }

    @Override
    public void draw() {
        System.out.println("Drawing circle with radius " + radius);
    }
}

// Usage - Cloning is faster than recreating
Circle original = new Circle(10);
Shape clone1 = original.clone();
Shape clone2 = original.clone();
```

### Shape Prototype Registry

```java
import java.util.HashMap;
import java.util.Map;

interface Shape {
    Shape clone();
    String getType();
}

class Circle implements Shape {
    private final int radius;

    public Circle(int radius) {
        this.radius = radius;
    }

    public Circle(Circle other) {
        this.radius = other.radius;
    }

    @Override
    public Shape clone() {
        return new Circle(this);
    }

    @Override
    public String getType() {
        return "Circle";
    }
}

class Rectangle implements Shape {
    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Rectangle(Rectangle other) {
        this.width = other.width;
        this.height = other.height;
    }

    @Override
    public Shape clone() {
        return new Rectangle(this);
    }

    @Override
    public String getType() {
        return "Rectangle";
    }
}

// Prototype registry
class ShapePrototypeFactory {
    private final Map<String, Shape> prototypes = new HashMap<>();

    public void registerPrototype(String key, Shape shape) {
        prototypes.put(key, shape);
    }

    public Shape createShape(String key) {
        Shape prototype = prototypes.get(key);
        if (prototype != null) {
            return prototype.clone();
        }
        return null;
    }
}

// Usage
ShapePrototypeFactory factory = new ShapePrototypeFactory();
factory.registerPrototype("circle", new Circle(5));
factory.registerPrototype("rectangle", new Rectangle(10, 20));

Shape circle = factory.createShape("circle");
Shape rect = factory.createShape("rectangle");
Shape circle2 = factory.createShape("circle");
```

### Document Prototype

```java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Document {
    private final String title;
    private final List<String> content;
    private final Map<String, String> metadata;

    public Document(String title) {
        this.title = title;
        this.content = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();
    }

    // Copy constructor performing a deep copy
    public Document(Document other) {
        this.title = other.title;
        this.content = new ArrayList<>(other.content);
        this.metadata = new LinkedHashMap<>(other.metadata);
    }

    public void addContent(String text) {
        content.add(text);
    }

    public void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public Document clone() {
        return new Document(this);
    }

    public void show() {
        System.out.println("Document: " + title);
        for (String line : content) {
            System.out.println("  " + line);
        }
        System.out.print("Metadata: ");
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        System.out.println();
    }
}

// Create template document
Document templateDoc = new Document("Template");
templateDoc.addContent("Introduction...");
templateDoc.addContent("Body...");
templateDoc.setMetadata("author", "Unknown");
templateDoc.setMetadata("version", "1.0");

// Clone for new documents
Document doc1 = templateDoc.clone();
Document doc2 = templateDoc.clone();
Document doc3 = templateDoc.clone();

doc1.show();
doc2.show();
doc3.show();
```

### Configuration Clone

```java
import java.util.HashMap;
import java.util.Map;

class Configuration {
    private final String appName;
    private String version;
    private int maxConnections;
    private boolean debugMode;
    private final Map<String, String> settings;

    public Configuration(String appName) {
        this.appName = appName;
        this.maxConnections = 100;
        this.debugMode = false;
        this.settings = new HashMap<>();
    }

    // Copy constructor performing a deep copy
    public Configuration(Configuration other) {
        this.appName = other.appName;
        this.version = other.version;
        this.maxConnections = other.maxConnections;
        this.debugMode = other.debugMode;
        this.settings = new HashMap<>(other.settings);
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public Configuration clone() {
        return new Configuration(this);
    }

    public void display() {
        System.out.println("Config: " + appName + " v" + version
                + " connections=" + maxConnections);
    }
}

// Usage - Create base config, clone for variations
Configuration baseConfig = new Configuration("MyApp");
baseConfig.setSetting("log_level", "INFO");
baseConfig.setSetting("database", "postgres");

Configuration devConfig = baseConfig.clone();
Configuration testConfig = baseConfig.clone();
Configuration prodConfig = baseConfig.clone();

// Modify clones as needed
```

## Advantages

✅ **Performance** - Faster than creating from scratch
✅ **Simplicity** - Simple copying instead of complex initialization
✅ **Polymorphism** - Clone without knowing concrete type
✅ **Decoupling** - Client doesn't know creation details
✅ **Runtime specification** - Type specified at runtime
✅ **Avoids subclassing** - Alternative to factory hierarchies

## Disadvantages

❌ **Circular references** - Complex to handle cycles
❌ **Deep vs shallow** - Must copy correctly
❌ **Clone complexity** - Copying complex objects can be tricky
❌ **Side effects** - Copying may miss some initialization
❌ **Performance** - Copying large objects can be expensive

## When to Use

✅ **Expensive creation** - Object creation is complex/slow
✅ **Similar objects** - Need variations of existing objects
✅ **Runtime type selection** - Type specified at runtime
✅ **Avoid subclassing** - Alternative to factory method
✅ **Independent copies** - Need isolated versions
✅ **Template objects** - Clone and modify templates

## When NOT to Use

❌ Simple objects (just create new ones)
❌ Circular references (hard to handle)
❌ Singleton objects (don't clone)
❌ Objects with external resources (hard to copy)

## Related Patterns

- **Factory Method** - Alternative for object creation
- **Abstract Factory** - Can work with prototypes
- **Memento** - Similar approach to capturing state
- **Singleton** - Opposite (single instance vs clones)

## Best Practices

1. **Deep copy correctly** - Copy all nested objects
2. **Handle cycles** - Detect and handle circular references
3. **Override copy constructor** - Ensure correct deep copy
4. **Use a Prototype interface** - Declare clone() on a shared interface
5. **Return references** - clone() returns a new instance (garbage-collected)
6. **Test copying** - Ensure clones are independent
7. **Document copy behavior** - Explain what gets copied
8. **Consider performance** - Measure clone vs creation time

## Implementation Checklist

- [ ] Define Prototype interface with clone()
- [ ] Implement clone() in all concrete prototypes
- [ ] Ensure deep copy of all members
- [ ] Return clone as a new instance (reference)
- [ ] Handle circular references if needed
- [ ] Create prototype registry if needed
- [ ] Test clone independence
- [ ] Verify complex member copying
- [ ] Performance test vs creation from scratch
- [ ] Document copy behavior
