package de.shurablack.core.util;

import de.shurablack.core.builder.CommandAction;
import net.dv8tion.jda.api.JDAInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBannerTest {

    @Test
    void placeholdersAreReplacedWithRealValues() throws IOException {
        final CommandAction action = new CommandAction("upload", "uploads commands", input -> { });

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new FileBanner("/internal/banner.txt", List.of(action)).print(new PrintStream(out, true, StandardCharsets.UTF_8));
        final String text = out.toString(StandardCharsets.UTF_8);

        assertFalse(text.contains("${"), text);
        assertFalse(text.contains("\\033"), "escape codes must be real ESC characters");
        assertTrue(text.contains("\u001B[0m"));
        assertTrue(text.matches("(?s).*\nVersion: \\d.*"), "the library version comes from the pom");
        assertTrue(text.contains("JDA Version: " + JDAInfo.VERSION));
        assertTrue(text.contains("upload -> uploads commands"));
    }
}
