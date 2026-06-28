// Chapter 6: Object-Oriented Programming - Java translation of chapter6_oop.cpp
// Compile: javac chapter6_oop.java
// Run:     java chapter6_oop
// Target: Java 17. Runs on JDK 17.
//
// ADAPTATION NOTES:
//   - C++ destructors (~Class) run deterministically at scope exit. Java has
//     no destructors; the GC reclaims objects nondeterministically. So the
//     "[Created]/[Destroyed]" lifecycle demo prints creation eagerly and
//     simulates "destruction" explicitly (e.g. a close()-style method), since
//     finalize() is deprecated and unreliable.
//   - C++ `friend` functions have no Java equivalent. Java's closest analogue
//     is same-package access (package-private). We implement the "friend"
//     helpers as static methods of the same class accessing private fields.
//   - C++ copy constructor: Java uses an explicit copy constructor method too,
//     but objects are reference types (assignment copies the reference).
//   - Operator overloading (operator=) does not exist in Java; we use a method.

import java.util.ArrayList;
import java.util.List;

public class chapter6_oop {

    // ============================================================
    // EXAMPLE 1: Basic Class Definition
    // ============================================================
    static class Person {
        private String name;
        private int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
            System.out.println("Person created: " + name);
        }

        String getName() { return name; }
        int getAge() { return age; }

        void setAge(int a) {
            if (a > 0) age = a;
        }

