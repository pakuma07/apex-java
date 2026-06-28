# Chapter 7: Inheritance & Polymorphism - Exercises (Java Edition)

## Section 1: Basic Inheritance 🟢

1. Create a `Vehicle` base class with:
   - Fields: `make`, `model`, `year`
   - Methods: `displayInfo()`, `accelerate()`
   - Create derived class `Car` (using `extends`) with additional field `numDoors`

2. Implement a `Shape` base class and derive `Circle` and `Rectangle`. Each should have:
   - Overridable method `area()` (in Java every non-`final`, non-`static` method is virtual/overridable by default)
   - Overridable method `perimeter()`
   - A `final` method `display()` to show it cannot be overridden

3. Create a `Person` class and derive `Student` class:
   - `Person` has: name, age
   - `Student` has: studentId, gpa
   - Implement display methods; call `super(...)` from the `Student` constructor

## Section 2: Overridable Methods & Polymorphism 🟡

> Note: Java has no `virtual` keyword. All non-`final`, non-`static`, non-`private` instance methods are dispatched dynamically (i.e. they are "virtual" by default).

4. Create a polymorphic container that stores different shape references (`List<Shape>`) and calculates total area

5. Write a method that accepts a base class reference (`Shape`) and calls overridable methods polymorphically

6. Implement a `BankAccount` hierarchy:
   - Base: `Account`
   - Derived: `SavingsAccount`, `CheckingAccount`
   - Each with an overridable `calculateInterest()` method
   - Demonstrate polymorphic behavior

## Section 3: Abstract Classes & Interfaces 🟡

7. Create an abstract `Employee` class with abstract methods:
   - `calculateSalary()`
   - `displayDetails()`
   - Create concrete `Manager` and `Developer` subclasses

8. Design an `Animal` interface with an abstract `makeSound()` method and implement concrete animals (`implements Animal`)

9. Create an abstract `DataWriter` class (or interface) with an abstract `write()` method:
   - Implement `FileWriter` and `ConsoleWriter`
   - Note: in Java, an `abstract class` can hold state and partial implementation; an `interface` defines a contract and can supply `default` methods

## Section 4: Multiple Interface Inheritance 🟡

> Java has no multiple class inheritance. A class extends at most one class but can implement many interfaces. These exercises are reframed around implementing multiple interfaces and using `default` methods.

10. Create two interfaces `Reader` and `Writer`:
    - Create a `TextProcessor` that `implements Reader, Writer`
    - Implement all interface methods (optionally provide `default` methods on the interfaces)

11. Design a `Drawable` interface and a `Movable` interface:
    - Create a `Sprite` class that `implements Drawable, Movable`
    - Demonstrate the benefits of composing behavior through multiple interfaces (and how this avoids the C++ diamond problem)

## Section 5: Constructor & Cleanup Order 🔴

12. Create a hierarchy where constructors print messages to show call order (note: `super(...)` runs before the subclass constructor body)

13. Implement a base class with a parameterized constructor and verify derived class initialization via `super(...)`

14. Create a scenario where cleanup order matters: implement `AutoCloseable` in a hierarchy and use try-with-resources to observe `close()` ordering

