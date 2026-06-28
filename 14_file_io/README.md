# Chapter 14: File I/O

File input/output (I/O) lets a program read data from and write data to files on disk, so information persists after the program ends. Java offers two layers for this: the original **`java.io`** package (streams and `File`, since Java 1.0) and the modern **`java.nio.file`** package (NIO.2, `Path`/`Files`, since Java 7). The `java.io` model is *stream-based* — data flows as a sequence of bytes or characters through wrapped stream objects — directly comparable to C++'s `<iostream>`/`<fstream>` `<<`/`>>` design. The `java.nio.file` model is *path- and utility-based* — you describe a location with a `Path` and call static helper methods on `Files` — and is the recommended starting point for most file work today.

This chapter covers both layers: the `File` vs `Path`/`Files` distinction, reading and writing text the easy way (`Files.readString`/`writeString`, `readAllLines`) and the streaming way (`BufferedReader`/`BufferedWriter`, `Scanner`, `PrintWriter`), byte streams (`InputStream`/`OutputStream`) versus character streams, try-with-resources for guaranteed close, the `Files` utility for copy/move/delete/walk, `Path` manipulation, and an introduction to serialization. Throughout, the cardinal rule of file I/O holds: always handle the possibility that a file does not open, and rely on try-with-resources so streams close automatically.

> **C++ `<fstream>` → Java equivalents — at a glance**
> - `std::ofstream` (write) → `BufferedWriter` / `PrintWriter` / `Files.write` / `OutputStream`
> - `std::ifstream` (read) → `BufferedReader` / `Scanner` / `Files.readAllLines` / `InputStream`
> - `std::fstream` (both) → `RandomAccessFile` or `SeekableByteChannel` (`FileChannel`)
> - `<sstream>` (`stringstream`) → `StringReader`/`StringWriter`, `String.split`, `Scanner` on a string
> - RAII auto-close (stream destructor) → **try-with-resources** (`AutoCloseable`)
> - `ios::binary` raw bytes → byte streams (`InputStream`/`OutputStream`); text is always explicit charset
> - C `rename`/`remove`, `<filesystem>` (C++17) → `Files.move`, `Files.delete`, `Files.copy`, `Files.walk`

## 14.1 File vs Path/Files

Java has two ways to *name* a file. The legacy `java.io.File` class (since 1.0) represents a path and carries a grab-bag of methods (`exists()`, `delete()`, `renameTo()`) whose error reporting is poor — many return a `boolean` instead of throwing. The modern `java.nio.file.Path` (since Java 7) is an immutable, abstract path, and the companion **`Files`** utility class provides the operations as static methods that throw `IOException` with real diagnostics. Prefer `Path` + `Files` for all new code; you only encounter `File` when interoperating with older APIs (and you can convert with `file.toPath()` / `path.toFile()`).

```java
import java.io.File;
import java.nio.file.*;

// Legacy java.io.File
File legacy = new File("data.txt");
boolean exists = legacy.exists();        // boolean, no detail on failure

// Modern java.nio.file.Path — the preferred way
Path path = Path.of("data.txt");         // or Paths.get("data.txt")
boolean here = Files.exists(path);

// Interconversion when bridging old and new APIs
Path  fromFile = legacy.toPath();
File  toFile   = path.toFile();
```

> **Contrast with C++:** C++ historically used C functions (`fopen`, `rename`, `remove`) and bare path strings; `std::filesystem::path` (C++17) finally gave a real path abstraction. Java's `Path`/`Files` is the analogue of `std::filesystem`, and `File` is the analogue of the older C-style approach.

---

## 14.2 Text File Operations — The Easy Way

For small-to-medium files, `java.nio.file.Files` provides one-call helpers that handle opening, reading/writing, and closing for you. `Files.readString(path)` returns the whole file as one `String`; `Files.readAllLines(path)` returns a `List<String>`; `Files.lines(path)` returns a lazy `Stream<String>` (ideal for huge files, and itself an `AutoCloseable` resource). For writing, `Files.writeString(path, text)` and `Files.write(path, listOfLines)` do the inverse. All of these default to **UTF-8** since Java 18 (and you can always pass an explicit `Charset`). These methods throw `IOException`, a checked exception, so callers must handle or declare it.

