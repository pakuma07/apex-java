# Chapter 20: Date & Time API (java.time) -- Java

Working with dates and times is deceptively hard: calendars have irregular month lengths, leap years and leap seconds, time zones shift, and Daylight Saving Time (DST) jumps the clock forward or back. For its first two decades Java handled all of this badly. The original `java.util.Date` (since Java 1.0) and `java.util.Calendar` (since Java 1.1), together with `SimpleDateFormat`, were **mutable, not thread-safe, and riddled with confusing design choices**. Java 8 (2014) introduced a clean replacement — the **`java.time`** package, designed under JSR-310 and heavily inspired by the Joda-Time library. It is immutable, thread-safe, type-rich, and fluent, and it is the API you should use for all new code on Java 21.

This chapter covers `java.time` comprehensively: why the legacy types were broken, the core type families (`LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, plus `ZoneId`/`ZoneOffset`), the crucial distinction between *machine time* and *human time*, the amount types `Duration` and `Period` (and `ChronoUnit`), parsing and formatting with `DateTimeFormatter`, immutable fluent manipulation including `TemporalAdjusters`, comparisons, time-zone and DST handling, and how to interoperate with the old `Date`/`Calendar` classes when you must.

> **C++ contrast:** Modern C++ caught up only with C++20's `<chrono>` calendar/time-zone extensions (`std::chrono::year_month_day`, `std::chrono::zoned_time`, `std::chrono::sys_time`). Before that, C++ relied on the C library's `time_t`, `struct tm`, `localtime`, and `strftime` — the rough analogue of Java's legacy `Date`/`Calendar`. Java's `java.time` and C++20 `<chrono>` actually share a common ancestry of ideas (a clear separation of *time points*, *durations*, and *calendar/zone* concerns), so many concepts here map closely onto `<chrono>`.

## 20.1 Why the Legacy API Was Broken

Before reaching for `java.time`, it helps to know what it fixed, because legacy code is everywhere. The old `java.util.Date` is not really a "date" at all — it is a millisecond count since the Unix epoch, yet it exposes deprecated `getYear()`/`getMonth()` methods that are **0-based for months** and offset the year from 1900. `Calendar` is mutable, so passing one to a method can let that method silently change your value. Worst of all, `SimpleDateFormat` is **not thread-safe**: sharing a single instance across threads produces corrupted output or exceptions, a bug that has bitten countless production systems.

```java
// ❌ LEGACY — do not use this style in new code (shown only to recognize it)
import java.util.*;
import java.text.SimpleDateFormat;

Date date = new Date();                 // a millisecond timestamp, misnamed "Date"
@SuppressWarnings("deprecation")
int month = date.getMonth();            // 0-based! January == 0, December == 11

Calendar cal = Calendar.getInstance();
cal.set(2024, 0, 15);                   // 0 means January — error-prone
cal.add(Calendar.DAY_OF_MONTH, 1);      // MUTATES cal in place

SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
// ❌ A shared SimpleDateFormat is NOT thread-safe — concurrent use corrupts data
```

The table below summarizes the failings and their `java.time` cures.

| Legacy problem | `java.time` cure |
|----------------|------------------|
| Mutable (`Date`, `Calendar`) | All `java.time` types are **immutable** and thread-safe |
| 0-based months | Months are 1-based; `Month` enum (`Month.JANUARY`) avoids magic numbers |
| `Date` conflates instant and calendar fields | Distinct types: `Instant` (machine), `LocalDate`/`LocalDateTime` (human) |
| `SimpleDateFormat` not thread-safe | `DateTimeFormatter` is immutable and thread-safe |
| Poor time-zone handling | First-class `ZoneId`, `ZonedDateTime`, automatic DST rules |
| Awkward arithmetic (`cal.add`) | Fluent `plusDays`, `minusMonths`, `with(...)` returning new objects |

> **C++ contrast:** The legacy `struct tm` in C has the same 0-based-month trap (`tm_mon` is 0–11) and the year is offset from 1900 (`tm_year`). C++20's `<chrono>` fixed this with strong types like `std::chrono::January` and `year_month_day`, exactly as Java fixed it with the `Month` enum and `LocalDate`.

---

## 20.2 The Core Types

`java.time` splits date/time concepts into purpose-built types so the compiler can catch misuse. The most important ones fall into two groups: **human time** (calendar fields a person reads off a wall clock or calendar) and **machine time** (a point on the timeline measured in seconds/nanos). Every type is immutable.

| Type | Represents | Example value |
|------|-----------|---------------|
| `LocalDate` | A date with no time or zone | `2024-01-15` |
| `LocalTime` | A time of day with no date or zone | `14:30:00` |
| `LocalDateTime` | A date **and** time, still no zone | `2024-01-15T14:30` |
| `Instant` | A point on the timeline (UTC), nanosecond precision | `2024-01-15T13:30:00Z` |
| `ZonedDateTime` | A date-time **with** a full time zone | `2024-01-15T14:30+01:00[Europe/Paris]` |
| `OffsetDateTime` | A date-time with a fixed UTC offset (no zone rules) | `2024-01-15T14:30+01:00` |
| `ZoneId` | A time-zone identifier with DST rules | `Europe/Paris` |
| `ZoneOffset` | A fixed offset from UTC | `+01:00` |
| `Year`, `YearMonth`, `MonthDay`, `Month`, `DayOfWeek` | Partial / enum date pieces | `2024`, `2024-01`, `--01-15` |

```java
import java.time.*;

LocalDate   date     = LocalDate.of(2024, 1, 15);          // months are 1-based
LocalTime   time     = LocalTime.of(14, 30, 0);            // 14:30:00
LocalDateTime dt     = LocalDateTime.of(date, time);       // 2024-01-15T14:30
LocalDateTime dt2    = LocalDateTime.of(2024, 1, 15, 14, 30);

Instant      instant = Instant.now();                      // current UTC moment
ZoneId       zone    = ZoneId.of("Europe/Paris");
ZonedDateTime zdt    = ZonedDateTime.of(dt, zone);         // adds zone + DST rules
OffsetDateTime odt   = OffsetDateTime.of(dt, ZoneOffset.ofHours(1));

// Using the Month enum instead of a magic number (1-based, but clearer still)
LocalDate clearer = LocalDate.of(2024, Month.JANUARY, 15);
```

Each type also offers `now()` (read the system clock) and `parse(...)` (from an ISO-8601 string) factory methods, and convenient queries like `date.getDayOfWeek()`, `date.getMonth()`, `date.isLeapYear()`, and `date.lengthOfMonth()`.

---

## 20.3 Machine Time vs Human Time

The single most important idea in `java.time` is the split between **machine time** and **human time**. `Instant` is machine time: an unambiguous point on the global timeline, always in UTC, ideal for timestamps, logging, and measuring elapsed time. `LocalDateTime` is human time: the date and time you would write on a calendar or read off a wall clock — but it has **no zone**, so it does *not* identify a unique moment. "2024-01-15 14:30" happens at different actual instants in Tokyo and New York.

```java
import java.time.*;

// MACHINE TIME — a precise, zone-free point on the global timeline (UTC)
Instant now = Instant.now();                  // e.g. 2024-01-15T13:30:00Z
long epochSecs = now.getEpochSecond();        // seconds since 1970-01-01T00:00:00Z

// HUMAN TIME — calendar fields, but ambiguous without a zone
LocalDateTime meeting = LocalDateTime.of(2024, 1, 15, 14, 30);
// This is NOT a unique moment: 14:30 in which city?

// To pin human time to a real instant you must supply a zone:
ZonedDateTime parisMeeting = meeting.atZone(ZoneId.of("Europe/Paris"));
Instant exactMoment = parisMeeting.toInstant();   // now it IS a unique point
```

Rule of thumb: **store and transmit `Instant` (UTC); convert to `LocalDateTime`/`ZonedDateTime` only for display** to a user in their own zone. A database "created_at" column should be an `Instant`; a "store closes at 17:00" business rule is a `LocalTime`.

> **C++ contrast:** This is exactly the `<chrono>` distinction between `std::chrono::sys_time` / `time_point` (machine time, the analogue of `Instant`) and the calendar types `year_month_day` + `local_time` (human time, the analogue of `LocalDate`/`LocalDateTime`). C++20's `zoned_time` corresponds to Java's `ZonedDateTime`.

---

## 20.4 Duration vs Period

There are two kinds of "amount of time," and `java.time` gives each its own type. **`Duration`** is *time-based*: a quantity of seconds and nanoseconds, used with `Instant`, `LocalTime`, and `LocalDateTime`. **`Period`** is *date-based*: a quantity of years, months, and days, used with `LocalDate`. The distinction matters because "1 day" as a `Period` means "the same wall-clock time tomorrow" (which can be 23 or 25 hours across a DST boundary), whereas "24 hours" as a `Duration` is always exactly 24 hours.

```java
import java.time.*;
import java.time.temporal.ChronoUnit;

// DURATION — time-based (hours, minutes, seconds, nanos)
Duration twoHours   = Duration.ofHours(2);
Duration ninetyMin  = Duration.ofMinutes(90);
Duration combined   = twoHours.plus(ninetyMin);     // PT3H30M
long totalSeconds   = combined.getSeconds();        // 12600

Instant start = Instant.now();
Instant later = start.plus(Duration.ofHours(2));
Duration elapsed = Duration.between(start, later);  // PT2H

// PERIOD — date-based (years, months, days)
Period twoMonths = Period.ofMonths(2);
Period mixed     = Period.of(1, 2, 10);             // 1 year, 2 months, 10 days
LocalDate due    = LocalDate.of(2024, 1, 15).plus(twoMonths);   // 2024-03-15

Period age = Period.between(LocalDate.of(2000, 1, 1),
                            LocalDate.of(2024, 1, 15));
System.out.println(age.getYears() + "y " + age.getMonths()
                   + "m " + age.getDays() + "d");   // 24y 0m 14d

// CHRONOUNIT — measure a single unit between two temporals
long days  = ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1),
                                     LocalDate.of(2024, 1, 15));   // 14
