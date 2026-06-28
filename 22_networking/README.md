# Chapter 22: Networking -- Java

Networking lets a program talk to another program — on the same machine or across the world — by exchanging bytes over a connection. Java's networking lives in two packages: the original **`java.net`** (sockets, `URL`, `InetAddress`, since Java 1.0) and the modern **`java.net.http`** (the fluent `HttpClient`, since Java 11). The socket layer is *low-level and stream-based* — you open a connection and read/write bytes much as you would a file, directly comparable to the BSD sockets API that C and C++ programmers reach for. The `HttpClient` layer is *high-level and declarative* — you build a request object, hand it to a client, and receive a typed response — and is the recommended starting point for any HTTP work today.

This chapter covers both layers: the TCP/IP client-server model and `InetAddress`, TCP sockets with `ServerSocket`/`Socket` (a complete echo server and client, plus multi-client handling with threads and executors), connectionless UDP with `DatagramSocket`/`DatagramPacket`, the `URL`/`URI` types, and the modern `HttpClient` with synchronous `send`, asynchronous `sendAsync` returning a `CompletableFuture`, GET/POST, headers, `BodyHandlers`, timeouts, and HTTP/2. Throughout, the cardinal rule of network code holds: connections fail, hang, and drop, so always set timeouts and rely on try-with-resources so sockets close.

> **C++/BSD sockets → Java equivalents — at a glance**
> - `socket()` + `bind()` + `listen()` + `accept()` (server) → `new ServerSocket(port)` + `serverSocket.accept()`
> - `socket()` + `connect()` (client) → `new Socket(host, port)`
> - `send()`/`recv()` on a TCP fd → `socket.getOutputStream()` / `socket.getInputStream()`
> - `socket()` with `SOCK_DGRAM` (UDP) → `DatagramSocket` + `DatagramPacket`
> - `getaddrinfo` / `gethostbyname` → `InetAddress.getByName` / `getAllByName`
> - `setsockopt(SO_RCVTIMEO)` → `socket.setSoTimeout(ms)`
> - `close(fd)` (manual) → **try-with-resources** (`Socket`/`ServerSocket` are `AutoCloseable`)
> - libcurl / a hand-rolled HTTP parser → `java.net.http.HttpClient`
> - `epoll`/`select` (non-blocking) → NIO `Selector` + `SocketChannel`

## 22.1 The TCP/IP and Client-Server Model

Almost all Java networking rests on the **TCP/IP** stack. Two machines are identified by **IP addresses** (IPv4 like `192.168.0.1` or IPv6 like `::1`), and within a machine a specific program is identified by a **port** number (0–65535; ports below 1024 are typically privileged). A **socket** is the endpoint of a connection: the pairing of an IP address and a port. The dominant pattern is **client-server** — a *server* waits (listens) on a known port, and a *client* initiates a connection to the server's address and port. Once connected, both sides can read and write.

Two transport protocols matter. **TCP** (Transmission Control Protocol) is *connection-oriented* and *reliable*: bytes arrive in order, without loss or duplication, like a phone call — you dial, talk, hang up. **UDP** (User Datagram Protocol) is *connectionless* and *unreliable*: you fire off independent datagrams that may arrive out of order, duplicated, or not at all, like dropping postcards in the mail — but with far less overhead, ideal for streaming, gaming, and DNS.

Before connecting, you often need to resolve a hostname to an address. The **`InetAddress`** class wraps an IP address and performs DNS lookups.

```java
import java.net.InetAddress;

// Resolve a hostname to an address (a DNS lookup)
InetAddress addr = InetAddress.getByName("example.com");
System.out.println(addr.getHostAddress());   // e.g. "93.184.216.34"
System.out.println(addr.getHostName());      // "example.com"

// The local machine
InetAddress local = InetAddress.getLocalHost();

// The loopback address (127.0.0.1 / ::1) — never leaves this machine
InetAddress loopback = InetAddress.getLoopbackAddress();

// A host can have multiple addresses (IPv4 + IPv6, load-balanced hosts)
InetAddress[] all = InetAddress.getAllByName("example.com");
```

> **Contrast with C++:** there is no portable standard-library networking in C++ before C++23's limited additions; you use the platform BSD sockets API (`getaddrinfo`, `sockaddr_in`) or a library such as Boost.Asio. Java folds address resolution into a single `InetAddress` class with checked-exception error reporting (`UnknownHostException`), so there is no manual `struct sockaddr` juggling.

---

## 22.2 TCP Sockets — ServerSocket and Socket

