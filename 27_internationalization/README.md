# Chapter 27: Internationalization, Formatting & BigDecimal -- Java

Software that crosses borders must adapt: `1,234.56` in the United States is `1.234,56` in Germany and `1 234,56` in France; `$` becomes `€` or `¥`; dates, message wording, and even sort order all depend on the user's **locale**. **Internationalization** (often abbreviated *i18n* — "i", 18 letters, "n") is the practice of writing code that adapts to locale without being rewritten, by externalizing locale-specific data (text, formats) and letting the runtime fill in the right rendering. Java has first-class i18n support: `Locale` to describe a region, `NumberFormat`/`DateTimeFormatter`/`MessageFormat` to render values the local way, and `ResourceBundle` to look up translated text. Alongside i18n this chapter covers **`BigDecimal`**, the class for *exact* decimal arithmetic — indispensable for money, where the floating-point `double` you would reach for in other languages gives subtly *wrong* answers.

> **C++ contrast:** C++ has `std::locale` with `std::num_put`/`std::money_put` facets and `<format>` (C++20), but the model is notoriously awkward and underused. Java's i18n APIs are more uniform and far more widely adopted. For money, C++ has no standard arbitrary-precision decimal type — you reach for a third-party library (e.g. Boost.Multiprecision) or fixed-point integers; Java ships `BigDecimal` in the standard library.

## 27.1 Locale

A `Locale` identifies a language and (optionally) a country/region and variant — it is the parameter that drives every formatting decision. A locale is *not* a character encoding and *not* a time zone; it is "who is reading this, and by what regional conventions". Java provides constants for common locales (`Locale.US`, `Locale.GERMANY`, `Locale.JAPAN`, `Locale.FRANCE`) and the factory `Locale.of(...)` (since Java 19; older code used the now-deprecated `new Locale(...)` constructor).

```java
import java.util.Locale;

// Language only, or language + country
Locale french   = Locale.of("fr");          // French (no country)
Locale canadaFr = Locale.of("fr", "CA");     // French as used in Canada
Locale us       = Locale.US;                 // built-in constant (en, US)
Locale germany  = Locale.GERMANY;            // de, DE
Locale japan    = Locale.JAPAN;              // ja, JP

// Inspect a locale
System.out.println(germany.getLanguage());   // de
System.out.println(germany.getCountry());    // DE
System.out.println(germany.getDisplayName(Locale.US));   // German (Germany)

// The JVM-wide default (derived from the host OS) — avoid relying on it
Locale current = Locale.getDefault();
Locale.setDefault(Locale.US);                // process-wide; affects all formatting
```

The **default locale** comes from the host operating system. Relying on it makes output machine-dependent — the same code prints `1,234.56` on a US laptop and `1.234,56` on a German one. For predictable behavior (and for tests), pass an *explicit* `Locale` to every formatter rather than depending on the default.

---

## 27.2 Formatting Numbers, Currency, and Percent

`java.text.NumberFormat` renders numbers according to a locale's conventions. Its factory methods return a formatter for a purpose: `getNumberInstance` (grouping separators and decimals), `getCurrencyInstance` (currency symbol and the right number of fraction digits), and `getPercentInstance` (multiplies by 100 and appends the percent sign). Each takes a `Locale`.

```java
import java.text.NumberFormat;
import java.util.Locale;

double amount = 1234567.89;

// Plain number — grouping and decimal separators differ by locale
NumberFormat us = NumberFormat.getNumberInstance(Locale.US);
System.out.println(us.format(amount));        // 1,234,567.89

NumberFormat de = NumberFormat.getNumberInstance(Locale.GERMANY);
System.out.println(de.format(amount));        // 1.234.567,89   (dot groups, comma decimal)

// Currency — picks the locale's currency symbol and fraction digits
System.out.println(NumberFormat.getCurrencyInstance(Locale.US).format(amount));
//  $1,234,567.89
System.out.println(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount));
//  1.234.567,89 €
System.out.println(NumberFormat.getCurrencyInstance(Locale.JAPAN).format(amount));
//  ￥1,234,568    (yen has no minor unit — no decimals)

// Percent — input is a fraction; 0.75 -> "75%"
NumberFormat pct = NumberFormat.getPercentInstance(Locale.US);
System.out.println(pct.format(0.75));          // 75%
```