```java
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

Path path = Path.of("text.txt");

// Write the whole file at once
Files.writeString(path, "Name: Alice\nAge: 25\n");

// Or write a list of lines (a newline is added after each)
Files.write(path, List.of("Name: Alice", "Age: 25"));

// Read the whole file as one String
String all = Files.readString(path);

// Read into a List of lines (loads everything into memory)
List<String> lines = Files.readAllLines(path);
for (String line : lines) {
    System.out.println(line);
}

// Stream lines lazily — best for large files; close the stream (try-with-resources)
try (Stream<String> stream = Files.lines(path)) {
    stream.filter(l -> l.startsWith("Name"))
          .forEach(System.out::println);
}
```

> **Contrast with C++:** C++ has no standard one-liner to slurp a whole file into a `std::string`; the common idiom is constructing a string from `istreambuf_iterator`s or looping with `getline`. Java's `Files.readString`/`readAllLines` are convenience methods with no direct C++ equivalent, and `Files.lines` returning a lazy `Stream<String>` mirrors how you might wrap an `ifstream` in a generator — but as a first-class, closeable stream.

---

## 14.3 Buffered Character Streams

When you need to read or write incrementally (line by line, or in a loop), wrap a stream in a **buffered** reader or writer. `BufferedReader.readLine()` returns the next line or `null` at end-of-file — the loop idiom `while ((line = r.readLine()) != null)` is the direct analogue of C++'s `while (getline(in, line))`. `BufferedWriter` buffers output and offers `newLine()` for the platform line separator. Buffering matters for performance: it batches the many tiny OS read/write calls into a few large ones. Obtain them via `Files.newBufferedReader`/`newBufferedWriter`, and always use try-with-resources so they close.

```java
import java.io.*;
import java.nio.file.*;

// Write line by line
Path out = Path.of("text.txt");
try (BufferedWriter writer = Files.newBufferedWriter(out)) {
    writer.write("Name: Alice");
    writer.newLine();                 // platform line separator
    writer.write("Age: 25");
    writer.newLine();
}   // flushed and closed automatically

// Read line by line — the idiomatic loop
Path in = Path.of("text.txt");
try (BufferedReader reader = Files.newBufferedReader(in)) {
    String line;
    while ((line = reader.readLine()) != null) {   // null == end of file
        System.out.println(line);
    }
}

// Read character by character (int return; -1 == EOF)
try (BufferedReader reader = Files.newBufferedReader(in)) {
    int ch;
    while ((ch = reader.read()) != -1) {
        System.out.print((char) ch);
    }
}

// Append mode: pass an OpenOption
try (BufferedWriter w = Files.newBufferedWriter(out, StandardOpenOption.APPEND)) {
    w.write("appended line");
    w.newLine();
}
```

> **Contrast with C++:** `BufferedReader.readLine()` ↔ `std::getline(in, line)`, but Java signals EOF by returning `null` (or `-1` from `read()`), whereas C++ signals it by setting the stream's `eof`/`fail` state which makes `getline` evaluate as false in the `while` condition. Java reads are always buffered explicitly via the wrapper; C++ streams buffer internally. By default Java overwrites the file; pass `StandardOpenOption.APPEND` to append, analogous to C++'s `ios::app`.

---

## 14.4 Scanner and PrintWriter

Two higher-level convenience classes round out text I/O. **`Scanner`** parses tokens and primitives from any source (a file, an `InputStream`, or even a `String`), with methods like `nextInt()`, `nextDouble()`, `next()` (whitespace-delimited token), `nextLine()`, and the `hasNext...()` guards — it is the natural analogue of C++'s `>>` extraction. **`PrintWriter`** offers `print`, `println`, and `printf`/`format` for formatted output — the analogue of `<<` and `std::cout` formatting. `Scanner` is convenient but slower than `BufferedReader` for bulk line reading; use it when you want token-level parsing.

```java
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

// Token parsing with Scanner (like C++ operator>>)
try (Scanner sc = new Scanner(Path.of("numbers.txt"))) {
    while (sc.hasNextInt()) {
        int n = sc.nextInt();         // whitespace-delimited, like  in >> n
        System.out.println(n);
    }
}

// Mixing tokens and lines — beware the leftover newline (same pitfall as C++!)
try (Scanner sc = new Scanner(Path.of("data.txt"))) {
    int age = sc.nextInt();
    sc.nextLine();                    // consume the rest of the line before reading a full line
    String rest = sc.nextLine();
}

// Formatted output with PrintWriter
try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Path.of("report.txt")))) {
    pw.println("Name: Alice");
    pw.printf("Age: %d%n", 25);       // %n is the platform newline
    pw.print("Done");
}

// Scanner over a String (like std::istringstream)
Scanner lineScanner = new Scanner("10 20 30");
int a = lineScanner.nextInt(), b = lineScanner.nextInt(), c = lineScanner.nextInt();
```

