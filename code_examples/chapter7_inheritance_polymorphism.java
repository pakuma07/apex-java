// Chapter 7: Inheritance & Polymorphism - Runnable Java Examples
// Java equivalent of the C++ chapter. Compile/run on Java 17:
//   javac chapter7_inheritance_polymorphism.java
//   java  chapter7_inheritance_polymorphism
//
// NOTE on naming: the public class name must match the file name stem
// (snake_case here), even though idiomatic Java normally uses PascalCase.

import java.util.ArrayList;
import java.util.List;

public class chapter7_inheritance_polymorphism {

    // ============================================================
    // EXAMPLE 1: Basic Inheritance
    // In Java every method is virtual by default (dynamic dispatch),
    // so we do NOT need a "virtual" keyword. We use @Override for safety.
    // ============================================================
    static class Animal {
        protected String name;
        Animal(String n) { this.name = n; }
        void sleep() { System.out.println(name + " is sleeping"); }
        void speak() { System.out.println(name + " makes a sound"); }
    }

    static class Dog extends Animal {
        Dog(String n) { super(n); }
        @Override void speak() { System.out.println(name + " barks: Woof!"); }
        void fetch() { System.out.println(name + " fetches the ball"); }
    }

    static class Cat extends Animal {
        Cat(String n) { super(n); }
        @Override void speak() { System.out.println(name + " meows: Meow!"); }
    }

    static void example1_basicInheritance() {
        System.out.println("\n=== EXAMPLE 1: Basic Inheritance ===");
        Dog dog = new Dog("Rex");
        Cat cat = new Cat("Whiskers");
        dog.speak();
        dog.sleep();
        dog.fetch();
        cat.speak();
        cat.sleep();
    }

    // ============================================================
    // EXAMPLE 2: Virtual Functions & Polymorphism
    // In C++ you needed Animal* and "virtual"; in Java a base-type
    // reference automatically dispatches to the overridden method.
    // There is no manual delete: the GC reclaims objects.
    // ============================================================
    static void example2_virtualFunctions() {
        System.out.println("\n=== EXAMPLE 2: Virtual Functions & Polymorphism ===");
        Animal[] animals = {
            new Dog("Buddy"),
            new Cat("Mittens"),
            new Dog("Max")
        };
        System.out.println("Calling speak() through Animal references:");
        for (Animal a : animals) {
            a.speak(); // calls correct derived implementation
        }
        // No delete needed - garbage collector frees memory.
    }

    // ============================================================
    // EXAMPLE 3: Abstract Base Classes
    // C++ pure virtual (= 0) maps to Java abstract methods.
    // ============================================================
    abstract static class Shape {
        abstract double area();
        abstract double perimeter();
        abstract void display();
    }

    static class Circle extends Shape {
        private final double radius;
        Circle(double r) { this.radius = r; }
        @Override double area() { return Math.PI * radius * radius; }
        @Override double perimeter() { return 2 * Math.PI * radius; }
        @Override void display() {
            System.out.printf("Circle: radius=%.1f, area=%.4f%n", radius, area());
        }
    }

    static class Rectangle extends Shape {
        private final double width, height;
        Rectangle(double w, double h) { this.width = w; this.height = h; }
        @Override double area() { return width * height; }
        @Override double perimeter() { return 2 * (width + height); }
        @Override void display() {
            System.out.printf("Rectangle: %.1fx%.1f, area=%.1f%n", width, height, area());
        }
    }

