// Chapter 21: Regular Expressions (java.util.regex)
// Compiles and runs on Java 17+ (uses Matcher.replaceAll(Function) and
// Matcher.results(), both Java 9+). No external dependencies.
//
//   javac chapter21_regular_expressions.java
//   java  chapter21_regular_expressions

import java.util.regex.*;
import java.util.*;
import java.util.stream.*;

public class chapter21_regular_expressions {

    public static void main(String[] args) {
        compileMatchFind();
        groups();
        replacement();
        splitting();
        findLoop();
        resultsStream();
        practical();
    }

    // --- 1. compile / matches / find ----------------------------------------
    static void compileMatchFind() {
        System.out.println("=== 1. compile, matches vs find vs lookingAt ===");

        // Compile once, reuse. \\d+ == one or more digits.
        Pattern digits = Pattern.compile("\\d+");

        System.out.println("matches('123')    = " + digits.matcher("123").matches());     // true
        System.out.println("matches('123abc') = " + digits.matcher("123abc").matches());  // false
        System.out.println("lookingAt('123ab')= " + digits.matcher("123ab").lookingAt()); // true (prefix)
        System.out.println("find in 'a1b22'   = " + digits.matcher("a1b22").find());      // true

        // Case-insensitive flag
        Pattern hi = Pattern.compile("hello", Pattern.CASE_INSENSITIVE);
        System.out.println("CASE_INSENSITIVE 'HELLO' = " + hi.matcher("HELLO").matches()); // true
        System.out.println();
    }

    // --- 2. capturing groups: numbered + named ------------------------------
    static void groups() {
        System.out.println("=== 2. numbered and named groups ===");

        Pattern date = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
        Matcher m = date.matcher("event on 2026-06-23 today");

        if (m.find()) {
            System.out.println("whole match group(0) = " + m.group());          // 2026-06-23
            System.out.println("group(1)             = " + m.group(1));          // 2026
            System.out.println("group(\"month\")       = " + m.group("month"));  // 06
            System.out.println("group(\"day\")         = " + m.group("day"));    // 23
            System.out.println("start..end           = " + m.start() + ".." + m.end());
        }

        // Backreference: find a doubled word
        Matcher dup = Pattern.compile("\\b(\\w+)\\s+\\1\\b").matcher("the the cat sat sat");
        while (dup.find()) {
            System.out.println("doubled word: " + dup.group(1));                 // the, then sat
        }
        System.out.println();
    }

    // --- 3. replaceAll with group references and a Function -----------------
    static void replacement() {
        System.out.println("=== 3. replacement with references and Function ===");

        // $1 / $2 numbered references: "First Last" -> "Last, First"
        System.out.println("Ada Lovelace -> "
                + "Ada Lovelace".replaceAll("(\\w+)\\s+(\\w+)", "$2, $1"));      // Lovelace, Ada

        // ${name} named references: reformat the date
        Pattern d = Pattern.compile("(?<y>\\d{4})-(?<m>\\d{2})-(?<day>\\d{2})");
        System.out.println("2026-06-23 -> "
                + d.matcher("2026-06-23").replaceAll("${day}/${m}/${y}"));        // 23/06/2026

        // replaceAll(Function) (Java 9+): uppercase each word
        String shout = Pattern.compile("\\b\\w+\\b")
                              .matcher("hello regex world")
                              .replaceAll(mr -> mr.group().toUpperCase());
        System.out.println("uppercased: " + shout);                              // HELLO REGEX WORLD
        System.out.println();
    }

    // --- 4. split -----------------------------------------------------------
    static void splitting() {
        System.out.println("=== 4. split on a delimiter pattern ===");

        // Split on a comma surrounded by optional whitespace
        String[] parts = "a, b ,c,  d".split("\\s*,\\s*");
        System.out.println("split = " + Arrays.toString(parts));                 // [a, b, c, d]
        System.out.println();
    }

    // --- 5. find() loop with start/end --------------------------------------
    static void findLoop() {
        System.out.println("=== 5. iterating matches with find() ===");

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher("order 42 ships in 7 days, qty 100");
        while (m.find()) {
            System.out.println("found '" + m.group() + "' at [" + m.start() + ", " + m.end() + ")");
        }
        System.out.println();
    }

    // --- 6. Matcher.results() stream (Java 9+) ------------------------------
    static void resultsStream() {
        System.out.println("=== 6. Matcher.results() stream ===");

        int total = Pattern.compile("\\d+")
                           .matcher("3 apples, 4 pears, 5 plums")
                           .results()                       // Stream<MatchResult>
                           .map(MatchResult::group)
                           .mapToInt(Integer::parseInt)
                           .sum();
        System.out.println("sum of numbers = " + total);                         // 12
        System.out.println();
    }

    // --- 7. practical: email-ish, key=value, tokenize -----------------------
    static void practical() {
        System.out.println("=== 7. practical examples ===");

        // Pragmatic email check (not full RFC 5322)
        Pattern email = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
        System.out.println("ada@example.com valid? " + email.matcher("ada@example.com").matches()); // true
        System.out.println("nope@@x valid?        " + email.matcher("nope@@x").matches());          // false

        // Extract key=value pairs into a Map. (MatchResult.group(String) is
        // Java 20+, so over results() we use the numbered groups 1 and 2,
        // which keeps this example compiling on Java 17.)
        Pattern kv = Pattern.compile("(\\w+)=(\\w+)");
        Map<String, String> config = new LinkedHashMap<>();
        kv.matcher("host=localhost port=8080 tls=on")
          .results()
          .forEach(mr -> config.put(mr.group(1), mr.group(2)));
        System.out.println("config = " + config);            // {host=localhost, port=8080, tls=on}

        // Tokenize a simple arithmetic expression
        Pattern token = Pattern.compile("\\d+|[-+*/()]");
        List<String> tokens = token.matcher("12+34*(5-6)")
                                   .results()
                                   .map(MatchResult::group)
                                   .collect(Collectors.toList());
        System.out.println("tokens = " + tokens);            // [12, +, 34, *, (, 5, -, 6, )]
    }
}