For TCP, Java gives you two classes. **`ServerSocket`** is the server's listening endpoint: you construct it on a port, then call `accept()`, which *blocks* until a client connects and then returns a connected **`Socket`**. The client side simply constructs a `Socket(host, port)`, which connects immediately. Once you hold a connected `Socket` on either side, you obtain an `InputStream` (`getInputStream()`) and an `OutputStream` (`getOutputStream()`) and read/write bytes exactly as in Chapter 14 — typically wrapping them in `BufferedReader`/`PrintWriter` for line-oriented text. Both `Socket` and `ServerSocket` implement `AutoCloseable`, so use try-with-resources.

```java
import java.io.*;
import java.net.*;

// ---- SERVER: a single-connection echo server ----
try (ServerSocket server = new ServerSocket(5000)) {        // listen on port 5000
    System.out.println("Listening on " + server.getLocalPort());

    try (Socket client = server.accept();                   // blocks until a client connects
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(client.getInputStream()));
         PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {  // true = auto-flush

        String line;
        while ((line = in.readLine()) != null) {            // null == client closed the connection
            out.println("echo: " + line);                   // write a response line back
        }
    }
}
```

```java
import java.io.*;
import java.net.*;

// ---- CLIENT: connect, send a line, read the echoed reply ----
try (Socket socket = new Socket("127.0.0.1", 5000);
     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
     BufferedReader in = new BufferedReader(
             new InputStreamReader(socket.getInputStream()))) {

    out.println("Hello, server");          // send a line
    String reply = in.readLine();          // read the echoed reply
    System.out.println(reply);             // "echo: Hello, server"
}
```

The end-of-stream signal is the same as file I/O: `readLine()` returns `null` (and `read()` returns `-1`) when the peer closes its side. Closing a `Socket` closes both its streams.

> **Contrast with C++:** the Java `accept()`/`getInputStream()`/`getOutputStream()` trio maps directly onto BSD `accept()`/`recv()`/`send()`, but Java hands you real stream objects so you never touch raw file descriptors or byte buffers unless you want to. `PrintWriter` with auto-flush plays the role of writing to the socket and flushing, and try-with-resources replaces the manual `close(fd)` you must remember in C.

---

## 22.3 Handling Multiple Clients

The single-connection server above can talk to only one client at a time, because `accept()` and `readLine()` both *block*. A real server loops on `accept()` and handles each accepted `Socket` concurrently. The classic approach is **thread-per-connection** — spawn a new thread for each client. Spawning a raw `Thread` per client does not scale to tens of thousands of connections, so production code uses an **`ExecutorService`** (a thread pool) to bound the number of OS threads, or — new in Java 21 — **virtual threads**, which make thread-per-connection cheap again because a blocked virtual thread costs almost nothing.

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

try (ServerSocket server = new ServerSocket(5000);
     ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {  // Java 21: virtual threads

    while (true) {
        Socket client = server.accept();        // accept the next connection
        pool.submit(() -> handle(client));       // handle it on its own (virtual) thread
    }
}

// One client's whole conversation runs here, on its own thread
static void handle(Socket client) {
    try (client;                                 // try-with-resources on an existing variable (Java 9+)
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(client.getInputStream()));
         PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
        String line;
        while ((line = in.readLine()) != null) {
            out.println("echo: " + line);
        }
    } catch (IOException e) {
        System.err.println("client error: " + e.getMessage());
    }
}
```

Before virtual threads, the standard pool was a fixed pool — `Executors.newFixedThreadPool(200)` — which caps OS threads but also caps concurrency. With virtual threads you can afford one thread per connection even for very high connection counts, because they are scheduled onto a small number of carrier threads and parked (not blocking an OS thread) whenever they wait on I/O. (See Chapter 19 on concurrency for the threading model behind this.)

> **C++ contrast:** the C++ world solves the same problem with `std::thread` per connection (the analogue of thread-per-connection) or an event loop over `epoll`/`select` (the analogue of Java NIO, below). Java 21 virtual threads give you the simple blocking programming model *and* event-loop-level scalability without writing the event loop yourself.

---

## 22.4 UDP — DatagramSocket and DatagramPacket

For connectionless messaging, use **`DatagramSocket`** (the endpoint) and **`DatagramPacket`** (a single message, carrying both the payload bytes and the destination/source address). There is no `accept`, no connection, and no guarantee of delivery or ordering: you `send(packet)` and `receive(packet)` individual datagrams. The receiver pre-allocates a byte buffer; `receive` blocks until a packet arrives and fills in the buffer plus the sender's address.

```java
import java.net.*;

// ---- UDP receiver (server) ----
try (DatagramSocket socket = new DatagramSocket(6000)) {     // bind to port 6000
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    socket.receive(packet);                                  // blocks until a datagram arrives
    String msg = new String(packet.getData(), 0, packet.getLength());
    System.out.println("from " + packet.getAddress() + ": " + msg);
}

