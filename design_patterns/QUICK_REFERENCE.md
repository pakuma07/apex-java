# Design Patterns Summary & Quick Reference

A quick lookup guide for all 23 Gang of Four design patterns.

## Pattern Categories at a Glance

| Type | Patterns | Purpose | Count |
|------|----------|---------|-------|
| **Creational** | Singleton, Factory, Abstract Factory, Builder, Prototype | Object creation | 5 |
| **Structural** | Adapter, Bridge, Composite, Decorator, Facade, Proxy, Flyweight | Object composition | 7 |
| **Behavioral** | Observer, Strategy, Command, State, Template, Chain, Iterator, Memento, Visitor, Mediator, Interpreter | Object interaction | 11 |

## The 23 Design Patterns

### CREATIONAL PATTERNS (Object Creation)

#### 1. Singleton ⭐ EASIEST
**Problem**: Need exactly one instance
**Solution**: Enum singleton, or private constructor + static getter
**Example**: Logger, Configuration manager
```java
enum Logger { INSTANCE; public void log(String m) { System.out.println(m); } }
```
**When**: Global resource that must be unique
**Avoid**: Global variables (use dependency injection instead)

#### 2. Factory Method ⭐⭐ MEDIUM
**Problem**: Create objects without knowing concrete classes
**Solution**: Abstract factory method in base class, implement in subclasses
**Example**: GUI components on different platforms
```java
Shape s = factory.createShape("circle");
```
**When**: Multiple implementations, runtime selection needed
**Avoid**: For simple single-type creation

#### 3. Abstract Factory ⭐⭐⭐ HARD
**Problem**: Create families of related objects
**Solution**: Factory interface for each product family
**Example**: UI components (Windows, Mac, Linux)
```java
var button = windowsFactory.createButton();
var checkbox = windowsFactory.createCheckbox();
```
**When**: Multiple product families, consistency required
**Avoid**: Single product family

#### 4. Builder ⭐⭐ MEDIUM
**Problem**: Complex object with many optional parameters
**Solution**: Separate builder class, fluent interface
**Example**: HTML document, HTTP request
```java
House h = new HouseBuilder().windows("glass").doors("wooden").build();
```
**When**: Complex objects, many optional fields
**Avoid**: Simple objects (use constructor instead)

#### 5. Prototype ⭐⭐ MEDIUM
**Problem**: Expensive object creation
**Solution**: Clone existing objects instead
**Example**: Document templates, configuration cloning
```java
Shape clone = original.clone();
```
**When**: Creation is expensive, need variations
**Avoid**: Shallow copy issues, circular references

### STRUCTURAL PATTERNS (Object Composition)

#### 6. Adapter ⭐⭐ MEDIUM
**Problem**: Incompatible interfaces need to work together
**Solution**: Wrapper class converting interfaces
**Example**: Legacy code integration, different libraries
```java
class VLCAdapter implements MediaPlayer { /* adapts VLCPlayer */ }
```
**When**: Integrating incompatible code, third-party libraries
**Avoid**: When interfaces are compatible

#### 7. Bridge ⭐⭐⭐ HARD
**Problem**: Decouple abstraction from implementation
**Solution**: Two hierarchies: abstract and implementation
**Example**: Window abstraction on different OS
**When**: Multiple implementations, abstraction independence
**Avoid**: Simple cases (Adapter may be better)

#### 8. Composite ⭐⭐ MEDIUM
**Problem**: Treat individual and composite objects uniformly
**Solution**: Tree structure with uniform interface
**Example**: File system, UI component hierarchy
```java
dir.add(file); dir.add(subdir);
```
**When**: Tree structures, hierarchical composition
**Avoid**: Non-hierarchical data

#### 9. Decorator ⭐⭐ MEDIUM
**Problem**: Add responsibilities dynamically
**Solution**: Wrapper class implementing same interface
**Example**: Coffee with toppings, UI with effects
```java
Coffee coffee = new MilkDecorator(new SimpleCoffee());
```
**When**: Dynamic feature addition, avoid subclass explosion
**Avoid**: Fixed set of combinations (subclasses better)

#### 10. Facade ⭐ EASIEST
**Problem**: Complex subsystem too hard to use
**Solution**: Unified simple interface to subsystem
**Example**: Library API, application startup
```java
app.getUserData(id); // Hides DB, cache, logging
```
**When**: Simplify complex interactions
**Avoid**: When direct access needed

