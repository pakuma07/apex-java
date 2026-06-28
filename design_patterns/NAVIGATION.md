# Design Patterns Navigation Guide

Welcome to the comprehensive Design Patterns guide! This folder contains detailed documentation and examples for all 23 Gang of Four design patterns.

## Quick Start

1. **New to patterns?** Start with [README.md](README.md) for overview
2. **Want examples?** Check [code_examples/README.md](code_examples/README.md)
3. **Learning path?** Follow beginner → intermediate → advanced in main README

## Folder Structure

```
design_patterns/
├── README.md ⭐ START HERE - Overview & learning paths
├── code_examples/ - Runnable Java 21 examples
│   └── README.md - All code examples
├── creational/ - Object creation patterns
│   ├── singleton.md
│   ├── factory_method.md
│   ├── abstract_factory.md
│   ├── builder.md
│   └── prototype.md
├── structural/ - Object composition patterns
│   └── structural_patterns.md (covers all 7)
│       ├── Adapter - Interface conversion
│       ├── Bridge - Decouple abstraction
│       ├── Composite - Tree structures
│       ├── Decorator - Dynamic responsibilities
│       ├── Facade - Unified interface
│       ├── Proxy - Controlled access
│       └── Flyweight - Object sharing
└── behavioral/ - Object interaction patterns
    └── behavioral_patterns.md (covers all 11)
        ├── Observer - Event systems
        ├── Strategy - Interchangeable algorithms
        ├── Command - Encapsulate requests
        ├── State - State-dependent behavior
        ├── Template Method - Algorithm skeleton
        ├── Chain of Responsibility - Handler chains
        ├── Iterator - Sequential access
        ├── Memento - State snapshots
        ├── Visitor - Generic operations
        ├── Mediator - Centralized communication
        └── Interpreter - Language processing
```

## Documentation Quality

Each pattern documentation includes:

✅ **Overview** - What it is and why it matters
✅ **Intent** - What problem it solves
✅ **Structure** - UML-style diagrams
✅ **Java 21 Implementation** - Complete, runnable code
✅ **Usage Examples** - Real-world scenarios
✅ **Advantages** - When it helps
✅ **Disadvantages** - Trade-offs
✅ **When to Use** - Decision guide
✅ **When NOT to Use** - Avoid over-engineering
✅ **Related Patterns** - How patterns work together
✅ **Best Practices** - Implementation tips
✅ **Implementation Checklist** - Verification steps

## Learning Paths

### Path 1: Beginner (5 patterns)
For those new to design patterns:

1. **Singleton** (`creational/singleton.md`)
   - Understand single instance control
   - Thread safety basics

2. **Factory Method** (`creational/factory_method.md`)
   - Object creation abstraction
   - Loose coupling

3. **Strategy** (`behavioral/behavioral_patterns.md#strategy`)
   - Algorithm switching
   - Simple behavior variation

4. **Observer** (`behavioral/behavioral_patterns.md#observer`)
   - Event systems
   - Loose coupling with updates

5. **Builder** (`creational/builder.md`)
   - Complex object construction
   - Method chaining

### Path 2: Intermediate (10 patterns)
Build on basics with composition:

6. **Adapter** (`structural/structural_patterns.md#adapter`)
7. **Decorator** (`structural/structural_patterns.md#decorator`)
8. **Command** (`behavioral/behavioral_patterns.md#command`)
9. **Composite** (`structural/structural_patterns.md#composite`)
10. **Facade** (`structural/structural_patterns.md#facade`)
11. **State** (`behavioral/behavioral_patterns.md#state`)
12. **Template Method** (`behavioral/behavioral_patterns.md#template`)
13. **Abstract Factory** (`creational/abstract_factory.md`)
14. **Prototype** (`creational/prototype.md`)
15. **Iterator** (`behavioral/behavioral_patterns.md#iterator`)

### Path 3: Advanced (8 patterns)
Master complex patterns:

16. **Bridge** (`structural/structural_patterns.md#bridge`)
17. **Chain of Responsibility** (`behavioral/behavioral_patterns.md#chain`)
18. **Mediator** (`behavioral/behavioral_patterns.md#mediator`)
19. **Memento** (`behavioral/behavioral_patterns.md#memento`)
20. **Proxy** (`structural/structural_patterns.md#proxy`)
21. **Visitor** (`behavioral/behavioral_patterns.md#visitor`)
22. **Flyweight** (`structural/structural_patterns.md#flyweight`)
23. **Interpreter** (`behavioral/behavioral_patterns.md#interpreter`)

## Pattern Selection Quick Reference

### "I need to create objects..."

- **Single instance** → Singleton
- **Hide concrete class** → Factory Method
- **Families of objects** → Abstract Factory
- **Complex construction** → Builder
- **Clone objects** → Prototype

