# Behavioral Patterns

Behavioral patterns deal with object collaboration and the delegation of responsibility. They focus on how objects interact and distribute work.

---

# Observer Pattern

## Overview

The Observer pattern defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified automatically.

## Intent

- Define loose coupling between subjects and observers
- Notify multiple objects of state changes
- Update dependents automatically
- Create event systems

## Java 21 Implementation

```java
// Observer interface
interface Observer {
    void update(String message);
}

// Subject
class Subject {
    private final List<Observer> observers = new ArrayList<>();
    private String state;

    public void attach(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (var obs : observers) {
            obs.update(state);
        }
    }

    public void setState(String newState) {
        state = newState;
        notifyObservers();
    }
}

// Concrete observers
class ConsoleObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("Console: " + message);
    }
}

class LoggerObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[LOG] " + message);
    }
}

// Usage
var subject = new Subject();
var console = new ConsoleObserver();
var logger = new LoggerObserver();

subject.attach(console);
subject.attach(logger);

subject.setState("Temperature changed");
// Output:
// Console: Temperature changed
// [LOG] Temperature changed
```

> **Java idiom:** `Observer` is a functional interface (one abstract method),
> so observers can also be registered as lambdas — no concrete class needed.
> Avoid the deprecated `java.util.Observer`/`java.util.Observable`; roll your
> own functional interface as above.
>
> ```java
> subject.attach(msg -> System.out.println("Console: " + msg));
> subject.attach(msg -> System.out.println("[LOG] " + msg));
> ```

## When to Use

✅ Multiple objects need to react to state changes
✅ Loosely coupled event systems
✅ MVC architectures
✅ Real-time data updates
✅ Event-driven programming

---

# Strategy Pattern

## Overview

The Strategy pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable.

## Java 21 Implementation

```java
// Strategy interface
interface PaymentStrategy {
    void pay(double amount);
}

// Concrete strategies
class CreditCardStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paying $" + amount + " by credit card");
    }
}

class PayPalStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paying $" + amount + " via PayPal");
    }
}

class CryptoStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paying $" + amount + " in cryptocurrency");
    }
}

// Context
class ShoppingCart {
    private PaymentStrategy strategy;

    public void setPaymentStrategy(PaymentStrategy s) {
        strategy = s;
    }

    public void checkout(double total) {
        if (strategy != null) strategy.pay(total);
    }
}

// Usage
var cart = new ShoppingCart();

cart.setPaymentStrategy(new CreditCardStrategy());
cart.checkout(99.99);

cart.setPaymentStrategy(new PayPalStrategy());
cart.checkout(49.99);

cart.setPaymentStrategy(new CryptoStrategy());
cart.checkout(25.00);
```

> **Java idiom:** `PaymentStrategy` has a single abstract method, so it is a
> functional interface. Simple strategies can be passed as lambdas instead of
> declaring a class:
>
> ```java
> cart.setPaymentStrategy(amount ->
>     System.out.println("Paying $" + amount + " by gift card"));
> cart.checkout(10.00);
> ```

## When to Use

✅ Multiple algorithms for a task
✅ Switch algorithms at runtime
✅ Avoid conditional statements
✅ Families of related algorithms
✅ Testing different implementations

---

# Command Pattern

## Overview

The Command pattern encapsulates a request as an object, thereby letting you parameterize clients with different requests, queue requests, and log requests.

## Java 21 Implementation

```java
// Command interface
interface Command {
    void execute();
    void undo();
}

// Receiver
class Light {
    private boolean isOn = false;

    public void on() {
        isOn = true;
        System.out.println("Light is ON");
    }

    public void off() {
        isOn = false;
        System.out.println("Light is OFF");
    }
}

// Concrete commands
class TurnOnCommand implements Command {
    private final Light light;

    public TurnOnCommand(Light light) { this.light = light; }

    @Override
    public void execute() { light.on(); }

    @Override
    public void undo() { light.off(); }
}

class TurnOffCommand implements Command {
    private final Light light;

    public TurnOffCommand(Light light) { this.light = light; }

    @Override
    public void execute() { light.off(); }

    @Override
    public void undo() { light.on(); }
}

// Invoker
class RemoteControl {
    private final Deque<Command> commandHistory = new ArrayDeque<>();

    public void executeCommand(Command cmd) {
        cmd.execute();
        commandHistory.push(cmd);
    }

    public void undo() {
        if (!commandHistory.isEmpty()) {
            commandHistory.pop().undo();
        }
    }
}

// Usage
var light = new Light();
var remote = new RemoteControl();

remote.executeCommand(new TurnOnCommand(light));   // Light is ON
remote.executeCommand(new TurnOffCommand(light));  // Light is OFF
remote.undo();                                     // Light is ON
```