// ---- UDP sender (client) ----
try (DatagramSocket socket = new DatagramSocket()) {         // any free local port
    byte[] data = "ping".getBytes();
    InetAddress dest = InetAddress.getByName("127.0.0.1");
    DatagramPacket packet = new DatagramPacket(data, data.length, dest, 6000);
    socket.send(packet);                                     // fire and forget — may be lost
}
```

Use UDP when occasional loss is acceptable and latency matters (live audio/video, telemetry, DNS); use TCP when every byte must arrive in order. You can also call `setSoTimeout` on a `DatagramSocket` so `receive` does not block forever.

---

## 22.5 URL and URI

The **`URI`** class (since Java 1.4) is a pure, immutable parser/representation of a Uniform Resource Identifier — it splits a string into scheme, host, port, path, query, and fragment, and performs no I/O. The older **`URL`** class can additionally *open a connection* (`openStream`, `openConnection`), but its connection API is dated and its `equals`/`hashCode` perform blocking DNS lookups. Modern practice: use `URI` to model and validate addresses, and use `HttpClient` (next section) for the actual HTTP I/O rather than `URL.openStream`. Note that in Java 20+, `new URL(String)` is deprecated in favor of `URI.create(...).toURL()`.

```java
import java.net.*;

// Parse and inspect a URI — no network access happens here
URI uri = URI.create("https://example.com:443/path/page?query=java#section");
uri.getScheme();     // "https"
uri.getHost();       // "example.com"
uri.getPort();       // 443
uri.getPath();       // "/path/page"
uri.getQuery();      // "query=java"
uri.getFragment();   // "section"

// Resolve a relative reference against a base (like joining paths)
URI base = URI.create("https://example.com/docs/");
URI full = base.resolve("guide.html");     // https://example.com/docs/guide.html

// Convert to a URL only when an old API demands one
URL url = uri.toURL();
```

---

## 22.6 The Modern HttpClient

Since Java 11, **`java.net.http.HttpClient`** is the standard, fully supported way to make HTTP requests. It is built around three immutable, reusable pieces: a **`HttpClient`** (the connection manager — build one and reuse it), an **`HttpRequest`** (method, URI, headers, body), and an **`HttpResponse<T>`** (status, headers, and a typed body). The body type is chosen by a **`BodyHandler`** — `BodyHandlers.ofString()` for text, `ofByteArray()`, `ofInputStream()`, `ofFile(path)` to stream straight to disk, or `discarding()`. The client speaks **HTTP/2** by default and transparently falls back to HTTP/1.1.

### Building the client

```java
import java.net.http.*;
import java.time.Duration;

HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)          // default; will downgrade if needed
        .connectTimeout(Duration.ofSeconds(10))      // timeout for establishing the connection
        .followRedirects(HttpClient.Redirect.NORMAL) // follow 3xx (except https->http)
        .build();

// Or just: HttpClient client = HttpClient.newHttpClient();   // sensible defaults
```

### Synchronous GET

The `send` method blocks the calling thread until the response arrives (or a timeout fires). It throws checked `IOException` and `InterruptedException`.

```java
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/users/1"))
        .timeout(Duration.ofSeconds(30))             // timeout for the whole request
        .header("Accept", "application/json")
        .GET()                                       // GET is the default method
        .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println(response.statusCode());           // e.g. 200
System.out.println(response.headers().firstValue("Content-Type").orElse(""));
System.out.println(response.body());                 // the response body as a String
```

### POST with a body and headers

```java
import java.net.URI;
import java.net.http.*;

String json = "{\"name\":\"Alice\",\"age\":25}";

HttpRequest post = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/users"))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + token)
        .POST(HttpRequest.BodyPublishers.ofString(json))   // request body
        .build();

HttpResponse<String> resp = client.send(post, HttpResponse.BodyHandlers.ofString());
```

`BodyPublishers` mirrors `BodyHandlers` on the request side: `ofString`, `ofByteArray`, `ofFile(path)`, `noBody()`, and `ofInputStream(...)`.

### Asynchronous requests with CompletableFuture

`sendAsync` returns immediately with a **`CompletableFuture<HttpResponse<T>>`**, so the calling thread is never blocked; you chain continuations that run when the response arrives. This is ideal for firing many requests concurrently. (See Chapter 20 on `CompletableFuture` for the composition API.)

```java
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

CompletableFuture<String> future = client
        .sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)               // extract the body when it arrives
        .exceptionally(ex -> "failed: " + ex.getMessage());

future.thenAccept(System.out::println);              // non-blocking callback

// Fire many requests in parallel and wait for all of them
var urls = java.util.List.of("https://a.example", "https://b.example");
CompletableFuture<?>[] all = urls.stream()
        .map(u -> client.sendAsync(
                HttpRequest.newBuilder(URI.create(u)).build(),
                HttpResponse.BodyHandlers.ofString()))
        .toArray(CompletableFuture[]::new);
