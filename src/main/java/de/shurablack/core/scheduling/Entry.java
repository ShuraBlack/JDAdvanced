package de.shurablack.core.scheduling;

/**
 * <p>
 * A cron task registered in the {@link Dispatcher}: its name, the ID assigned by the cron4j scheduler
 * and the task itself.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public class Entry {

    /** The name given when scheduling the task */
    private final String name;

    /** The ID returned by the cron4j scheduler, used to deschedule the task */
    private final String id;

    /** The scheduled task */
    private final Runnable task;

    /**
     * Creates a new entry.
     *
     * @param name the name given when scheduling the task
     * @param id   the ID returned by the cron4j scheduler
     * @param task the scheduled task
     */
    protected Entry(final String name, final String id, final Runnable task) {
        this.name = name;
        this.id = id;
        this.task = task;
    }

    /** @return the name given when scheduling the task */
    public String getName() {
        return name;
    }

    /** @return the ID assigned by the cron4j scheduler */
    public String getId() {
        return id;
    }

    /** @return the scheduled task */
    public Runnable getTask() {
        return task;
    }

    /**
     * Compares this entry to another object for equality.
     * Two entries are considered equal if their IDs are the same.
     *
     * @param obj the object to compare with
     * @return true if the other object is an Entry with the same ID, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entry && ((Entry) obj).id.equals(id);
    }
}