long hours = ChronoUnit.HOURS.between(LocalTime.of(9, 0),
                                      LocalTime.of(17, 30));        // 8
```

Both `Duration` and `Period` parse and print in **ISO-8601** form: `Duration` as `PT2H30M` (the `P` for period, `T` separating the time part) and `Period` as `P1Y2M10D`. Use `ChronoUnit.<UNIT>.between(a, b)` when you want the answer as a single number in one unit.

| Need | Use |
|------|-----|
| Elapsed seconds/minutes/hours, timeouts | `Duration` |
| Calendar gaps in years/months/days, ages, billing cycles | `Period` |
| The count between two points in exactly one unit | `ChronoUnit.X.between(a, b)` |

---

## 20.5 Parsing and Formatting with DateTimeFormatter

To convert between text and `java.time` objects, use `DateTimeFormatter` — immutable and thread-safe, so a single instance can be shared as a `static final` constant. It comes in three flavours: **predefined ISO formatters** (`DateTimeFormatter.ISO_LOCAL_DATE`, etc.), **pattern-based** formatters built with `ofPattern("yyyy-MM-dd")`, and **localized** formatters built with `ofLocalizedDate(FormatStyle.MEDIUM)`. Formatting calls `dateTime.format(fmt)`; parsing calls the type's static `parse(text, fmt)`.

```java
import java.time.*;
import java.time.format.*;
import java.util.Locale;

