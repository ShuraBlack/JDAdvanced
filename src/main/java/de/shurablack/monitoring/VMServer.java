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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VMServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VMServer.class);

    private static VMServer instance;

    private ServerSocket server;

    private final List<Client> clients = Collections.synchronizedList(new ArrayList<>());

    private int readInterval = 5;

    private MBeansTracker tracker;

    private Thread transfer;

    private VMServer() {
        // Private constructor to prevent instantiation
    }

    public static VMServer get() {
        if (instance == null) {
            instance = new VMServer();
        }
        return instance;
    }

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
        tracker = MBeansTracker.get();
        try {
            server = new ServerSocket(port, 1, InetAddress.getByAddress(new byte[]{0, 0, 0, 0}));
            LOGGER.info("VMServer started on port {}", port);
            listenForClients();
        } catch (Exception e) {
            LOGGER.error("Failed to start VMServer on port {}", port, e);
        }
    }

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

    private static class Client {

        private final Socket socket;
        private PrintWriter out;

        public Client(Socket socket) {
            this.socket = socket;
        }

        public boolean set() {
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                LOGGER.error("Failed to create PrintWriter for client", e);
                return false;
            }
            return true;
        }

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
