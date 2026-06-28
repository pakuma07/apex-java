// Chapter 27: Internationalization, Formatting & BigDecimal -- Java
//
// A single self-contained program demonstrating:
//   - NumberFormat: currency & percent across Locale.US / GERMANY / JAPAN
//   - DecimalFormat: explicit pattern-based formatting
//   - BigDecimal: exact money math, the double pitfall (0.1 + 0.2),
//     setScale + RoundingMode, and compareTo vs equals
//   - MessageFormat: parameterized, localized messages
//
// Explicit Locales are passed everywhere so the output is deterministic.
// Compiles and runs on Java 17+ with no external dependencies:
//     javac chapter27_internationalization.java
//     java  chapter27_internationalization

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class chapter27_internationalization {

    public static void main(String[] args) {
        numberAndCurrencyFormatting();
        percentFormatting();
        decimalFormatPatterns();
        bigDecimalMoneyMath();
        doubleVsBigDecimalPitfall();
        compareToVsEquals();
        messageFormatExample();
    }

    // -- 1. NumberFormat: currency across locales -------------------------
    private static void numberAndCurrencyFormatting() {
        System.out.println("=== Currency formatting across locales ===");
        double amount = 1234567.89;

        // Each locale chooses its own symbol, grouping, and fraction digits.
        System.out.println("US     : " +
                NumberFormat.getCurrencyInstance(Locale.US).format(amount));
        System.out.println("Germany: " +
                NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount));
        System.out.println("Japan  : " +
                NumberFormat.getCurrencyInstance(Locale.JAPAN).format(amount));

        // Plain number formatting differs too (separators swap).
        System.out.println("US plain     : " +
                NumberFormat.getNumberInstance(Locale.US).format(amount));
        System.out.println("Germany plain: " +
                NumberFormat.getNumberInstance(Locale.GERMANY).format(amount));
        System.out.println();
    }

    // -- 2. NumberFormat: percent -----------------------------------------
    private static void percentFormatting() {
        System.out.println("=== Percent formatting ===");
        double fraction = 0.1234;   // input is a fraction; *100 happens internally

        System.out.println("US     : " +
                NumberFormat.getPercentInstance(Locale.US).format(fraction));
        System.out.println("Germany: " +
                NumberFormat.getPercentInstance(Locale.GERMANY).format(fraction));
        System.out.println();
    }

    // -- 3. DecimalFormat: explicit patterns ------------------------------
    private static void decimalFormatPatterns() {
        System.out.println("=== DecimalFormat patterns ===");
        double value = 1234.5;

        // Symbols pinned to US so output is deterministic regardless of host.
        DecimalFormatSymbols us = DecimalFormatSymbols.getInstance(Locale.US);

        System.out.println("#,##0.00  -> " + new DecimalFormat("#,##0.00", us).format(value));
        System.out.println("000000.0  -> " + new DecimalFormat("000000.0", us).format(value));
        System.out.println("#.##      -> " + new DecimalFormat("#.##", us).format(value));
        System.out.println("0.00%     -> " + new DecimalFormat("0.00%", us).format(0.1234));
        System.out.println("0.00E0    -> " + new DecimalFormat("0.00E0", us).format(value));

        // Same pattern, German symbols: '.' groups, ',' is the decimal separator.
        DecimalFormatSymbols de = DecimalFormatSymbols.getInstance(Locale.GERMANY);
        System.out.println("#,##0.00 (DE) -> " + new DecimalFormat("#,##0.00", de).format(value));
        System.out.println();
    }

    // -- 4. BigDecimal: exact money arithmetic ----------------------------
    private static void bigDecimalMoneyMath() {
        System.out.println("=== BigDecimal money math ===");

        BigDecimal price = new BigDecimal("19.99");   // construct from String!
        BigDecimal qty   = new BigDecimal("3");

        BigDecimal subtotal = price.multiply(qty);                    // 59.97
        BigDecimal taxRate  = new BigDecimal("0.0825");               // 8.25%
        BigDecimal tax      = subtotal.multiply(taxRate)
                                      .setScale(2, RoundingMode.HALF_UP);   // -> 4.95
        BigDecimal total    = subtotal.add(tax);                      // 64.92

        System.out.println("Unit price : " + price);
        System.out.println("Quantity   : " + qty);
        System.out.println("Subtotal   : " + subtotal);
        System.out.println("Tax (8.25%): " + tax);
        System.out.println("Total      : " + total);

        // Division MUST specify scale + rounding (non-terminating otherwise).
        BigDecimal third = BigDecimal.ONE.divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP);
        System.out.println("1/3 (scale 4, HALF_UP): " + third);       // 0.3333

        // MathContext bounds significant digits instead of fixed scale.
        BigDecimal r = BigDecimal.ONE.divide(new BigDecimal("3"), new MathContext(5));
        System.out.println("1/3 (MathContext 5)   : " + r);           // 0.33333
        System.out.println();
    }

    // -- 5. The double-vs-BigDecimal pitfall ------------------------------
    private static void doubleVsBigDecimalPitfall() {
        System.out.println("=== double vs BigDecimal: 0.1 + 0.2 ===");

        double dbl = 0.1 + 0.2;
        System.out.println("double      0.1 + 0.2 = " + dbl);          // 0.30000000000000004

        BigDecimal exact = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        System.out.println("BigDecimal  0.1 + 0.2 = " + exact);        // 0.3

        // Constructing from the double literal copies its error -- avoid this.
        System.out.println("new BigDecimal(0.1)   = " + new BigDecimal(0.1));
        System.out.println("BigDecimal.valueOf(0.1)= " + BigDecimal.valueOf(0.1)); // 0.1
        System.out.println();
    }

    // -- 6. compareTo vs equals -------------------------------------------
    private static void compareToVsEquals() {
        System.out.println("=== BigDecimal compareTo vs equals ===");

        BigDecimal x = new BigDecimal("2.0");
        BigDecimal y = new BigDecimal("2.00");

        // equals also compares scale: 2.0 (scale 1) != 2.00 (scale 2).
        System.out.println("2.0 .equals(2.00)   = " + x.equals(y));        // false
        // compareTo compares numeric value only -- what you usually want.
        System.out.println("2.0 .compareTo(2.00) = " + x.compareTo(y));    // 0
        System.out.println("numeric equality (compareTo == 0): " + (x.compareTo(y) == 0));
        System.out.println();
    }

    // -- 7. MessageFormat: parameterized localized message ----------------
    private static void messageFormatExample() {
        System.out.println("=== MessageFormat ===");

        // Fixed date so output is deterministic across runs.
        Date when = new Date(1_750_000_000_000L); // a fixed instant

        MessageFormat usMsg = new MessageFormat(
                "On {0,date,long}, {1} had a balance of {2,number,currency}.", Locale.US);
        System.out.println(usMsg.format(new Object[] { when, "Alice", 1234.5 }));

        MessageFormat deMsg = new MessageFormat(
                "Am {0,date,long} hatte {1} ein Guthaben von {2,number,currency}.",
                Locale.GERMANY);
        System.out.println(deMsg.format(new Object[] { when, "Alice", 1234.5 }));
    }
}