#### 11. Proxy ⭐⭐ MEDIUM
**Problem**: Control access to another object
**Solution**: Surrogate implementing same interface
**Example**: Lazy loading, remote objects, protection
```java
class ProxyImage implements Image { /* controls RealImage */ }
```
**When**: Lazy creation, remote access, protection
**Avoid**: When no control needed

#### 12. Flyweight ⭐⭐⭐ HARD
**Problem**: Too many similar objects consume memory
**Solution**: Share common state across objects
**Example**: Character objects, game particles
**When**: Millions of similar objects
**Avoid**: Few objects (unnecessary complexity)

### BEHAVIORAL PATTERNS (Object Interaction)

#### 13. Observer ⭐⭐ MEDIUM
**Problem**: Many objects need updates
**Solution**: Subject notifies observers of changes
**Example**: Event systems, MVC, data binding
```java
subject.attach(observer); subject.setState(newState);
```
**When**: Many dependents, loose coupling needed
**Avoid**: Tightly coupled systems

#### 14. Strategy ⭐⭐ MEDIUM
**Problem**: Multiple algorithms for same task
**Solution**: Encapsulate algorithms, make interchangeable (lambdas work great)
**Example**: Sorting algorithms, payment methods
```java
context.setStrategy(new CreditCard());
```
**When**: Selectable algorithms, avoid conditionals
**Avoid**: Single algorithm (direct class better)

#### 15. Command ⭐⭐ MEDIUM
**Problem**: Encapsulate requests, undo/redo, queuing
**Solution**: Encapsulate request as object (or lambda)
**Example**: UI buttons, macro recording, transaction logs
```java
invoker.executeCommand(new TurnOnCommand());
```
**When**: Parameterize actions, undo/redo, queuing
**Avoid**: Simple calls (a `Runnable` lambda is sufficient)

#### 16. State ⭐⭐ MEDIUM
**Problem**: Behavior changes with internal state
**Solution**: State object encapsulates behavior
**Example**: Traffic light, workflow, player states
```java
light.setState(new GreenState());
```
**When**: Behavior depends on state, many conditions
**Avoid**: Few states (simple if/switch better)

#### 17. Template Method ⭐ EASIEST
**Problem**: Define algorithm structure, vary steps
**Solution**: Base class defines template, subclasses implement steps
**Example**: Data processing, game loop, test fixtures
```java
final void process() { read(); validate(); save(); } // template
```
**When**: Common algorithm, variable steps
**Avoid**: No variation between subclasses

#### 18. Chain of Responsibility ⭐⭐ MEDIUM
**Problem**: Pass request through chain of handlers
**Solution**: Handlers linked, each decides to handle or pass
**Example**: Logging levels, request filtering, approval chains
**When**: Variable handlers, unknown handlers
**Avoid**: Single handler (direct call better)

#### 19. Iterator ⭐⭐ MEDIUM
**Problem**: Access collection elements without exposing structure
**Solution**: Implement `Iterator<T>` / `Iterable<T>` for sequential access
**Example**: Container iteration, different traversals
**When**: Hide collection internals, multiple traversals
**Avoid**: Simple containers (enhanced for sufficient)

#### 20. Memento ⭐⭐ MEDIUM
**Problem**: Capture object state for later restoration
**Solution**: Memento object (a `record`) stores snapshot
**Example**: Undo/redo, transaction rollback, save points
```java
history.push(editor.save());
editor.restore(history.pop());
```
**When**: Undo/redo, checkpoints, versioning
**Avoid**: When history not needed

#### 21. Visitor ⭐⭐⭐ HARD
**Problem**: Perform operations on object structure without changing classes
**Solution**: Visitor object encapsulates operations
**Example**: Compilers, document processing, simulations
**When**: Many operations on complex structures
**Avoid**: Few operations (methods better)

#### 22. Mediator ⭐⭐⭐ HARD
**Problem**: Many interacting objects with complex communication
**Solution**: Mediator encapsulates interaction logic
**Example**: GUI dialogs, Air Traffic Control, Chat Room
**When**: Complex object interactions, coupling reduction
**Avoid**: Simple interactions (direct better)

#### 23. Interpreter ⭐⭐⭐ HARD
**Problem**: Define language grammar and interpret sentences
**Solution**: Expression classes for grammar rules
**Example**: Expression parsers, query languages, DSLs
**When**: Domain-specific languages, expression evaluation
**Avoid**: Complex languages (parser generators better)

