package de.shurablack.core.builder;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleCommandTest {

    private static CommandAction action(String name) {
        return new CommandAction(name, "", input -> { });
    }

    private static final List<CommandAction> ACTIONS = List.of(action("exit"), action("up"), action("upload"),
            action("force"), action("force cron"));

    private static String match(String input) {
        return BaseUtil.findCommand(ACTIONS, input).map(CommandAction::getName).orElse(null);
    }

    @Test
    void matchesTheNameWithOrWithoutArguments() {
        assertEquals("exit", match("exit"));
        assertEquals("upload", match("upload force"));
    }

    @Test
    void nameMustBeAWholeWord() {
        assertNull(match("exitfoo"));
        assertEquals("upload", match("upload"), "up must not shadow upload");
    }

    @Test
    void longestMatchingNameWins() {
        assertEquals("force cron", match("force cron daily"));
        assertEquals("force", match("force other"));
    }

    @Test
    void unknownInput() {
        assertEquals(Optional.empty(), BaseUtil.findCommand(ACTIONS, "help"));
        assertEquals(Optional.empty(), BaseUtil.findCommand(ACTIONS, ""));
    }
}
