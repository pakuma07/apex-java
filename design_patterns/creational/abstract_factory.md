# Abstract Factory Pattern

## Overview

The Abstract Factory pattern provides an interface for creating families of related or dependent objects without specifying their concrete classes.

## Intent

- Create families of related objects
- Ensure products work together
- Decouple client from concrete classes
- Switch entire families of products easily

## Problem

You need to create multiple related objects that must work together:
- UI components for different platforms (Windows, Mac, Linux)
- Database and cache implementations that pair
- Furniture styles (modern, victorian) with matching pieces
- Game assets (characters, enemies, items) with consistent style

You want to ensure products from the same family are used together.

## Solution

Define abstract factory that declares methods for creating each product type. Implement concrete factories for each product family.

## Structure

```
         ┌──────────────────┐
         │ AbstractFactory  │
         ├──────────────────┤
         │ +createProductA()│
         │ +createProductB()│
         └──────────────────┘
                △
                │
        ┌───────┴────────┐
        │                │
   ┌──────────┐      ┌──────────┐
   │ConcreteA │      │ConcreteB │
   ├──────────┤      ├──────────┤
   │factory   │      │factory   │
   └──────────┘      └──────────┘
        │                 │
        ├─────────┬───────┤
        │         │       │
   ┌─────────┐ ┌──────┐ ┌──────┐
   │ProductA1│ │Product│ │Product
   │ProductA2│ │B1    │ │B2
   └─────────┘ └──────┘ └──────┘
```

## Java 21 Implementation

### GUI Components Example

```java
// Abstract Products
interface Button {
    void click();
}

interface Checkbox {
    void check();
}

// Concrete Products - Windows
class WindowsButton implements Button {
    @Override
    public void click() {
        System.out.println("Windows button clicked");
    }
}

class WindowsCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Windows checkbox checked");
    }
}

// Concrete Products - Mac
class MacButton implements Button {
    @Override
    public void click() {
        System.out.println("Mac button clicked");
    }
}

class MacCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Mac checkbox checked");
    }
}

// Abstract Factory
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete Factories
class WindowsFactory implements GUIFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}

class MacFactory implements GUIFactory {
    @Override
    public Button createButton() {
        return new MacButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}

// Client
void createUI(GUIFactory factory) {
    var button = factory.createButton();
    var checkbox = factory.createCheckbox();

    button.click();
    checkbox.check();
}
```

## Usage Examples

### Furniture Factory

```java
interface Chair {
    void sitOn();
}

interface Table {
    void placeOn();
}

// Modern furniture
class ModernChair implements Chair {
    @Override
    public void sitOn() { System.out.println("Modern chair"); }
}

class ModernTable implements Table {
    @Override
    public void placeOn() { System.out.println("Modern table"); }
}

// Victorian furniture
class VictorianChair implements Chair {
    @Override
    public void sitOn() { System.out.println("Victorian chair"); }
}

class VictorianTable implements Table {
    @Override
    public void placeOn() { System.out.println("Victorian table"); }
}

// Abstract furniture factory
interface FurnitureFactory {
    Chair createChair();
    Table createTable();
}

// Concrete factories
class ModernFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new ModernChair();
    }

    @Override
    public Table createTable() {
        return new ModernTable();
    }
}

class VictorianFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new VictorianChair();
    }

    @Override
    public Table createTable() {
        return new VictorianTable();
    }
}

// Usage
void furnishRoom(FurnitureFactory factory) {
    var chair = factory.createChair();
    var table = factory.createTable();
    chair.sitOn();
    table.placeOn();
}
```

### Database and Cache Factory

```java
interface Database {
    void query(String sql);
}

interface Cache {
    void set(String key, String value);
    String get(String key);
}

// MySQL with Redis
class MySQLDatabase implements Database {
    @Override
    public void query(String sql) {
        System.out.println("MySQL: " + sql);
    }
}

class RedisCache implements Cache {
    @Override
    public void set(String key, String value) {
        System.out.println("Redis SET " + key + " = " + value);
    }

    @Override
    public String get(String key) {
        System.out.println("Redis GET " + key);
        return "value";
    }
}

// PostgreSQL with Memcached
class PostgresDatabase implements Database {
    @Override
    public void query(String sql) {
        System.out.println("PostgreSQL: " + sql);
    }
}

class MemcachedCache implements Cache {
    @Override
    public void set(String key, String value) {
        System.out.println("Memcached SET " + key + " = " + value);
    }

    @Override
    public String get(String key) {
        System.out.println("Memcached GET " + key);
        return "value";
    }
}

// Abstract factory
interface DataStackFactory {
    Database createDatabase();
    Cache createCache();
}

// Concrete factories
class MySQLRedisFactory implements DataStackFactory {
    @Override
    public Database createDatabase() {
        return new MySQLDatabase();
    }

    @Override
    public Cache createCache() {
        return new RedisCache();
    }
}

class PostgresMemcachedFactory implements DataStackFactory {
    @Override
    public Database createDatabase() {
        return new PostgresDatabase();
    }

    @Override
    public Cache createCache() {
        return new MemcachedCache();
    }
}
```

## Advantages

✅ **Ensures consistency** - Products from same family work together
✅ **Decoupling** - Clients don't depend on concrete classes
✅ **Easy switching** - Change entire product family easily
✅ **Centralized creation** - All creation logic in one place
✅ **Single Responsibility** - Each factory has one job

## Disadvantages

❌ **Complexity** - Many classes and interfaces
❌ **Over-engineering** - Overkill for simple scenarios
❌ **Adding products** - Need new method in all factories
❌ **Parallel hierarchies** - Both products and factories grow together

## When to Use

✅ **Multiple product families** - Related products that must work together
✅ **Platform independence** - Different implementations per platform
✅ **Flexible configuration** - Switch families at runtime
✅ **Complex initialization** - Multiple interdependent objects
✅ **Consistency required** - Products must be compatible

## When NOT to Use

❌ Single product family
❌ Few product variations
❌ Simple creation logic
❌ Only one variant at a time

## Related Patterns

- **Factory Method** - Often used by Abstract Factory
- **Singleton** - Factories often are singletons
- **Bridge** - Combines with abstract factory for abstraction/implementation
- **Strategy** - Can be used to select factory family

## Best Practices

1. **Use object references** - Rely on garbage collection for memory management
2. **Return interfaces** - Clients should only see abstract types
3. **Document families** - List all product families
4. **Consider defaults** - Have sensible default factory
5. **Cache factories** - Avoid creating factories repeatedly
6. **Use static methods** - For factory creation if appropriate
7. **Validate invariants** - Ensure products from family work together

## Implementation Checklist

- [ ] Define abstract product interfaces
- [ ] Create concrete product classes for each family
- [ ] Define abstract factory interface
- [ ] Implement concrete factories for each family
- [ ] Ensure products from same family work together
- [ ] Rely on garbage collection for memory management
- [ ] Document all product families
- [ ] Validate that incompatible products can't be mixed
- [ ] Unit test each factory combination
- [ ] Document how to add new product families