LocalDateTime dt = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

// 1) ISO-8601 (no formatter needed — toString() and parse() use ISO by default)
String iso = dt.toString();                       // 2024-01-15T14:30:45
LocalDate isoBack = LocalDate.parse("2024-01-15"); // ISO_LOCAL_DATE

// 2) Pattern-based
DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String formatted = dt.format(pattern);            // 15/01/2024 14:30
LocalDateTime parsed = LocalDateTime.parse("15/01/2024 14:30", pattern);

// 3) Localized — output adapts to the locale's conventions
DateTimeFormatter french = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.FRENCH);
String fr = LocalDate.of(2024, 1, 15).format(french);   // lundi 15 janvier 2024
```

Common pattern letters (case-sensitive — a frequent bug source) are summarized below.

| Letter | Meaning | Example |
|--------|---------|---------|
| `y` | Year | `2024` |
| `M` / `MMM` / `MMMM` | Month number / short name / full name | `01` / `Jan` / `January` |
| `d` | Day of month | `15` |
| `E` / `EEEE` | Day of week short / full | `Mon` / `Monday` |
| `H` / `h` | Hour 0–23 / 1–12 | `14` / `02` |
| `m` | Minute | `30` |
| `s` | Second | `45` |
| `a` | AM/PM marker | `PM` |
| `z` / `Z` / `VV` | Zone name / offset / zone id | `CET` / `+0100` / `Europe/Paris` |

> **C++ contrast:** Pattern letters here are the `java.time` analogue of C's `strftime`/`std::put_time` format codes (`%Y`, `%m`, `%d`, `%H`), and of C++20's `std::format` chrono specifiers. Unlike `SimpleDateFormat`, a `DateTimeFormatter` is safe to share across threads — closer in spirit to constructing an immutable formatter object than to a stateful stream manipulator.

---

## 20.6 Immutability and Fluent Manipulation

Every `java.time` object is immutable, so manipulation methods **return a new object** rather than mutating the receiver — just like Java `String`. The methods come in predictable families: `plusX`/`minusX` (add or subtract an amount), `withX` (set one field), and `at...`/`to...` (convert between types). Because each returns a new value, calls chain fluently. Forgetting to capture the result — `date.plusDays(1);` on its own line — is a no-op bug, exactly the same trap as `str.trim();`.

```java
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