15. Implement `AutoCloseable` cleanup in a class hierarchy and show why deterministic cleanup matters (Java's replacement for virtual destructors — there is no destructor; `finalize()` is deprecated) 🏆

## Section 6: Protected Members 🟡

16. Create a base class with `protected` members and show subclass access

17. Design a class hierarchy and discuss visibility: in Java `protected` also grants same-package access (there is no "protected inheritance" — inheritance does not change member visibility)

18. Create a scenario showing access levels: `public`, `protected`, package-private (default), `private`

## Section 7: Upcasting & Downcasting 🟡

19. Demonstrate upcasting (safe, implicit):
    - Subclass reference → base reference (`Animal a = new Cat();`)

20. Implement downcasting with `instanceof` + cast, and Java 21 pattern matching:
    - Base reference → subclass reference
    - Handle a failed cast: a plain cast throws `ClassCastException`; guard with `instanceof` first

    ```java
    Animal a = getAnimal();
    if (a instanceof Cat c) {       // pattern matching instanceof (Java 16+)
        c.purr();
    } else {
        System.out.println("Not a Cat");
    }
    ```

21. Compare guarded `instanceof` pattern matching vs an unchecked cast for type safety (an unchecked cast risks `ClassCastException`)

## Section 8: Polymorphic Containers 🟡

22. Create a `List<Animal>` (base type) storing various subclass instances

23. Implement a collection that uses polymorphism to call the appropriate overridden methods

24. Design a game system with a polymorphic `List<Character>` container

## Section 9: `@Override` Annotation 🟡

25. Use the `@Override` annotation to ensure methods actually match a base class/interface signature

26. Introduce a typo in a method name without `@Override` (creating an accidental new method), then add `@Override` to surface the compile error and fix it

27. Demonstrate the `final` keyword on a method (and on a class) to prevent further overriding/subclassing

## Section 10: Polymorphism Pitfalls 🔴

28. Show the constructor-calls-overridable-method pitfall: calling an overridable method from a base constructor runs the subclass override **before** the subclass fields are initialized (so it sees default/`null` values) 🏆

    ```java
    class Base {
        Base() { init(); }              // calls overridable method during construction
        void init() { System.out.println("Base.init"); }
    }
    class Derived extends Base {
        String name = "set";
        @Override void init() {
            System.out.println("Derived.init sees name=" + name); // prints null!
        }
    }
    ```

29. Demonstrate why Java has no object slicing: variables hold references, not values, so assigning a `Cat` to an `Animal` variable keeps the full `Cat` object and dynamic dispatch still works. Contrast this with C++ by-value slicing.

30. Discuss dynamic dispatch cost in Java and how the JIT optimizes monomorphic call sites; show a `final` method/class as a way to make intent explicit

---

## Tips for Success

- **Overridable methods**: Use for behavior that varies by type (all eligible methods are virtual by default)
- **Abstract classes & interfaces**: Define contracts; interfaces support multiple inheritance of type
- **AutoCloseable in subclasses**: Properly manage resources with try-with-resources
- **@Override**: Always annotate overrides to catch signature mismatches at compile time
- **Reference semantics**: No object slicing — variables hold references
- **Multiple interfaces**: Prefer composing behavior via interfaces (no multiple class inheritance)
- **instanceof pattern matching**: Use for safe downcasting
- **Cleanup order**: Subclass `close()` logic, then `super.close()` if applicable

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Basic inheritance, simple polymorphism
- **Medium (🟡)**: 20 exercises - Overridable methods, multiple interfaces, containers
- **Hard (🔴)**: 7 exercises - Advanced patterns, cleanup management, pitfalls

## Challenge Problems 🏆

- **Challenge 1**: AutoCloseable cleanup and resource ordering in a hierarchy
- **Challenge 2**: Overridable-method calls in constructors (initialization pitfall)
- **Challenge 3**: Multiple interface implementation with `default` methods (Java's answer to the diamond problem)

## Expected Time Commitment

- Easy: 5-10 minutes per exercise
- Medium: 15-30 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Learning Outcomes

After completing these exercises, you will:
✓ Understand inheritance hierarchies and design
✓ Master overridable methods and polymorphism
✓ Use abstract classes and interfaces effectively
✓ Implement multiple interfaces (Java's multiple inheritance of type)
✓ Manage object lifetime/cleanup in polymorphic contexts with AutoCloseable
✓ Use the `@Override` annotation and `final` keyword
✓ Avoid common pitfalls (constructor/override interaction, casting)

## Java Exercise Example: Class Basics

```java
public class Student {
    public String name;
    public int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// Or, as an immutable record:
public record StudentRecord(String name, int age) {}
```
