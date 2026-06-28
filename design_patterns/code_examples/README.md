# Design Patterns Code Examples

Complete, runnable Java 21 examples for all major design patterns.

## Compilation

Compile individual examples with:
```bash
javac ExampleName.java
java ExampleName
```

## Table of Contents

1. **Creational Patterns** - Object creation
2. **Structural Patterns** - Object composition
3. **Behavioral Patterns** - Object interaction

---

## CREATIONAL PATTERNS

### Singleton Example

```java
// Enum singleton: the JVM guarantees a single, thread-safe instance.
enum Logger {
    INSTANCE;

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

public class SingletonDemo {
    public static void main(String[] args) {
        Logger.INSTANCE.log("Application started");
        Logger.INSTANCE.log("Processing data");
        Logger.INSTANCE.log("Application ended");
    }
}
```

### Factory Method Example

```java
import java.util.List;

interface Shape {
    void draw();
}

class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing Circle");
    }
}

class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing Rectangle");
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

public class FactoryMethodDemo {
    public static void main(String[] args) {
        List<Shape> shapes = List.of(
            ShapeFactory.createShape("circle"),
            ShapeFactory.createShape("rectangle"),
            ShapeFactory.createShape("circle")
        );

        for (Shape shape : shapes) {
            if (shape != null) shape.draw();
        }
    }
}
```

### Builder Example

```java
class House {
    String windows;
    String doors;
    String roof;
    String garage;

    void show() {
        System.out.println("House: " + windows + ", " + doors
            + ", " + roof + ", " + garage);
    }
}

class HouseBuilder {
    private final House house = new House();

    public HouseBuilder windows(String w) {
        house.windows = w;
        return this;
    }

    public HouseBuilder doors(String d) {
        house.doors = d;
        return this;
    }

    public HouseBuilder roof(String r) {
        house.roof = r;
        return this;
    }

    public HouseBuilder garage(String g) {
        house.garage = g;
        return this;
    }

    public House build() {
        return house;
    }
}

public class BuilderDemo {
    public static void main(String[] args) {
        House myHouse = new HouseBuilder()
            .windows("glass")
            .doors("wooden")
            .roof("tiles")
            .garage("concrete")
            .build();

        myHouse.show();
    }
}
```

---

## STRUCTURAL PATTERNS

### Adapter Example

```java
interface MediaPlayer {
    void play(String file);
}

class VLCPlayer {
    public void playVLC(String file) {
        System.out.println("Playing VLC file: " + file);
    }
}

class VLCAdapter implements MediaPlayer {
    private final VLCPlayer vlc = new VLCPlayer();

    @Override
    public void play(String file) {
        vlc.playVLC(file);
    }
}

public class AdapterDemo {
    public static void main(String[] args) {
        MediaPlayer player = new VLCAdapter();
        player.play("movie.mkv");
    }
}
```

### Decorator Example

```java
interface Component {
    void operation();
}

class ConcreteComponent implements Component {
    @Override
    public void operation() {
        System.out.println("Base operation");
    }
}

abstract class Decorator implements Component {
    protected final Component component;

    protected Decorator(Component component) {
        this.component = component;
    }
}

class ConcreteDecorator extends Decorator {
    public ConcreteDecorator(Component component) {
        super(component);
    }

    @Override
    public void operation() {
        component.operation();
        System.out.println(" + Extra operation");
    }
}

public class DecoratorDemo {
    public static void main(String[] args) {
        Component component = new ConcreteComponent();
        component = new ConcreteDecorator(component);
        component = new ConcreteDecorator(component);

        component.operation();
    }
}
```

---

## BEHAVIORAL PATTERNS

### Observer Example

```java
import java.util.ArrayList;
import java.util.List;

interface Observer {
    void update(String msg);
}

class Subject {
    private final List<Observer> observers = new ArrayList<>();
    private String state;

    public void attach(Observer obs) {
        observers.add(obs);
    }

    public void notifyObservers() {
        for (Observer obs : observers) {
            obs.update(state);
        }
    }

    public void setState(String s) {
        state = s;
        notifyObservers();
    }
}

class ConcreteObserver implements Observer {
    private final String name;

    public ConcreteObserver(String name) {
        this.name = name;
    }

    @Override
    public void update(String msg) {
        System.out.println(name + " notified: " + msg);
    }
}

public class ObserverDemo {
    public static void main(String[] args) {
        Subject subject = new Subject();

        subject.attach(new ConcreteObserver("Observer1"));
        // Observer is a functional interface, so a lambda works too:
        subject.attach(msg -> System.out.println("Observer2 notified: " + msg));

        subject.setState("State changed!");
    }
}
```

### Strategy Example

```java
interface Strategy {
    void execute();
}

class StrategyA implements Strategy {
    @Override
    public void execute() {
        System.out.println("Executing Strategy A");
    }
}

class StrategyB implements Strategy {
    @Override
    public void execute() {
        System.out.println("Executing Strategy B");
    }
}

class Context {
    private Strategy strategy;

    public void setStrategy(Strategy s) {
        strategy = s;
    }

    public void execute() {
        if (strategy != null) strategy.execute();
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        Context context = new Context();

        context.setStrategy(new StrategyA());
        context.execute();

        context.setStrategy(new StrategyB());
        context.execute();

        // Strategy is a functional interface, so a lambda works too:
        context.setStrategy(() -> System.out.println("Executing lambda strategy"));
        context.execute();
    }
}
```

### Command Example

```java
import java.util.ArrayList;
import java.util.List;

interface Command {
    void execute();
}

class Light {
    public void on() {
        System.out.println("Light ON");
    }

    public void off() {
        System.out.println("Light OFF");
    }
}

class TurnOn implements Command {
    private final Light light;

    public TurnOn(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.on();
    }
}

class TurnOff implements Command {
    private final Light light;

    public TurnOff(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.off();
    }
}

class Invoker {
    private final List<Command> commands = new ArrayList<>();

    public void addCommand(Command cmd) {
        commands.add(cmd);
    }

    public void executeAll() {
        for (Command cmd : commands) {
            cmd.execute();
        }
    }
}

public class CommandDemo {
    public static void main(String[] args) {
        Light light = new Light();
        Invoker invoker = new Invoker();

        invoker.addCommand(new TurnOn(light));
        invoker.addCommand(new TurnOff(light));
        // Command is a functional interface, so a lambda works too:
        invoker.addCommand(light::on);

        invoker.executeAll();
    }
}
```

---

## Additional Pattern Combinations

### Template Method + Strategy

Combine template method for algorithm structure with strategy for flexible implementations.

### Factory Method + Singleton

Use singleton factories to manage creation of objects.

### Decorator + Factory

Use factory to create decorated objects.

### Observer + Mediator

Combine for complex multi-object communication patterns.

---

## Best Practices Summary

1. **Composition over inheritance** - Prefer decorators and wrappers
2. **Dependency injection** - Pass dependencies explicitly
3. **Rely on garbage collection** - No manual memory management
4. **Favor immutability** - Use `record` and `final` fields when appropriate
5. **Use lambdas and functional interfaces** - For Strategy, Command, and Observer
6. **Return interfaces** - Return abstract types, not concrete classes
7. **Validate early** - Check preconditions in constructors/methods
8. **Single Responsibility** - Each class has one reason to change

---

For more detailed information, see the individual pattern documentation in the creational/, structural/, and behavioral/ directories.
