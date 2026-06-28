# Chapter 6: OOP Basics - Practice Exercises (Java Edition)

## 1. Class Declaration & Objects

### Exercise 1.1: Simple Class
Create a `Rectangle` class:
- Private fields: `width`, `height`
- Public methods: `getWidth()`, `getHeight()`, `setWidth()`, `setHeight()`
- Constructor: `Rectangle(int w, int h)`
- Test with multiple objects

### Exercise 1.2: Class for Point
Create a `Point` class:
- Private: `x`, `y` coordinates
- Public: getters, setters
- Method: `distance()` - calculate distance from origin
- Method: `distanceTo(Point p)` - distance to another point

### Exercise 1.3: Circle Class
Create a `Circle` class:
- Private: `radius`, `centerX`, `centerY`
- Public: `getArea()`, `getCircumference()`, `getRadius()`
- Constructor with parameters
- Read-only access: make fields `final` and expose only getters (Java has no `const` methods; immutability comes from `final` fields and not providing setters)

## 2. Constructors & Cleanup

### Exercise 2.1: Multiple Constructors
Create `Book` class:
- Default constructor (no parameters)
- Parameterized constructor (title, author, pages)
- Constructor with default values (use constructor chaining with `this(...)`)
- Test all constructors

### Exercise 2.2: Constructor Field Initialization
Create `Person` class:
- Fields: `name`, `age`, `email`
- Initialize fields in the constructor using `this.name = name;` etc.
- Java has no constructor initialization list; fields are assigned in the body
- Show constructor chaining with `this(...)` and compare with direct assignment

### Exercise 2.3: Resource Cleanup with AutoCloseable
Create `FileHandle` class:
- Java has no destructors. Implement `AutoCloseable` and use try-with-resources.
- Constructor: acquires a resource (simulate with `System.out.println`)
- `close()`: releases the resource
- Show acquire/release ordering via try-with-resources blocks
- Note: `Object.finalize()` is deprecated and must not be relied upon for cleanup

```java
public class FileHandle implements AutoCloseable {
    private final String name;

    public FileHandle(String name) {
        this.name = name;
        System.out.println("Acquired " + name);
    }

    @Override
    public void close() {
        System.out.println("Released " + name);
    }
}

// Usage
try (FileHandle f = new FileHandle("data.txt")) {
    // use f
} // close() called automatically here
```

## 3. Access Modifiers

### Exercise 3.1: Public, Private, Protected
Create base class and derived class:
- Public members: accessible everywhere
- Private members: only in class
- Protected members: in class, subclasses, and same package
- Test access from different contexts

### Exercise 3.2: Encapsulation
Create `BankAccount` class:
- Private: `balance`, `accountNumber`
- Public: `deposit()`, `withdraw()`, `getBalance()`
- Prevent direct balance manipulation
- Validate operations in public methods

### Exercise 3.3: Data Hiding
Create `Employee` class:
- Hide salary with private field
- Public getter returns base info only
- Public setter validates input
- Show benefits of encapsulation

## 4. Static Members

### Exercise 4.1: Static Variables
Create `Counter` class:
- Static field: `count`
- Constructor increments count
- Static method: `getCount()`
- Create multiple objects and check count

### Exercise 4.2: Static Method
Create `MathUtil` utility class:
- Static methods: `add()`, `subtract()`, `multiply()`, `divide()`
- No object creation needed
- Call via `MathUtil.add()`

### Exercise 4.3: Class Constants
Create `Constants` class:
- `static final` constants: `PI`, `E`, `MAX_SIZE`
- Use in calculations
- Compare with C-style `#define`: in Java use `static final` constants instead; they are typed, scoped, and visible to the debugger

## 5. Read-Only Methods & Immutability

### Exercise 5.1: Read-Only vs Mutating Methods
Create `Temperature` class:
- Method `getCelsius()` - read-only (does not modify state)
- Method `setFahrenheit(double)` - modifying
- Note: Java has no `const` methods. Distinguish read-only from mutating methods by convention (getters vs setters) and by making fields `final` where possible.