    static void example3_abstractClasses() {
        System.out.println("\n=== EXAMPLE 3: Abstract Base Classes ===");
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(5));
        shapes.add(new Rectangle(3, 4));
        shapes.add(new Circle(2));
        for (Shape s : shapes) s.display();
    }

    // ============================================================
    // EXAMPLE 4: Multiple Inheritance
    // Java has NO multiple inheritance of classes (no diamond problem).
    // The Java equivalent is implementing MULTIPLE INTERFACES.
    // Interfaces may carry default methods to share behavior.
    // ============================================================
    interface Swimmer {
        default void swim() { System.out.println("Swimming"); }
    }
    interface Flyer {
        default void fly() { System.out.println("Flying"); }
    }
    static class Duck implements Swimmer, Flyer {
        @Override public void swim() { System.out.println("Duck swimming"); }
        @Override public void fly() { System.out.println("Duck flying"); }
        void quack() { System.out.println("Duck quacking: Quack!"); }
    }

    static void example4_multipleInheritance() {
        System.out.println("\n=== EXAMPLE 4: Multiple Inheritance (via interfaces) ===");
        Duck duck = new Duck();
        duck.swim();
        duck.fly();
        duck.quack();
        Swimmer s = duck;
        Flyer f = duck;
        s.swim();
        f.fly();
    }

    // ============================================================
    // EXAMPLE 5: Constructor order in Inheritance
    // Java has no destructors; super() runs first like C++ base ctor.
    // Cleanup is the GC's job (see chapter 12 for AutoCloseable).
    // ============================================================
    static class Vehicle {
        protected String type;
        Vehicle(String t) { this.type = t; System.out.println("Vehicle constructor: " + t); }
    }
    static class Car extends Vehicle {
        private final int doors;
        Car(String t, int d) {
            super(t);
            this.doors = d;
            System.out.println("Car constructor: " + doors + " doors");
        }
    }

    static void example5_constructors() {
        System.out.println("\n=== EXAMPLE 5: Constructor Order in Inheritance ===");
        Car myCar = new Car("Sedan", 4);
        System.out.println("Car created");
        // No destructor: Java relies on GC, not deterministic destruction.
    }

    // ============================================================
    // EXAMPLE 6: Protected Members
    // ============================================================
    static class Account {
        protected double balance;
        Account(double b) { this.balance = b; }
        void display() { System.out.printf("Balance: $%.2f%n", balance); }
    }
    static class SavingsAccount extends Account {
        private final double interestRate;
        SavingsAccount(double b, double r) { super(b); this.interestRate = r; }
        void addInterest() {
            balance += balance * interestRate; // access protected member
            System.out.printf("Interest added. New balance: $%.2f%n", balance);
        }
        @Override void display() {
            System.out.printf("Savings Account - Balance: $%.2f, Rate: %.1f%%%n",
                    balance, interestRate * 100);
        }
    }

    static void example6_protectedMembers() {
        System.out.println("\n=== EXAMPLE 6: Protected Members ===");
        SavingsAccount account = new SavingsAccount(1000, 0.05);
        account.display();
        account.addInterest();
        account.display();
    }

    // ============================================================
    // EXAMPLE 7: Resource cleanup (no virtual destructors in Java)
    // C++ uses virtual destructors so "delete base*" frees derived state.
    // Java has GC, but for non-memory resources we implement
    // AutoCloseable + try-with-resources to get deterministic cleanup.
    // ============================================================
    static class BaseResource implements AutoCloseable {
        BaseResource() { System.out.println("BaseResource constructed"); }
        @Override public void close() { System.out.println("BaseResource closed"); }
    }
    static class DerivedResource extends BaseResource {
        DerivedResource() { System.out.println("DerivedResource constructed"); }
        @Override public void close() {
            System.out.println("DerivedResource closing (frees its own state)");
            super.close();
        }
    }

    static void example7_resourceCleanup() {
        System.out.println("\n=== EXAMPLE 7: Deterministic Cleanup (AutoCloseable) ===");
        // try-with-resources guarantees close() is called, even via base type.
        try (BaseResource r = new DerivedResource()) {
            System.out.println("Using the resource");
        } // close() dispatches to DerivedResource.close()
    }

    // ============================================================
    // EXAMPLE 8: Upcasting & Downcasting
    // C++ dynamic_cast maps to Java's instanceof + cast.
    // Java 17 supports pattern-matching instanceof.
    // ============================================================
    static void example8_casting() {
        System.out.println("\n=== EXAMPLE 8: Upcasting & Downcasting ===");
        Dog dog = new Dog("Spot");
        Animal animal = dog; // upcasting (always safe)
        animal.speak();

        // Downcasting with pattern matching instanceof (Java 16+).
        if (animal instanceof Dog d) {
            System.out.println("Downcasting succeeded, calling fetch()");
            d.fetch();
        }

        Animal catAnimal = new Cat("Felix");
        if (!(catAnimal instanceof Dog)) {
            System.out.println("Downcasting to Dog failed (as expected)");
        }
    }

    // ============================================================
    // EXAMPLE 9: Polymorphic Container
    // ============================================================
    static void example9_polymorphicContainer() {
        System.out.println("\n=== EXAMPLE 9: Polymorphic Container ===");
        List<Animal> animals = new ArrayList<>();
        animals.add(new Dog("Fido"));
        animals.add(new Cat("Tom"));
        animals.add(new Dog("Pax"));
        animals.add(new Cat("Sylvester"));

        System.out.println("All animals speak:");
        for (Animal a : animals) a.speak();

        System.out.println("\nAll animals sleep:");
        for (Animal a : animals) a.sleep();
    }

    // ============================================================
    // EXAMPLE 10: Override safety
    // C++11 added the "override" keyword; Java's @Override annotation
    // serves the same purpose - the compiler errors if nothing is overridden.
    // ============================================================
    static class Base10 {
        void method() { System.out.println("Base method"); }
    }
    static class Derived10 extends Base10 {
        @Override void method() { System.out.println("Derived method (@Override verified by compiler)"); }
    }

    static void example10_overrideKeyword() {
        System.out.println("\n=== EXAMPLE 10: @Override Annotation ===");
        Base10 ref = new Derived10();
        ref.method(); // calls Derived10.method()
        System.out.println("@Override prevents accidental signature mismatches");
        System.out.println("If the signature didn't match a supertype method, compilation fails");
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 7: INHERITANCE & POLYMORPHISM (Java 17)");
        System.out.println("======================================================");

        example1_basicInheritance();
        example2_virtualFunctions();
        example3_abstractClasses();
        example4_multipleInheritance();
        example5_constructors();
        example6_protectedMembers();
        example7_resourceCleanup();
        example8_casting();
        example9_polymorphicContainer();
        example10_overrideKeyword();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
