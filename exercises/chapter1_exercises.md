# Chapter 1: Basics - Practice Exercises

## 1. Data Types & Variables

### Exercise 1.1: Calculate Circle Area
Write a program that:
- Declares a variable `radius` of type `double` and initialize it with 5.0
- Declares a constant `PI` (`final double`) with value 3.14159
- Calculate and display the area of a circle: `area = PI * radius * radius`

**Expected Output:** Area of circle with radius 5.0 is approximately 78.54

### Exercise 1.2: Type Sizes & Ranges
Write a program that displays the size (in bytes/bits) and range of:
- byte, short, int, long
- float, double
- char, boolean

Use the wrapper constants (`Integer.SIZE`, `Long.BYTES`, `Integer.MIN_VALUE`, `Integer.MAX_VALUE`, etc.) and create a nicely formatted table.

### Exercise 1.3: Temperature Conversion
Write a program that:
- Reads a temperature in Celsius as input (use `Scanner`)
- Converts it to Fahrenheit using formula: `F = C * 9/5 + 32`
- Displays both values with 2 decimal places (use `String.format` / `printf`)

### Exercise 1.4: Simple Banking
Create variables for:
- Account holder name (`String`)
- Account number (`int`)
- Balance (`double`)
- Account type (`char`: 'S' for Savings, 'C' for Checking)

Display all information in a formatted way.

## 2. Constants & Literals

### Exercise 2.1: Octal, Hex, Binary
Write a program that:
- Defines a number in octal (e.g., `052`)
- Defines the same number in hexadecimal (e.g., `0x2A`)
- Defines the same number in binary (Java 7+, e.g., `0b101010`)
- Use underscores in numeric literals where helpful (e.g., `1_000_000`)
- Displays all three and verify they're equal

### Exercise 2.2: String Literals
Create a program with different types of literals:
- Regular `String`: `"Hello"`
- Text block (Java 13+): `"""..."""`
- Character literal: `'H'`
- Escape sequences: `"Line1\nLine2"`, `"Tab\tSeparated"`

Display each type.

## 3. Type Conversion

### Exercise 3.1: Implicit vs Explicit Casting
Write a program that:
- Declares `int x = 10`
- Implicitly converts (widens) to `double` and displays
- Declares `double y = 3.99`
- Explicitly casts to `int` using a cast `(int) y` and displays (should be 3)
- Show the data loss with comments

### Exercise 3.2: String to Number Conversion
Write a program that:
- Reads a string: `"42"`
- Converts it to `int` using `Integer.parseInt()`
- Converts `"3.14"` to `double` using `Double.parseDouble()`
- Converts `"123"` to `long` using `Long.parseLong()`
- Display all values with their types

### Exercise 3.3: Number to String
Write a program that:
- Declares `int x = 42`, `double d = 3.14`
- Converts both to strings using `String.valueOf()` / `Integer.toString()`
- Concatenates strings and displays result

## 4. Scope & Initialization

### Exercise 4.1: Field vs Local Variables
Create a program with:
- A static field `globalX = 100`
- A method that declares a local `localX = 50`
- Display both before and after the method call
- Show that the field remains unchanged

### Exercise 4.2: Variable Shadowing
Write a program that:
- Declares `int x = 10` in an outer scope (a field)
- Declares `int x = 20` in an inner scope (a method/block) that shadows it
- Display both to show the shadowing effect (use `this.x` to reach the field)

### Exercise 4.3: Final Variables & Arrays
Write a program that:
- Declares a `final int x = 10` (try to reassign — should be a compile error)
- Initializes an array: `int[] arr = {1, 2, 3}`
- Comment out the failing reassignment line

## 5. Input/Output Formatting

### Exercise 5.1: Formatted Table Output
Create a formatted table displaying:
- Product | Price | Quantity | Total
- ------- | ----- | -------- | -----
- Item1 | $19.99 | 5 | $99.95
- Item2 | $9.99 | 3 | $29.97

Use `System.out.printf` / `String.format` with width and precision specifiers (`%-10s`, `%8.2f`).

### Exercise 5.2: Number Base Display
Write a program that reads an integer and displays it in:
- Decimal (base 10)
- Hexadecimal (`Integer.toHexString`)
- Octal (`Integer.toOctalString`)
- Binary (`Integer.toBinaryString`)

Example: 255 → 255 (decimal), ff (hex), 377 (octal)

### Exercise 5.3: Histogram
Create a program that:
- Reads 5 numbers from user
- Displays a simple histogram using asterisks (consider `"*".repeat(n)`)
- Example: 3 → ***, 7 → *******

## 6. Arithmetic Operations

### Exercise 6.1: BMI Calculator
Calculate Body Mass Index (BMI):
- Read weight (kg) and height (m)
- Formula: BMI = weight / (height * height)
- Display BMI with 1 decimal place
- Display category: Underweight (<18.5), Normal (18.5-24.9), etc.

### Exercise 6.2: Modulo Operations
Write a program that:
- Tests if a number is even or odd using modulo
- Extracts last digit of a number using modulo
- Tests if a number is divisible by 3 and 5
- Displays results for numbers 1-20

### Exercise 6.3: Power Calculation
Write a program that:
- Calculates x^y manually without using `Math.pow()`
- Example: 2^5 = 2 * 2 * 2 * 2 * 2 = 32
- Try with multiple bases and exponents

## 7. Challenge Problems

### Challenge 7.1: Currency Converter
Create a program that:
- Defines exchange rates (USD, EUR, GBP, JPY) as constants
- Reads amount in one currency and target currency
- Converts and displays result
- Validates input

### Challenge 7.2: Simple Vending Machine
Simulate a simple calculator:
- Declare total price = 5.50
- User enters payment amount
- Calculate and display change with proper precision
- Validate: payment >= total price

### Challenge 7.3: Age Calculator
Write a program that:
- Reads birth year as input
- Calculates current age (assume current year is 2026)
- Handles edge cases (future birth year, etc.)
- Displays age category: Child, Teen, Adult, Senior

---

## Solutions Tips

For each exercise, think about:
- What data types do I need?
- What values need to be constants (`final`)?
- How should I format the output?
- What validation do I need?

Use `import java.util.Scanner;` for input and `System.out.printf` / `String.format` for formatting as needed.

---

## Difficulty Levels
- **Easy**: Exercises 1.1-1.4, 2.1, 3.1
- **Medium**: Exercises 3.2-3.3, 4.1-4.3, 5.1-5.3, 6.1-6.2
- **Hard**: Exercises 6.3, Challenge 7.1-7.3

## Java 21 Exercise Example: Circle Area

```java
public class CircleArea {
    public static void main(String[] args) {
        final double PI = 3.14159;
        double radius = 5.0;
        System.out.printf("%.2f%n", PI * radius * radius);
    }
}
```