LocalDate date = LocalDate.of(2024, 1, 15);

// plus / minus return NEW objects (original is unchanged)
LocalDate tomorrow   = date.plusDays(1);          // 2024-01-16
LocalDate nextMonth  = date.plusMonths(1);        // 2024-02-15
LocalDate lastYear   = date.minusYears(1);        // 2023-01-15
LocalDate chained    = date.plusYears(1).plusMonths(2).minusDays(3);

// with* sets a single field, returning a new object
LocalDate newYear    = date.withYear(2025);       // 2025-01-15
LocalDate firstDay   = date.withDayOfMonth(1);    // 2024-01-01

// ❌ Common bug — result discarded, 'date' is untouched
date.plusDays(10);                                 // does nothing useful!

// TemporalAdjusters — named, reusable date logic
LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());   // 2024-01-31
LocalDate nextMonday = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
LocalDate firstMon   = date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
```

`TemporalAdjusters` packages common calendar rules ("last day of month," "next Friday," "first Monday in month") so you do not hand-roll loops. You can also write your own adjuster as a lambda implementing `TemporalAdjuster`.

> **C++ contrast:** Immutability with new-object returns mirrors how C++20 `<chrono>` calendar types are value types you recompute (`ymd + months{1}`) rather than mutate in place — and it is the opposite of the legacy `Calendar.add()` / C `struct tm` mutation model.

---

## 20.7 Comparisons and "between"

To order or compare temporals, use the boolean methods `isBefore`, `isAfter`, and `isEqual`; the types also implement `Comparable`, so `compareTo` and sorted collections work directly. To measure the gap between two points, use `Duration.between` / `Period.between` (Section 20.4) or `ChronoUnit.<UNIT>.between`. Note `isEqual` compares the instant/value, whereas `equals` additionally requires the same type/chronology — for `ZonedDateTime`, two values at the same moment in different zones are `isEqual` but not `equals`.

```java
import java.time.*;
import java.time.temporal.ChronoUnit;

LocalDate a = LocalDate.of(2024, 1, 15);
LocalDate b = LocalDate.of(2024, 3, 20);

boolean before = a.isBefore(b);     // true
boolean after  = a.isAfter(b);      // false
boolean same   = a.isEqual(b);      // false
int     cmp    = a.compareTo(b);    // negative (a < b)

// Gaps
long days   = ChronoUnit.DAYS.between(a, b);    // 65
Period gap  = Period.between(a, b);             // P2M5D (2 months, 5 days)

// Works for instants and times too
Instant t1 = Instant.parse("2024-01-15T10:00:00Z");
Instant t2 = Instant.parse("2024-01-15T12:30:00Z");
boolean earlier = t1.isBefore(t2);              // true
Duration d = Duration.between(t1, t2);          // PT2H30M
```

---

## 20.8 Time Zones and DST

A `ZoneId` (e.g. `ZoneId.of("America/New_York")`) carries the full set of rules for a region, including when DST starts and ends, so `ZonedDateTime` arithmetic respects DST automatically. A `ZoneOffset` (e.g. `+05:30`) is just a fixed displacement from UTC with no rules. Prefer region-based `ZoneId` over raw offsets, because an offset alone cannot know that, say, New York shifts between `-05:00` and `-04:00`.

```java
import java.time.*;
import java.time.temporal.ChronoUnit;

