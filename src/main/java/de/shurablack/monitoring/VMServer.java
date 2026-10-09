package de.shurablack.monitoring;

import de.shurablack.core.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * Singleton TCP server that streams JVM metrics from {@link MBeansTracker} to connected clients.
 * <br><br>
 * Every message is a single line of JSON. A client receives {@link MBeansTracker#getSystemInfo()} once after
 * connecting and then {@link MBeansTracker#getLiveInfo()} every {@code vm.read-interval} seconds. Clients don't
 * need to send anything.
 * <br><br>
 * Configuration keys:
 * </p>
 * <ul>
 *     <li>{@code vm.port} (required): the port to listen on</li>
 *     <li>{@code vm.read-interval} (required): seconds between live data messages, must be positive</li>
 *     <li>{@code vm.host} (optional): the address to bind to, default {@code 127.0.0.1}</li>
 * </ul>
 * <p>
 * <b>Security:</b> the socket is plain text and has no authentication. Anyone who can reach the port can read
 * the metrics, so only bind to a non-loopback address inside a trusted network.
 * </p>
 *
 * @author ShuraBlack
 */
public class VMServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VMServer.class);

    /** Lazily created singleton */
    private static VMServer instance;

    /** The listening socket, {@code null} until {@link #enable()} succeeds */
    private ServerSocket server;

    /** The connected clients */
    private final List<Client> clients = new CopyOnWriteArrayList<>();

    /** Seconds between live data messages, read from {@code vm.read-interval} */
    private int readInterval = 5;

    /** The metrics source */
    private MBeansTracker tracker;

    /** The thread sending live data, ends when no client is connected */
    private Thread transfer;

    private VMServer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the singleton, creating it on the first call. The lazy creation isn't synchronized.
     * @return the server instance
     */
    public static VMServer get() {
        if (instance == null) {
            instance = new VMServer();
        }
        return instance;
    }

    /**
     * Reads the configuration, binds the server socket and starts accepting clients on a daemon thread.
     * <br><br>
     * Missing or invalid configuration and bind failures are logged and the server is not started.
     * The socket has no authentication, see the class description.
     * @throws NumberFormatException if {@code vm.port} isn't a number
     */
    public void enable() {
        final String rawPort = Config.getConfig("vm.port");
        if (rawPort == null || rawPort.isEmpty()) {
            LOGGER.error("VM port is not configured in the config file");
            return;
        }
        final String readInterval = Config.getConfig("vm.read-interval");
        if (readInterval == null || readInterval.isEmpty()) {
            LOGGER.error("VM read interval is not configured in the config file");
            return;
        }
        try {
            this.readInterval = Integer.parseInt(readInterval);
            if (this.readInterval <= 0) {
                LOGGER.error("VM read interval must be a positive integer");
                return;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid VM read interval format: {}", readInterval, e);
            return;
        }

        final int port = Integer.parseInt(Config.getConfig("vm.port"));

        // Loopback by default: the stream is unauthenticated. Set vm.host=0.0.0.0 to expose it
        final String host = Config.getConfig("vm.host") == null ? "127.0.0.1" : Config.getConfig("vm.host");
        tracker = MBeansTracker.get();
        try {
            server = new ServerSocket(port, 1, InetAddress.getByName(host));
            LOGGER.info("VMServer started on {}:{}", host, port);
            listenForClients();
        } catch (Exception e) {
            LOGGER.error("Failed to start VMServer on port {}", port, e);
        }
    }

    /**
     * Closes the server socket and all client connections.
     */
    public void stop() {
        if (server != null && !server.isClosed()) {
            try {
                server.close();
                LOGGER.info("VMServer stopped");
            } catch (Exception e) {
                LOGGER.error("Error while stopping VMServer", e);
            }
        }
        for (Client client : clients) {
            client.close();
        }
    }

    /**
     * Starts the daemon thread that accepts clients, sends them the system information and starts the
     * live data thread for the first client.
     */
    private void listenForClients() {
        Thread accept = new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = server.accept();
                    Client client = new Client(clientSocket);
                    if (client.set()) {
                        clients.add(client);
                        Thread.sleep(100);
                        client.send(tracker.getSystemInfo());
                        if (clients.size() == 1) {
                            startLiveData();
                        }
                        LOGGER.info("New client connected: {}", clientSocket.getInetAddress());
                    } else {
                        LOGGER.error("Failed to set up client connection");
                        client.close();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error accepting client connection", e);
                }
            }
        }, "VMServer_accept");
        accept.setDaemon(true);
        accept.start();

        startLiveData();
    }

    /**
     * Starts the daemon thread that sends live data to all clients unless it is already running.
     * <br><br>
     * Clients whose send fails are closed and removed. The thread ends once no client is connected.
     */
    private void startLiveData() {
        if (transfer != null && transfer.isAlive()) {
            LOGGER.warn("Live data transfer thread is already running");
            return;
        }
        transfer = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(readInterval * 1000L);
                    if (clients.isEmpty()) {
                        LOGGER.info("No clients connected, skipping live data transfer");
                        return;
                    }
                    JSONObject data = tracker.getLiveInfo();
                    for (Client client : clients) {
                        if (!client.send(data)) {
                            client.close();
                            clients.remove(client);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error in live data transfer", e);
                }
            }
        }, "VMServer_transfer");
        transfer.setDaemon(true);
        transfer.start();
    }

    /** A connected client with its output stream. */
    private static class Client {

        private final Socket socket;
        private PrintWriter out;

        public Client(Socket socket) {
            this.socket = socket;
        }

        /**
         * Opens the auto-flushing writer for the socket.
         * @return true if the writer was created
         */
        public boolean set() {
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                LOGGER.error("Failed to create PrintWriter for client", e);
                return false;
            }
            return true;
        }

        /**
         * Writes the message as one line.
         * <br><br>
         * {@link PrintWriter} doesn't throw on I/O errors, so a connection closed by the remote side
         * isn't detected here.
         * @param message the message to send
         * @return false if the socket was closed locally or writing threw an exception, true otherwise
         */
        public boolean send(JSONObject message) {
            if (socket.isClosed()) {
                return false;
            }

            try {
                out.println(message.toString());
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to send message to client", e);
                return false;
            }
        }

        /** Closes the writer and the socket; errors are logged. */
        public void close() {
            try {
                if (out != null) {
                    out.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to close client connection", e);
            }
        }
    }

    /** Manual test: starts the server from the config file and keeps the JVM alive. */
    private static class TestClient {
        public static void main(String[] args) {
            Config.loadConfig();
            VMServer server = VMServer.get();
            server.enable();

            // try {
            //     TestClient client = new TestClient("localhost", Integer.parseInt(Config.getConfig("vm.port")));
            //     client.listen();
            // } catch (Exception e) {
            //     System.err.println("Failed to connect to VMServer: " + e.getMessage());
            // }

            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private final Socket socket;
        private final BufferedReader in;

        public TestClient(String host, int port) throws Exception {
            this.socket = new Socket(host, port);
            this.in = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
        }

        public void listen() {
            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        System.out.println("Received: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                } finally {
                    try {
                        in.close();
                        socket.close();
                    } catch (Exception e) {
                        System.err.println("Error closing socket: " + e.getMessage());
                    }
                }
            }, "TestClient_listener").start();
        }

    }

}