Note how the currency formatter knows that Japanese yen has *no* fractional unit, while dollars and euros use two decimal places — encoded in the locale data, not hard-coded by you.

---

## 27.3 DecimalFormat and Patterns

When you need explicit control over the output shape — fixed digit counts, custom grouping, scientific notation — use `java.text.DecimalFormat` with a *pattern*. The pattern characters: `0` is a required digit (shows `0` if absent), `#` is an optional digit (suppressed if absent), `,` is the grouping separator, `.` is the decimal separator, and `%` formats as a percentage. The actual *symbols* substituted for `.` and `,` still come from the locale (or `DecimalFormatSymbols`).

```java
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

double value = 1234.5;

new DecimalFormat("#,##0.00").format(value);     // 1,234.50  (always 2 decimals, grouped)
new DecimalFormat("000000.0").format(value);     // 001234.5  (zero-padded to 6 integer digits)
new DecimalFormat("#.##").format(value);         // 1234.5    (up to 2 decimals, no padding)
new DecimalFormat("0.00%").format(0.1234);       // 12.34%
new DecimalFormat("0.00E0").format(value);       // 1.23E3    (scientific)

// Bind a pattern to a specific locale's symbols
DecimalFormat german =
    new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
System.out.println(german.format(value));        // 1.234,50
```

| Pattern symbol | Meaning |
|:---:|---|
| `0` | Digit, always shown (zero-padding) |
| `#` | Digit, shown only if significant |
| `.` | Decimal separator placeholder |
| `,` | Grouping separator placeholder |
| `%` | Multiply by 100 and show percent sign |
| `E` | Scientific notation |

---

## 27.4 Locale-Aware Date and Time Formatting

The modern `java.time` API (Chapter 20) formats dates with `DateTimeFormatter`. For *localized* output, use `ofLocalizedDate`/`ofLocalizedTime`/`ofLocalizedDateTime` with a `FormatStyle`, then attach a locale with `withLocale(...)`. This yields the culturally correct field order, month names, and separators — without you hard-coding any pattern.

```java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

LocalDate date = LocalDate.of(2026, 6, 23);

DateTimeFormatter us =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.US);
System.out.println(date.format(us));        // June 23, 2026

DateTimeFormatter de =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.GERMANY);
System.out.println(date.format(de));        // 23. Juni 2026

DateTimeFormatter jp =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.JAPAN);
System.out.println(date.format(jp));        // 2026年6月23日

// FormatStyle controls verbosity: SHORT, MEDIUM, LONG, FULL
DateTimeFormatter shortUs =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.US);
System.out.println(date.format(shortUs));   // 6/23/26
```

Prefer these localized formatters over a fixed pattern like `ofPattern("MM/dd/yyyy")` whenever the audience is international — the pattern bakes in one culture's ordering. (See Chapter 20 for the full `java.time` model.)

---

## 27.5 MessageFormat: Parameterized Localized Messages

Concatenating strings to build a sentence (`"You have " + n + " messages"`) breaks across languages, where word order and grammar differ. `java.text.MessageFormat` builds a message from a *pattern* with numbered placeholders `{0}`, `{1}`, each optionally typed (`{0,number,currency}`, `{1,date,long}`). The placeholder order can differ between translations, so a translator can rearrange the sentence freely.

```java
import java.text.MessageFormat;
import java.util.Locale;

// Placeholders with type/style formatting, evaluated for a Locale
MessageFormat mf = new MessageFormat(
    "On {0,date,long}, {1} had a balance of {2,number,currency}.", Locale.US);

String msg = mf.format(new Object[] {
    new java.util.Date(),   // {0} -> formatted as a long date
    "Alice",                // {1} -> inserted as-is
    1234.5                  // {2} -> formatted as US currency
});
System.out.println(msg);
// On June 23, 2026, Alice had a balance of $1,234.50.

// The same numbered placeholders let a German translation reorder words:
//   "{1} hatte am {0,date,long} ein Guthaben von {2,number,currency}."
```

A subtlety: in a `MessageFormat` pattern a literal single quote escapes special characters, and `''` produces one literal apostrophe — relevant for languages like French. Combine `MessageFormat` with `ResourceBundle` (next) so the *pattern strings themselves* are externalized per language.

---

