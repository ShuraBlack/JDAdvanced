package de.shurablack.core.util;

import de.shurablack.core.builder.InitException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmallUtilTest {

    /** Tests run in target/test-workdir, see the surefire configuration */
    private static final Path CONFIG = Path.of("config.properties");

    @AfterEach
    void removeConfig() throws IOException {
        Files.deleteIfExists(CONFIG);
    }

    @Test
    void mentionsRoundTrip() {
        assertEquals("<@&1>", MentionUtil.wrapRole("1"));
        assertEquals("<@2>", MentionUtil.wrapUser("2"));
        assertEquals("<#3>", MentionUtil.wrapChannel("3"));
        assertEquals("<:smile:4>", MentionUtil.wrapEmoji("4", "smile"));

        assertEquals("1", MentionUtil.unwrap(MentionUtil.wrapRole("1")));
        assertEquals("2", MentionUtil.unwrap(MentionUtil.wrapUser("2")));
        assertEquals("3", MentionUtil.unwrap(MentionUtil.wrapChannel("3")));
        assertEquals("4", MentionUtil.unwrap(MentionUtil.wrapEmoji("4", "smile")));
    }

    @Test
    void unwrapReturnsTheIdForEveryMentionType() {
        assertEquals("5", MentionUtil.unwrap("<@!5>"), "legacy nickname mention");
        assertEquals("6", MentionUtil.unwrap("<a:dance:6>"), "animated emoji");
        assertEquals("7", MentionUtil.unwrap("</music play:7>"), "slash command");
        assertEquals("8", MentionUtil.unwrap(MentionUtil.wrapSlashCommand("8")));
    }

    @Test
    void unwrapRejectsInvalidInput() {
        assertNull(MentionUtil.unwrap(null));
        assertNull(MentionUtil.unwrap(""));
        assertNull(MentionUtil.unwrap("plain text"));
        assertNull(MentionUtil.unwrap("<@123"));
        assertNull(MentionUtil.unwrap("<x>"));
    }

    @Test
    void configNeedsTheFileAndTheToken() throws IOException {
        Files.deleteIfExists(CONFIG);
        assertThrows(InitException.class, Config::loadConfig);

        Files.writeString(CONFIG, "other=value");
        assertThrows(InitException.class, Config::loadConfig);
    }

    @Test
    void configValues() throws IOException {
        Files.writeString(CONFIG, "access_token=token\nthreads=8\nbig=9000000000\n");
        Config.loadConfig();

        assertEquals("token", Config.getConfig("access_token"));
        assertNull(Config.getConfig("missing"));
        assertEquals(8, Config.getConfigAsInt("threads", 1));
        assertEquals(1, Config.getConfigAsInt("missing", 1));
        assertEquals(9_000_000_000L, Config.getConfigAsLong("big", 0));
        assertThrows(NumberFormatException.class, () -> Config.getConfigAsInt("access_token", 0));
    }

    @Test
    void hasRole() {
        final Role role = mock(Role.class);
        when(role.getId()).thenReturn("42");
        final Member member = mock(Member.class);
        when(member.getRoles()).thenReturn(List.of(role));

        assertTrue(ServerUtil.hasRole(member, "42"));
        assertFalse(ServerUtil.hasRole(member, "43"));
    }
}
