// Chapter 22: Networking -- runnable example
//
// Demonstrates a LOCAL loopback TCP echo on an ephemeral port:
//   - a ServerSocket bound to port 0 (the OS picks a free port) on a background thread
//   - a client connects to 127.0.0.1 on that port, sends a line, reads the echo
//   - clean shutdown of both sides
//
// Everything stays on the loopback interface (127.0.0.1), so it is fully offline
// and deterministic — no external network is touched.
//
// A short HttpClient snippet is included in a comment block (NOT executed in main,
// to keep this program offline and deterministic).
//
// Compile & run (Java 17+):
//   javac chapter22_networking.java
//   java  chapter22_networking

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class chapter22_networking {

    public static void main(String[] args) throws Exception {
        System.out.println("=== TCP loopback echo demo ===");

        // The server binds to port 0 → the OS assigns a free ephemeral port.
        // We open it here (in the main thread) so we can read the chosen port,
        // then run its accept/serve loop on a background thread.
        ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        int port = server.getLocalPort();           // the actual port the OS picked
        System.out.println("Server listening on 127.0.0.1:" + port);

        // A latch so the client only connects once the server thread is in accept().
        CountDownLatch ready = new CountDownLatch(1);

        Thread serverThread = new Thread(() -> runEchoServer(server, ready), "echo-server");
        serverThread.start();

        // Wait until the server signals it is about to accept (bounded, just in case).
        ready.await(2, TimeUnit.SECONDS);

        // ---- CLIENT side ----
        // try-with-resources closes the socket and both wrapped streams.
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);   // auto-flush
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            socket.setSoTimeout(2000);               // read timeout — never hang forever

            String message = "Hello, server";
            System.out.println("Client sends   : " + message);
            out.println(message);                    // send a line

            String reply = in.readLine();            // read the echoed reply
            System.out.println("Client receives: " + reply);
        }

        // The client closed its socket, so the server's readLine() returns null and
        // its handler loop ends. Wait for the server thread to finish, then close it.
        serverThread.join(2000);
        server.close();
        System.out.println("Server shut down cleanly.");

        httpClientSnippet();   // prints the (non-executed) HttpClient example for reference
    }

    /**
     * Accept ONE client connection and echo back each line it sends, prefixed with
     * "echo: ". Returns when the client closes the connection (readLine() == null).
     */
    private static void runEchoServer(ServerSocket server, CountDownLatch ready) {
        ready.countDown();                           // signal: about to block in accept()
        try (Socket client = server.accept();        // blocks until the client connects
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            System.out.println("Server accepted a connection from " + client.getInetAddress());
            String line;
            while ((line = in.readLine()) != null) { // null == client closed its side
                out.println("echo: " + line);        // write the echo back
            }
        } catch (IOException e) {
            // server.close() from main can interrupt accept(); that's expected at shutdown.
            System.out.println("Server stopped: " + e.getMessage());
        }
    }

    /**
     * Prints a reference HttpClient snippet. It is NOT executed (no real network call),
     * so the program stays offline and deterministic. The code below is valid Java 11+.
     */
    private static void httpClientSnippet() {
        System.out.println();
        System.out.println("=== HttpClient snippet (reference only; not executed) ===");
        System.out.println("""
            import java.net.URI;
            import java.net.http.*;
            import java.time.Duration;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))   // connection timeout
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.example.com/users/1"))
                    .timeout(Duration.ofSeconds(30))          // request timeout
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // Synchronous: blocks until the response (or a timeout) arrives.
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());

            // Asynchronous: returns a CompletableFuture, never blocks the caller.
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenApply(HttpResponse::body)
                  .thenAccept(System.out::println);
            """);
    }
}
