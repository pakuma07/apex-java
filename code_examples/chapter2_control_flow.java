// Chapter 2: Control Flow & Loops - Java translation of chapter2_control_flow.cpp
// Compile: javac chapter2_control_flow.java
// Run:     java chapter2_control_flow
// Target: Java 17 (uses enhanced switch where it reads better). Runs on JDK 17.
//
// NOTE on input: the C++ version reads from cin in examples 3 & 4. To keep this
// a self-contained, non-interactive runnable program, those examples use fixed
// scripted "inputs" instead of reading stdin, with comments explaining the change.

import java.util.List;

public class chapter2_control_flow {

    // ============================================================
    // EXAMPLE 1: If-Else Statements
    // ============================================================
    static void example1_if_else() {
        System.out.println("\n=== EXAMPLE 1: If-Else Statements ===");

        int score = 85;

        if (score >= 90) {
            System.out.println("Grade: A (Excellent)");
        } else if (score >= 80) {
            System.out.println("Grade: B (Good)");
        } else if (score >= 70) {
            System.out.println("Grade: C (Fair)");
        } else {
            System.out.println("Grade: F (Fail)");
        }

        int age = 25;
        if (age >= 18) {
            if (age >= 65) {
                System.out.println("Senior citizen");
            } else {
                System.out.println("Adult");
            }
        } else {
            System.out.println("Minor");
        }
    }

    // ============================================================
    // EXAMPLE 2: Switch Statements (classic + fall-through).
    // Java switch works like C++: cases fall through unless you break.
    // ============================================================
    static void example2_switch() {
        System.out.println("\n=== EXAMPLE 2: Switch Statements ===");

        int day = 3;

        switch (day) {
            case 1: System.out.println("Monday"); break;
            case 2: System.out.println("Tuesday"); break;
            case 3: System.out.println("Wednesday"); break;
            case 4: System.out.println("Thursday"); break;
            case 5: System.out.println("Friday"); break;
            case 6: System.out.println("Saturday"); break;
            case 7: System.out.println("Sunday"); break;
            default: System.out.println("Invalid day");
        }

        // Switch with intentional fall-through (same semantics as C++).
        int grade = 2;
        switch (grade) {
            case 1: System.out.print("Very Good");
            case 2: System.out.print(" - Good");
            case 3: System.out.println(" - Fair");
                break;
            case 4: System.out.println("Poor");
                break;
        }
    }

    // ============================================================
    // EXAMPLE 3: While Loop (cin replaced with scripted guesses)
    // ============================================================
    static void example3_while_loop() {
        System.out.println("\n=== EXAMPLE 3: While Loop ===");

        int counter = 1;
        System.out.print("Countdown from 5: ");
        while (counter <= 5) {
            System.out.print(counter + " ");
            counter++;
        }
        System.out.println();

        // Java: no cin. Simulate user guesses with a scripted array.
        int[] scriptedGuesses = {3, 5, 7};
        int idx = 0;
        int attempts = 0;
        int secret = 7;

        int guess = scriptedGuesses[idx++];
        System.out.println("Guess a number (1-10): " + guess);

        while (guess != secret && attempts < 2) {
            guess = scriptedGuesses[idx++];
            System.out.println("Wrong! Try again: " + guess);
            attempts++;
        }

        if (guess == secret) {
            System.out.println("Correct!");
        } else {
            System.out.println("Game over. Secret was: " + secret);
        }
    }

    // ============================================================
    // EXAMPLE 4: Do-While Loop (menu, cin replaced with scripted choices)
    // ============================================================
    static void example4_do_while() {
        System.out.println("\n=== EXAMPLE 4: Do-While Loop ===");

        int i = 1;
        do {
            System.out.println("i = " + i);
            i++;
        } while (i <= 3);

        // Scripted menu choices instead of interactive cin.
        int[] scriptedChoices = {1, 2, 3};
        int idx = 0;
        int choice;
        do {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Print Hello");
            System.out.println("2. Print Goodbye");
            System.out.println("3. Exit");
            choice = scriptedChoices[idx++];
            System.out.println("Choice: " + choice);

            switch (choice) {
                case 1: System.out.println("Hello!"); break;
                case 2: System.out.println("Goodbye!"); break;
                case 3: System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice");
            }
        } while (choice != 3);
    }

