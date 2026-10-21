package de.shurablack.monitoring;

import de.shurablack.core.util.Config;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VMServerTest {

    /** Tests run in target/test-workdir, see the surefire configuration */
    private static final Path CONFIG = Path.of("config.properties");

    private final VMServer server = VMServer.get();

    @BeforeEach
    void configure() throws IOException {
        // Port 0 binds a free port
        Files.writeString(CONFIG, "access_token=test\nvm.port=0\nvm.read-interval=1\n");
        Config.loadConfig();
    }

    @AfterEach
    void cleanUp() throws IOException {
        server.stop();
        Files.deleteIfExists(CONFIG);
    }

    private static boolean serverThreadsAlive() {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.getName().startsWith("VMServer_") && thread.isAlive());
    }

    @Test
    void streamsSystemInfoAndLiveData() throws IOException {
        server.enable();
        final int port = server.getPort();
        assertTrue(port > 0);

        try (Socket socket = new Socket("127.0.0.1", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(5_000);
            assertFalse(new JSONObject(in.readLine()).isEmpty(), "system info");
            assertFalse(new JSONObject(in.readLine()).isEmpty(), "live data");
        }
    }

    @Test
    void stopEndsBothThreadsAndAllowsARestart() throws InterruptedException {
        server.enable();
        assertTrue(serverThreadsAlive());

        server.stop();
        assertEquals(-1, server.getPort());
        // The transfer thread notices the stop after at most one read interval
        final long deadline = System.currentTimeMillis() + 5_000;
        while (serverThreadsAlive() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertFalse(serverThreadsAlive(), "the accept or transfer thread kept running after stop()");

        server.enable();
        assertTrue(server.getPort() > 0);
    }

    @Test
    void invalidPortIsLoggedInsteadOfThrown() throws IOException {
        Files.writeString(CONFIG, "access_token=test\nvm.port=abc\nvm.read-interval=1\n");
        Config.loadConfig();

        assertDoesNotThrow(server::enable);
        assertEquals(-1, server.getPort());
    }
}