### Exercise 5.2: Immutable Objects
Create program with:
- An immutable `Rectangle` whose `width`/`height` are `final` and set only in the constructor
- No setters are provided, so the object cannot be mutated after construction
- Show that this is Java's equivalent of a `const` object: immutability is enforced by the type design, not by the caller's variable declaration

### Exercise 5.3: Passing References Efficiently
Pass objects efficiently:
- `void display(final Shape shape)`
- Java always passes object references, so no copying happens regardless
- The `final` parameter only prevents reassigning the local variable, not mutating the object
- For true read-only guarantees, pass an immutable type, a record, or an unmodifiable view (e.g. `Collections.unmodifiableList`)

## 6. The `this` Reference

### Exercise 6.1: `this` Usage
Create `LinkedListNode` class:
- Data and a reference to next
- Method `getNext()` returns `this.next`
- Method `printNode()` uses `this`
- Show explicit vs implicit `this`

### Exercise 6.2: `this` for Chaining
Create a builder class. Method `append(String)` returns `this` to enable method chaining:

```java
MyBuilder builder = new MyBuilder();
builder.append("Hello").append(" ").append("World");
```

Then compare with Java's actual `StringBuilder`, which uses the same pattern:

```java
String result = new StringBuilder()
        .append("Hello")
        .append(" ")
        .append("World")
        .toString();
```

### Exercise 6.3: Self-Reference Check
Create `Node` class:
- A copy method checks `if (this != other)` (reference identity) before copying
- Prevents self-copy problems
- Copy data only if not the same object
- Note: Java uses reference comparison `==` for identity and `equals()` for value equality

## 7. Object Composition

### Exercise 7.1: Object Membership
Create:
- `Address` class (street, city, zip)
- `Person` class (name, `Address address`)
- Access nested members: `person.getAddress().getCity()`

### Exercise 7.2: Aggregate Objects
Create:
- `Engine` class
- `Car` class contains `Engine`
- `ParkingLot` contains multiple `Car` objects (use an `ArrayList<Car>`)
- Show composition hierarchy

### Exercise 7.3: Delegation
Create:
- `Logger` utility class
- `Application` class uses `Logger`
- Application delegates logging to Logger
- Show "has-a" relationship

## 8. Package-Private Access & Nested Classes

Java has no `friend` mechanism. Reframe these exercises around package-private (default) visibility and static nested classes, which together provide controlled access to otherwise-hidden members.

### Exercise 8.1: Package-Private Access
Create `SecretClass` with private members:
- Provide a package-private method or field that a helper class in the **same package** can access
- A class in another package cannot
- Show how package-private visibility scopes access (closest Java analog to a friend function)

### Exercise 8.2: Nested Classes for Privileged Access
Create two classes:
- `Engine` with private data
- A `static` nested class (or inner class) inside `Engine` that can access `Engine`'s private members directly
- A class outside cannot
- Show that nested classes are Java's idiom for granting one type access to another's internals (closest analog to a friend class)

### Exercise 8.3: `toString()` Instead of `operator<<`
Create `Vector` class:
- Override `toString()` to produce formatted output (Java has no `operator<<`)
- Access the class's own private fields inside `toString()`
- Print with `System.out.println(vector)` which calls `toString()` automatically

## 9. Initialization Order

### Exercise 9.1: Field Initialization Order
Create class with multiple fields:
- Show that field initializers and instance initializer blocks run in source (top-to-bottom) order, before the constructor body
- Demonstrate with output
- Compare with assigning fields in the constructor body

### Exercise 9.2: Array Initialization
Create class containing:
- An array (or `ArrayList`) of objects
- Initialize each element in a loop
- Show constructor calls for each element