## 27.6 ResourceBundle and .properties Files

The cornerstone of text i18n is **`ResourceBundle`**: a lookup table of keys to translated strings, with one bundle file per locale. You write `messages.properties` (the default/fallback), `messages_de.properties` (German), `messages_ja.properties` (Japanese), and so on. At runtime `ResourceBundle.getBundle("messages", locale)` loads the best match, and your code looks values up by *key* — never by hard-coded English.

```properties
# messages.properties  (default / fallback, on the classpath)
greeting = Hello, {0}!
item.count = You have {0} items in your cart.

# messages_de.properties
greeting = Hallo, {0}!
item.count = Sie haben {0} Artikel in Ihrem Warenkorb.

# messages_ja.properties
greeting = こんにちは、{0}さん！
item.count = カートに{0}個の商品があります。
```

```java
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

// Load the bundle matching the locale (falls back to messages.properties)
ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);

String template = bundle.getString("greeting");          // "Hallo, {0}!"
String greeting = MessageFormat.format(template, "Alice"); // "Hallo, Alice!"
System.out.println(greeting);

// Combine with MessageFormat for parameterized, localized text
String count = MessageFormat.format(bundle.getString("item.count"), 3);
System.out.println(count);   // "Sie haben 3 Artikel in Ihrem Warenkorb."
```

`getBundle` resolves by trying the most specific file first (`messages_de_DE`), then less specific (`messages_de`), then the default (`messages`). Since Java 9, `.properties` bundles are read as **UTF-8**, so you can write non-ASCII text directly (older JDKs required `\uXXXX` escapes).

---

## 27.7 Character Encodings and Charset

Text is ultimately stored and transmitted as *bytes*; a **charset** is the mapping between characters and bytes. `java.nio.charset.Charset` represents one — `StandardCharsets.UTF_8`, `US_ASCII`, `ISO_8859_1`, etc. **Since Java 18, UTF-8 is the default charset** for the entire platform (file I/O, `String.getBytes()`, etc.), removing a long-standing source of platform-dependent bugs. Even so, the best practice is to be *explicit*: pass the `Charset` you mean rather than relying on any default.

```java
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

String text = "Café ☕ — 日本語";

// Encode a String to bytes with an explicit charset (always prefer this)
byte[] utf8  = text.getBytes(StandardCharsets.UTF_8);     // multi-byte for non-ASCII
byte[] ascii = text.getBytes(StandardCharsets.US_ASCII);  // lossy! non-ASCII -> '?'

// Decode bytes back to a String, stating the charset they were written in
String back = new String(utf8, StandardCharsets.UTF_8);
System.out.println(back);                                  // Café ☕ — 日本語

// The platform default (UTF-8 since Java 18) — shown for awareness, not reliance
System.out.println(Charset.defaultCharset());              // UTF-8
```

UTF-8 is variable-width (1 byte for ASCII, up to 4 for other characters), which is why `text.length()` (a count of UTF-16 `char` units) is *not* the byte length. Mismatched charsets — writing UTF-8 and reading ISO-8859-1 — produce the classic "mojibake" garbled text; stating the charset on both ends prevents it.

> **C++ contrast:** C++ `std::string` is a bag of bytes with no inherent encoding; you manage UTF-8 manually and `std::codecvt` (deprecated in C++17) was the awkward conversion mechanism. Java separates the abstraction (`String`, internally UTF-16) from the byte encoding (`Charset`) cleanly, and now defaults the encoding to UTF-8 everywhere.

---

## 27.8 BigDecimal: Why double Is Wrong for Money

`double` (and `float`) are *binary* floating-point: they cannot represent most decimal fractions exactly. `0.1` has no finite binary representation, so `0.1 + 0.2` is **not** `0.3` — it is `0.30000000000000004`. For scientific work the rounding error is negligible; for *money*, where every cent must be exact and totals must reconcile, it is unacceptable. `java.math.BigDecimal` stores an arbitrary-precision *decimal* number (an unscaled integer plus a scale), giving exact decimal arithmetic.