> **Contrast with C++:** `Scanner.nextInt()` ↔ `in >> n` and `PrintWriter.printf` ↔ `printf`/`std::cout` formatting. The classic "mixing `>>` and `getline`" pitfall — where the leftover newline makes the next `getline` empty — has an exact Java counterpart: mixing `nextInt()`/`next()` with `nextLine()` requires an extra `nextLine()` to discard the leftover newline.

---

## 14.5 Byte vs Character Streams

Java draws a sharp line that C++ blurs: **character streams** (`Reader`/`Writer`, e.g. `BufferedReader`, `FileReader`) handle *text* and perform charset decoding/encoding (bytes ↔ `char`), while **byte streams** (`InputStream`/`OutputStream`, e.g. `FileInputStream`, `BufferedInputStream`) handle *raw bytes* with no interpretation. Use byte streams for images, audio, compressed data, or any binary format; use character streams for text. The bridge classes `InputStreamReader`/`OutputStreamWriter` convert between the two using a specified `Charset` — this is exactly where text encoding is decided.

```java
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

// BYTE streams — raw bytes, no character translation (like C++ ios::binary)
try (InputStream  in  = Files.newInputStream(Path.of("image.png"));
     OutputStream out = Files.newOutputStream(Path.of("copy.png"))) {
    in.transferTo(out);               // efficient byte pump (Java 9+)
}

// Read all bytes at once
byte[] data = Files.readAllBytes(Path.of("image.png"));
Files.write(Path.of("copy2.png"), data);

// CHARACTER streams — decode bytes to text using an explicit charset
try (Reader reader = new InputStreamReader(
                         Files.newInputStream(Path.of("text.txt")),
                         StandardCharsets.UTF_8)) {
    int ch;
    while ((ch = reader.read()) != -1) System.out.print((char) ch);
}

// Writing a few raw bytes
try (OutputStream out = Files.newOutputStream(Path.of("data.bin"))) {
    out.write(new byte[]{ 0x48, 0x69 });   // bytes for "Hi"
}
```

> **Contrast with C++:** C++ has a single stream model and uses the `ios::binary` flag to suppress newline translation; the `char`/byte distinction is not enforced by the type system. Java enforces it: a `Reader`/`Writer` is fundamentally about `char` (and charset), while an `InputStream`/`OutputStream` is fundamentally about `byte`. There is no implicit platform encoding guesswork if you pass an explicit `Charset` — which you always should.

---

## 14.6 Reading and Writing Binary Data

For structured binary data, wrap a byte stream in **`DataOutputStream`/`DataInputStream`**, which write and read Java primitives in a portable, well-defined **big-endian** format (`writeInt`, `writeDouble`, `readInt`, `readDouble`, `writeUTF`/`readUTF` for length-prefixed strings). Unlike C++'s `reinterpret_cast` of an object's raw memory, this is type-safe and platform-independent — the same bytes read back correctly on any JVM. You must read fields back in the **same order and type** you wrote them.

```java
import java.io.*;
import java.nio.file.*;

// Write primitives in a portable binary format
try (DataOutputStream out = new DataOutputStream(
                                Files.newOutputStream(Path.of("data.bin")))) {
    out.writeInt(42);
    out.writeDouble(3.14);
    out.writeUTF("Alice");            // length-prefixed UTF-8 string
}

// Read them back — same order, same types
try (DataInputStream in = new DataInputStream(
                              Files.newInputStream(Path.of("data.bin")))) {
    int    x    = in.readInt();       // 42
    double d    = in.readDouble();    // 3.14
    String name = in.readUTF();       // "Alice"
    System.out.println(x + ", " + d + ", " + name);
}
```

> **Contrast with C++:** C++ writes binary with `out.write(reinterpret_cast<char*>(&x), sizeof(x))`, which dumps raw memory — fast but non-portable (endianness, padding) and *unsafe* for any type containing pointers (like writing a `Person` with a `std::string` member, as the C++ chapter warned). Java's `DataOutputStream` is the safe, portable replacement: it serializes each primitive in a defined layout, so there is no endianness surprise and no danger of writing an internal pointer. The cost is that you cannot blit a whole struct in one call — you write field by field.

---

## 14.7 Serialization Intro

To persist whole objects (not just primitives), Java offers **serialization**: a class that implements the marker interface `java.io.Serializable` can be written to an `ObjectOutputStream` and reconstructed from an `ObjectInputStream`, including its referenced object graph. Mark fields you do not want stored as `transient`. Serialization is convenient for quick persistence and for some RPC mechanisms, but it has well-known drawbacks (versioning fragility, security risks when deserializing untrusted data), so modern code often prefers JSON/protobuf libraries instead. This section shows the mechanics; treat built-in serialization as a tool of last resort for untrusted input.

