// Chapter 23: Logging -- runnable example
//
// Uses ONLY java.util.logging (JUL), which is built into the JDK — no dependencies.
// Demonstrates:
//   - obtaining a Logger and setting its Level
//   - attaching a ConsoleHandler with a SimpleFormatter (and a custom line format)
//   - logging at several levels
//   - a parameterized message ("{0}", "{1}")
//   - a lazy Supplier message (only built if the level is enabled)
//   - logging an exception with log(Level.SEVERE, msg, throwable)
//
// Compile & run (Java 17+):
//   javac chapter23_logging.java
//   java  chapter23_logging
//
// JUL's ConsoleHandler writes to System.err, so the log lines appear on stderr.

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class chapter23_logging {

    // Make the SimpleFormatter print a clean one-line layout:
    //   2026-06-23 10:15:00 LEVEL logger - message
    // Set BEFORE any handler/formatter is created (it is read once at construction).
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$-7s %3$s - %5$s%6$s%n");
    }

    // Convention: one logger per class, named after the class.
    private static final Logger log = Logger.getLogger(chapter23_logging.class.getName());

    public static void main(String[] args) {
        // ---- Configure the logger ----
        // Let the logger itself pass FINE and above.
        log.setLevel(Level.FINE);
        // Don't also forward records to the root logger's default handler (avoids duplicates).
        log.setUseParentHandlers(false);

        // Attach our own console handler that also passes FINE, with a simple text format.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);

        System.out.println("(Log lines below are written to stderr by the ConsoleHandler.)");

        // ---- Log at several levels ----
        log.severe("A serious failure occurred");          // SEVERE
        log.warning("A potential problem was detected");   // WARNING
        log.info("Application started");                   // INFO
        log.config("Loaded configuration from app.yml");   // CONFIG
        log.fine("Detailed tracing: entering main loop");  // FINE (visible: threshold is FINE)
        log.finest("This FINEST line is BELOW the threshold and will NOT appear");

        // ---- Parameterized message: placeholders filled only when published ----
        String user = "alice";
        int orderId = 42;
        log.log(Level.INFO, "User {0} placed order {1}", new Object[]{ user, orderId });

        // ---- Lazy Supplier: the lambda runs ONLY if the level is enabled ----
        // FINE is enabled here, so this DOES run and prints the built message.
        log.fine(() -> "Lazy (enabled) report: " + buildExpensiveReport());
        // FINEST is below the threshold, so buildExpensiveReport() is NEVER called here.
        log.finest(() -> "Lazy (disabled) report: " + buildExpensiveReport());

        // ---- Logging an exception WITH its throwable (full stack trace) ----
        try {
            riskyOperation();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed while processing order " + orderId, e);
        }

        System.out.println("Done.");
    }

    /** Pretend this is costly; we want to avoid calling it when the level is disabled. */
    private static String buildExpensiveReport() {
        System.out.println("   [buildExpensiveReport() was actually invoked]");
        return "report-data";
    }

    /** Throws so we can demonstrate logging an exception with its stack trace. */
    private static void riskyOperation() {
        throw new IllegalStateException("simulated failure");
    }
}