        void display() {
            System.out.println("Name: " + name + ", Age: " + age);
        }
    }

    static void example1_basic_class() {
        System.out.println("\n=== EXAMPLE 1: Basic Class ===");

        Person p1 = new Person("Alice", 25);
        p1.display();

        Person p2 = new Person("Bob", 30);
        p2.display();

        System.out.println("Bob's age: " + p2.getAge());
        p2.setAge(31);
        System.out.println("After birthday: " + p2.getAge());
    }

    // ============================================================
    // EXAMPLE 2: Access Modifiers (private / protected / public)
    // ============================================================
    static class Rectangle {
        private double width;
        private double height;

        private boolean isValid() {
            return width > 0 && height > 0;
        }

        protected void setDimensions(double w, double h) {
            if (w > 0 && h > 0) {
                width = w;
                height = h;
            }
        }

        Rectangle(double w, double h) {
            this.width = w;
            this.height = h;
        }

        double getArea() {
            return isValid() ? width * height : 0;
        }

        double getPerimeter() {
            return isValid() ? 2 * (width + height) : 0;
        }
    }

    static void example2_access() {
        System.out.println("\n=== EXAMPLE 2: Access Modifiers ===");

        Rectangle r = new Rectangle(5, 10);
        System.out.println("Area: " + r.getArea());
        System.out.println("Perimeter: " + r.getPerimeter());
        // r.width = -5;  // ERROR: width is private
    }

    // ============================================================
    // EXAMPLE 3: Static Members (no separate out-of-class init needed in Java)
    // ============================================================
    static class Counter {
        private static int count = 0;  // initialized inline, unlike C++
        private int id;

        Counter() {
            id = ++count;
            System.out.println("Counter " + id + " created");
        }

        int getId() { return id; }

        static int getCount() { return count; }
    }

    static void example3_static() {
        System.out.println("\n=== EXAMPLE 3: Static Members ===");

        Counter c1 = new Counter();
        Counter c2 = new Counter();
        Counter c3 = new Counter();

        System.out.println("Total counters: " + Counter.getCount());
        System.out.println("c1 ID: " + c1.getId());
        System.out.println("c3 ID: " + c3.getId());
        // reference c2 so it is clearly used
        System.out.println("c2 ID: " + c2.getId());
    }

    // ============================================================
    // EXAMPLE 4: "Const member functions". Java has no const methods; the
    // analogue is simply not mutating fields (and/or making fields final).
    // ============================================================
    static class Temperature {
        private double celsius;

        Temperature(double c) { this.celsius = c; }

        double getCelsius() { return celsius; }          // does not mutate state
        double getFahrenheit() { return celsius * 9.0 / 5.0 + 32.0; }

        void setCelsius(double c) { this.celsius = c; }   // mutator
    }

    static void example4_const() {
        System.out.println("\n=== EXAMPLE 4: Const-like Member Functions ===");

        Temperature t = new Temperature(25);
        System.out.println("Celsius: " + t.getCelsius() + " C");
        System.out.println("Fahrenheit: " + t.getFahrenheit() + " F");

        t.setCelsius(30);
        System.out.println("New Fahrenheit: " + t.getFahrenheit() + " F");
    }

    // ============================================================
    // EXAMPLE 5: `this` reference + method chaining. Java has `this`;
    // operator= overloading does not exist, so we use an assign() method.
    // ============================================================
    static class MyNumber {
        private int value;

        MyNumber(int v) { this.value = v; }

        MyNumber add(MyNumber other) {
            this.value += other.value;
            return this;  // return this object to allow chaining
        }

        MyNumber assign(MyNumber other) {  // stand-in for C++ operator=
            if (this != other) {
                this.value = other.value;
            }
            return this;
        }

        int getValue() { return value; }
    }

    static void example5_this() {
        System.out.println("\n=== EXAMPLE 5: 'this' Reference ===");

        MyNumber n1 = new MyNumber(10);
        MyNumber n2 = new MyNumber(5);

        n1.add(n2).add(n2);  // chained calls
        System.out.println("Result: " + n1.getValue());
    }

    // ============================================================
    // EXAMPLE 6: Encapsulation
    // ============================================================
    static class BankAccount {
        private String accountNumber;
        private double balance;

        private boolean isValidAmount(double amount) {
            return amount > 0 && amount <= 1e9;
        }

        BankAccount(String num, double initial) {
            this.accountNumber = num;
            this.balance = initial;
        }

        double getBalance() { return balance; }

        boolean deposit(double amount) {
            if (isValidAmount(amount)) {
                balance += amount;
                return true;
            }
            return false;
        }

        boolean withdraw(double amount) {
            if (isValidAmount(amount) && amount <= balance) {
                balance -= amount;
                return true;
            }
            return false;
        }
    }

    static void example6_encapsulation() {
        System.out.println("\n=== EXAMPLE 6: Encapsulation ===");

        BankAccount acc = new BankAccount("ACC001", 1000);
        System.out.println("Initial balance: $" + acc.getBalance());

        acc.deposit(500);
        System.out.println("After deposit: $" + acc.getBalance());

        acc.withdraw(200);
        System.out.println("After withdrawal: $" + acc.getBalance());

        boolean result = acc.withdraw(2000);
        System.out.println("Withdraw $2000: " + (result ? "Success" : "Failed"));
    }

    // ============================================================
    // EXAMPLE 7: Constructor Types (default / parameterized / copy).
    // No manual char* buffer or destructor: Java String + GC handle memory.
    // ============================================================
    static class MyString {
        private String data;

        MyString() { this.data = ""; }                 // default constructor

        MyString(String str) {                          // parameterized
            this.data = (str != null) ? str : "";
        }

        MyString(MyString other) {                       // copy constructor
            this.data = other.data;  // String is immutable, so a shallow copy is safe
        }

        void display() {
            System.out.println(data.isEmpty() ? "(empty)" : data);
        }
    }

    static void example7_constructors() {
        System.out.println("\n=== EXAMPLE 7: Constructor Types ===");

        MyString s1 = new MyString();          // default
        MyString s2 = new MyString("Hello");   // parameterized
        MyString s3 = new MyString(s2);        // copy

        s1.display();  // (empty)
        s2.display();  // Hello
        s3.display();  // Hello
    }

    // ============================================================
    // EXAMPLE 8: "Friend functions" -> same-class static methods accessing
    // private fields (Java's nearest analogue is package-private access).
    // ============================================================
    static class Box {
        private int width;
        private int height;

        Box(int w, int h) { this.width = w; this.height = h; }
    }

    // These methods live in the enclosing class, so they can read Box's
    // private fields - similar in spirit to C++ friend functions.
    static void printBox(Box b) {
        System.out.println("Box: " + b.width + "x" + b.height);
    }

    static boolean compareBoxes(Box b1, Box b2) {
        return (b1.width * b1.height) == (b2.width * b2.height);
    }

    static void example8_friend() {
        System.out.println("\n=== EXAMPLE 8: 'Friend' Functions ===");

        Box b1 = new Box(5, 10);
        Box b2 = new Box(10, 5);
        Box b3 = new Box(2, 25);

        printBox(b1);
        printBox(b2);

        System.out.println("b1 and b2 same area: " + compareBoxes(b1, b2));
        System.out.println("b1 and b3 same area: " + compareBoxes(b1, b3));
    }

    // ============================================================
    // EXAMPLE 9: Object Lifecycle. Java has no destructor; we model the
    // "[Destroyed]" step with an explicit close() call (try-with-resources
    // style), because GC timing is nondeterministic.
    // ============================================================
    static class Logger implements AutoCloseable {
        private String name;

        Logger(String n) {
            this.name = n;
            System.out.println("[Created] " + name);
        }

        void log(String msg) {
            System.out.println("[" + name + "] " + msg);
        }

        @Override
        public void close() {  // explicit cleanup, replaces ~Logger()
            System.out.println("[Destroyed] " + name);
        }
    }

    static void example9_lifecycle() {
        System.out.println("\n=== EXAMPLE 9: Object Lifecycle ===");

        Logger l1 = new Logger("Main");
        l1.log("Program started");

        // try-with-resources gives deterministic cleanup like a C++ scope.
        try (Logger l2 = new Logger("Block")) {
            l2.log("In block");
        }  // l2.close() called here -> "[Destroyed] Block"

        l1.log("After block");
        l1.close();  // explicit cleanup of l1 (no automatic destructor in Java)
    }

    // ============================================================
    // EXAMPLE 10: Array / List of Objects
    // ============================================================
    static void example10_arrays() {
        System.out.println("\n=== EXAMPLE 10: List of Objects ===");

        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 25));
        people.add(new Person("Bob", 30));
        people.add(new Person("Charlie", 35));

        System.out.println("\nPeople in list:");
        for (Person person : people) {
            person.display();
        }
    }

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("    Chapter 6: Object-Oriented Programming");
        System.out.println("=======================================");

        example1_basic_class();
        example2_access();
        example3_static();
        example4_const();
        example5_this();
        example6_encapsulation();
        example7_constructors();
        example8_friend();
        example9_lifecycle();
        example10_arrays();

        System.out.println("\n=======================================");
        System.out.println("       All Examples Completed!");
        System.out.println("=======================================");
    }
}
