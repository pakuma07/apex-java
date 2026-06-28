# Design Patterns in Java 21

A comprehensive guide to all 23 Gang of Four (GoF) design patterns with detailed explanations, Java 21 examples, and practical use cases.

## What are Design Patterns?

Design patterns are reusable solutions to common problems in software design. They represent best practices, can speed up development, and help make code more readable and maintainable.

## Pattern Categories

Design patterns are organized into three main categories:

### 1. **Creational Patterns** (5/5 ✅ COMPLETE)
Patterns that deal with object creation mechanisms.

- ✅ [Singleton](creational/singleton.md) - Ensure a class has only one instance
- ✅ [Factory Method](creational/factory_method.md) - Create objects without specifying exact classes
- ✅ [Abstract Factory](creational/abstract_factory.md) - Create families of related objects
- ✅ [Builder](creational/builder.md) - Construct complex objects step by step
- ✅ [Prototype](creational/prototype.md) - Create objects by cloning existing ones

### 2. **Structural Patterns** (7/7 ✅ COMPLETE)
Patterns that deal with object composition and relationships.

- ✅ [Adapter](structural/structural_patterns.md#adapter) - Convert interface of a class to another clients expect
- ✅ [Bridge](structural/bridge.md) - Decouple abstraction from implementation
- ✅ [Composite](structural/structural_patterns.md#composite) - Compose objects into tree structures
- ✅ [Decorator](structural/structural_patterns.md#decorator) - Attach responsibilities to objects dynamically
- ✅ [Facade](structural/structural_patterns.md#facade) - Provide unified interface to subsystem
- ✅ [Proxy](structural/proxy.md) - Provide surrogate for another object
- ✅ [Flyweight](structural/flyweight.md) - Share objects to support large numbers efficiently

### 3. **Behavioral Patterns** (11/11 ✅ COMPLETE)
Patterns that deal with object collaboration and responsibility distribution.

- ✅ [Chain of Responsibility](behavioral/behavioral_patterns.md#chain) - Pass request along chain of handlers
- ✅ [Command](behavioral/behavioral_patterns.md#command) - Encapsulate request as object
- ✅ [Iterator](behavioral/behavioral_patterns.md#iterator) - Access elements sequentially without exposing structure
- ✅ [Mediator](behavioral/behavioral_patterns.md#mediator) - Define object that encapsulates how set of objects interact
- ✅ [Memento](behavioral/behavioral_patterns.md#memento) - Capture and restore object's internal state
- ✅ [Observer](behavioral/behavioral_patterns.md#observer) - Notify multiple objects about state changes
- ✅ [State](behavioral/behavioral_patterns.md#state) - Allow object to alter behavior when state changes
- ✅ [Strategy](behavioral/behavioral_patterns.md#strategy) - Define interchangeable algorithms
- ✅ [Template Method](behavioral/behavioral_patterns.md#template) - Define skeleton of algorithm in base class
- ✅ [Visitor](behavioral/behavioral_patterns.md#visitor) - Represent operation to perform on elements
- ✅ [Interpreter](behavioral/behavioral_patterns.md#interpreter) - Define language and interpret its sentences

**📊 Completion Status: 23/23 Patterns Documented ✅**

All Gang of Four design patterns are now fully documented with:
- Detailed explanations and intent
- Structure diagrams
- Java 21 implementations using references and garbage collection
- Multiple usage examples
- Advantages and disadvantages
- When to use/avoid
- Related patterns
- Best practices and checklists

## Quick Reference

| Pattern | Type | Purpose | Complexity |
|---------|------|---------|-----------|
| Singleton | Creational | Single instance | ⭐ Easy |
| Factory Method | Creational | Object creation | ⭐⭐ Medium |
| Abstract Factory | Creational | Family creation | ⭐⭐⭐ Hard |
| Builder | Creational | Complex construction | ⭐⭐ Medium |
| Prototype | Creational | Clone objects | ⭐⭐ Medium |
| Adapter | Structural | Interface conversion | ⭐⭐ Medium |
| Bridge | Structural | Decouple abstraction | ⭐⭐⭐ Hard |
| Composite | Structural | Tree structures | ⭐⭐ Medium |
| Decorator | Structural | Add behavior | ⭐⭐ Medium |
| Facade | Structural | Unified interface | ⭐ Easy |
| Proxy | Structural | Surrogate object | ⭐⭐ Medium |
| Flyweight | Structural | Share objects | ⭐⭐⭐ Hard |
| Chain of Resp. | Behavioral | Request chain | ⭐⭐ Medium |
| Command | Behavioral | Encapsulate request | ⭐⭐ Medium |
| Iterator | Behavioral | Sequential access | ⭐⭐ Medium |
| Mediator | Behavioral | Centralize interaction | ⭐⭐⭐ Hard |
| Memento | Behavioral | Save/restore state | ⭐⭐ Medium |
| Observer | Behavioral | Notify dependents | ⭐⭐ Medium |
| State | Behavioral | Alter behavior | ⭐⭐ Medium |
| Strategy | Behavioral | Interchangeable algorithms | ⭐⭐ Medium |
| Template Method | Behavioral | Algorithm skeleton | ⭐ Easy |
| Visitor | Behavioral | Perform operations | ⭐⭐⭐ Hard |
| Interpreter | Behavioral | Language interpretation | ⭐⭐⭐ Hard |

## Learning Path

### Beginner (Start Here)
1. **Singleton** - Understand single instance control
2. **Factory Method** - Learn object creation
3. **Adapter** - Understand interface adaptation
4. **Strategy** - Learn algorithm switching
5. **Observer** - Understand event systems

### Intermediate
6. **Builder** - Complex object construction
7. **Decorator** - Dynamic behavior addition
8. **Facade** - Simplify complex subsystems
9. **Command** - Encapsulate requests
10. **State** - State-dependent behavior

### Advanced
11. **Abstract Factory** - Multiple factories
12. **Bridge** - Decouple abstraction
13. **Composite** - Tree structures
14. **Mediator** - Centralized communication
15. **Visitor** - Generic operations
16. **Chain of Responsibility** - Handler chains
17. **Template Method** - Algorithm structure
18. **Memento** - State snapshots
19. **Proxy** - Controlled access
20. **Prototype** - Cloning
21. **Flyweight** - Object pooling
22. **Iterator** - Element traversal
23. **Interpreter** - Language processing

## Directory Structure

```
design_patterns/
├── README.md (this file)
├── creational/
│   ├── singleton.md
│   ├── factory_method.md
│   ├── abstract_factory.md
│   ├── builder.md
│   └── prototype.md
├── structural/
│   ├── adapter.md
│   ├── bridge.md
│   ├── composite.md
│   ├── decorator.md
│   ├── facade.md
│   ├── proxy.md
│   └── flyweight.md
├── behavioral/
│   ├── chain_of_responsibility.md
│   ├── command.md
│   ├── iterator.md
│   ├── mediator.md
│   ├── memento.md
│   ├── observer.md
│   ├── state.md
│   ├── strategy.md
│   ├── template_method.md
│   ├── visitor.md
│   └── interpreter.md
└── code_examples/
    ├── CreationalPatterns.java
    ├── StructuralPatterns.java
    ├── BehavioralPatterns.java
    └── README.md
```

## How to Use This Guide

1. **Read the overview** - Understand pattern purpose and structure
2. **Study the UML diagram** - Visualize relationships
3. **Review code examples** - See implementation in Java 21
4. **Identify use cases** - Know when to apply
5. **Practice implementation** - Write your own version

## Key Benefits of Design Patterns

✅ **Reusability** - Solve recurring problems with proven solutions
✅ **Maintainability** - Well-structured, documented code
✅ **Readability** - Team understands standard solutions
✅ **Scalability** - Patterns support system growth
✅ **Flexibility** - Easy to modify and extend
✅ **Communication** - Common vocabulary with other developers

## Java 21 Features Used

- `interface` and `abstract class` for abstractions
- `enum` singletons for safe single-instance classes
- `record` types for immutable value objects
- Functional interfaces and lambdas for Strategy, Command, and Observer
- `var` for local type inference and clarity
- `@Override` annotation for override safety
- Enhanced `for` loops and streams
- Garbage collection instead of manual memory management
- Generics for type-safe containers and factories

## When to Use Design Patterns

### DO Use:
- When solving recurring design problems
- To communicate design to team members
- When you need proven, tested solutions
- For complex, multi-layered systems
- When maintainability is important

### DON'T Use:
- For simple, one-off solutions
- Just because it's a "pattern"
- When it adds unnecessary complexity
- To solve a different problem
- Without understanding why

## Common Pitfalls

⚠️ **Over-engineering** - Using patterns for simple code
⚠️ **Wrong pattern** - Applying pattern to wrong problem
⚠️ **Over-abstraction** - Too many layers of indirection
⚠️ **Tight coupling** - Not achieving decoupling goal
⚠️ **Performance** - Patterns may add overhead

## Best Practices

1. **Understand the pattern first** - Know its intent and structure
2. **Know your problem** - Match pattern to problem
3. **Keep it simple** - Don't over-engineer
4. **Document it** - Explain why you chose this pattern
5. **Test thoroughly** - Patterns don't guarantee correctness
6. **Refactor when needed** - Remove patterns that no longer serve

## Pattern Selection Guide

### If you need to...

**Create objects:**
- Single instance → Singleton
- Families of objects → Abstract Factory
- Specific subclass hidden → Factory Method
- Complex construction → Builder
- Copy existing object → Prototype

**Compose objects:**
- Into tree structure → Composite
- Add behavior dynamically → Decorator
- Provide simpler interface → Facade
- Decouple abstraction → Bridge
- Adapt incompatible interface → Adapter
- Share many similar objects → Flyweight
- Control access to another → Proxy

**Distribute responsibility:**
- Define algorithm skeleton → Template Method
- Encapsulate as object → Command
- Switch algorithms → Strategy
- Object changes behavior → State
- Notify dependents → Observer
- Multi-step chain → Chain of Responsibility
- Centralize interaction → Mediator
- Save/restore state → Memento
- Traversal without exposing → Iterator
- Generic operations → Visitor
- Parse languages → Interpreter

## References

- Gang of Four: "Design Patterns: Elements of Reusable Object-Oriented Software"
- Modern Java Standards (Java 21 and later)
- Professional software architecture practices

## Getting Started

1. Start with **Creational Patterns** folder for basic patterns
2. Move to **Structural Patterns** for composition
3. Advance to **Behavioral Patterns** for complex interactions
4. Review **code_examples/** for runnable Java 21 implementations
5. Practice each pattern with your own variations

---

**Author's Note:** Master these patterns, and you'll write better, more maintainable code. Understand why each exists, and you'll know when to apply them wisely.

## Java 21 Pattern Example (Strategy)

```java
interface SortStrategy {
    void run();
}

final class QuickSortStrategy implements SortStrategy {
    @Override
    public void run() {
        System.out.println("Quick sort strategy");
    }
}

final class Context {
    private final SortStrategy strategy;

    public Context(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute() {
        strategy.run();
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        // Classic OOP form
        Context ctx = new Context(new QuickSortStrategy());
        ctx.execute();

        // Functional form: a SortStrategy is a functional interface,
        // so a lambda works just as well.
        Context lambdaCtx = new Context(() -> System.out.println("Quick sort strategy"));
        lambdaCtx.execute();
    }
}
```
