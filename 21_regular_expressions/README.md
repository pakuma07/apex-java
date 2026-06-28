# Chapter 21: Regular Expressions (java.util.regex) -- Java

A **regular expression** (regex) is a compact pattern language for describing sets of strings — used to validate input, search text, split on delimiters, and extract structured data. Java's support lives in the **`java.util.regex`** package (since Java 1.4), built around two classes: **`Pattern`** (a compiled regex) and **`Matcher`** (the engine that applies a pattern to a specific input). Java also exposes regex through convenience methods on `String` (`matches`, `split`, `replaceAll`, `replaceFirst`). Java's flavor is a Perl-compatible (PCRE-like) backtracking NFA, so most patterns you know from grep, Perl, or JavaScript carry over with minor syntax differences.

This chapter covers compiling and reusing a `Pattern`, the difference between `matches`/`find`/`lookingAt`, capturing and named groups, the metacharacter and quantifier vocabulary, lookahead/lookbehind, replacement with group references and computed replacements, streaming matches, escaping literal text, and the catastrophic-backtracking (ReDoS) trap to avoid. Throughout, the cardinal rule holds: **compile a `Pattern` once and reuse it** — recompiling a regex inside a loop is a common and costly mistake.

> **C++ contrast — `<regex>` → Java equivalents — at a glance**
> - `std::regex` (compiled pattern) → `Pattern` (`Pattern.compile`)
> - `std::smatch` / `std::cmatch` (match results) → `Matcher` (`group`, `start`, `end`)
> - `std::regex_match` (whole input) → `Matcher.matches()` / `String.matches`
> - `std::regex_search` (find anywhere) → `Matcher.find()`
> - `std::regex_replace` → `Matcher.replaceAll` / `String.replaceAll`
> - `std::sregex_iterator` (iterate matches) → `while (m.find())` / `Matcher.results()` stream (Java 9+)
> - `std::regex::icase` flag → `Pattern.CASE_INSENSITIVE`
> - `std::regex_constants::ECMAScript` (default grammar) → Java's Perl-like flavor (no grammar selection needed)

## 21.1 Pattern and Matcher

The fundamental workflow is: **compile** a regex into a `Pattern` (a thread-safe, immutable object that does the expensive parsing once), then create a `Matcher` from it for each input string (a `Matcher` is *not* thread-safe and is cheap to create). Compilation is the costly step, so a `Pattern` should be a constant — typically a `private static final Pattern` field — and reused across many inputs. The `Matcher` holds the per-match state (position, captured groups) and is discarded or reset between inputs.

```java
import java.util.regex.*;

// Compile ONCE — ideally a static final constant reused for the program's life
Pattern pattern = Pattern.compile("\\d+");        // one or more digits

// Create a Matcher PER input (cheap; not thread-safe)
Matcher matcher = pattern.matcher("order 42 ships in 7 days");

while (matcher.find()) {                            // walk every match
    System.out.println(matcher.group());            // 42, then 7
}

// A Matcher can be reused for a new input via reset(...)
matcher.reset("only 1 here");
matcher.find();
System.out.println(matcher.group());                // 1
```

Note the **double backslash**: in Java source, `"\\d"` is the two-character string `\d` that the regex engine then interprets. A single `"\d"` is a compile error because `\d` is not a valid Java string escape. (Text blocks, `"""..."""`, do not help here — they still process backslash escapes — so regex patterns always carry doubled backslashes.)

> **C++ contrast:** This is exactly the `std::regex` / `std::smatch` split. `Pattern.compile` ≈ constructing a `std::regex` (do it once, not per match); `Matcher` ≈ `std::smatch`. Java enforces the double-backslash because the *Java string literal* is processed first — the same reason C++ programmers use raw string literals `R"(\d+)"` to avoid escaping `\d` as `\\d`.

---

## 21.2 Compilation Flags

`Pattern.compile` takes an optional second `int` argument of OR-combined flags that change matching behavior globally for the pattern. The most useful four:

| Flag | Constant | Effect |
|------|----------|--------|
| Case-insensitive | `Pattern.CASE_INSENSITIVE` | `a` matches `a` and `A` (ASCII; add `UNICODE_CASE` for full Unicode) |
| Multiline | `Pattern.MULTILINE` | `^` and `$` match at every line boundary, not just string start/end |
| Dot-all | `Pattern.DOTALL` | `.` also matches line terminators (`\n`) |
| Comments | `Pattern.COMMENTS` | Whitespace in the pattern is ignored and `#` starts a comment |