ZoneId paris    = ZoneId.of("Europe/Paris");
ZoneId newYork  = ZoneId.of("America/New_York");
ZoneId utc      = ZoneOffset.UTC;

// Same instant viewed in two zones (the moment is identical; the clock differs)
ZonedDateTime parisTime = ZonedDateTime.of(2024, 6, 15, 14, 30, 0, 0, paris);
ZonedDateTime nyTime    = parisTime.withZoneSameInstant(newYork);
// parisTime 14:30+02:00 == nyTime 08:30-04:00 (same instant)

// DST awareness: crossing the spring-forward boundary in Europe (2024-03-31)
ZonedDateTime beforeDst = ZonedDateTime.of(2024, 3, 31, 1, 30, 0, 0, paris); // +01:00
ZonedDateTime afterDst  = beforeDst.plusHours(1);   // jumps to 03:30 +02:00
// Adding 1 hour of real time advanced the wall clock by 2 hours due to DST.

// Listing available zones
// ZoneId.getAvailableZoneIds()  // returns Set<String> of all region ids
```

Two conversion methods are easy to confuse: `withZoneSameInstant` keeps the *same moment* and changes the displayed wall-clock (what you want for "show this event in the user's zone"), while `withZoneSameLocal` keeps the *same wall-clock fields* and changes the instant. Use the former for almost everything.

> **C++ contrast:** This is the C++20 `std::chrono::time_zone` / `zoned_time` model. `withZoneSameInstant` corresponds to constructing a `zoned_time` for a new zone from the same `sys_time`. Both Java and C++20 ship the IANA time-zone database; pre-C++20 you had to drop to platform C APIs (`tzset`, `localtime_r`) with far less safety.

---

## 20.9 Converting Instant <-> ZonedDateTime

Because `Instant` is machine time and `ZonedDateTime` is human time with a zone, you convert between them constantly: take a stored UTC `Instant`, attach a zone for display, and vice versa. The bridge methods are `instant.atZone(zoneId)` (→ `ZonedDateTime`) and `zonedDateTime.toInstant()` (→ `Instant`). `LocalDateTime` similarly converts via `atZone(zoneId)` and back with `toLocalDateTime()`.

```java
import java.time.*;

Instant stored = Instant.parse("2024-01-15T13:30:00Z");   // from a database

// Instant -> human time in a specific zone (for display)
ZonedDateTime inParis = stored.atZone(ZoneId.of("Europe/Paris"));  // 14:30+01:00
LocalDateTime wall     = inParis.toLocalDateTime();                // 2024-01-15T14:30

// Human time + zone -> Instant (for storage)
ZonedDateTime event = LocalDateTime.of(2024, 1, 15, 14, 30)
                          .atZone(ZoneId.of("Europe/Paris"));
Instant toStore = event.toInstant();                               // 2024-01-15T13:30:00Z
```

The pattern is symmetric: **store the `Instant`, render through a `ZoneId` only at the edges of your system.**

---

## 20.10 Interop with Legacy Date / Calendar

You will inevitably meet old APIs that hand you a `java.util.Date` or `Calendar`, or demand one. Java 8 added bridge methods so you can cross over without manual epoch math. `java.util.Date` ↔ `Instant` via `Date.from(instant)` and `date.toInstant()`. `Calendar.toInstant()` works too, and `GregorianCalendar` offers `toZonedDateTime()`. The old `java.sql.Date`/`Time`/`Timestamp` likewise gained `toLocalDate()`/`toLocalTime()`/`toInstant()` (and `valueOf(...)` going back).

```java
import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;

// Modern -> legacy (when an old API demands a java.util.Date)
Instant instant = Instant.now();
Date legacyDate = Date.from(instant);

// Legacy -> modern (when an old API hands you a java.util.Date)
Instant back        = legacyDate.toInstant();
ZonedDateTime zdt   = back.atZone(ZoneId.systemDefault());
LocalDate localDate = zdt.toLocalDate();

// GregorianCalendar bridges directly to ZonedDateTime
GregorianCalendar gc = new GregorianCalendar();
ZonedDateTime fromCal = gc.toZonedDateTime();
GregorianCalendar toCal = GregorianCalendar.from(zdt);