```java
import java.io.*;

class Person implements Serializable {
    private static final long serialVersionUID = 1L;   // versioning handle
    String name;
    int    age;
    transient String tempToken;       // 'transient' fields are NOT serialized

    Person(String name, int age) { this.name = name; this.age = age; }
}

// Serialize an object graph to a file
try (ObjectOutputStream out = new ObjectOutputStream(
                                  new FileOutputStream("person.ser"))) {
    out.writeObject(new Person("Alice", 25));
}

// Deserialize it back
try (ObjectInputStream in = new ObjectInputStream(
                                new FileInputStream("person.ser"))) {
    Person p = (Person) in.readObject();   // throws ClassNotFoundException too
    System.out.println(p.name + ", " + p.age);
}
```

> **Contrast with C++:** C++ has **no built-in serialization** — you roll your own (writing each field) or use a library such as Boost.Serialization or Cereal. The C++ chapter's `reinterpret_cast` of a `Person` with a `std::string` was explicitly flagged as a bug; Java's `Serializable` is the correct, safe way to persist a whole object, handling the string contents and the object graph properly. The trade-off is the versioning/security caveats noted above.

---

## 14.8 Random Access and File Positioning

Most streams are sequential, but for random access (seek to an offset, overwrite in place) use `RandomAccessFile` or a NIO `SeekableByteChannel`/`FileChannel`. `RandomAccessFile` maintains a *file pointer* you query with `getFilePointer()` and move with `seek(offset)` (an absolute byte position from the start), and it can be opened read-only (`"r"`) or read-write (`"rw"`). This is the Java counterpart to C++'s `seekg`/`seekp`/`tellg`/`tellp`.

```java
import java.io.RandomAccessFile;

try (RandomAccessFile raf = new RandomAccessFile("data.bin", "rw")) {
    long pos = raf.getFilePointer();   // current position (like tellg/tellp)

    raf.seek(0);                       // to beginning (like seekg(0, ios::beg))
    raf.writeInt(42);

    raf.seek(raf.length() - 4);        // 4 bytes before end (like seekg(-4, ios::end))
    int last = raf.readInt();

    raf.seek(10);                      // absolute offset 10
    raf.write("Data".getBytes());
}
```

> **Contrast with C++:** C++ splits the position indicator into get (`tellg`/`seekg`) and put (`tellp`/`seekp`) sides and offsets can be relative to `ios::beg`/`cur`/`end`. Java's `RandomAccessFile.seek()` takes a single *absolute* byte offset (compute end-relative positions yourself with `length()`), and a single pointer serves both reading and writing. There is no separate get/put pointer.

---

## 14.9 The Files Utility — Copy, Move, Delete, Walk

Beyond reading and writing contents, programs manage files themselves. The `Files` class provides clear, exception-throwing operations: `Files.exists`, `Files.copy`, `Files.move` (with `StandardCopyOption.REPLACE_EXISTING`/`ATOMIC_MOVE`), `Files.delete` / `Files.deleteIfExists`, `Files.createDirectories`, and `Files.size`. To traverse a directory tree, `Files.walk(path)` returns a lazy `Stream<Path>` of every entry (and `Files.list` for one level, `Files.find` for a filtered walk). These supersede the old `File` boolean-returning methods and the C-style `rename`/`remove`.

```java
import java.nio.file.*;
import java.util.stream.Stream;

Path src = Path.of("source.txt");
Path dst = Path.of("destination.txt");

// Existence & metadata
boolean exists = Files.exists(src);
long    size   = Files.size(src);

// Copy (overwrite if present)
Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);

// Move / rename (optionally atomic)
Files.move(Path.of("old.txt"), Path.of("new.txt"), StandardCopyOption.REPLACE_EXISTING);

// Delete
Files.deleteIfExists(Path.of("temp.txt"));

// Create directories (parents too)
Files.createDirectories(Path.of("out/reports/2026"));

// Walk a directory tree (lazy stream — close it)
try (Stream<Path> tree = Files.walk(Path.of("project"))) {
    tree.filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".java"))
        .forEach(System.out::println);
}
```