```java
import java.math.BigDecimal;

// The floating-point trap
double bad = 0.1 + 0.2;
System.out.println(bad);                         // 0.30000000000000004  — WRONG for money

// BigDecimal is exact... but ONLY if you construct from a String
BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));                    // 0.3  — exact

// ⚠️ new BigDecimal(double) inherits the double's error — DON'T do this
System.out.println(new BigDecimal(0.1));         // 0.1000000000000000055511151231257827...

// Use BigDecimal.valueOf(double) or, best, the String constructor
System.out.println(BigDecimal.valueOf(0.1));     // 0.1  (valueOf uses Double.toString)
```

The cardinal rule: **construct `BigDecimal` from a `String`** (or from an `int`/`long`/`BigInteger`), never from a `double` literal — `new BigDecimal(0.1)` faithfully copies the `double`'s error. `BigDecimal.valueOf(double)` is acceptable because it routes through `Double.toString`, but a string literal is the clearest and safest.

---

## 27.9 BigDecimal: Scale, Rounding, and Arithmetic

A `BigDecimal` has a **scale** — the number of digits after the decimal point. Arithmetic must control scale and rounding: `setScale(n, RoundingMode)` fixes the number of fraction digits, and *every* division must specify rounding (otherwise an exact result with infinite digits, like `1/3`, throws `ArithmeticException`). `RoundingMode.HALF_UP` ("round half away from zero") is the everyday accounting choice; `HALF_EVEN` ("banker's rounding") avoids bias over many operations.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;

BigDecimal price = new BigDecimal("19.99");
BigDecimal qty   = new BigDecimal("3");

BigDecimal subtotal = price.multiply(qty);            // 59.97
BigDecimal taxRate  = new BigDecimal("0.0825");       // 8.25%
BigDecimal tax      = subtotal.multiply(taxRate)      // 4.947525
                              .setScale(2, RoundingMode.HALF_UP);   // 4.95 (to cents)
BigDecimal total    = subtotal.add(tax);              // 64.92

// Division MUST state scale + rounding (else ArithmeticException on non-terminating result)
BigDecimal third = BigDecimal.ONE.divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP);
System.out.println(third);                            // 0.3333

// MathContext bounds significant digits (precision) for an operation
BigDecimal r = BigDecimal.ONE.divide(new BigDecimal("3"), new MathContext(5));
System.out.println(r);                                // 0.33333  (5 significant digits)

// Money is immutable like String — methods return NEW values
BigDecimal money = new BigDecimal("100.00");
money.add(new BigDecimal("50"));                      // result DISCARDED — common bug
money = money.add(new BigDecimal("50"));              // correct: reassign
```

Like `String`, `BigDecimal` is **immutable**: `add`, `multiply`, `setScale` all return a *new* object. Forgetting to assign the result is a classic bug.

---

## 27.10 BigDecimal: compareTo vs equals (and BigInteger)

A `BigDecimal` carries its scale as part of its identity, which trips up equality. `equals` returns `true` only if *both the value and the scale* match: `2.0` and `2.00` are **not** `equals` (different scale) even though they are numerically equal. To compare *numeric value* — which is almost always what you want — use `compareTo`, which returns `0` for equal values regardless of scale.

```java
import java.math.BigDecimal;

BigDecimal x = new BigDecimal("2.0");
BigDecimal y = new BigDecimal("2.00");

System.out.println(x.equals(y));        // false  — different scale (1 vs 2)
System.out.println(x.compareTo(y));     // 0      — equal numeric value  ✅ use this

// Idiom: test numeric equality with compareTo == 0
boolean sameValue = x.compareTo(y) == 0;          // true

// BigInteger: arbitrary-precision INTEGERS (no scale, no fractions)
import java.math.BigInteger;
BigInteger big = new BigInteger("123456789012345678901234567890");
System.out.println(big.multiply(BigInteger.TWO)); // exact, unbounded magnitude
BigInteger factorial = BigInteger.ONE;
for (int i = 1; i <= 30; i++)
    factorial = factorial.multiply(BigInteger.valueOf(i));   // 30! — far beyond long
```

`BigInteger` is the integer counterpart: arbitrary-precision whole numbers with no overflow (a `long` overflows past ~9.2×10¹⁸; `BigInteger` does not), used for cryptography, combinatorics, and anything exceeding 64 bits.

> **Pitfall:** Never put `BigDecimal` in a `HashSet` or use it as a `HashMap` key while relying on numeric equality — `equals`/`hashCode` are scale-sensitive, so `2.0` and `2.00` are distinct entries. Normalize the scale first (`setScale`) or use a `TreeSet`/`TreeMap` (which order by `compareTo`).

---

## 27.11 Collator: Locale-Aware Sorting

Sorting strings with the natural ordering (Unicode code-point order) is wrong for human-readable text: it puts all uppercase before lowercase, and mishandles accented characters and language-specific rules (in Swedish, `ä` sorts after `z`). `java.text.Collator` sorts according to a locale's rules.

```java
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

