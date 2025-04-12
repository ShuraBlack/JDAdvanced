package de.shurablack.core.scheduling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntryTest {

    @Test
    void getNameReturnsCorrectName() {
        Entry entry = new Entry("TestName", "123", () -> {});
        assertEquals("TestName", entry.getName());
    }

    @Test
    void getIdReturnsCorrectId() {
        Entry entry = new Entry("TestName", "123", () -> {});
        assertEquals("123", entry.getId());
    }

    @Test
    void getTaskReturnsCorrectTask() {
        Runnable task = () -> {};
        Entry entry = new Entry("TestName", "123", task);
        assertEquals(task, entry.getTask());
    }

    @Test
    void equalsReturnsTrueForEntriesWithSameId() {
        Entry entry1 = new Entry("TestName1", "123", () -> {});
        Entry entry2 = new Entry("TestName2", "123", () -> {});
        assertTrue(entry1.equals(entry2));
    }

    @Test
    void equalsReturnsFalseForEntriesWithDifferentIds() {
        Entry entry1 = new Entry("TestName1", "123", () -> {});
        Entry entry2 = new Entry("TestName2", "456", () -> {});
        assertFalse(entry1.equals(entry2));
    }

    @Test
    void equalsReturnsFalseForNullObject() {
        Entry entry = new Entry("TestName", "123", () -> {});
        assertFalse(entry.equals(null));
    }

    @Test
    void equalsReturnsFalseForDifferentObjectType() {
        Entry entry = new Entry("TestName", "123", () -> {});
        assertFalse(entry.equals("NotAnEntry"));
    }
}