> **Java idiom:** For fire-and-forget commands with no `undo`, a functional
> `Command` interface (or the built-in `Runnable`) lets you queue behavior as
> lambdas:
>
> ```java
> @FunctionalInterface
> interface SimpleCommand { void execute(); }
>
> List<SimpleCommand> queue = new ArrayList<>();
> queue.add(() -> System.out.println("Lights on"));
> queue.add(() -> System.out.println("Doors locked"));
> queue.forEach(SimpleCommand::execute);
> ```

---

# State Pattern

## Overview

The State pattern allows an object to alter its behavior when its internal state changes.

## Java 21 Implementation

```java
// State interface
interface State {
    void handle(TrafficLight light);
}

// Concrete states
class RedState implements State {
    @Override
    public void handle(TrafficLight light) {
        System.out.println("Red -> Green");
        light.setState(new GreenState());
    }
}

class GreenState implements State {
    @Override
    public void handle(TrafficLight light) {
        System.out.println("Green -> Yellow");
        light.setState(new YellowState());
    }
}

class YellowState implements State {
    @Override
    public void handle(TrafficLight light) {
        System.out.println("Yellow -> Red");
        light.setState(new RedState());
    }
}

// Context
class TrafficLight {
    private State currentState;

    public TrafficLight() {
        this.currentState = new RedState();
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public void change() {
        currentState.handle(this);
    }
}
```

---

# Template Method Pattern

## Overview

The Template Method pattern defines the skeleton of an algorithm in a method, deferring some steps to subclasses.

## Java 21 Implementation

```java
abstract class DataProcessor {

    // Template method - defines algorithm structure
    public final void process() {
        readData();
        validate();
        transform();
        save();
    }

    protected abstract void readData();
    protected abstract void validate();
    protected abstract void transform();
    protected abstract void save();
}

// Concrete implementations
class CSVProcessor extends DataProcessor {
    @Override
    protected void readData() { System.out.println("Reading CSV..."); }

    @Override
    protected void validate() { System.out.println("Validating CSV..."); }

    @Override
    protected void transform() { System.out.println("Transforming CSV..."); }

    @Override
    protected void save() { System.out.println("Saving CSV..."); }
}

class JSONProcessor extends DataProcessor {
    @Override
    protected void readData() { System.out.println("Reading JSON..."); }

    @Override
    protected void validate() { System.out.println("Validating JSON..."); }

    @Override
    protected void transform() { System.out.println("Transforming JSON..."); }

    @Override
    protected void save() { System.out.println("Saving JSON..."); }
}

// Usage
DataProcessor csv = new CSVProcessor();
csv.process();  // Calls all steps in order
```

---

# Chain of Responsibility Pattern

## Overview

The Chain of Responsibility pattern passes a request along a chain of handlers where each handler decides to process it or pass it along.

## Java 21 Implementation

```java
class Request {
    public final int level;
    public final String message;

    public Request(int level, String message) {
        this.level = level;
        this.message = message;
    }
}

class Handler {
    protected Handler next;

    public void setNext(Handler n) { this.next = n; }

    public void handle(Request request) {
        if (next != null) next.handle(request);
    }
}

class ConsoleHandler extends Handler {
    @Override
    public void handle(Request request) {
        if (request.level <= 2) {
            System.out.println("Console: " + request.message);
        } else {
            super.handle(request);
        }
    }
}

class FileHandler extends Handler {
    @Override
    public void handle(Request request) {
        if (request.level <= 5) {
            System.out.println("File: " + request.message);
        } else {
            super.handle(request);
        }
    }
}

class DatabaseHandler extends Handler {
    @Override
    public void handle(Request request) {
        System.out.println("Database: " + request.message);
    }
}

// Usage
var chain = new ConsoleHandler();
var fileHandler = new FileHandler();
var dbHandler = new DatabaseHandler();
chain.setNext(fileHandler);
fileHandler.setNext(dbHandler);

var req1 = new Request(1, "Low priority");
var req2 = new Request(3, "Medium priority");
var req3 = new Request(10, "High priority");

chain.handle(req1);
chain.handle(req2);
chain.handle(req3);
```

---

# Iterator Pattern

## Overview

The Iterator pattern provides a way to access elements of a collection sequentially without exposing its underlying representation.

## Java 21 Implementation

Java already models iteration with `java.util.Iterator<T>` and `Iterable<T>`.
Implementing them lets the collection participate in the enhanced for loop.