### "I need to compose objects..."

- **Into tree structure** → Composite
- **Add behaviors dynamically** → Decorator
- **Provide simple interface** → Facade
- **Decouple abstraction** → Bridge
- **Adapt incompatible interface** → Adapter
- **Control access** → Proxy
- **Share similar objects** → Flyweight

### "I need objects to interact..."

- **Define algorithm structure** → Template Method
- **Encapsulate requests** → Command
- **Switch algorithms** → Strategy
- **Behavior depends on state** → State
- **Notify multiple objects** → Observer
- **Handle request chain** → Chain of Responsibility
- **Sequential access** → Iterator
- **Save/restore state** → Memento
- **Generic operations** → Visitor
- **Centralize interaction** → Mediator
- **Parse languages** → Interpreter

## Using Code Examples

All examples in `code_examples/README.md` are:
- ✅ Complete and runnable
- ✅ Java 21 compliant
- ✅ Self-contained
- ✅ Well-commented
- ✅ Best-practice implementations

Compile and run:
```bash
# Navigate to code_examples directory
cd code_examples

# Compile any example (replace with actual example)
javac Example.java

# Run
java Example
```

## Key Java 21 Features Used

Throughout all patterns:
- `interface` and `abstract class` for abstractions
- `enum` singletons for safe single-instance classes
- `record` types for immutable value objects
- `var` for local type inference
- `@Override` annotation for safety
- Lambdas and functional interfaces for callbacks
- Garbage collection instead of manual memory management
- Enhanced for loops and streams

## Common Pattern Combinations

Patterns work well together:

- **Abstract Factory + Factory Method** - Flexible family creation
- **Composite + Iterator** - Traverse tree structures
- **Strategy + Factory** - Create algorithms
- **Decorator + Factory** - Create decorated objects
- **Observer + Mediator** - Complex communication
- **Template Method + Strategy** - Flexible algorithms
- **Singleton + Factory** - Single creation point
- **Command + Memento** - Undo/redo systems

## Anti-Patterns to Avoid

❌ **Using patterns for trivial problems** - Add complexity unnecessarily
❌ **Not understanding why** - Using pattern because it's called a pattern
❌ **Over-abstraction** - Too many layers between intent and execution
❌ **Mixing concerns** - Single class doing too much
❌ **Tight coupling** - Despite patterns being about decoupling
❌ **Premature optimization** - Using patterns before problems exist

## Design Principles

All patterns follow SOLID principles:

- **S** - Single Responsibility: Each class has one reason to change
- **O** - Open/Closed: Open for extension, closed for modification
- **L** - Liskov Substitution: Subtypes replaceable for base types
- **I** - Interface Segregation: Many specific interfaces over general
- **D** - Dependency Inversion: Depend on abstractions, not concretions

## When to Refer Back

✓ Before writing complex code - "Is there a pattern for this?"
✓ During code review - "Could this be clearer with a pattern?"
✓ When refactoring - "What pattern would improve this design?"
✓ When learning - "How do experts solve this problem?"
✓ When communicating - "Let's use the Strategy pattern here"

## Recommended Study Order

1. **Week 1**: Creational patterns (understand object creation)
2. **Week 2**: Structural patterns (understand composition)
3. **Week 3**: Behavioral patterns (understand interaction)
4. **Week 4**: Review and practice (implement your own)
5. **Ongoing**: Recognize patterns in existing code

## Practice Exercises

For each pattern:
1. Read the documentation
2. Understand the problem it solves
3. Study the Java implementation
4. Write your own version
5. Combine with other patterns
6. Refactor existing code using it

## Resources in This Guide

- 📖 23 complete pattern documentations
- 💻 Multiple Java examples per pattern
- 📊 UML-style structure diagrams
- ✅ Implementation checklists
- 🎯 Usage decision guides
- 🔗 Pattern relationship maps
- 💡 Real-world examples

## Getting Help

If a pattern is unclear:
1. Read "Intent" section
2. Review "Problem" section
3. Study the Java code example
4. Check "When to Use" section
5. Review related patterns
6. Try implementing it yourself

## Contributing Improvements

This guide is designed to be:
- Clear and practical
- Java 21 focused
- Example-rich
- Easy to navigate
- Quick to reference

## Summary

Design patterns are:
- ✅ Proven solutions to common problems
- ✅ Tools for better communication
- ✅ Foundation for good design
- ✅ Subject for continued learning
- ❌ NOT a silver bullet
- ❌ NOT for every problem
- ❌ NOT a substitute for thinking

**Start with the main [README.md](README.md), choose your learning path, and begin!**

---

*Last Updated: 2024*
*Java 21 Standard throughout*
*23 Gang of Four Patterns Covered*

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
        Context ctx = new Context(new QuickSortStrategy());
        ctx.execute();
    }
}
```
