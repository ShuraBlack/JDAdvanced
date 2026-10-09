package de.shurablack.core.event;

import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;

import net.dv8tion.jda.api.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A registered event: the {@link EventWorker} that processes it together with its cooldowns,
 * channel restriction, guild validity bypass and required permissions.
 * <br><br>
 * The event handler creates one Event per {@link Interaction} when
 * {@link de.shurablack.core.event.handler.EventHandler#registerEvent(java.util.List)} gets called with the
 * {@link InteractionSet InteractionSets} of {@link InteractionSet#fromAnnotation()}.
 * </p>
 *
 * @see InteractionSet
 * @see Interaction Interaction
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class Event {

    /** The {@link EventWorker} that processes the event */
    private final EventWorker worker;

    /** Global cooldown of the event in milliseconds, -1 if there is none */
    private final long globalCooldown;

    /** Cooldown per user of the event in milliseconds, -1 if there is none */
    private final long userCooldown;

    /** Channel IDs the event is restricted to, empty if it can be triggered in every channel */
    private final List<String> channelRestriction;

    /** If true, the channel restriction and the admin bypass are skipped for events with a guild member */
    private final boolean bypassGuildValidity;

    /** Permissions a member needs to trigger the event */
    private final Set<Permission> requiredPermissions;

    /**
     * Creates an event without required permissions.
     * @param worker the {@link EventWorker} that processes the event
     * @param globalCooldown the global cooldown in milliseconds, -1 for none
     * @param userCooldown the cooldown per user in milliseconds, -1 for none
     * @param channelRestriction the channel IDs the event is restricted to, empty for every channel
     * @param bypassGuildValidity whether the channel restriction and admin bypass are skipped
     */
    public Event(final EventWorker worker, final long globalCooldown
            , final long userCooldown, final List<String> channelRestriction, final boolean bypassGuildValidity) {
        this(worker, globalCooldown, userCooldown, channelRestriction, bypassGuildValidity, Collections.emptySet());
    }

    /**
     * Creates an event.
     * @param worker the {@link EventWorker} that processes the event
     * @param globalCooldown the global cooldown in milliseconds, -1 for none
     * @param userCooldown the cooldown per user in milliseconds, -1 for none
     * @param channelRestriction the channel IDs the event is restricted to, empty for every channel
     * @param bypassGuildValidity whether the channel restriction and admin bypass are skipped
     * @param requiredPermissions the permissions a member needs in the channel of the event, empty for none
     */
    public Event(final EventWorker worker, final long globalCooldown, final long userCooldown, final List<String> channelRestriction,
                 final boolean bypassGuildValidity, final Set<Permission> requiredPermissions) {
        this.worker = worker;
        this.globalCooldown = globalCooldown;
        this.userCooldown = userCooldown;
        this.channelRestriction = channelRestriction;
        this.bypassGuildValidity = bypassGuildValidity;
        this.requiredPermissions = requiredPermissions;
    }

    /**
     * @return the {@link EventWorker} that processes the event
     */
    public EventWorker getWorker() {
        return worker;
    }

    /**
     * @return the global cooldown of the event in milliseconds, -1 if there is none
     */
    public long getGlobalCooldown() {
        return globalCooldown;
    }

    /**
     * @return the cooldown per user of the event in milliseconds, -1 if there is none
     */
    public long getUserCooldown() {
        return userCooldown;
    }

    /**
     * @return true if the channel restriction and the admin bypass are skipped, false otherwise
     */
    public boolean isBypassGuildValidity() {
        return bypassGuildValidity;
    }

    /**
     * @return the permissions a member needs to trigger the event, empty if there are none
     */
    public Set<Permission> getRequiredPermissions() {
        return requiredPermissions;
    }

    /**
     * Checks if the event may be triggered in the given channel.
     * @param id the channel ID
     * @return true if the event has no channel restriction or the channel is part of it
     */
    public boolean isAllowedChannel(final String id) {
        return channelRestriction.isEmpty() || channelRestriction.contains(id);
    }
}