String[] names = { "Zürich", "apple", "Äpfel", "Banana", "äpfel" };

// Naive sort — code-point order: uppercase before lowercase, accents misplaced
String[] naive = names.clone();
Arrays.sort(naive);   // [Banana, Zürich, Äpfel, apple, äpfel] — not human-friendly

// Locale-aware sort with a Collator
Collator german = Collator.getInstance(Locale.GERMANY);
String[] proper = names.clone();
Arrays.sort(proper, german);   // ordered by German rules, case- and accent-aware
System.out.println(Arrays.toString(proper));
```

A `Collator` can also be tuned by *strength* (`PRIMARY` ignores case and accents, `TERTIARY` distinguishes them) for use cases like case-insensitive search. For sorting any user-facing list of text, prefer a `Collator` over the default `String` comparison.

---

## 27.12 Best Practices

```text
# ✅ NEVER use double/float for money — use BigDecimal (exact decimal)
# ✅ Construct BigDecimal from a String, never from a double literal
#    new BigDecimal("0.1")          ✅      new BigDecimal(0.1)   ❌
# ✅ Compare BigDecimal values with compareTo() == 0, not equals() (scale-sensitive)
# ✅ Specify scale + RoundingMode on every division and when reducing to cents

# ✅ Externalize all user-facing text into ResourceBundle .properties files
#    look up by key — never hard-code a display language in the code

# ✅ Pass an EXPLICIT Locale to every formatter (don't rely on Locale.getDefault())
#    → deterministic output, testable, machine-independent

# ✅ State the Charset explicitly (StandardCharsets.UTF_8) on every encode/decode
#    → even though UTF-8 is the platform default since Java 18

# ✅ Use locale-aware tools for locale-aware tasks:
#    NumberFormat (numbers/currency), DateTimeFormatter.ofLocalized... (dates),
#    MessageFormat (sentences), Collator (sorting)
```

The recurring themes: money is exact decimal, so use `BigDecimal` (from strings, compared with `compareTo`); locale-specific rendering is data, not code, so externalize text and pass explicit `Locale`s; and text is bytes plus an encoding, so always state the `Charset`.

---

## Summary

| Task | API |
|------|-----|
| **Describe a locale** | `Locale.of("fr", "CA")`, `Locale.US`, `Locale.getDefault()` |
| **Format numbers/currency/percent** | `NumberFormat.getNumberInstance/getCurrencyInstance/getPercentInstance(locale)` |
| **Custom number patterns** | `DecimalFormat("#,##0.00")` + `DecimalFormatSymbols` |
| **Localized dates** | `DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)` |
| **Parameterized localized messages** | `MessageFormat` (`{0}`, `{0,number,currency}`) |
| **Externalized translated text** | `ResourceBundle` + `messages_xx.properties` |
| **Encode/decode text** | `Charset` / `StandardCharsets.UTF_8`, `String.getBytes(charset)` |
| **Exact decimal / money math** | `BigDecimal` (from `String`), `setScale`, `RoundingMode`, `MathContext` |
| **Arbitrary-precision integers** | `BigInteger` |
| **Compare decimal values** | `compareTo() == 0` (not `equals`) |
| **Locale-aware sorting** | `Collator.getInstance(locale)` |

---

## Next Steps
- Format the same number and date for `Locale.US`, `Locale.GERMANY`, and `Locale.JAPAN` and compare
- Move every user-facing string into a `ResourceBundle` and look it up by key
- Rewrite a money calculation that used `double` to use `BigDecimal` with explicit `setScale`/`RoundingMode`
- Always pass an explicit `Locale` and `Charset` — make output deterministic
- Run the companion example: `code_examples/chapter27_internationalization.java`
- Revisit [Chapter 20: Date and Time](../20_date_time/README.md) for the full `java.time` model that pairs with `DateTimeFormatter`