    // ============================================================
    // EXAMPLE 5: For Loop
    // ============================================================
    static void example5_for_loop() {
        System.out.println("\n=== EXAMPLE 5: For Loop ===");

        System.out.print("Numbers 1-5: ");
        for (int i = 1; i <= 5; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.print("Countdown: ");
        for (int i = 5; i >= 1; i--) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("\n5 x 1 to 5 x 10:");
        for (int i = 1; i <= 10; i++) {
            System.out.println("5 x " + i + " = " + (5 * i));
        }
    }

    // ============================================================
    // EXAMPLE 6: Enhanced for / for-each (Java analogue of C++11 range-for).
    // C++ uses `int&` to modify elements in place; Java's for-each gives a
    // copy of a primitive, so to "double in place" we index a List/array.
    // ============================================================
    static void example6_range_for() {
        System.out.println("\n=== EXAMPLE 6: Enhanced For Loop ===");

        int[] numbers = {10, 20, 30, 40, 50};

        System.out.print("Array elements: ");
        for (int num : numbers) {
            System.out.print(num + " ");
        }
        System.out.println();

        // Java for-each variable is a copy; index to modify the backing array.
        System.out.print("Doubled: ");
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] *= 2;
            System.out.print(numbers[i] + " ");
        }
        System.out.println();

        String text = "Hello";
        System.out.print("\nCharacters in '" + text + "': ");
        for (char c : text.toCharArray()) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 7: Break Statement
    // ============================================================
    static void example7_break() {
        System.out.println("\n=== EXAMPLE 7: Break Statement ===");

        System.out.print("Numbers until 4: ");
        for (int i = 1; i <= 10; i++) {
            if (i == 5) {
                break;
            }
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.print("Break in switch: ");
        for (int i = 1; i <= 3; i++) {
            switch (i) {
                case 1: System.out.print("One ");
                case 2: System.out.print("Two ");
                case 3: System.out.print("Three");
                    break;
            }
        }
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 8: Continue Statement
    // ============================================================
    static void example8_continue() {
        System.out.println("\n=== EXAMPLE 8: Continue Statement ===");

        System.out.print("Odd numbers 1-10: ");
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                continue;
            }
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.print("Numbers except 3,6,9: ");
        for (int i = 1; i <= 10; i++) {
            if (i % 3 == 0) {
                continue;
            }
            System.out.print(i + " ");
        }
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 9: Nested Loops
    // ============================================================
    static void example9_nested_loops() {
        System.out.println("\n=== EXAMPLE 9: Nested Loops ===");

        System.out.println("Multiplication Table (1-3):");
        System.out.print("  ");
        for (int i = 1; i <= 3; i++) System.out.print(i + " ");
        System.out.println();

        for (int i = 1; i <= 3; i++) {
            System.out.print(i + ": ");
            for (int j = 1; j <= 3; j++) {
                System.out.print((i * j) + " ");
            }
            System.out.println();
        }

        System.out.println("\nTriangle Pattern:");
        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }
    }

    // ============================================================
    // EXAMPLE 10: Complex Control Flow
    // ============================================================
    static void example10_complex_control() {
        System.out.println("\n=== EXAMPLE 10: Complex Control Flow ===");

        System.out.print("First prime > 20: ");
        for (int num = 21; num <= 50; num++) {
            boolean isPrime = true;

            if (num < 2) isPrime = false;

            for (int i = 2; i * i <= num; i++) {
                if (num % i == 0) {
                    isPrime = false;
                    break;
                }
            }

            if (isPrime) {
                System.out.println(num);
                break;
            }
        }

        List<Integer> values = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int sum = 0;

        for (int val : values) {
            if (val % 2 == 0) continue;  // skip even
            sum += val;
            if (sum > 20) break;          // stop if sum > 20
        }
        System.out.println("Sum of odd numbers until > 20: " + sum);
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CHAPTER 2: CONTROL FLOW & LOOPS (Java)");
        System.out.println("==================================================");

        example1_if_else();
        example2_switch();
        example3_while_loop();
        example4_do_while();
        example5_for_loop();
        example6_range_for();
        example7_break();
        example8_continue();
        example9_nested_loops();
        example10_complex_control();

        System.out.println("\n==================================================");
        System.out.println("All examples completed!");
        System.out.println("==================================================");
    }
}