```java
import java.util.regex.*;

// Multiple flags combined with bitwise OR
Pattern p = Pattern.compile("^error", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
Matcher m = p.matcher("warn: ok\nERROR: boom\nError: again");
while (m.find()) System.out.println(m.start());      // matches "ERROR" and "Error"

// DOTALL: let . cross newlines (e.g. match an entire multi-line block)
Pattern block = Pattern.compile("start.*end", Pattern.DOTALL);
System.out.println(block.matcher("start\nmiddle\nend").matches());  // true

// COMMENTS (a.k.a. "extended"/"x" mode): self-documenting patterns
Pattern phone = Pattern.compile("""
        \\d{3}    # area code
        -         # separator
        \\d{4}    # local number
        """, Pattern.COMMENTS);
System.out.println(phone.matcher("555-1234").matches());            // true
```

Flags can also be set *inline* inside the pattern with the `(?flags)` syntax — `(?i)` for case-insensitive, `(?m)` multiline, `(?s)` dot-all, `(?x)` comments — and scoped to part of the pattern with `(?i:...)`. For example `"(?i)hello"` is equivalent to compiling `"hello"` with `CASE_INSENSITIVE`.

---

## 21.3 The String Convenience Methods

`String` exposes four regex shortcuts that compile a throwaway `Pattern` internally:

- `str.matches(regex)` — does the **whole** string match? (anchored end to end, like `Matcher.matches`)
- `str.split(regex)` — split into a `String[]` on the delimiter pattern
- `str.replaceAll(regex, replacement)` — replace every match
- `str.replaceFirst(regex, replacement)` — replace only the first match

```java
// Whole-string validation
System.out.println("12345".matches("\\d+"));          // true
System.out.println("12a45".matches("\\d+"));          // false (the 'a' breaks it)

// Split on a delimiter pattern (one or more commas/spaces)
String[] parts = "a, b ,c,  d".split("\\s*,\\s*");    // [a, b, c, d]

// Replace
String masked = "call 555-1234".replaceAll("\\d", "#");   // "call ###-####"
String once   = "a-b-c".replaceFirst("-", "+");           // "a+b-c"
```

These are convenient for one-off use, but each call **recompiles the regex**. Inside a loop or a hot method that is pure waste — compile a `Pattern` once and reuse it instead:

```java
// ❌ Recompiles the regex on every iteration
for (String s : inputs) {
    if (s.matches("\\d{4}-\\d{2}-\\d{2}")) process(s);
}

// ✅ Compile once, reuse the Matcher per input
private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
for (String s : inputs) {
    if (DATE.matcher(s).matches()) process(s);
}
```

> **C++ contrast:** `String.matches`/`split`/`replaceAll` are convenience wrappers with no exact `std::string` analogue — C++ has no member functions that take a regex. They correspond to free functions `std::regex_match` / `std::regex_replace` plus manual token-splitting with `std::sregex_token_iterator`. The same performance advice applies in both languages: constructing the pattern is expensive, so hoist it out of loops.

---

## 21.4 matches vs find vs lookingAt

These three `Matcher` methods differ in *where* and *how much* of the input the pattern must cover — a frequent source of confusion:

| Method | Anchored at start? | Must reach end? | Advances? |
|--------|--------------------|-----------------|-----------|
| `matches()` | yes | yes (entire input) | no |
| `lookingAt()` | yes | no (just a prefix) | no |
| `find()` | no (anywhere) | no | yes (resumes after last match) |

```java
import java.util.regex.*;

Pattern p = Pattern.compile("\\d+");

System.out.println(p.matcher("123").matches());        // true  (all digits)
System.out.println(p.matcher("123abc").matches());     // false (trailing letters)

System.out.println(p.matcher("123abc").lookingAt());   // true  (matches the prefix)
System.out.println(p.matcher("abc123").lookingAt());   // false (doesn't start with digits)

Matcher m = p.matcher("a1b22c333");
while (m.find()) System.out.print(m.group() + " ");    // 1 22 333
```

The key takeaway: use `matches()` for whole-string **validation**, `find()` for **searching/extraction** (it can be called repeatedly to walk all matches), and `lookingAt()` for the rarer "does the input *begin* with this pattern" check.

