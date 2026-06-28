# Factory Method Pattern

## Overview

The Factory Method pattern defines an interface for creating an object, but lets subclasses decide which class to instantiate.

## Intent

- Define interface for object creation
- Let subclasses decide the concrete class
- Hide instantiation details
- Promote loose coupling
- Centralize object creation

## Problem

You need to create objects, but:
- The exact class isn't known until runtime
- Different subclasses may need different object types
- Client shouldn't depend on concrete classes
- Object creation is complex or context-dependent

## Solution

Define an abstract method in a base class for creating objects. Let subclasses override this method to create their specific types.

## Structure

```
        ┌──────────────┐
        │ Creator      │
        ├──────────────┤
        │ +factoryMethod()│ (abstract)
        │ +operation() │
        └──────────────┘
               △
               │
        ┌──────┴──────┐
        │             │
   ┌─────────┐    ┌─────────┐
   │ConcreteA│    │ConcreteB│
   ├─────────┤    ├─────────┤
   │factoryM()│  │factoryM()│
   └─────────┘    └─────────┘
         │              │
         └──────┬───────┘
                │
         ┌──────▼──────┐
         │ Product     │
         ├─────────────┤
         │ +operation()│
         └─────────────┘
```

## Java 21 Implementation

### Basic Factory Method

```java
// Abstract Product
interface Transport {
    void deliver();
}

// Concrete Products
class Truck implements Transport {
    @Override
    public void deliver() {
        System.out.println("Delivering by truck (land)");
    }
}

class Ship implements Transport {
    @Override
    public void deliver() {
        System.out.println("Delivering by ship (sea)");
    }
}

// Abstract Creator
abstract class Logistics {
    public abstract Transport createTransport();

    public void planDelivery() {
        Transport transport = createTransport();
        transport.deliver();
    }
}

// Concrete Creators
class RoadLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Truck();
    }
}

class SeaLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Ship();
    }
}
```

### Parameter-Based Factory Method

```java
abstract class DocumentFactory {
    public abstract Document createDocument();

    public void openDocument() {
        Document doc = createDocument();
        doc.open();
        doc.save();
        doc.close();
    }
}

class PdfFactory extends DocumentFactory {
    @Override
    public Document createDocument() {
        return new PdfDocument();
    }
}

class WordFactory extends DocumentFactory {
    @Override
    public Document createDocument() {
        return new WordDocument();
    }
}
```

### Static Factory Method

```java
interface Button {
    void render();
}

class WindowsButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Windows button");
    }
}

class MacButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Mac button");
    }
}

class Dialog {
    // Static factory method
    public static Button createButton(String os) {
        return switch (os) {
            case "Windows" -> new WindowsButton();
            case "Mac" -> new MacButton();
            default -> throw new RuntimeException("Unknown OS");
        };
    }
}
```

## Usage Examples

### Shape Factory

```java
interface Shape {
    void draw();
}

class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
}

class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing rectangle");
    }
}

class ShapeFactory {
    public static Shape createShape(String type) {
        return switch (type) {
            case "circle" -> new Circle();
            case "rectangle" -> new Rectangle();
            default -> null;
        };
    }
}

// Usage
Shape shape1 = ShapeFactory.createShape("circle");
Shape shape2 = ShapeFactory.createShape("rectangle");
shape1.draw();  // Drawing circle
shape2.draw();  // Drawing rectangle
```

### Database Connection Factory

```java
interface Database {
    void connect();
    void executeQuery(String query);
}

class MySQLDatabase implements Database {
    @Override
    public void connect() {
        System.out.println("Connecting to MySQL");
    }

    @Override
    public void executeQuery(String q) {
        System.out.println("MySQL: " + q);
    }
}

class PostgresDatabase implements Database {
    @Override
    public void connect() {
        System.out.println("Connecting to PostgreSQL");
    }

    @Override
    public void executeQuery(String q) {
        System.out.println("PostgreSQL: " + q);
    }
}

class DatabaseFactory {
    public static Database create(String type) {
        return switch (type) {
            case "mysql" -> new MySQLDatabase();
            case "postgres" -> new PostgresDatabase();
            default -> throw new RuntimeException("Unknown database type");
        };
    }
}
```

### Notification System

```java
interface Notification {
    void send(String message);
}

class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

class SMSNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "email" -> new EmailNotification();
            case "sms" -> new SMSNotification();
            default -> null;
        };
    }
}
```

## Advantages

✅ **Loose coupling** - Clients don't depend on concrete classes
✅ **Extensible** - Add new products without changing existing code
✅ **Single Responsibility** - Creation logic in one place
✅ **Open/Closed Principle** - Open for extension, closed for modification
✅ **Flexible** - Easy to switch implementations

## Disadvantages

❌ **Increased complexity** - More classes and interfaces
❌ **Over-engineering** - May be overkill for simple cases
❌ **Parallel class hierarchies** - Creator and Product both have subclasses
❌ **Indirection** - Extra layer between object creation and use

## When to Use

✅ **Multiple implementations** - Different versions of same interface
✅ **Runtime selection** - Choose implementation at runtime
✅ **Framework code** - Let subclasses decide what to create
✅ **Plugin systems** - Load implementations dynamically
✅ **Cross-platform** - Different implementations per platform

## When NOT to Use

❌ Simple, single product type
❌ Few variations of object
❌ Creation logic is trivial
❌ Premature optimization (YAGNI)

## Related Patterns

- **Abstract Factory** - Often uses Factory Method
- **Template Method** - Factory method commonly implemented as template method
- **Singleton** - Can be used with factory methods

## Best Practices

1. **Return interface, not concrete class** - Use the abstract type
2. **Rely on garbage collection** - For automatic memory management
3. **Parameter validation** - Check parameters before creating
4. **Default behavior** - Provide sensible defaults
5. **Cache when appropriate** - Reuse expensive objects
6. **Document subclasses** - List all product types available
7. **Consistent naming** - Use clear names for factory methods

## Implementation Checklist

- [ ] Define abstract Product type (interface or abstract class)
- [ ] Create concrete Product classes
- [ ] Define abstract Creator class with factory method
- [ ] Implement concrete Creator classes
- [ ] Return Product as the abstract type
- [ ] Validate parameters
- [ ] Handle unknown types gracefully
- [ ] Unit test all variants
- [ ] Document available product types
