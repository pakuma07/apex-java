# 01 — I/O and CP Basics

Fast I/O can be the difference between AC and TLE. Set it up **first**, before writing any logic. In Java this matters even more than in C++: `Scanner` is convenient but far too slow for large inputs.

---

## 1.1 The Essential Fast I/O Choice

In Java the equivalent of "turn on fast I/O" is: **never use `Scanner` for big inputs**. Pick one of:

- `BufferedReader` + `split`/manual parsing — simple, fast enough for most problems.
- `StreamTokenizer` — fastest stock option for whitespace-separated numbers.
- A custom `FastReader` over `DataInputStream` — fastest, for N > 10^6 tokens.

For output, always accumulate into a `StringBuilder` (or wrap `System.out` in a `BufferedWriter`/`PrintWriter`) and flush once.

**Rule**: Use fast I/O in every contest. `Scanner` is roughly 5–10× slower than `BufferedReader` and will TLE on large inputs.

---

## 1.2 Full I/O Setup Template

```java
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int n = Integer.parseInt(br.readLine().trim());
        // ... read more, build answer into sb ...

        System.out.print(sb);   // single flush at the end
    }
}
```

### StreamTokenizer variant (fastest stock reader for numbers)

```java
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        StreamTokenizer in = new StreamTokenizer(
            new BufferedReader(new InputStreamReader(System.in)));
        PrintWriter out = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(System.out)));

        in.nextToken(); int n = (int) in.nval;   // nval is a double
        // ...
        out.flush();
    }
}
```

> Note: in Java there is no `bits/stdc++.h` and no `using namespace`. Just `import java.io.*;` and `import java.util.*;` — that covers nearly everything CP needs.

---

## 1.3 Reading Until EOF

```java
// Pattern 1: read integers until EOF (StreamTokenizer)
while (in.nextToken() != StreamTokenizer.TT_EOF) {
    int x = (int) in.nval;
    // process x
}

// Pattern 1b: read integers until EOF (BufferedReader, line of tokens)
String line;
while ((line = br.readLine()) != null) {
    for (String tok : line.split("\\s+")) {
        if (tok.isEmpty()) continue;
        int x = Integer.parseInt(tok);
        // process x
    }
}

// Pattern 2: read lines until EOF
String s;
while ((s = br.readLine()) != null) {
    // process s
}

// Pattern 3: T test cases
int T = Integer.parseInt(br.readLine().trim());
while (T-- > 0) {
    // solve one test case
}
```

---

## 1.4 Reading a Whole Line After a Number

With `BufferedReader`, lines are read whole, so the C++ "leftover newline" problem largely disappears. If you mix token reading and line reading, just read the rest of the current line explicitly.

```java
// If the count and the line are on separate lines:
int n = Integer.parseInt(br.readLine().trim());
String line = br.readLine();   // the next full line

// If you used a tokenizer and now need the rest of a line, prefer
// reading everything with BufferedReader to avoid mixing styles.
```

---

## 1.5 Output Tips

```java
// Build everything in a StringBuilder, print once. '\n' does NOT flush.
StringBuilder sb = new StringBuilder();
sb.append(ans).append('\n');
System.out.print(sb);

// Avoid System.out.println in tight loops — each call can flush/auto-sync.
// Append '\n' to a StringBuilder instead, or use a PrintWriter and flush once.

// Fixed floating point (9 decimals). Use Locale.US so the decimal
// separator is '.' regardless of system locale.
System.out.printf(java.util.Locale.US, "%.9f%n", 3.14159265358979);
// or:
out.printf(java.util.Locale.US, "%.6f%n", ans);
```

> Pitfall: `String.format`/`printf` use the default locale, which on some systems uses a comma as the decimal separator. Always pass `Locale.US` in CP.

---

## 1.6 Common Constants (no typedefs/macros needed)

Java has no `typedef` or preprocessor macros — and you don't need them. Use plain types and named constants. `ArrayList<Integer>`, `long[]`, `int[][]`, and `Map.Entry`/`int[]` pairs replace the C++ aliases directly.

```java
// --- Short names for common values ---
static final int  INF  = (int) 1e9;        // infinity for int problems
static final long LINF = (long) 4e18;      // infinity for long
static final int  MOD  = 1_000_000_007;    // standard modulo (prime)
static final int  MOD2 = 998_244_353;      // alternative prime modulo

// --- "typedef" equivalents: just use the types directly ---
// long long  -> long          (always 64-bit in Java)
// pair<int,int> -> int[]{a,b} or a small record/class
// vector<int>   -> int[]  (fixed) or ArrayList<Integer> (dynamic)

// A tiny pair via a record (Java 16+):
record Pair(int first, int second) {}

// Loops are just plain for-loops; macros like FOR/REP/all/sz are unnecessary.
for (int i = 0; i < n; ++i) { /* ... */ }
```