> **C++ contrast:** `matches()` ≈ `std::regex_match` (whole sequence), `find()` ≈ `std::regex_search` (anywhere). `lookingAt()` has no single `<regex>` equivalent — you would anchor the pattern with `^` and use `regex_search`. Note Java's `matches()` is *implicitly* anchored at both ends (no `^`/`$` needed), unlike `find()`.

---

## 21.5 Match Boundaries and Groups

After a successful `matches`, `find`, or `lookingAt`, the `Matcher` exposes the matched text and its position. `group()` (or `group(0)`) returns the entire match; `start()` and `end()` return its half-open `[start, end)` character offsets. **Capturing groups** — parenthesized sub-patterns — are numbered left to right by their opening parenthesis (group 0 is the whole match), and `group(n)`, `start(n)`, `end(n)` retrieve each one.

```java
import java.util.regex.*;

Pattern p = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");   // year-month-day
Matcher m = p.matcher("date: 2026-06-23 end");

if (m.find()) {
    System.out.println(m.group());     // 2026-06-23  (group 0 = whole match)
    System.out.println(m.group(1));    // 2026        (first ( ... ))
    System.out.println(m.group(2));    // 06
    System.out.println(m.group(3));    // 23
    System.out.println(m.start() + ".." + m.end());   // 6..16  (offsets into input)
    System.out.println(m.start(2));    // 11           (where month begins)
}
```

A group that did not participate in the match (e.g. inside an unmatched alternative) returns `null` from `group(n)`, so guard for `null` when groups are optional.

---

## 21.6 Named, Non-Capturing, and Backreferenced Groups

Numbered groups become hard to read when there are many; **named groups** `(?<name>...)` let you retrieve a capture by name with `group("name")`. **Non-capturing groups** `(?:...)` group sub-patterns for quantification or alternation *without* allocating a numbered slot — use them when you only need grouping, not the captured text. **Backreferences** match the same text a prior group captured: `\1` (numbered) or `\k<name>` (named) inside the pattern.

```java
import java.util.regex.*;

// Named groups — readable extraction
Pattern p = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
Matcher m = p.matcher("2026-06-23");
if (m.matches()) {
    System.out.println(m.group("year"));    // 2026
    System.out.println(m.group("month"));   // 06
}

// Non-capturing group: group "ab" for the quantifier, but don't capture it
Pattern rep = Pattern.compile("(?:ab)+");
System.out.println(rep.matcher("ababab").matches());          // true

// Backreference: detect a doubled word (\\1 must equal what group 1 matched)
Pattern dup = Pattern.compile("\\b(\\w+)\\s+\\1\\b");
Matcher dm = dup.matcher("the the cat");
if (dm.find()) System.out.println("doubled: " + dm.group(1)); // the

// Named backreference: matching opening/closing tag names
Pattern tag = Pattern.compile("<(?<t>\\w+)>.*</\\k<t>>");
System.out.println(tag.matcher("<b>hi</b>").matches());       // true
```

Named groups are still numbered as well, so `(?<year>\d{4})` is simultaneously group 1 — both `group(1)` and `group("year")` work.

> **C++ contrast:** Non-capturing `(?:...)` and numbered backreferences `\1` work identically in `std::regex` (ECMAScript grammar). However, **named groups** `(?<name>...)` are *not* supported by `std::regex` — C++ only offers numbered groups via `smatch[n]`. Java's named-group support is a meaningful readability advantage over the C++ standard library here.

---

## 21.7 Metacharacters, Classes, and Quantifiers

The pattern language itself is shared across regex flavors. The essentials:

**Predefined character classes** (each matches one character):

| Token | Matches | Negation |
|-------|---------|----------|
| `.` | any character except line terminator (unless `DOTALL`) | — |
| `\d` | a digit `[0-9]` | `\D` |
| `\w` | a word char `[a-zA-Z0-9_]` | `\W` |
| `\s` | whitespace | `\S` |

**Custom character classes** use `[...]`: `[aeiou]` (a set), `[a-z]` (a range), `[^0-9]` (negation — *not* a digit), `[a-fA-F0-9]` (combined ranges for hex).

**Anchors and boundaries** match a *position*, not a character: `^` (start of input, or line in `MULTILINE`), `$` (end), `\b` (word boundary), `\B` (non-boundary), `\A`/`\z` (absolute start/end of input).

**Quantifiers** specify repetition of the preceding element:

| Quantifier | Meaning |
|------------|---------|
| `*` | zero or more |
| `+` | one or more |
| `?` | zero or one (optional) |
| `{n}` | exactly n |
| `{n,}` | n or more |
| `{n,m}` | between n and m |

