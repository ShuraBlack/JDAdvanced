package de.shurablack.core.scheduling;

/**
 * Represents a scheduling entry with a name, an ID, and an associated task.
 * This class is used to encapsulate information about a scheduled task.
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public class Entry {

    /**
     * The name of the scheduling entry.
     */
    private final String name;

    /**
     * The unique identifier of the scheduling entry.
     */
    private final String id;

    /**
     * The task associated with the scheduling entry.
     */
    private final Runnable task;

    /**
     * Constructs a new Entry with the specified name, ID, and task.
     *
     * @param name The name of the scheduling entry.
     * @param id   The unique identifier of the scheduling entry.
     * @param task The task associated with the scheduling entry.
     */
    protected Entry(final String name, final String id, final Runnable task) {
        this.name = name;
        this.id = id;
        this.task = task;
    }

    /**
     * Retrieves the name of the scheduling entry.
     *
     * @return The name of the entry.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the unique identifier of the scheduling entry.
     *
     * @return The ID of the entry.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the task associated with the scheduling entry.
     *
     * @return The associated task.
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * Compares this entry to another object for equality.
     * Two entries are considered equal if their IDs are the same.
     *
     * @param obj The object to compare with.
     * @return True if the other object is an Entry with the same ID, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entry && ((Entry) obj).id.equals(id);
    }
}