```java
import java.util.Iterator;

class ArrayIterator implements Iterator<Integer> {
    private final List<Integer> items;
    private int index = 0;

    public ArrayIterator(List<Integer> items) {
        this.items = items;
    }

    @Override
    public boolean hasNext() { return index < items.size(); }

    @Override
    public Integer next() { return items.get(index++); }
}

class ArrayCollection implements Iterable<Integer> {
    private final List<Integer> items = new ArrayList<>();

    public void add(int item) { items.add(item); }

    @Override
    public Iterator<Integer> iterator() {
        return new ArrayIterator(items);
    }
}

// Usage
var collection = new ArrayCollection();
collection.add(1);
collection.add(2);
collection.add(3);

Iterator<Integer> it = collection.iterator();
while (it.hasNext()) {
    System.out.print(it.next() + " ");
}

// Because ArrayCollection is Iterable, the enhanced for loop also works:
for (var value : collection) {
    System.out.print(value + " ");
}
```

---

# Memento Pattern

## Overview

The Memento pattern captures and externalizes an object's internal state without violating encapsulation.

## Java 21 Implementation

The immutable snapshot is a natural fit for a `record`.

```java
// Memento snapshot
record Memento(String state) {}

class TextEditor {
    private String content = "";

    public void type(String text) { content += text; }

    public String getContent() { return content; }

    public Memento save() { return new Memento(content); }

    public void restore(Memento m) { content = m.state(); }
}

class History {
    private final Deque<Memento> mementos = new ArrayDeque<>();

    public void push(Memento m) { mementos.push(m); }

    public Memento pop() {
        if (!mementos.isEmpty()) {
            return mementos.pop();
        }
        return new Memento("");
    }
}

// Usage
var editor = new TextEditor();
var history = new History();

editor.type("Hello");
history.push(editor.save());

editor.type(" World");
System.out.println(editor.getContent());  // Hello World

editor.restore(history.pop());
System.out.println(editor.getContent());  // Hello
```

---

# Visitor Pattern

## Overview

The Visitor pattern represents an operation to be performed on elements of an object structure, allowing you to define new operations without changing the classes of the elements.

## Java 21 Implementation

```java
interface Visitor {
    void visitA(ConcreteElementA e);
    void visitB(ConcreteElementB e);
}

interface Element {
    void accept(Visitor v);
}

class ConcreteElementA implements Element {
    @Override
    public void accept(Visitor v) { v.visitA(this); }
}

class ConcreteElementB implements Element {
    @Override
    public void accept(Visitor v) { v.visitB(this); }
}

class ConcreteVisitor implements Visitor {
    @Override
    public void visitA(ConcreteElementA e) {
        System.out.println("Visiting A");
    }

    @Override
    public void visitB(ConcreteElementB e) {
        System.out.println("Visiting B");
    }
}

// Usage
List<Element> elements = new ArrayList<>();
elements.add(new ConcreteElementA());
elements.add(new ConcreteElementB());

var visitor = new ConcreteVisitor();
for (var elem : elements) {
    elem.accept(visitor);
}
```

---

# Mediator Pattern

## Overview

The Mediator pattern defines an object that encapsulates how a set of objects interact.

## Java 21 Implementation

```java
interface Mediator {
    void send(String message, Colleague colleague);
}

abstract class Colleague {
    protected final Mediator mediator;

    protected Colleague(Mediator m) {
        this.mediator = m;
    }

    public abstract void send(String message);
    public abstract void receive(String message);
}

class ConcreteColleague extends Colleague {
    private final String name;

    public ConcreteColleague(String name, Mediator m) {
        super(m);
        this.name = name;
    }

    @Override
    public void send(String message) {
        mediator.send(message, this);
    }

    @Override
    public void receive(String message) {
        System.out.println(name + " received: " + message);
    }
}

class ConcreteMediator implements Mediator {
    private final List<Colleague> colleagues = new ArrayList<>();

    public void addColleague(Colleague c) {
        colleagues.add(c);
    }

    @Override
    public void send(String message, Colleague sender) {
        for (var c : colleagues) {
            if (c != sender) {
                c.receive(message);
            }
        }
    }
}
```

---

# Interpreter Pattern

## Overview

The Interpreter pattern defines a representation for a grammar and an interpreter to interpret sentences in that language.

## Java 21 Implementation

```java
interface Expression {
    int interpret();
}

class NumberExpression implements Expression {
    private final int number;

    public NumberExpression(int n) { this.number = n; }

    @Override
    public int interpret() { return number; }
}

class PlusExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public PlusExpression(Expression l, Expression r) {
        this.left = l;
        this.right = r;
    }

    @Override
    public int interpret() {
        return left.interpret() + right.interpret();
    }
}

class MinusExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public MinusExpression(Expression l, Expression r) {
        this.left = l;
        this.right = r;
    }

    @Override
    public int interpret() {
        return left.interpret() - right.interpret();
    }
}

// Usage
Expression expr = new PlusExpression(
    new NumberExpression(5),
    new MinusExpression(
        new NumberExpression(10),
        new NumberExpression(2)
    )
);

System.out.println("Result: " + expr.interpret());  // 5 + (10 - 2) = 13
```

---

All behavioral patterns focus on object interaction and responsibility distribution, helping create flexible and maintainable code.