By default quantifiers are **greedy** — they consume as much as possible, then backtrack if the rest of the pattern fails. Appending `?` makes them **lazy** (reluctant) — consume as little as possible. Appending `+` makes them **possessive** — consume greedily and *never give back* (no backtracking), which can prevent catastrophic backtracking at the cost of some matches.

```java
import java.util.regex.*;

String html = "<a><b>";

// Greedy .* grabs everything up to the LAST '>'
Matcher greedy = Pattern.compile("<.*>").matcher(html);
greedy.find();  System.out.println(greedy.group());   // <a><b>   (whole thing)

// Lazy .*? grabs the SHORTEST match — up to the first '>'
Matcher lazy = Pattern.compile("<.*?>").matcher(html);
lazy.find();    System.out.println(lazy.group());     // <a>

// Possessive .*+ grabs all and refuses to backtrack — here it then can't match '>'
Matcher poss = Pattern.compile("<.*+>").matcher(html);
System.out.println(poss.find());                      // false

// Alternation: this OR that
System.out.println("cat".matches("cat|dog|bird"));    // true
```

> **C++ contrast:** The class shortcuts, anchors, and `*`/`+`/`?`/`{n,m}` quantifiers, greedy-vs-lazy (`*?`), and alternation `|` all behave the same in `std::regex` (ECMAScript). **Possessive quantifiers** (`*+`, `++`) and atomic groups are *not* part of the C++ standard `<regex>` flavor — they are a Java (Perl/Java) feature, and they are the principal tool for hardening a pattern against ReDoS (Section 21.10).

---

## 21.8 Lookahead and Lookbehind

**Zero-width assertions** test whether a pattern does or does not occur at the current position *without consuming any characters*. They are invaluable for "match X only when followed/preceded by Y" rules.

| Syntax | Name | Meaning |
|--------|------|---------|
| `(?=...)` | positive lookahead | next chars match `...` |
| `(?!...)` | negative lookahead | next chars do **not** match `...` |
| `(?<=...)` | positive lookbehind | preceding chars match `...` |
| `(?<!...)` | negative lookbehind | preceding chars do **not** match `...` |

```java
import java.util.regex.*;

// Positive lookahead: a number that is FOLLOWED BY "px", but don't include "px"
Matcher m = Pattern.compile("\\d+(?=px)").matcher("width:120px height:40pt");
while (m.find()) System.out.println(m.group());   // 120   (40pt is excluded)

// Negative lookahead: a word NOT followed by a colon
System.out.println("foo".matches("\\w+(?!:)"));   // (asserts no following ':')

// Positive lookbehind: the amount AFTER a '$' sign, excluding the '$'
Matcher price = Pattern.compile("(?<=\\$)\\d+").matcher("costs $50");
if (price.find()) System.out.println(price.group());   // 50

// Password-style rule: at least one digit somewhere (lookahead) AND length >= 8
Pattern pw = Pattern.compile("(?=.*\\d).{8,}");
System.out.println(pw.matcher("abc12345").matches());  // true
System.out.println(pw.matcher("abcdefgh").matches());  // false (no digit)
```

In Java, lookbehind must be *bounded* — it may contain quantifiers like `{2,5}` but not unbounded `*`/`+` of arbitrary length (the engine needs a maximum length to look back). Lookahead has no such restriction.

> **C++ contrast:** **Lookbehind** `(?<=...)`/`(?<!...)` is *not supported* by `std::regex` (ECMAScript grammar) — only lookahead is. Java supports both, making "match based on preceding context" patterns far easier than in standard C++.

---

## 21.9 Replacement and Streaming Matches

In a replacement string, `$n` inserts the text of capturing group `n`, and `${name}` inserts a named group; a literal `$` or `\` must be escaped as `\$` / `\\`. For replacements that require *logic* (uppercasing, lookups, formatting) rather than a fixed template, Java 9 added `Matcher.replaceAll(Function<MatchResult,String>)`, which calls your function for each match. Java 9 also added `Matcher.results()`, returning a `Stream<MatchResult>` of all matches for fluent processing.

```java
import java.util.regex.*;
import java.util.stream.*;

// Group references in the replacement: swap "First Last" -> "Last, First"
String swapped = "Ada Lovelace".replaceAll("(\\w+)\\s+(\\w+)", "$2, $1");
System.out.println(swapped);                          // Lovelace, Ada

