// Chapter 5: Arrays & Strings - Java translation of chapter5_arrays_strings.cpp
// Compile: javac chapter5_arrays_strings.java
// Run:     java chapter5_arrays_strings
// Target: Java 17. Runs on JDK 17.
//
// KEY DIFFERENCES from C++:
//   - Java arrays know their own .length (no sizeof / size trick).
//   - std::vector -> java.util.ArrayList<Integer> (and List).
//   - std::string -> java.lang.String, which is IMMUTABLE. To mutate text
//     in place (e.g. change a char, insert, erase) use StringBuilder.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class chapter5_arrays_strings {

    // ============================================================
    // EXAMPLE 1: Array Basics. Java arrays carry .length; no sizeof.
    // ============================================================
    static void example1_array_basics() {
        System.out.println("\n=== EXAMPLE 1: Array Basics ===");

        int[] arr1 = {10, 20, 30, 40, 50};
        int[] arr2 = {1, 2, 3};

        System.out.print("Array 1: ");
        for (int i = 0; i < arr1.length; i++) {
            System.out.print(arr1[i] + " ");
        }
        System.out.println();

        System.out.println("Size of arr1: " + arr1.length + " elements");
        System.out.println("Size of arr2: " + arr2.length + " elements");

        arr1[2] = 999;
        System.out.println("After arr1[2] = 999: " + arr1[2]);
    }

    // ============================================================
    // EXAMPLE 2: Multi-dimensional Arrays. Java 2D arrays are arrays of arrays.
    // ============================================================
    static void example2_multidimensional_arrays() {
        System.out.println("\n=== EXAMPLE 2: Multi-dimensional Arrays ===");

        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        System.out.println("3x3 Matrix:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }

        System.out.print("\nMain diagonal: ");
        for (int i = 0; i < 3; i++) {
            System.out.print(matrix[i][i] + " ");
        }
        System.out.println();

        System.out.print("\nRow 1 sum: ");
        int rowSum = 0;
        for (int j = 0; j < 3; j++) rowSum += matrix[1][j];
        System.out.println(rowSum);

        System.out.print("Column 0 sum: ");
        int colSum = 0;
        for (int i = 0; i < 3; i++) colSum += matrix[i][0];
        System.out.println(colSum);
    }

    // ============================================================
    // EXAMPLE 3: Array Searching & Sorting (manual bubble sort).
    // ============================================================
    static void example3_search_sort() {
        System.out.println("\n=== EXAMPLE 3: Array Searching & Sorting ===");

        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        int size = arr.length;

        System.out.print("Original array: ");
        for (int i = 0; i < size; i++) System.out.print(arr[i] + " ");
        System.out.println();

        int target = 22;
        int pos = -1;
        for (int i = 0; i < size; i++) {
            if (arr[i] == target) { pos = i; break; }
        }
        System.out.print("Linear search for " + target + ": ");
        if (pos != -1) System.out.println("Found at index " + pos);
        else System.out.println("Not found");

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        System.out.print("Sorted array: ");
        for (int i = 0; i < size; i++) System.out.print(arr[i] + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 4: Vector Container -> ArrayList.
    // Java's ArrayList has no public capacity() accessor, so we note that.
    // ============================================================
    static void example4_vector() {
        System.out.println("\n=== EXAMPLE 4: ArrayList (Java's vector) ===");

        List<Integer> v = new ArrayList<>(List.of(10, 20, 30));

        System.out.print("Initial list: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        v.add(40);  // push_back
        v.add(50);
        System.out.print("After add: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        System.out.println("First element: " + v.get(0));
        System.out.println("Last element: " + v.get(v.size() - 1));
        System.out.println("Size: " + v.size());
        System.out.println("Capacity: (not exposed by ArrayList; managed internally)");

        v.remove(v.size() - 1);  // pop_back
        System.out.print("After remove last: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        v.add(1, 999);  // insert at index 1
        System.out.print("After insert at position 1: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 5: String Basics. String is immutable; "modify a char" uses
    // a char[] or StringBuilder. length() exists; no separate size().
    // ============================================================
    static void example5_string_basics() {
        System.out.println("\n=== EXAMPLE 5: String Basics ===");

        String s1 = "Hello";
        String s2 = "World";
        String s3;

        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);

        System.out.println("Length of s1: " + s1.length());
        System.out.println("Size of s1: " + s1.length() + " (Java has only length())");

        System.out.println("First character: " + s1.charAt(0));
        System.out.println("Last character: " + s1.charAt(s1.length() - 1));

        String s4 = s1 + " " + s2;
        System.out.println("Concatenated: " + s4);

        // Strings are immutable -> build a new one to "change" a char.
        StringBuilder sb = new StringBuilder(s1);
        sb.setCharAt(0, 'J');
        s1 = sb.toString();
        System.out.println("After changing char 0 to 'J': " + s1);

        s3 = "";
        System.out.println("Is s3 empty? " + (s3.isEmpty() ? "Yes" : "No"));
    }

    // ============================================================
    // EXAMPLE 6: String Operations. find->indexOf, substr->substring,
    // replace/insert/erase via StringBuilder, append via concat/StringBuilder.
    // ============================================================
    static void example6_string_operations() {
        System.out.println("\n=== EXAMPLE 6: String Operations ===");

        String text = "The quick brown fox";

        int pos = text.indexOf("quick");
        System.out.println("Text: " + text);
        System.out.println("Position of 'quick': " + pos);

        // C++ substr(start, length); Java substring(start, endExclusive).
        String sub = text.substring(4, 4 + 5);
        System.out.println("Substring from position 4, length 5: " + sub);

        // replace 5 chars starting at 6 with "C++"
        StringBuilder original = new StringBuilder("Hello World");
        original.replace(6, 6 + 5, "C++");
        System.out.println("After replace: " + original);

        StringBuilder s = new StringBuilder("Hello World");
        s.insert(5, " Beautiful");
        System.out.println("After insert: " + s);

        s = new StringBuilder("Hello World");
        s.delete(5, 5 + 6);  // erase from position 5, length 6
        System.out.println("After erase: " + s);

        String appended = "Hello" + " World!";
        System.out.println("After append: " + appended);
    }

    // ============================================================
    // EXAMPLE 7: Case Conversion + palindrome. Java has toUpperCase/
    // toLowerCase on String, and StringBuilder.reverse().
    // ============================================================
    static void example7_case_conversion() {
        System.out.println("\n=== EXAMPLE 7: String Case Conversion ===");

        String text = "Hello World";

        String upper = text.toUpperCase();
        System.out.println("Original: " + text);
        System.out.println("Uppercase: " + upper);

        String lower = text.toLowerCase();
        System.out.println("Lowercase: " + lower);

        String word = "racecar";
        String reversed = new StringBuilder(word).reverse().toString();
        System.out.println("\nWord: " + word);
        System.out.println("Reversed: " + reversed);
        System.out.println("Is palindrome? " + (word.equals(reversed) ? "Yes" : "No"));
    }

    // ============================================================
    // EXAMPLE 8: String Comparison & Search. Use .equals for content
    // equality (== compares references!), compareTo for lexicographic order.
    // ============================================================
    static void example8_string_comparison() {
        System.out.println("\n=== EXAMPLE 8: String Comparison & Search ===");

        String s1 = "apple";
        String s2 = "apple";
        String s3 = "banana";

        // In Java, use .equals for content (== would test object identity).
        System.out.println("\"apple\".equals(\"apple\")? " + (s1.equals(s2) ? "Yes" : "No"));
        System.out.println("\"apple\".equals(\"banana\")? " + (s1.equals(s3) ? "Yes" : "No"));

        System.out.println("\"apple\" < \"banana\"? " + (s1.compareTo(s3) < 0 ? "Yes" : "No"));
        System.out.println("\"banana\" < \"apple\"? " + (s3.compareTo(s1) < 0 ? "Yes" : "No"));

        String text = "Hello World";
        int pos = text.indexOf('o');
        System.out.println("\nFirst 'o' in \"" + text + "\" at position: " + pos);

        int count = 0;
        String s = "banana";
        for (char c : s.toCharArray()) {
            if (c == 'a') count++;
        }
        System.out.println("Occurrences of 'a' in \"banana\": " + count);
    }

    // ============================================================
    // EXAMPLE 9: Array vs ArrayList (Java's vector-vs-array story).
    // ============================================================
    static void example9_vector_vs_array() {
        System.out.println("\n=== EXAMPLE 9: Array vs ArrayList ===");

        int[] arr = {1, 2, 3, 4, 5};
        System.out.print("Array (fixed size 5): ");
        for (int x : arr) System.out.print(x + " ");
        System.out.println();

        List<Integer> v = new ArrayList<>();
        System.out.println("\nArrayList (dynamic):");
        for (int i = 1; i <= 5; i++) {
            v.add(i);
            System.out.println("  Added " + i + ", size: " + v.size());
        }

        System.out.print("\nArrayList elements: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        System.out.println("\nIteration:");
        System.out.print("Array with loop: ");
        for (int i = 0; i < arr.length; i++) System.out.print(arr[i] + " ");
        System.out.println();

        System.out.print("ArrayList with for-each: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 10: Complex Array/String Operations.
    // Word splitting, longest word, anagram check.
    // ============================================================
    static void example10_complex_operations() {
        System.out.println("\n=== EXAMPLE 10: Complex Operations ===");

        String sentence = "The quick brown fox jumps";
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();

        for (char c : sentence.toCharArray()) {
            if (c == ' ') {
                if (word.length() > 0) words.add(word.toString());
                word.setLength(0);
            } else {
                word.append(c);
            }
        }
        if (word.length() > 0) words.add(word.toString());

        System.out.println("Sentence: " + sentence);
        System.out.print("Words: ");
        for (String w : words) System.out.print(w + " ");
        System.out.println();

        String longest = words.get(0);
        for (String w : words) {
            if (w.length() > longest.length()) longest = w;
        }
        System.out.println("Longest word: " + longest + " (length: " + longest.length() + ")");

        String a1 = "listen";
        String a2 = "silent";
        char[] c1 = a1.toCharArray();
        char[] c2 = a2.toCharArray();
        Arrays.sort(c1);
        Arrays.sort(c2);
        boolean anagram = Arrays.equals(c1, c2);
        System.out.print("\nAre \"" + a1 + "\" and \"" + a2 + "\" anagrams? ");
        System.out.println(anagram ? "Yes" : "No");
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 5: ARRAYS & STRINGS (Java)");
        System.out.println("======================================================");

        example1_array_basics();
        example2_multidimensional_arrays();
        example3_search_sort();
        example4_vector();
        example5_string_basics();
        example6_string_operations();
        example7_case_conversion();
        example8_string_comparison();
        example9_vector_vs_array();
        example10_complex_operations();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