> **Contrast with C++:** This maps onto C++17's `std::filesystem` (`copy`, `rename`, `remove`, `recursive_directory_iterator`). Pre-C++17 the C++ chapter had to use C's `rename`/`remove` and a `dst << src.rdbuf()` trick to copy. Java has had `Files` since Java 7 — richer and exception-based, where `Files.walk` returning a `Stream<Path>` is the lazy analogue of `recursive_directory_iterator`.

---

## 14.10 Path Operations

A `Path` is an immutable description of a location, and the class offers pure (no-disk-access) manipulation methods: `getFileName()`, `getParent()`, `getRoot()`, `resolve(child)` to append, `relativize(other)` to compute a relative path between two, `normalize()` to collapse `.`/`..`, and `toAbsolutePath()`. Because `Path` is immutable, every method returns a *new* `Path` (like Java `String`), which makes path arithmetic safe and composable.

```java
import java.nio.file.*;

Path p = Path.of("/home/alice/projects/app/Main.java");

p.getFileName();       // Main.java
p.getParent();         // /home/alice/projects/app
p.getRoot();           // /
p.getNameCount();      // 5 (number of name elements)
p.getName(0);          // home

// Build paths by resolving (joining)
Path base  = Path.of("/home/alice");
Path full  = base.resolve("docs/notes.txt");   // /home/alice/docs/notes.txt

// Compute one path relative to another
Path rel = base.relativize(full);              // docs/notes.txt

// Clean up redundant elements
Path messy = Path.of("/home/alice/../alice/./docs");
Path clean = messy.normalize();                // /home/alice/docs

// Make absolute (resolves against the working directory)
Path abs = Path.of("data.txt").toAbsolutePath();
```

> **Contrast with C++:** These mirror `std::filesystem::path` member functions (`filename()`, `parent_path()`, `operator/` for join, `lexically_relative`, `lexically_normal`). The semantics are nearly identical; Java's `Path` is immutable (returns new objects) whereas `std::filesystem::path` is mutable and supports `operator/=` for in-place appending.

---

## 14.11 Best Practices

The following idioms summarize how to write robust file code in modern Java.

```java
// ✅ Use try-with-resources — never rely on manual close() or finally
try (BufferedReader r = Files.newBufferedReader(path)) {
    // ...
}   // auto-closed even on exception

// ✅ Prefer Path/Files over the legacy File API
Path path = Path.of("file.txt");

// ✅ Always specify a Charset for text (don't depend on platform default)
Files.readString(path, StandardCharsets.UTF_8);

// ✅ For huge files, stream lazily instead of loading everything
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(l -> !l.isBlank()).forEach(this::process);
}

// ✅ Use byte streams for binary, character streams for text
try (InputStream in = Files.newInputStream(binPath)) { /* ... */ }

// ✅ Handle (or declare) IOException — it is checked
void load(Path p) throws IOException { Files.readString(p); }

// ✅ Use buffered streams for incremental I/O performance
try (BufferedWriter w = Files.newBufferedWriter(path)) { /* ... */ }

// ❌ Avoid built-in Serializable for untrusted data — prefer JSON/protobuf libs
```

The central themes: lean on try-with-resources so streams always close, prefer `Path`/`Files` over `File`, always state the charset for text, stream large files lazily, keep the byte/character distinction clear, and buffer for performance.

---

## Summary

| Operation | Java API |
|-----------|----------|
| **Read whole text file** | `Files.readString`, `Files.readAllLines` |
| **Read text line by line** | `BufferedReader.readLine()`, `Files.lines` (lazy stream) |
| **Write text** | `Files.writeString`, `Files.write`, `BufferedWriter`, `PrintWriter` |
| **Parse tokens** | `Scanner` (`nextInt`, `nextLine`, `hasNext...`) |
| **Read/write bytes** | `InputStream`/`OutputStream`, `Files.readAllBytes`/`write` |
| **Structured binary** | `DataInputStream`/`DataOutputStream` |
| **Persist objects** | `ObjectOutputStream`/`ObjectInputStream` (`Serializable`) |
| **Random access** | `RandomAccessFile`, `FileChannel`, `seek()` |
| **File management** | `Files.copy/move/delete/walk/createDirectories` |
| **Path manipulation** | `Path.resolve/relativize/normalize/getFileName` |
| **Auto-close** | try-with-resources (`AutoCloseable`) — Java's RAII |

---

## Next Steps
- Read/write text with `Files` and `BufferedReader`/`BufferedWriter`
- Handle binary data with byte streams and `DataInputStream`/`DataOutputStream`
- Parse structured text with `Scanner` and `String.split`
- Manage files and walk directories with the `Files` utility
- Move to [Chapter 15: Advanced Features](../15_advanced_features/README.md)