### Exercise 9.3: Default Values
Create class with:
- Fields with default values (note Java auto-initializes fields to 0/false/null, unlike local variables)
- Overloaded constructors using `this(...)` chaining for defaults
- Show default value usage
- Compare with explicit initialization

## 10. Complete OOP Example

### Exercise 10.1: Student Management
Create `Student` class:
- Fields: ID, name, GPA, courses (`List<String>`)
- Constructor
- Methods: `addCourse`, `removeCourse`, `getGpa`
- Grade calculation

### Exercise 10.2: Library System
Create `Book` and `Library` classes:
- Book: title, author, ISBN, available
- Library: collection of Books (`List<Book>`)
- Methods: addBook, removeBook, borrowBook, returnBook
- Show composition and encapsulation

### Exercise 10.3: Game Characters
Create:
- `Character` base concept (health, name)
- `Warrior` and `Mage` variations
- Equipment system (separate class)
- Interaction methods

## Challenge Problems

### Challenge 11.1: Banking System
Create complete bank system:
- `BankAccount` class (account number, balance)
- `Customer` class (personal info, accounts)
- `Bank` class (manages customers and accounts)
- Transactions, validations, reporting

### Challenge 11.2: Graphics System
Create shape hierarchy:
- `Shape` base class (abstract-like)
- `Circle`, `Rectangle`, `Triangle` derived
- Overridable methods: `area()`, `perimeter()`
- Collection of shapes with calculations

### Challenge 11.3: Company Payroll
Create:
- `Employee` base class
- `Manager` and `Developer` variations
- `Department` managing employees
- Salary calculations, overtime handling
- Reporting and statistics

---

## Tips for Solving

1. **Design first**: Sketch class structure on paper
2. **Start simple**: Basic class, then add features
3. **Test methods**: Verify each method works
4. **Prefer immutability**: Use `final` fields and avoid setters when state should not change
5. **Initialize properly**: Assign all fields in the constructor or via field initializers
6. **Encapsulate**: Hide internal details with `private`
7. **Compose wisely**: Use objects within objects
8. **Document**: Comment on the public interface

## Difficulty Levels
- **Easy**: Exercises 1.1-1.3, 2.1, 3.1-3.2, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1
- **Medium**: Exercises 2.2-2.3, 3.3, 4.2-4.3, 5.2-5.3, 6.2-6.3, 7.2-7.3, 8.2-8.3, 9.2-9.3, 10.1
- **Hard**: Exercises 10.2-10.3, Challenge 11.1-11.3

---

## Object-Oriented Principles

As you solve these exercises, practice:

1. **Encapsulation**: Hide implementation, expose interface
2. **Abstraction**: Focus on what, not how
3. **Inheritance**: Share common functionality (later)
4. **Polymorphism**: Different behavior, same interface (later)

These are foundations for larger OOP projects.

---

## Compilation Reminder

All solutions should compile cleanly with:
```bash
javac -Xlint:all Solution.java
```
Then run with:
```bash
java Solution
```

---

## Common OOP Mistakes

1. **Direct field access**: Use methods, not public fields
2. **Uninitialized references**: A field left `null` causes `NullPointerException`
3. **Resources not closed**: Implement `AutoCloseable` and use try-with-resources
4. **Mutable static**: Avoid, hard to debug
5. **Circular dependencies**: Design carefully
6. **Over-exposing internals**: Limit package-private and public access
7. **Heavy inheritance**: Prefer composition

---

## Java Closing Example: Immutable Record

Java records give you a concise, immutable data carrier with auto-generated
constructor, accessors, `equals()`, `hashCode()`, and `toString()`:

```java
public record StudentRecord(String name, int age) {
    public StudentRecord {
        if (age < 0) throw new IllegalArgumentException("age must be >= 0");
    }
}

// Usage
StudentRecord s = new StudentRecord("Ada", 30);
System.out.println(s.name() + " is " + s.age()); // accessors, not getters
```