// java.sql types (e.g. from JDBC) also bridge
// java.sql.Timestamp ts = ...;  Instant i = ts.toInstant();
// java.sql.Date sd = java.sql.Date.valueOf(LocalDate.of(2024, 1, 15));
```

Note that `java.util.Date` has no zone and only millisecond precision, while `Instant` has nanosecond precision — so a round-trip through `Date` can lose sub-millisecond information. Convert only at the boundary with legacy code and keep `java.time` types internally.

---

## 20.11 Best Practices

The following idioms summarize how to use `java.time` correctly.

```java
import java.time.*;
import java.time.format.DateTimeFormatter;

// ✅ Store and transmit machine time as Instant (UTC); convert to local only for display
Instant createdAt = Instant.now();

// ✅ Use region-based ZoneId, not a raw fixed offset — it knows DST rules
ZoneId zone = ZoneId.of("America/New_York");      // not ZoneOffset.ofHours(-5)

// ✅ Share DateTimeFormatter as a constant — it is immutable and thread-safe
static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

// ✅ Pick the right type for the concept
LocalDate  birthday   = LocalDate.of(1990, 5, 20);   // a date, no time/zone
LocalTime  storeOpens = LocalTime.of(9, 0);          // a wall-clock time
Instant    logStamp   = Instant.now();               // a precise moment

// ✅ Remember immutability — capture the returned value
LocalDate due = LocalDate.now().plusDays(30);        // NOT  d.plusDays(30); alone

// ✅ Duration for time-based amounts, Period for date-based amounts
Duration timeout = Duration.ofSeconds(30);
Period   trial   = Period.ofDays(14);

// ✅ withZoneSameInstant to re-display the same moment in another zone
// ❌ Avoid java.util.Date / Calendar / SimpleDateFormat in new code
// ❌ Don't do epoch math by hand — use the provided conversion methods
```

The central themes: keep machine time (`Instant`, UTC) separate from human time, store UTC and localize only at the edges, prefer `ZoneId` over raw offsets, treat every object as immutable, reuse thread-safe `DateTimeFormatter` constants, and abandon the legacy classes entirely except at interop boundaries.

---

## Summary

| Task | `java.time` API |
|------|-----------------|
| **A date (no time/zone)** | `LocalDate.of(2024, 1, 15)` |
| **A time of day** | `LocalTime.of(14, 30)` |
| **Date + time (no zone)** | `LocalDateTime.of(...)` |
| **A precise moment (UTC)** | `Instant.now()` / `Instant.parse(...)` |
| **Date-time with a zone** | `ZonedDateTime`, `ZoneId.of("Europe/Paris")` |
| **Fixed-offset date-time** | `OffsetDateTime`, `ZoneOffset` |
| **Time-based amount** | `Duration.ofHours(2)` |
| **Date-based amount** | `Period.of(1, 2, 10)` |
| **Count in one unit** | `ChronoUnit.DAYS.between(a, b)` |
| **Add / subtract / set** | `plusDays`, `minusMonths`, `withYear` (return new objects) |
| **Calendar rules** | `TemporalAdjusters.lastDayOfMonth()`, `next(DayOfWeek.MONDAY)` |
| **Compare** | `isBefore`, `isAfter`, `isEqual`, `compareTo` |
| **Format / parse** | `DateTimeFormatter.ofPattern(...)`, `format`, `parse` |
| **Zone conversion** | `instant.atZone(zone)`, `zdt.toInstant()`, `withZoneSameInstant` |
| **Legacy interop** | `Date.from(instant)`, `date.toInstant()`, `GregorianCalendar.toZonedDateTime()` |

---

## Next Steps
- Create and manipulate `LocalDate`/`LocalDateTime`/`Instant` immutably
- Measure time with `Duration`, `Period`, and `ChronoUnit`
- Parse and format with thread-safe `DateTimeFormatter` patterns and locales
- Handle zones and DST with `ZoneId` and `ZonedDateTime`, storing UTC `Instant`
- Revisit [Chapter 14: File I/O](../14_file_io/README.md) to timestamp and persist data correctly
- Continue to [Chapter 21: Regular Expressions](../21_regular_expressions/README.md) for pattern matching and text processing
- See [Chapter 16: Concurrency](../16_concurrency/README.md) for why the old `SimpleDateFormat` was a thread-safety hazard that `java.time` fixes
