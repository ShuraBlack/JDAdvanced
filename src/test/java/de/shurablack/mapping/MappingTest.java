package de.shurablack.mapping;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingTest {

    @Test
    void commandIdSplitsPrimaryAndSubCommands() {
        final CommandId id = new CommandId("music play loud");

        assertEquals("music", id.getPrimary());
        assertTrue(id.isSubPresent());
        assertEquals(2, id.subSize());
        assertEquals(3, id.totalSize());
        assertEquals("play", id.getSub(0));
        assertEquals("loud", id.getSub(1));
        assertNull(id.getSub(2));
    }

    @Test
    void commandIdWithoutSubCommands() {
        final CommandId id = new CommandId("ping");

        assertEquals("ping", id.getPrimary());
        assertFalse(id.isSubPresent());
        assertEquals(1, id.totalSize());
        assertNull(id.getSub(0));
    }

    @Test
    void commandIdRejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> new CommandId(""));
    }

    @Test
    void multiKeyMapTracksTheKeysPerValue() {
        final MultiKeyMap<String, String> map = new MultiKeyMap<>();
        map.put("a", "vote");
        map.put("b", "vote");

        assertEquals("vote", map.get("a"));
        assertEquals(List.of("a", "b"), map.getKeys("vote"));

        map.remove("a");
        assertEquals(List.of("b"), map.getKeys("vote"));
        assertFalse(map.containsKey("a"));
    }

    @Test
    void multiKeyMapMovesAReassignedKey() {
        final MultiKeyMap<String, String> map = new MultiKeyMap<>();
        map.put("a", "vote");
        map.put("a", "vote");
        assertEquals(List.of("a"), map.getKeys("vote"), "the same pair must not be stored twice");

        assertEquals("vote", map.put("a", "poll"));
        assertEquals(List.of("a"), map.getKeys("poll"));
        assertNull(map.getKeys("vote"), "the key must leave the list of its old value");
    }

    @Test
    void multiKeyMapClear() {
        final MultiKeyMap<String, String> map = new MultiKeyMap<>();
        map.put("a", "vote");
        map.clear();

        assertTrue(map.isEmpty());
        assertNull(map.getKeys("vote"));
    }
}
