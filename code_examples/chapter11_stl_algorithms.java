// Chapter 11: STL Algorithms -> Java Streams API + Collections methods
// Compile/run on Java 17:
//   javac chapter11_stl_algorithms.java
//   java  chapter11_stl_algorithms
//
// MAPPING (C++ <algorithm>/<numeric> -> Java):
//   find / find_if      -> Stream.filter().findFirst(), List.indexOf
//   count / count_if    -> Stream.filter().count()
//   sort                -> Collections.sort / List.sort / Stream.sorted
//   binary_search       -> Collections.binarySearch
//   transform           -> Stream.map (+ collect / mapToInt)
//   copy / copy_if      -> Stream.collect / filter().collect
//   for_each            -> Stream.forEach / Iterable.forEach
//   accumulate          -> Stream.reduce / IntStream.sum
//   fill / replace      -> Collections.fill / Collections.replaceAll / replaceAll
//   reverse / rotate    -> Collections.reverse / Collections.rotate
//   unique / remove     -> distinct() / removeIf / removeAll
//   min/max_element     -> Stream.min/max, Collections.min/max
// Streams are typically NON-MUTATING (produce new results), whereas many STL
// algorithms mutate ranges in place. Collections.* helpers do mutate in place.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class chapter11_stl_algorithms {

    // ============================================================
    // EXAMPLE 1: Find / count (find, find_if, count, count_if)
    // ============================================================
    static void example1_find() {
        System.out.println("\n=== EXAMPLE 1: Find Algorithms ===");
        List<Integer> v = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        int idx = v.indexOf(5); // like find -> position
        if (idx >= 0) System.out.println("Found 5 at position " + idx);

        Optional<Integer> firstGt5 = v.stream().filter(x -> x > 5).findFirst(); // find_if
        firstGt5.ifPresent(x -> System.out.println("First element > 5: " + x));

        long count3 = v.stream().filter(x -> x == 3).count(); // count
        System.out.println("Count of 3: " + count3);

        long countEven = v.stream().filter(x -> x % 2 == 0).count(); // count_if
        System.out.println("Count of even numbers: " + countEven);
    }

    // ============================================================
    // EXAMPLE 2: Sort and binary search
    // ============================================================
    static void example2_sortSearch() {
        System.out.println("\n=== EXAMPLE 2: Sort and Binary Search ===");
        List<Integer> v = new ArrayList<>(List.of(64, 34, 25, 12, 22, 11, 90));
        System.out.println("Original: " + v);

        Collections.sort(v); // ascending, in place
        System.out.println("Sorted ascending: " + v);

        v.sort(Collections.reverseOrder()); // descending
        System.out.println("Sorted descending: " + v);

        Collections.sort(v); // re-sort ascending for binary search
        int pos = Collections.binarySearch(v, 25);
        System.out.println("Binary search for 25: " + (pos >= 0 ? "Found" : "Not found"));
    }

    // ============================================================
    // EXAMPLE 3: Transform (map)
    // ============================================================
    static void example3_transform() {
        System.out.println("\n=== EXAMPLE 3: Transform (map) ===");
        List<Integer> v = List.of(1, 2, 3, 4, 5);
        System.out.println("Original: " + v);

        List<Integer> squared = v.stream().map(x -> x * x).collect(Collectors.toList());
        System.out.println("After square: " + squared);

        List<Integer> halved = squared.stream().map(x -> x / 2).collect(Collectors.toList());
        System.out.println("Halved in new list: " + halved);
    }

    // ============================================================
    // EXAMPLE 4: Copy / copy_if (collect / filter+collect)
    // ============================================================
    static void example4_copy() {
        System.out.println("\n=== EXAMPLE 4: Copy Algorithms ===");
        List<Integer> src = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        List<Integer> dst = new ArrayList<>(src); // copy all
        System.out.println("Copy all: " + dst);

        List<Integer> even = src.stream().filter(x -> x % 2 == 0).collect(Collectors.toList());
        System.out.println("Copy even numbers: " + even);
    }

    // ============================================================
    // EXAMPLE 5: for_each
    // ============================================================
    static void example5_forEach() {
        System.out.println("\n=== EXAMPLE 5: For_each ===");
        List<Integer> v = List.of(1, 2, 3, 4, 5);

        System.out.print("Using forEach to print: ");
        v.forEach(x -> System.out.print(x + " "));
        System.out.println();

        int sum = v.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Sum = " + sum);
    }

    // ============================================================
    // EXAMPLE 6: Accumulate (reduce)
    // ============================================================
    static void example6_accumulate() {
        System.out.println("\n=== EXAMPLE 6: Accumulate (reduce) ===");
        List<Integer> v = List.of(1, 2, 3, 4, 5);

        int sum = v.stream().reduce(0, Integer::sum);
        System.out.println("Sum: " + sum);

        int product = v.stream().reduce(1, (a, b) -> a * b);
        System.out.println("Product: " + product);
    }

    // ============================================================
    // EXAMPLE 7: Fill and replace
    // ============================================================
    static void example7_fillReplace() {
        System.out.println("\n=== EXAMPLE 7: Fill and Replace ===");
        List<Integer> base = List.of(1, 2, 3, 4, 5, 6, 7);
        System.out.println("Original: " + base);

        // fill first 3 with 99 (Collections.fill fills the whole list, so
        // operate on a sublist view to mimic fill(begin, begin+3, 99))
        List<Integer> v1 = new ArrayList<>(base);
        Collections.fill(v1.subList(0, 3), 99);
        System.out.println("After fill first 3 with 99: " + v1);

        // replace value 3 with 999
        List<Integer> v2 = new ArrayList<>(base);
        Collections.replaceAll(v2, 3, 999);
        System.out.println("After replace 3 with 999: " + v2);

        // replace_if even -> 0  (List.replaceAll takes a UnaryOperator)
        List<Integer> v3 = new ArrayList<>(base);
        v3.replaceAll(x -> x % 2 == 0 ? 0 : x);
        System.out.println("After replace even with 0: " + v3);
    }

    // ============================================================
    // EXAMPLE 8: Reverse and rotate
    // ============================================================
    static void example8_reverseRotate() {
        System.out.println("\n=== EXAMPLE 8: Reverse and Rotate ===");
        List<Integer> v = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println("Original: " + v);

        Collections.reverse(v);
        System.out.println("Reversed: " + v);

        // C++ rotate(begin, begin+2, end) brings element[2] to front.
        // Collections.rotate shifts right by distance, so use -2 to match.
        Collections.rotate(v, -2);
        System.out.println("After rotate by 2: " + v);
    }

    // ============================================================
    // EXAMPLE 9: Unique and remove
    // ============================================================
    static void example9_uniqueRemove() {
        System.out.println("\n=== EXAMPLE 9: Unique and Remove ===");
        List<Integer> v1 = List.of(1, 1, 2, 2, 2, 3, 3, 4);
        List<Integer> distinct = v1.stream().distinct().collect(Collectors.toList());
        System.out.println("After distinct: " + distinct);

        List<Integer> v2 = new ArrayList<>(List.of(1, 2, 3, 4, 5, 3, 6, 3, 7));
        v2.removeIf(x -> x == 3); // remove+erase idiom in one call
        System.out.println("After remove 3: " + v2);
    }

    // ============================================================
    // EXAMPLE 10: Min / max
    // ============================================================
    static void example10_minMax() {
        System.out.println("\n=== EXAMPLE 10: Min/Max Elements ===");
        List<Integer> v = List.of(3, 1, 4, 1, 5, 9, 2, 6);

        int min = Collections.min(v);
        int max = Collections.max(v);
        System.out.println("Min element: " + min);
        System.out.println("Max element: " + max);

        // Stream equivalents of min_element / max_element
        Optional<Integer> sMin = v.stream().min(Integer::compareTo);
        Optional<Integer> sMax = v.stream().max(Integer::compareTo);
        System.out.println("Min and max (via stream): " + sMin.get() + " and " + sMax.get());
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 11: STL ALGORITHMS -> JAVA STREAMS");
        System.out.println("======================================================");

        example1_find();
        example2_sortSearch();
        example3_transform();
        example4_copy();
        example5_forEach();
        example6_accumulate();
        example7_fillReplace();
        example8_reverseRotate();
        example9_uniqueRemove();
        example10_minMax();

        // Bonus: IntStream summary statistics (min/max/sum/avg in one pass)
        int[] data = {3, 1, 4, 1, 5, 9, 2, 6};
        var stats = IntStream.of(data).summaryStatistics();
        System.out.println("\nSummaryStatistics: min=" + stats.getMin()
                + " max=" + stats.getMax() + " sum=" + stats.getSum()
                + " avg=" + stats.getAverage());
        System.out.println("Array via Arrays.stream sorted: "
                + Arrays.toString(IntStream.of(data).sorted().toArray()));

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
