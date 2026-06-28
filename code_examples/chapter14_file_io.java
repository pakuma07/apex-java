// Chapter 14: File I/O - Runnable Java Examples
// Compile: javac chapter14_file_io.java
// Run:     java chapter14_file_io
//
// Java adaptation of the C++ file I/O chapter. This program is fully
// self-contained: every file it creates is written to the SYSTEM TEMP DIR
// and DELETED before the program exits, so it leaves no junk and requires
// no pre-existing files.
//
// Key Java APIs demonstrated:
//   - java.nio.file.Path / Files  (modern file API)
//   - BufferedReader / BufferedWriter
//   - try-with-resources (auto-close)
//   - DataOutputStream / DataInputStream  (binary I/O, C++ ios::binary analogue)
//   - RandomAccessFile  (seek/tell analogue)
//   - String.split / String.format / printf  (parsing & formatting)

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class chapter14_file_io {

    // Track every temp file we touch so we can clean them all up at the end.
    static final List<Path> createdFiles = new ArrayList<>();

    static Path tempFile(String name) throws IOException {
        Path p = Files.createTempFile("ch14_" + name + "_", ".tmp");
        createdFiles.add(p);
        return p;
    }

    // ============================================================
    // EXAMPLE 1: Writing Text Files (BufferedWriter)
    // ============================================================
    static Path example1WriteText() throws IOException {
        System.out.println("\n=== EXAMPLE 1: Writing Text Files ===");
        Path file = tempFile("text");
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("Hello, World!");      out.newLine();
            out.write("Line 2: " + 42);      out.newLine();
            out.write("Line 3: " + 3.14159); out.newLine();
        }
        System.out.println("File written: " + file.getFileName());
        return file;
    }

    // ============================================================
    // EXAMPLE 2: Reading Text Files (BufferedReader, line by line)
    // ============================================================
    static void example2ReadText(Path file) throws IOException {
        System.out.println("\n=== EXAMPLE 2: Reading Text Files ===");
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = in.readLine()) != null) {
                System.out.println("Line " + (++lineCount) + ": " + line);
            }
        }
    }

    // ============================================================
    // EXAMPLE 3: Word-by-Word Reading (split on whitespace)
    // ============================================================
    static void example3ReadWords() throws IOException {
        System.out.println("\n=== EXAMPLE 3: Word-by-Word Reading ===");
        Path file = tempFile("words");
        Files.writeString(file, "apple banana cherry date elderberry fig grape\n");

        String content = Files.readString(file).trim();
        String[] words = content.split("\\s+");
        System.out.print("Words in file: ");
        for (String w : words) System.out.print(w + " ");
        System.out.println("\nTotal words: " + words.length);
    }

    // ============================================================
    // EXAMPLE 4: Structured Data (CSV-like)
    // ============================================================
    static void example4StructuredData() throws IOException {
        System.out.println("\n=== EXAMPLE 4: Structured Data (CSV) ===");
        Path file = tempFile("data");
        List<String> lines = List.of(
                "Name,Age,Score",
                "Alice,25,95.5",
                "Bob,30,87.3",
                "Charlie,28,92.1");
        Files.write(file, lines, StandardCharsets.UTF_8);

        List<String> read = Files.readAllLines(file, StandardCharsets.UTF_8);
        System.out.println("Header: " + read.get(0));
        for (int i = 1; i < read.size(); i++) {
            String[] cols = read.get(i).split(",");
            System.out.println("Record: name=" + cols[0]
                    + ", age=" + cols[1] + ", score=" + cols[2]);
        }
    }

    // ============================================================
    // EXAMPLE 5: In-Memory String Building/Parsing
    // (C++ stringstream analogue; Java uses StringBuilder + split)
    // ============================================================
    static void example5StringStreams() {
        System.out.println("\n=== EXAMPLE 5: String Building/Parsing ===");
        String input = "10 20 30 40 50";
        List<Integer> numbers = new ArrayList<>();
        for (String tok : input.split("\\s+")) {
            numbers.add(Integer.parseInt(tok));
        }
        System.out.print("Parsed numbers: ");
        for (int n : numbers) System.out.print(n + " ");
        System.out.println();

        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(numbers.size()).append(" numbers");
        System.out.println(sb);
    }

    // ============================================================
    // EXAMPLE 6: Binary File Operations (DataOutputStream/DataInputStream)
    // ============================================================
    // A simple fixed-shape record written as binary fields.
    record EmployeeRecord(int id, String name, double salary) {}

    static void example6BinaryFiles() throws IOException {
        System.out.println("\n=== EXAMPLE 6: Binary File Operations ===");
        Path file = tempFile("binary");

        List<EmployeeRecord> recs = List.of(
                new EmployeeRecord(1, "Alice", 50000.0),
                new EmployeeRecord(2, "Bob", 60000.0));

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
            for (EmployeeRecord r : recs) {
                out.writeInt(r.id());
                out.writeUTF(r.name());   // length-prefixed UTF string
                out.writeDouble(r.salary());
            }
        }
        System.out.println("Binary file written");

        System.out.println("Binary file contents:");
        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            while (true) {
                try {
                    int id = in.readInt();
                    String name = in.readUTF();
                    double salary = in.readDouble();
                    System.out.println("  ID: " + id + ", Name: " + name
                            + ", Salary: " + salary);
                } catch (EOFException eof) {
                    break;  // clean end-of-stream
                }
            }
        }
    }

    // ============================================================
    // EXAMPLE 7: File Positioning (RandomAccessFile = seek/tell)
    // ============================================================
    static void example7FilePositioning() throws IOException {
        System.out.println("\n=== EXAMPLE 7: File Positioning ===");
        Path file = tempFile("position");
        Files.writeString(file, "0123456789ABCDEFGHIJ");

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(5);                       // move read position (seekg)
            char ch = (char) raf.read();
            System.out.println("Character at position 5: " + ch);

            long size = raf.length();          // file size
            System.out.println("File size: " + size + " bytes");

            raf.seek(size - 3);                // last 3 characters
            byte[] last = new byte[3];
            raf.readFully(last);
            System.out.println("Last 3 characters: " + new String(last, StandardCharsets.UTF_8));
        }
    }

    // ============================================================
    // EXAMPLE 8: File State / Existence Checks
    // (Java uses Files.exists / isReadable rather than stream state bits)
    // ============================================================
    static void example8FileState() throws IOException {
        System.out.println("\n=== EXAMPLE 8: File State / Existence ===");
        Path file = tempFile("flags");
        Files.writeString(file, "Test data\n");

        System.out.println("exists:     " + Files.exists(file));
        System.out.println("isReadable: " + Files.isReadable(file));
        System.out.println("isWritable: " + Files.isWritable(file));
        System.out.println("size:       " + Files.size(file) + " bytes");
        System.out.println("File closed (Files.writeString auto-closes)");
    }

    // ============================================================
    // EXAMPLE 9: Formatted Output (printf / String.format)
    // ============================================================
    static void example9FormattedOutput() throws IOException {
        System.out.println("\n=== EXAMPLE 9: Formatted Output ===");
        Path file = tempFile("formatted");
        List<String> lines = new ArrayList<>();
        lines.add("Hex: " + Integer.toHexString(255));     // ff
        lines.add("Octal: " + Integer.toOctalString(255)); // 377
        lines.add("Decimal: " + 255);
        lines.add(String.format("Pi: %.2f", 3.14159));     // 3.14
        lines.add(String.format("E: %.2f", 2.71828));      // 2.72
        lines.add(String.format("%-10s%-10s", "Name", "Age"));
        lines.add(String.format("%-10s%-10d", "Alice", 25));
        Files.write(file, lines, StandardCharsets.UTF_8);

        System.out.println("Formatted output file:");
        for (String l : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            System.out.println("  " + l);
        }
    }

    // ============================================================
    // EXAMPLE 10: Error Handling (missing file)
    // ============================================================
    static void example10ErrorHandling() throws IOException {
        System.out.println("\n=== EXAMPLE 10: Error Handling ===");
        Path missing = Path.of("nonexistent_file_xyz.txt");
        try {
            Files.readAllLines(missing);
        } catch (NoSuchFileException e) {
            System.out.println("Cannot open file: " + e.getFile());
            System.out.println("exists: " + Files.exists(missing));
        }

        Path ok = tempFile("success");
        Files.writeString(ok, "Operation successful\n");
        System.out.println("Successfully wrote " + ok.getFileName());
    }

    // Clean up every temp file we created.
    static void cleanup() {
        System.out.println("\n=== CLEANUP: deleting temp files ===");
        for (Path p : createdFiles) {
            try {
                boolean deleted = Files.deleteIfExists(p);
                System.out.println((deleted ? "deleted " : "missing ") + p.getFileName());
            } catch (IOException e) {
                System.out.println("could not delete " + p + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 14: FILE I/O (Java)");
        System.out.println("======================================================");

        try {
            Path text = example1WriteText();
            example2ReadText(text);
            example3ReadWords();
            example4StructuredData();
            example5StringStreams();
            example6BinaryFiles();
            example7FilePositioning();
            example8FileState();
            example9FormattedOutput();
            example10ErrorHandling();
        } finally {
            cleanup();  // always leave no junk behind
        }

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
