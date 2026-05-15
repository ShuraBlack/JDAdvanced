package de.shurablack.core.event;

import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;

import java.util.List;

/**
 * <p>
 * The Event class is a Java class that represents a user-defined event, with a worker
 * that does something when the event is triggered, and optional cooldown and channel restrictions.
 * <br><br>
 * This class will be create when you call the {@link de.shurablack.core.event.handler.EventHandler#registerEvent(InteractionSet...)} with valid
 * {@link InteractionSet}
 * </p>
 *
 * @see InteractionSet
 * @see Interaction Interaction
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class Event {

    /** An instance of the {@link EventWorker} interface that defines the action to take when the event is triggered */
    private final EventWorker worker;

    /** Global cooldown time for the event in nanoseconds */
    private final long globalCooldown;

    /** User cooldown time for the event in nanoseconds */
    private final long userCooldown;

    /** List of channelIDs which restrict where the event cann be triggered */
    private final List<String> channelRestriction;

    /** If true, the event will bypass guild validity checks (only for GUILD events) */
    private final boolean bypassGuildValidity;

    /**
     * This is the constructor for the Event class
     * @param worker the instance of an {@link EventWorker}
     * @param globalCooldown the global cooldown of the event
     * @param userCooldown the user cooldwn of the event
     * @param channelRestriction the channel restriction of the event
     */
    public Event(final EventWorker worker, final long globalCooldown
            , final long userCooldown, final List<String> channelRestriction, final boolean bypassGuildValidity) {
        this.worker = worker;
        this.globalCooldown = globalCooldown;
        this.userCooldown = userCooldown;
        this.channelRestriction = channelRestriction;
        this.bypassGuildValidity = bypassGuildValidity;
    }

    /**
     * @return the EventWorker instance associated with this event
     */
    public EventWorker getWorker() {
        return worker;
    }

    /**
     * @return the global cooldown time for the event in seconds
     */
    public long getGlobalCooldown() {
        return globalCooldown;
    }

    /**
     * @return the user-specific cooldown time for the event in seconds
     */
    public long getUserCooldown() {
        return userCooldown;
    }

    /**
     * @return true if the event should bypass guild validity checks, false otherwise
     */
    public boolean isBypassGuildValidity() {
        return bypassGuildValidity;
    }

    /**
     * Checks if the channelID is included in the restriction
     * @param id the specified channelID
     * @return true if the event has no restriction or if it is contained
     */
    public boolean isAllowedChannel(final String id) {
        return channelRestriction.isEmpty() || channelRestriction.contains(id);
    }
}
