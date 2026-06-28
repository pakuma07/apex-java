# Chapter 14: File I/O - Exercises

## Section 1: Text File Writing 🟢

1. Write text to file using `Files.writeString` / `PrintWriter`

2. Write multiple lines with line numbers (`BufferedWriter`)

3. Write structured text with formatting (`String.format` / `printf`)

## Section 2: Text File Reading 🟡

4. Read entire file line by line (`Files.readAllLines` / `BufferedReader`)

5. Read file word by word (`Scanner`)

6. Count lines and words in file

## Section 3: File Parsing 🟡

7. Parse CSV-like format (`String.split`)

8. Extract specific fields from records (use a `record`)

9. Validate data during parsing

## Section 4: String Streams 🟡

10. Parse a string using `Scanner` (analog of `istringstream`)

11. Format output using `StringBuilder` / `String.format` (analog of `ostringstream`)

12. Implement custom string parser

## Section 5: Binary Files 🟡

13. Write a binary data structure to file (`DataOutputStream`)

14. Read binary file and reconstruct objects (`DataInputStream`)

15. Implement binary serialization (`ObjectOutputStream` / `byte[]`)

## Section 6: File Positioning 🟡

16. Seek to specific position in file (`RandomAccessFile.seek`)

17. Read file in reverse

18. Implement random access to fixed-size records (`RandomAccessFile`)

## Section 7: File State Checking 🟡

19. Check file state (`Files.exists`, `Files.isReadable`, `readLine() == null` for EOF)

20. Implement error recovery (`IOException` handling)

21. Validate file before processing

## Section 8: Advanced Formatting 🟡

22. Format output with width and precision (`printf` / `String.format`)

23. Write formatted table to file

24. Implement custom formatting rules

## Section 9: Combining Streams 🔴

25. Read from file and write to another (`Files.copy` / `BufferedReader`+`BufferedWriter`) 🏆

26. Implement data transformation pipeline

27. Create log file with timestamps (`java.time`)

## Section 10: Real-World File Operations 🔴

28. Implement configuration file reader (`Properties`)

29. Create data export system (CSV, binary)

30. Design robust file I/O wrapper class (`AutoCloseable` + try-with-resources)

---

## Tips for Success

- **Use try-with-resources**: Streams close automatically and exceptions are suppressed safely
- **`Files.exists` / `Files.isReadable`**: Verify a file before opening
- **`BufferedReader.readLine()`**: Returns `null` at end of file (EOF)
- **`Scanner`**: For tokenized/formatted input (`next`, `nextInt`, ...)
- **Binary data**: Use `DataOutputStream`/`DataInputStream` for typed values
- **Positioning**: `RandomAccessFile.seek(pos)` for random read/write
- **State**: Catch `IOException`; check return values (`null`, byte counts)
- **Formatting**: `String.format` / `printf` for width & precision
- **Charset**: Be explicit — prefer `StandardCharsets.UTF_8`
- **Flush**: `flush()` before relying on output (try-with-resources flushes on close)

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Basic text I/O
- **Medium (🟡)**: 18 exercises - Parsing, streams, positioning, formatting
- **Hard (🔴)**: 9 exercises - Transformation pipelines, robust systems

## Challenge Problems 🏆

- **Challenge 1**: Complex file transformation
- **Challenge 2**: Configuration file system
- **Challenge 3**: Robust error handling

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Not checking if the file exists / is readable
- Wrong stream object (`System.in` vs a file `InputStream`)
- Forgetting to use a binary stream (`DataInputStream`) for non-text data
- Reading past EOF without checking for `null`
- Position confusion (read offset vs write offset in `RandomAccessFile`)
- Relying on the default platform charset instead of UTF-8
- Not flushing / not closing output (use try-with-resources)
- Encoding issues with text

## Learning Outcomes

After completing these exercises, you will:
✓ Read and write text files effectively
✓ Parse structured data reliably
✓ Use `Scanner`/`StringBuilder` for in-memory parsing and formatting
✓ Work with binary files
✓ Implement random file access
✓ Check and handle file errors with exceptions
✓ Format output professionally
✓ Create robust file I/O systems
✓ Handle real-world file scenarios

## Java Exercise Example: Read a File with try-with-resources

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReadFile {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("notes.txt");
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
```
