// Chapter 25: Testing - Runnable Java Example
// Compile: javac chapter25_testing.java
// Run:     java chapter25_testing
//
// JUnit 5 is NOT on this book's bare classpath, so this file does not use it.
// Instead it implements a TINY hand-rolled test harness (a few static assert
// helpers that count passes/failures and print a summary) and uses it to test
// two small functions: reverse(String) and isPalindrome(String).
//
// This demonstrates the CORE IDEA every test framework is built on:
//   1. call the code under test,
//   2. assert the result matches what you expect,
//   3. count pass/fail and report.
//
// At the bottom is a comment block showing the EQUIVALENT JUnit 5 test for the
// same functions (illustrative only -- it is NOT compiled here).
//
// Self-contained: no external dependencies. Runs on Java 17+.

public class chapter25_testing {

    // ============================================================
    // CODE UNDER TEST -- the small functions we want to verify.
    // ============================================================

    /** Returns the input string reversed. reverse("abc") -> "cba". */
    static String reverse(String s) {
        if (s == null) throw new IllegalArgumentException("input must not be null");
        return new StringBuilder(s).reverse().toString();
    }

    /** True if s reads the same forwards and backwards (case-sensitive, as given). */
    static boolean isPalindrome(String s) {
        if (s == null) throw new IllegalArgumentException("input must not be null");
        return s.equals(reverse(s));
    }

    // ============================================================
    // A MINIMAL TEST HARNESS -- the essence of a test framework.
    //   - static counters track total / passed / failed
    //   - each assert helper checks a condition and records the outcome
    //   - printSummary() reports at the end and sets the exit code
    // ============================================================

    static int total = 0;
    static int passed = 0;
    static int failed = 0;

    /** Core check: record a pass or a detailed fail line. */
    static void check(boolean condition, String testName, String detail) {
        total++;
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + testName);
        } else {
            failed++;
            System.out.println("  [FAIL] " + testName + "  --  " + detail);
        }
    }

    static void assertTrue(boolean condition, String testName) {
        check(condition, testName, "expected: true, but was: false");
    }

    static void assertEquals(Object expected, Object actual, String testName) {
        boolean eq = (expected == null) ? actual == null : expected.equals(actual);
        check(eq, testName, "expected: <" + expected + ">, but was: <" + actual + ">");
    }

    /** Asserts that running 'action' throws an exception of the given type. */
    static void assertThrows(Class<? extends Throwable> expectedType,
                             Runnable action, String testName) {
        try {
            action.run();
            check(false, testName, "expected " + expectedType.getSimpleName()
                                   + " but nothing was thrown");
        } catch (Throwable t) {
            check(expectedType.isInstance(t), testName,
                  "expected " + expectedType.getSimpleName()
                  + " but threw " + t.getClass().getSimpleName());
        }
    }

    static void printSummary() {
        System.out.println();
        System.out.println("=".repeat(48));
        System.out.printf("Test summary: %d run, %d passed, %d failed%n",
                          total, passed, failed);
        System.out.println(failed == 0 ? "RESULT: ALL TESTS PASSED"
                                        : "RESULT: THERE WERE FAILURES");
        System.out.println("=".repeat(48));
    }

    // ============================================================
    // THE TESTS -- Arrange / Act / Assert, one concept per check.
    // ============================================================

    static void testReverse() {
        System.out.println("\n--- reverse(String) ---");
        assertEquals("cba", reverse("abc"), "reverse of \"abc\" is \"cba\"");
        assertEquals("a", reverse("a"), "reverse of single char is itself");
        assertEquals("", reverse(""), "reverse of empty string is empty");
        assertEquals("olleh", reverse("hello"), "reverse of \"hello\"");
        // reversing twice returns the original (a useful property-style check)
        assertEquals("abc", reverse(reverse("abc")), "reverse is its own inverse");
    }

    static void testIsPalindrome() {
        System.out.println("\n--- isPalindrome(String) ---");
        assertTrue(isPalindrome("racecar"), "\"racecar\" is a palindrome");
        assertTrue(isPalindrome("level"), "\"level\" is a palindrome");
        assertTrue(isPalindrome(""), "empty string is a palindrome");
        assertTrue(isPalindrome("a"), "single char is a palindrome");
        assertTrue(!isPalindrome("hello"), "\"hello\" is NOT a palindrome");
    }

    static void testErrorCases() {
        System.out.println("\n--- error cases ---");
        assertThrows(IllegalArgumentException.class,
                     () -> reverse(null),
                     "reverse(null) throws IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                     () -> isPalindrome(null),
                     "isPalindrome(null) throws IllegalArgumentException");
    }

    public static void main(String[] args) {
        System.out.println("Running hand-rolled test suite for chapter25_testing");

        testReverse();
        testIsPalindrome();
        testErrorCases();

        printSummary();

        // A real runner signals failure to the build via a non-zero exit code.
        System.exit(failed == 0 ? 0 : 1);
    }

    // ============================================================
    // EQUIVALENT JUnit 5 TEST (illustrative -- NOT compiled here).
    // With JUnit on the classpath you would delete the harness above
    // and write the following instead. The framework provides the
    // counting, reporting, and exit-code handling for you, and the
    // build tool (mvn test / gradle test) discovers and runs it.
    //
    //   import org.junit.jupiter.api.Test;
    //   import org.junit.jupiter.api.DisplayName;
    //   import org.junit.jupiter.params.ParameterizedTest;
    //   import org.junit.jupiter.params.provider.CsvSource;
    //   import org.junit.jupiter.params.provider.ValueSource;
    //   import static org.junit.jupiter.api.Assertions.*;
    //
    //   class StringUtilTest {
    //
    //       @Test
    //       @DisplayName("reverse flips the characters")
    //       void reversesString() {
    //           assertEquals("cba", StringUtil.reverse("abc"));
    //           assertEquals("",    StringUtil.reverse(""));
    //       }
    //
    //       @Test
    //       void reverseIsItsOwnInverse() {
    //           assertEquals("abc", StringUtil.reverse(StringUtil.reverse("abc")));
    //       }
    //
    //       // Data-driven: one method, many inputs.
    //       @ParameterizedTest
    //       @ValueSource(strings = {"racecar", "level", "noon", "a", ""})
    //       void detectsPalindromes(String word) {
    //           assertTrue(StringUtil.isPalindrome(word));
    //       }
    //
    //       @ParameterizedTest
    //       @CsvSource({ "abc, cba", "hello, olleh" })
    //       void reversesViaCsv(String input, String expected) {
    //           assertEquals(expected, StringUtil.reverse(input));
    //       }
    //
    //       @Test
    //       void rejectsNull() {
    //           assertThrows(IllegalArgumentException.class,
    //                        () -> StringUtil.reverse(null));
    //       }
    //   }
    // ============================================================
}