> Note on pairs: prefer `int[]{a, b}` or `long[]{a, b}` in hot loops to avoid object overhead; use a `record` or small class when you want named fields and readability.

---

## 1.7 JVM Performance Notes (the Java analogue of GCC pragmas)

There is no per-file optimization pragma in Java; the JIT optimizes hot code automatically. Instead:

```text
- Warm-up: the first runs of a method are interpreted, then JIT-compiled.
  This is rarely an issue in a single run, but avoid micro-benchmarking the
  first iteration.
- Prefer primitive arrays (int[], long[]) over boxed collections in hot loops.
- Avoid creating garbage inside inner loops (no new Integer, no new String
  per iteration) to keep GC pauses away.
- For very deep recursion, increase the stack instead of an "optimize" flag:
  run your solver on a thread with a big stack (see 1.10).
```

---

## 1.8 Standard Imports (Non-trivial programs)

```java
// These two cover almost all CP needs:
import java.io.*;     // BufferedReader, StreamTokenizer, PrintWriter, ...
import java.util.*;   // ArrayList, HashMap, TreeMap, PriorityQueue, Arrays, ...

// Occasionally:
import java.math.BigInteger;   // exact arithmetic beyond 2^63
import java.math.BigDecimal;   // exact decimal arithmetic
import java.util.stream.*;     // streams (use sparingly in hot paths)
```

---

## 1.9 Custom Fast Read (when even StreamTokenizer is too slow)

```java
// Ultra-fast integer reader — for N > 10^6 inputs.
import java.io.*;

class FastReader {
    private final DataInputStream in;
    private final byte[] buffer = new byte[1 << 16];
    private int bufferPointer = 0, bytesRead = 0;

    FastReader(InputStream is) { in = new DataInputStream(is); }

    int nextInt() throws IOException {
        int ret = 0, b = read();
        while (b <= ' ') b = read();           // skip whitespace
        boolean neg = (b == '-');
        if (neg) b = read();
        while (b >= '0' && b <= '9') {         // accumulate digits
            ret = ret * 10 + (b - '0');
            b = read();
        }
        return neg ? -ret : ret;
    }

    long nextLong() throws IOException {
        long ret = 0; int b = read();
        while (b <= ' ') b = read();
        boolean neg = (b == '-');
        if (neg) b = read();
        while (b >= '0' && b <= '9') { ret = ret * 10 + (b - '0'); b = read(); }
        return neg ? -ret : ret;
    }

    private int read() throws IOException {
        if (bufferPointer == bytesRead) {
            bytesRead = in.read(buffer, 0, buffer.length);
            bufferPointer = 0;
            if (bytesRead == -1) return -1;
        }
        return buffer[bufferPointer++];
    }
}
// Usage: FastReader fr = new FastReader(System.in); int x = fr.nextInt();
```

This is the Java equivalent of a `getchar`-based reader; it works identically on every OS (no `getchar_unlocked` portability concern).

---

## 1.10 Recursion Depth — Big Stack Thread

Java's default thread stack overflows around ~10^4–10^5 deep recursive calls. For deep DFS, run your solver on a thread with a large stack:

```java
public class Main {
    public static void main(String[] args) {
        // 256 MB stack — plenty for deep DFS
        new Thread(null, Main::solve, "solver", 1 << 28).start();
    }
    static void solve() {
        // ... your recursive DFS here ...
    }
}
```

Alternatively, convert the recursion to an explicit stack (iterative DFS). There is no `-Xss` you can pass on most judges, so the big-stack thread is the portable trick.

---

## 1.11 Submitting: Last-Minute Checklist

```
✓ Fast I/O used (BufferedReader/StreamTokenizer/FastReader, never Scanner)
✓ Class is named Main (most judges require public class Main)
✓ Array sizes are large enough (+1 for safety)
✓ long used where values exceed 2×10^9; BigInteger for > 2^63
✓ Modular arithmetic applied where needed
✓ Output built in StringBuilder/PrintWriter, flushed once (no println in tight loops)
✓ Removed all debug output
✓ Deep recursion? Big-stack thread set up (1.10)
✓ printf uses Locale.US for floating point
✓ Read the problem constraints one more time
```

---

## Summary

| Technique | When to use |
|-----------|-------------|
| `BufferedReader` / `StreamTokenizer` | Always (never `Scanner` for big input) |
| `StringBuilder` / `PrintWriter`, single flush | Always for output |
| `'\n'` appended to a buffer instead of `println` | Always in loops |
| `import java.io.*; import java.util.*;` | Every program |
| Custom `FastReader` over `DataInputStream` | N > 10^6 integer inputs |
| Big-stack `Thread` (`1 << 28`) | Deep recursion / DFS |
| `BigInteger` | Values beyond `long` (2^63) |

---

**Next**: [02 — Time and Space Complexity](02_time_complexity.md)
