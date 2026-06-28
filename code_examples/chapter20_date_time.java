// Chapter 20: Date & Time API (java.time)
// Compile:  javac chapter20_date_time.java
// Run:      java chapter20_date_time
// Requires: Java 17+ (uses only the standard java.time API, no dependencies)
//
// Most inputs are explicit (e.g. LocalDate.of(2024, 1, 15)) so the output is
// deterministic; only a couple of illustrative lines use now().

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class chapter20_date_time {

    public static void main(String[] args) {
        section("1. Core types: LocalDate / LocalTime / LocalDateTime");
        coreTypes();

        section("2. Machine time vs human time (Instant vs LocalDateTime)");
        machineVsHuman();

        section("3. Duration (time-based) vs Period (date-based) + ChronoUnit");
        durationVsPeriod();

        section("4. Formatting and parsing with DateTimeFormatter");
        formattingAndParsing();

        section("5. Immutable, fluent manipulation + TemporalAdjusters");
        manipulation();

        section("6. Comparisons");
        comparisons();

        section("7. Time zones, DST, and zone conversion");
        zonesAndDst();

        section("8. Interop with legacy java.util.Date / Calendar");
        legacyInterop();

        section("9. A couple of live (now) examples");
        liveExamples();
    }

    // ----------------------------------------------------------------------
    private static void coreTypes() {
        LocalDate date = LocalDate.of(2024, 1, 15);          // months are 1-based
        LocalTime time = LocalTime.of(14, 30, 0);
        LocalDateTime dt = LocalDateTime.of(date, time);

        System.out.println("LocalDate      : " + date);
        System.out.println("LocalTime      : " + time);
        System.out.println("LocalDateTime  : " + dt);
        System.out.println("Day of week    : " + date.getDayOfWeek());      // MONDAY
        System.out.println("Month (enum)   : " + date.getMonth());          // JANUARY
        System.out.println("Day of year    : " + date.getDayOfYear());      // 15
        System.out.println("Leap year?     : " + date.isLeapYear());        // true (2024)
        System.out.println("Length of month: " + date.lengthOfMonth());     // 31
    }

    // ----------------------------------------------------------------------
    private static void machineVsHuman() {
        // Machine time: a precise point on the global timeline, always UTC.
        Instant instant = Instant.parse("2024-01-15T13:30:00Z");
        System.out.println("Instant (UTC)        : " + instant);
        System.out.println("Epoch seconds        : " + instant.getEpochSecond());

        // Human time: calendar fields, but ambiguous with no zone attached.
        LocalDateTime meeting = LocalDateTime.of(2024, 1, 15, 14, 30);
        System.out.println("LocalDateTime (human): " + meeting + "  (no zone -> not a unique moment)");

        // Pin human time to a real instant by supplying a zone.
        ZonedDateTime parisMeeting = meeting.atZone(ZoneId.of("Europe/Paris"));
        System.out.println("Pinned to Paris      : " + parisMeeting);
        System.out.println("As an Instant        : " + parisMeeting.toInstant());
    }

    // ----------------------------------------------------------------------
    private static void durationVsPeriod() {
        // Duration: time-based (hours/minutes/seconds/nanos).
        Duration twoHours = Duration.ofHours(2);
        Duration ninetyMin = Duration.ofMinutes(90);
        Duration combined = twoHours.plus(ninetyMin);
        System.out.println("Duration 2h + 90m    : " + combined
                + "  (" + combined.getSeconds() + " seconds)");

        Instant start = Instant.parse("2024-01-15T10:00:00Z");
        Instant later = start.plus(Duration.ofHours(2));
        System.out.println("Duration.between     : " + Duration.between(start, later));

        // Period: date-based (years/months/days).
        Period mixed = Period.of(1, 2, 10);
        LocalDate base = LocalDate.of(2024, 1, 15);
        System.out.println("Period 1y2m10d       : " + mixed);
        System.out.println("date + 2 months      : " + base.plus(Period.ofMonths(2)));

        Period age = Period.between(LocalDate.of(2000, 1, 1), LocalDate.of(2024, 1, 15));
        System.out.println("Age 2000-01 -> 2024  : "
                + age.getYears() + "y " + age.getMonths() + "m " + age.getDays() + "d");

        // ChronoUnit: the count between two temporals in a single unit.
        long days = ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15));
        long hours = ChronoUnit.HOURS.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.println("ChronoUnit DAYS      : " + days);
        System.out.println("ChronoUnit HOURS     : " + hours);
    }

    // ----------------------------------------------------------------------
    private static void formattingAndParsing() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

        // ISO-8601 by default.
        System.out.println("ISO toString()       : " + dt);

        // Pattern-based.
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formatted = dt.format(pattern);
        System.out.println("Pattern dd/MM/yyyy   : " + formatted);

        LocalDateTime parsed = LocalDateTime.parse("15/01/2024 14:30", pattern);
        System.out.println("Parsed back          : " + parsed);

        // Localized output for two locales.
        DateTimeFormatter us = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.US);
        DateTimeFormatter fr = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.FRENCH);
        LocalDate d = LocalDate.of(2024, 1, 15);
        System.out.println("Localized (US)       : " + d.format(us));
        System.out.println("Localized (FR)       : " + d.format(fr));
    }

    // ----------------------------------------------------------------------
    private static void manipulation() {
        LocalDate date = LocalDate.of(2024, 1, 15);

        // plus/minus/with all return NEW objects; the original is unchanged.
        System.out.println("original             : " + date);
        System.out.println("plusDays(1)          : " + date.plusDays(1));
        System.out.println("plusMonths(1)        : " + date.plusMonths(1));
        System.out.println("minusYears(1)        : " + date.minusYears(1));
        System.out.println("withYear(2025)       : " + date.withYear(2025));
        System.out.println("chained              : "
                + date.plusYears(1).plusMonths(2).minusDays(3));
        System.out.println("original (unchanged) : " + date);

        // TemporalAdjusters: named, reusable calendar logic.
        System.out.println("lastDayOfMonth       : "
                + date.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.println("next MONDAY          : "
                + date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
        System.out.println("first MONDAY in month: "
                + date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
    }

    // ----------------------------------------------------------------------
    private static void comparisons() {
        LocalDate a = LocalDate.of(2024, 1, 15);
        LocalDate b = LocalDate.of(2024, 3, 20);

        System.out.println("a.isBefore(b)        : " + a.isBefore(b));   // true
        System.out.println("a.isAfter(b)         : " + a.isAfter(b));    // false
        System.out.println("a.isEqual(b)         : " + a.isEqual(b));    // false
        System.out.println("a.compareTo(b)       : " + a.compareTo(b));  // negative
        System.out.println("DAYS between         : " + ChronoUnit.DAYS.between(a, b));
        System.out.println("Period between       : " + Period.between(a, b));

        Instant t1 = Instant.parse("2024-01-15T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-15T12:30:00Z");
        System.out.println("t1.isBefore(t2)      : " + t1.isBefore(t2));
        System.out.println("Duration between     : " + Duration.between(t1, t2));
    }

    // ----------------------------------------------------------------------
    private static void zonesAndDst() {
        ZoneId paris = ZoneId.of("Europe/Paris");
        ZoneId newYork = ZoneId.of("America/New_York");

        // Same instant viewed in two zones: the moment is identical, clocks differ.
        ZonedDateTime parisTime = ZonedDateTime.of(2024, 6, 15, 14, 30, 0, 0, paris);
        ZonedDateTime nyTime = parisTime.withZoneSameInstant(newYork);
        System.out.println("Paris                : " + parisTime);
        System.out.println("Same instant in NY   : " + nyTime);
        System.out.println("Same moment?         : "
                + parisTime.toInstant().equals(nyTime.toInstant()));

        // DST: crossing Europe's spring-forward boundary (2024-03-31 02:00 -> 03:00).
        ZonedDateTime beforeDst = ZonedDateTime.of(2024, 3, 31, 1, 30, 0, 0, paris);
        ZonedDateTime afterDst = beforeDst.plusHours(1);
        System.out.println("Before DST (+01:00)  : " + beforeDst);
        System.out.println("+1 real hour         : " + afterDst
                + "  (wall clock jumped 2 hours due to DST)");

        // Round-trip: store an Instant, render through a zone, convert back.
        Instant stored = Instant.parse("2024-01-15T13:30:00Z");
        ZonedDateTime inParis = stored.atZone(paris);
        System.out.println("Stored Instant       : " + stored);
        System.out.println("Displayed in Paris   : " + inParis);
        System.out.println("Back to Instant      : " + inParis.toInstant());
    }

    // ----------------------------------------------------------------------
    private static void legacyInterop() {
        // Modern -> legacy (when an old API demands a java.util.Date).
        Instant instant = Instant.parse("2024-01-15T13:30:00Z");
        Date legacyDate = Date.from(instant);
        System.out.println("Instant -> Date      : " + legacyDate.toInstant());

        // Legacy -> modern.
        Instant back = legacyDate.toInstant();
        LocalDate localDate = back.atZone(ZoneId.of("UTC")).toLocalDate();
        System.out.println("Date -> Instant      : " + back);
        System.out.println("Date -> LocalDate    : " + localDate);

        // GregorianCalendar <-> ZonedDateTime.
        ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
        GregorianCalendar gc = GregorianCalendar.from(zdt);
        System.out.println("ZDT -> Calendar      : " + gc.toZonedDateTime());
    }

    // ----------------------------------------------------------------------
    private static void liveExamples() {
        // A couple of illustrative lines using the real system clock (non-deterministic).
        Instant nowInstant = Instant.now();
        LocalDate today = LocalDate.now();
        System.out.println("Instant.now()        : " + nowInstant);
        System.out.println("LocalDate.now()      : " + today);
        System.out.println("System default zone  : " + ZoneId.systemDefault());
    }

    // ----------------------------------------------------------------------
    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