// Named-group reference ${...}
Pattern d = Pattern.compile("(?<y>\\d{4})-(?<m>\\d{2})-(?<day>\\d{2})");
System.out.println(d.matcher("2026-06-23").replaceAll("${day}/${m}/${y}"));  // 23/06/2026

// Computed replacement with a Function (Java 9+): uppercase every matched word
Pattern word = Pattern.compile("\\b\\w+\\b");
String shout = word.matcher("hello regex world")
                   .replaceAll(mr -> mr.group().toUpperCase());
System.out.println(shout);                            // HELLO REGEX WORLD

// Matcher.results() stream (Java 9+): collect all numbers as ints, then sum
int total = Pattern.compile("\\d+").matcher("3 apples, 4 pears, 5 plums")
                   .results()                          // Stream<MatchResult>
                   .map(MatchResult::group)
                   .mapToInt(Integer::parseInt)
                   .sum();
System.out.println(total);                            // 12
```

> **C++ contrast:** `$1`/`$2` group references in the replacement string work in `std::regex_replace` too (ECMAScript uses `$1`). C++ has **no built-in computed replacement** equivalent to `replaceAll(Function)` — you must iterate matches manually and rebuild the string. `Matcher.results()` returning a `Stream<MatchResult>` is the lazy analogue of iterating with `std::sregex_iterator`, but composes with the Streams API.

---

## 21.10 Escaping and Catastrophic Backtracking (ReDoS)

When you need to match **literal** text that may contain metacharacters (a user-supplied search term, a file extension like `.txt`), do not hand-escape it — call `Pattern.quote(literal)`, which wraps the string so every character is treated literally. For building a literal replacement string, the analogous helper is `Matcher.quoteReplacement`.

```java
import java.util.regex.*;

String userInput = "a.b+c";                  // '.' and '+' are metacharacters!
// ✅ Treat the whole thing literally
Pattern safe = Pattern.compile(Pattern.quote(userInput));
System.out.println(safe.matcher("a.b+c").matches());     // true
System.out.println(safe.matcher("axbxc").matches());     // false (not literal)

// Literal replacement text containing $ or \
String r = Matcher.quoteReplacement("price: $5");        // keeps "$5" literal
System.out.println("X".replaceAll("X", r));              // price: $5
```

**Catastrophic backtracking (ReDoS).** Java's regex engine is a *backtracking* implementation. Certain patterns applied to certain inputs cause the number of paths the engine explores to grow exponentially, hanging the thread on a short string — a denial-of-service vector if the pattern or input is attacker-controlled. The classic trigger is **nested quantifiers over overlapping alternatives**, such as `(a+)+$` or `(a|a)*$` against a long string of `a`s followed by a non-matching character.

```java
// ❌ DANGER: nested quantifier — can hang for seconds/minutes on ~30 'a's + 'X'
Pattern evil = Pattern.compile("(a+)+$");
// evil.matcher("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaX").matches();  // catastrophic!

// ✅ Mitigations:
// 1. Avoid nesting quantifiers; rewrite to an unambiguous form
Pattern ok = Pattern.compile("a+$");
// 2. Use a POSSESSIVE quantifier to forbid backtracking
Pattern possessive = Pattern.compile("(a++)+$");
// 3. Never run untrusted patterns; validate/limit input length first
```

The defenses: keep patterns simple and unambiguous, prefer possessive quantifiers (`a++`) or atomic groups `(?>...)` where a sub-pattern should not be re-tried, never compile attacker-supplied regexes, and bound the length of attacker-supplied input.

> **C++ contrast:** `std::regex` is *also* a backtracking engine and is *also* vulnerable to catastrophic backtracking — in fact several standard-library implementations are notoriously slow even on benign patterns. Both languages share this NFA-backtracking risk; neither uses the linear-time RE2/Thompson-NFA approach by default. The Java-specific mitigations (possessive quantifiers, atomic groups) are unavailable in standard C++.

---

## 21.11 Practical Examples

Putting the pieces together on real tasks.

```java
import java.util.regex.*;
import java.util.*;

// 1) Email-ish validation (pragmatic, NOT full RFC 5322 — that is enormous)
Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
System.out.println(EMAIL.matcher("ada@example.com").matches());   // true
System.out.println(EMAIL.matcher("nope@@x").matches());           // false

