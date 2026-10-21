package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.handler.EventHandler;

import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.List;

/**
 * <p>
 * A single registered interaction of an {@link EventWorker}: type, identifier, cooldowns, channel restriction
 * and required permissions.
 * <br><br>
 * Interactions are only created internally by {@link InteractionSet#fromAnnotation()} from the annotations
 * ({@link de.shurablack.core.event.annotation.EventProcess EventProcess},
 * {@link de.shurablack.core.event.annotation.RedirectedProcess RedirectedProcess},
 * {@link de.shurablack.core.event.annotation.command.CommandHandler CommandHandler}), so the factory and setters are package-private.
 * Users only read them.
 * </p>
 *
 * @see InteractionSet InteractionSet
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class Interaction {

    /** Unused, the class isn't {@link java.io.Serializable} */
    private static final long serialVersionUID = 6183499542633126705L;

    /** Cooldown value for a disabled cooldown, the lowest accepted value */
    public static final int NO_COOLDOWN = -1;

    /** {@link Type} of the Event */
    private final Type type;

    /**
     * Identifies the interaction within its {@link Type}. For {@link Type#PRIVATE_CHANNEL} and {@link Type#PUBLIC_CHANNEL}
     * it starts with the {@link EventHandler#getPREFIX() prefix} that was set on creation
     */
    private final String identifier;

    /**
     * If true, guild events skip the admin bypass and the channel restriction (cooldowns and permissions still apply)
     */
    private boolean bypassGuildValidity = false;

    /**
     * List of channel IDs where the interaction is allowed to be used.
     * If the list is empty, the interaction can be used in any channel.
     */
    private final List<String> channelRestriction = new ArrayList<>();

    /** Permissions a member needs to trigger the interaction */
    private final Set<Permission> requiredPermissions = EnumSet.noneOf(Permission.class);

    /**
     * The global cooldown for the interaction.
     * <br><br>
     * It specifies the minimum time interval between uses of the interaction, in milliseconds. A value of 0 or less disables it.
     */
    private long globalCooldown;

    /**
     * The user cooldown for the interaction.
     * <br><br>
     * It specifies the minimum time interval between uses of the interaction by a single user, in milliseconds.
     * A value of 0 or less disables it.
     */
    private long userCooldown;

    /**
     * Creates an Interaction without cooldowns, channel restriction or permissions
     * @param type the type of the event
     * @param identifier the unique string within the type, without the prefix
     * @return an Interaction for chaining
     */
    static Interaction create(final Type type, final String identifier) {
        return new Interaction(type, identifier, false);
    }

    /**
     * Creates an Interaction without cooldowns, channel restriction or permissions
     * @param type the type of the event
     * @param identifier the unique string within the type, without the prefix
     * @param bypassGuildValidity if true, guild events skip the admin bypass and the channel restriction
     * @return an Interaction for chaining
     */
    static Interaction create(final Type type, final String identifier, final boolean bypassGuildValidity) {
        return new Interaction(type, identifier, bypassGuildValidity);
    }

    /**
     * Creates an Interaction without cooldowns, channel restriction or permissions
     * @param type the type of the event
     * @param identifier the unique string within the type, the prefix gets added for message types
     * @param bypassGuildValidity if true, guild events skip the admin bypass and the channel restriction
     */
    Interaction(final Type type, final String identifier, final boolean bypassGuildValidity) {
        this.type = type;
        this.identifier = addPrefix(identifier);
        this.bypassGuildValidity = bypassGuildValidity;
        this.globalCooldown = -1L;
        this.userCooldown = -1L;
    }

    /**
     * Sets the global cooldown, shared by all users.
     * The value is given in seconds and stored in milliseconds, 0 and {@link #NO_COOLDOWN} disable it
     * @param globalCooldownSec the cooldown in seconds
     * @return the Interaction for chaining
     * @throws IllegalArgumentException if the value is less than {@link #NO_COOLDOWN}
     */
    Interaction setGlobalCD(final long globalCooldownSec) {
        if (globalCooldownSec < NO_COOLDOWN) {
            throw new IllegalArgumentException("Cooldown cant be lower than -1");
        }
        this.globalCooldown = globalCooldownSec;
        if (this.globalCooldown > NO_COOLDOWN) {
            this.globalCooldown *= 1_000;
        }
        return this;
    }

    /**
     * Sets the cooldown per user.
     * The value is given in seconds and stored in milliseconds, 0 and {@link #NO_COOLDOWN} disable it
     * @param userCooldownSec the cooldown in seconds
     * @return the Interaction for chaining
     * @throws IllegalArgumentException if the value is less than {@link #NO_COOLDOWN}
     */
    Interaction setUserCD(final long userCooldownSec) {
        if (userCooldownSec < NO_COOLDOWN) {
            throw new IllegalArgumentException("Cooldown cant be lower than -1");
        }
        this.userCooldown = userCooldownSec;
        if (this.userCooldown > NO_COOLDOWN) {
            this.userCooldown *= 1_000;
        }
        return this;
    }

    /**
     * Adds channels to the channel restriction. Without any channel, the interaction can be triggered anywhere
     * @param channelIDs the IDs of the allowed channels
     * @return the Interaction for chaining
     */
    Interaction setChannelRestriction(final List<String> channelIDs) {
        this.channelRestriction.addAll(channelIDs);
        return this;
    }

    /**
     * Adds permissions a member needs to trigger the interaction. Checked in the channel of the
     * interaction, so channel overrides count. Without a member (DMs, webhooks) the interaction gets denied
     * @param permissions the required permissions
     * @return the Interaction for chaining
     */
    Interaction setRequiredPermissions(final Permission... permissions) {
        this.requiredPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * @return the permissions a member needs to trigger the interaction
     */
    public Set<Permission> getRequiredPermissions() {
        return requiredPermissions;
    }

    /**
     * Adds the current {@link EventHandler#getPREFIX() prefix} to the identifier of message types
     * ({@link Type#PRIVATE_CHANNEL}, {@link Type#PUBLIC_CHANNEL})
     * @param identifier the unique string
     * @return the identifier, with the prefix for message types
     */
    private String addPrefix(final String identifier) {
        if (this.type.equals(Type.PRIVATE_CHANNEL) || this.type.equals(Type.PUBLIC_CHANNEL)) {
            return EventHandler.getPREFIX() + identifier;
        }
        return identifier;
    }

    /**
     * @return true, if guild events skip the admin bypass and the channel restriction
     */
    public boolean isBypassGuildValidity() {
        return bypassGuildValidity;
    }

    /**
     * @return the type of the interaction
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the identifier of the interaction, including the prefix for message types
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the allowed channel IDs, empty if the interaction isn't restricted (mutable internal list)
     */
    public List<String> getChannelRestriction() {
        return channelRestriction;
    }

    /**
     * @return the global cooldown in milliseconds, 0 or less if disabled
     */
    public long getGlobalCooldown() {
        return globalCooldown;
    }

    /**
     * @return the user cooldown in milliseconds, 0 or less if disabled
     */
    public long getUserCooldown() {
        return userCooldown;
    }
}