CompletableFuture.allOf(all).join();                 // wait for all to complete
```

> **Contrast with C++:** C++ has no standard HTTP client at all — you reach for libcurl, cpp-httplib, or Boost.Beast. Java's `HttpClient` is a batteries-included, HTTP/2-capable client in the standard library, with first-class async via `CompletableFuture` rather than callbacks or a third-party future type.

---

## 22.7 Blocking vs Non-Blocking (NIO)

The `Socket`/`ServerSocket` API above is **blocking**: a thread calling `accept`, `read`, or `receive` is parked until the operation completes. This is simple and, with virtual threads (22.3), now also scalable. The alternative is **non-blocking I/O** from the `java.nio.channels` package: a **`SocketChannel`**/`ServerSocketChannel` can be put in non-blocking mode and registered with a **`Selector`**, which lets a *single* thread monitor thousands of channels and react only to those that are ready to read or write — the Java analogue of `epoll`/`select`/`kqueue`.

```java
import java.nio.channels.*;
import java.net.InetSocketAddress;

Selector selector = Selector.open();
ServerSocketChannel server = ServerSocketChannel.open();
server.bind(new InetSocketAddress(5000));
server.configureBlocking(false);                       // non-blocking
server.register(selector, SelectionKey.OP_ACCEPT);     // tell the selector what to watch

while (true) {
    selector.select();                                 // blocks until at least one channel is ready
    for (SelectionKey key : selector.selectedKeys()) {
        if (key.isAcceptable()) { /* accept a new connection */ }
        if (key.isReadable())   { /* read from a ready channel */ }
    }
    selector.selectedKeys().clear();
}
```

NIO selectors are powerful but considerably more complex than blocking sockets. For most applications on Java 21, the recommendation is: write straightforward **blocking** code on **virtual threads**, and reach for `Selector`-based NIO (or a framework like Netty that wraps it) only when you have measured a specific need.

---

## 22.8 Best Practices

The following idioms summarize how to write robust network code in modern Java.

```java
// ✅ Always set timeouts — networks hang. Connection AND request/read timeouts.
HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
socket.setSoTimeout(30_000);          // read timeout on a blocking Socket (ms)

// ✅ Close sockets with try-with-resources (they are AutoCloseable)
try (Socket s = new Socket(host, port)) { /* ... */ }

// ✅ Reuse one HttpClient — it pools connections; do not create one per request
HttpClient client = HttpClient.newHttpClient();   // build once, share

// ✅ Prefer thread pools or virtual threads over an unbounded thread-per-connection
try (var pool = Executors.newVirtualThreadPerTaskExecutor()) { /* submit handlers */ }

// ✅ Use blocking I/O on virtual threads for IO-bound servers (simple AND scalable)

// ✅ Use URI to model addresses; HttpClient (not URL.openStream) for HTTP

// ✅ Handle IOException — it is checked; a peer can drop the connection anytime

// ❌ Don't trust input length blindly; bound buffers and validate before parsing
```

The central themes: set timeouts on everything, close sockets via try-with-resources, build one `HttpClient` and reuse it, bound concurrency with pools or lean on virtual threads, prefer the high-level `HttpClient` over hand-rolled HTTP, and treat every network operation as something that can fail.

---

## Summary

| Task | Java API |
|------|----------|
| **Resolve a hostname** | `InetAddress.getByName` / `getAllByName` |
| **TCP server** | `ServerSocket`, `accept()` → `Socket` |
| **TCP client** | `new Socket(host, port)` |
| **Read/write a socket** | `socket.getInputStream()` / `getOutputStream()` (often `BufferedReader`/`PrintWriter`) |
| **Multiple clients** | `ExecutorService` pool or `newVirtualThreadPerTaskExecutor()` (Java 21) |
| **UDP** | `DatagramSocket` + `DatagramPacket` (`send`/`receive`) |
| **Parse an address** | `URI` (pure); `URL` only for legacy APIs |
| **HTTP requests** | `HttpClient` + `HttpRequest` + `HttpResponse` (Java 11+) |
| **Sync vs async HTTP** | `client.send(...)` vs `client.sendAsync(...)` → `CompletableFuture` |
| **Response/request body** | `BodyHandlers.*` / `BodyPublishers.*` |
| **Read timeout** | `socket.setSoTimeout(ms)`; `HttpRequest.timeout(...)` |
| **Non-blocking I/O** | `SocketChannel` + `Selector` (`java.nio.channels`) |
| **Auto-close** | try-with-resources on `Socket`/`ServerSocket` |

---

## Next Steps
- Build a TCP echo server and client with `ServerSocket`/`Socket`
- Handle many clients with an `ExecutorService` or virtual threads
- Make HTTP calls with `HttpClient` — synchronous `send` and async `sendAsync`
- Model addresses with `URI`; reuse a single `HttpClient`
- Move to [Chapter 23: Logging](../23_logging/README.md)