// 2) Extract key=value pairs into a Map (results() stream).
//    Note: MatchResult.group(String) (named access over a stream) is Java 20+,
//    so over results() we use numbered groups 1 and 2 for Java 17 compatibility.
Pattern KV = Pattern.compile("(\\w+)=(\\w+)");
Map<String,String> config = new LinkedHashMap<>();
KV.matcher("host=localhost port=8080 tls=on")
  .results()
  .forEach(mr -> config.put(mr.group(1), mr.group(2)));
System.out.println(config);          // {host=localhost, port=8080, tls=on}

// 3) Tokenize a simple expression into numbers and operators
Pattern TOKEN = Pattern.compile("\\d+|[-+*/()]");
List<String> tokens = TOKEN.matcher("12+34*(5-6)")
                           .results()
                           .map(MatchResult::group)
                           .toList();
System.out.println(tokens);          // [12, +, 34, *, (, 5, -, 6, )]
```

A word of caution on the email example: validating email addresses *fully* per RFC 5322 requires a pattern thousands of characters long, and even then does not prove the address is deliverable. For real systems, a pragmatic pattern plus a confirmation email is the right approach — do not chase regex perfection here.

---

## Best Practices

```java
// ✅ Compile a Pattern ONCE and reuse it — make it a static final constant
private static final Pattern ID = Pattern.compile("[A-Z]{2}\\d{4}");

// ✅ Reuse the constant; create a cheap Matcher per input
boolean valid = ID.matcher(input).matches();

// ✅ Use matches() for whole-string validation, find() for searching/extraction
//    (matches() is implicitly anchored; no ^ / $ needed)

// ✅ Prefer named groups for readability when there are several captures
Pattern.compile("(?<area>\\d{3})-(?<num>\\d{4})");

// ✅ Use non-capturing groups (?:...) when you only need grouping
Pattern.compile("(?:https?|ftp)://\\S+");

// ✅ Escape literal text with Pattern.quote instead of hand-escaping
Pattern.compile(Pattern.quote(userSuppliedTerm));

// ✅ Use COMMENTS mode (or inline (?x)) to document complex patterns
// ✅ Use replaceAll(Function) for computed replacements (Java 9+)

// ❌ Don't recompile a regex inside a loop (String.matches/replaceAll do this)
// ❌ Don't nest quantifiers like (a+)+ — ReDoS risk; use possessive a++ / atomic (?>...)
// ❌ Don't compile attacker-supplied patterns; bound untrusted input length
```

The themes: compile once and reuse, pick the right `Matcher` method for the job, name your groups, escape literals with `Pattern.quote`, and stay alert to catastrophic backtracking when patterns or inputs are not fully under your control.

---

## Summary

| Task | Java API |
|------|----------|
| **Compile a pattern** | `Pattern.compile(regex[, flags])` (reuse as `static final`) |
| **Apply to input** | `pattern.matcher(input)` → `Matcher` (per input, not thread-safe) |
| **Whole-string match** | `Matcher.matches()` / `String.matches` |
| **Search anywhere** | `Matcher.find()` (loop), `Matcher.lookingAt()` (prefix) |
| **Get matched text/pos** | `group()`, `group(n)`, `start()`, `end()` |
| **Named group** | `(?<name>...)`, retrieve with `group("name")` |
| **Non-capturing / backref** | `(?:...)` / `\1`, `\k<name>` |
| **Lookahead / lookbehind** | `(?=)`, `(?!)`, `(?<=)`, `(?<!)` |
| **Replace** | `replaceAll`/`replaceFirst`, `$n`/`${name}`, `replaceAll(Function)` (Java 9+) |
| **Split** | `String.split(regex)`, `Pattern.split` |
| **Stream matches** | `Matcher.results()` → `Stream<MatchResult>` (Java 9+) |
| **Escape literal text** | `Pattern.quote`, `Matcher.quoteReplacement` |
| **Flags** | `CASE_INSENSITIVE`, `MULTILINE`, `DOTALL`, `COMMENTS` (or inline `(?imsx)`) |
| **Avoid** | recompiling in loops; nested quantifiers (ReDoS) |

---

## Next Steps
- Compile reusable `Pattern` constants and pick `matches`/`find`/`lookingAt` deliberately
- Extract structured data with numbered and named capturing groups
- Master the quantifier vocabulary, including lazy and possessive forms
- Use lookahead/lookbehind for context-sensitive matching
- Guard against ReDoS with simple patterns and possessive quantifiers
- Move to [Chapter 22: Networking](../22_networking/README.md)