---

## Quick Decision Matrix

### Choose Based On Problem

| Problem | Pattern | Priority |
|---------|---------|----------|
| Only one instance needed | Singleton | 🔴 High |
| Hide concrete class creation | Factory Method | 🔴 High |
| Complex object construction | Builder | 🔴 High |
| Incompatible interfaces | Adapter | 🟠 Medium |
| Dynamic features | Decorator | 🟠 Medium |
| Simplify complex subsystem | Facade | 🟠 Medium |
| Many algorithm variants | Strategy | 🔴 High |
| Many dependent objects | Observer | 🔴 High |
| Object behavior by state | State | 🟠 Medium |
| Tree structure | Composite | 🟠 Medium |
| Undo/Redo functionality | Command/Memento | 🟠 Medium |
| Chain processing | Chain of Responsibility | 🟡 Low |
| Sequential access | Iterator | 🟡 Low |
| Generic operations | Visitor | 🟡 Low |

---

## Pattern Relationships

**Creational patterns often work together:**
- Factory Method may use Singleton
- Abstract Factory often uses Factory Methods
- Builder separates from Factory for complex objects
- Prototype clones instead of creating new

**Structural patterns combine:**
- Decorator adds to Component (multiple)
- Adapter converts between interfaces
- Facade simplifies Composite structures
- Proxy controls access like Decorator

**Behavioral patterns coordinate:**
- Strategy replaces algorithms
- Command queues requests
- State encapsulates behavior
- Observer notifies of changes
- Mediator coordinates interactions

---

## Implementation Speed

| Time | Patterns | Count |
|------|----------|-------|
| < 30 min | Singleton, Factory, Facade, Strategy, Template Method | 5 |
| 30-60 min | Builder, Adapter, Decorator, Observer, Command, State | 6 |
| 60-120 min | Abstract Factory, Bridge, Composite, Proxy, Chain, Iterator | 6 |
| 120+ min | Prototype, Flyweight, Memento, Visitor, Mediator, Interpreter | 6 |

---

## Learning Difficulty

| Difficulty | Patterns | Count |
|-----------|----------|-------|
| ⭐ Easy | Singleton, Facade, Template Method | 3 |
| ⭐⭐ Medium | Factory, Builder, Adapter, Decorator, Composite, Strategy, Observer, Command, State, Iterator, Memento, Proxy, Chain | 13 |
| ⭐⭐⭐ Hard | Abstract Factory, Bridge, Prototype, Flyweight, Visitor, Mediator, Interpreter | 7 |

---

## Java 21 Features Used

All patterns use:
- ✅ `interface` and `abstract class` (abstractions)
- ✅ `enum` singletons (single instance)
- ✅ `record` types (immutable value objects)
- ✅ `var` (local type inference)
- ✅ `@Override` (override safety)
- ✅ Functional interfaces and lambdas (callbacks)
- ✅ Enhanced for loops and streams (iteration)
- ✅ Garbage collection (automatic memory management)
- ✅ `switch` expressions (concise branching)

---

## Testing Patterns

Each pattern has a recommended test strategy:

- **Creational**: Test object creation, single instance
- **Structural**: Test composition, interface compatibility
- **Behavioral**: Test state transitions, interactions

---

## Common Mistakes

- ❌ Using patterns for simple code (YAGNI)
- ❌ Not understanding the problem first
- ❌ Over-abstraction (unnecessary complexity)
- ❌ Mixing pattern concerns
- ❌ Not following SOLID principles
- ❌ Ignoring performance implications
- ❌ Tight coupling despite patterns

---

## Quick Reference Commands

```bash
# Compile pattern examples
javac Pattern.java

# Run example
java Pattern

# Study a pattern
cat creational/singleton.md

# Quick lookup
grep -r "When to use" .
```

---

## Related Reading

- Gang of Four: "Design Patterns" (original book)
- Refactoring Guru website
- Your own codebase (recognize patterns)
- Open source projects
- Code reviews

---

## Practice Recommendations

1. **Implement each pattern** - Write from scratch
2. **Combine patterns** - Use together in projects
3. **Recognize patterns** - In existing code
4. **Refactor using patterns** - Improve old code
5. **Teach others** - Explain what you learned

---

For detailed information, see individual pattern documentation in README.md and respective folders.

**Master these patterns, and you'll write better, more maintainable code!**
