package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventHandler;
import de.shurablack.core.event.EventWorker;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The Interaction class is a utility class that allows you to define interactions for a given {@link EventWorker}.
 * <br><br>
 * It provides methods for setting the type, identifier, global cooldown, user cooldown, and channel restriction for the interaction.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * public class Worker extends EventWorker {
 *     @Override
 *     public void processButtonEvent(...) { }
 *     @Override
 *     public void processModalEvent(...) { }
 * }
 *
 * InteractionSet.create().addInteraction(
 *      new Worker(),
 *      Interaction.create(Type.BUTTON, "identifier"),
 *      Interaction.create(Type.MODAL, "identifier")
 * )
 * }</pre>
 * </p>
 *
 * @see InteractionSet InteractionSet
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class Interaction {

    private static final long serialVersionUID = 6183499542633126705L;

    /** Static constant for a disabled cooldown */
    public static final int NO_COOLDOWN = -1;

    /** {@link Type} of the Event */
    private final Type type;

    /** Identify the interaction and is prefixed with the type of the interaction */
    private final String identifier;

    /** List of channelIDs where the interaction is allowed to be used.
     * If the list is empty, the interaction can be used in any channel
     */
    private final List<String> channelRestriction = new ArrayList<>();

    /**
     * The global cooldown for the interaction.
     * <br><br>
     * It specifies the minimum time interval between uses of the interaction, in nanoseconds.
     */
    private long globalCooldown;

    /**
     * The user cooldown for the interaction.
     * <br><br>
     * It specifies the minimum time interval between uses of the interaction by a single user, in nanoseconds.
     */
    private long userCooldown;

    /**
     * This is a static factory method that creates a new Interaction object with the specified type and identifier
     * @param type the specified type of the event
     * @param identifier the specified unique string
     * @return an Interaction for chaining
     */
    public static Interaction create(final Type type, final String identifier) {
        return new Interaction(type, identifier);
    }

    /**
     * This is the constructor for the Interaction class.
     * @param type  the specified type of the event
     * @param identifier the specified unique string
     */
    Interaction(final Type type, final String identifier) {
        this.type = type;
        this.identifier = addPrefix(identifier);
        this.globalCooldown = -1L;
        this.userCooldown = -1L;
    }

    /**
     * This method sets the global cooldown for the interaction.
     * <br><br>
     * It takes a value in seconds as an argument and converts it to nanoseconds before storing it
     * <br>
     * This method throws an {@link IllegalArgumentException} if given value is less than {@link Interaction#NO_COOLDOWN}
     * @param global_cooldown_sec the specified time in seconds
     * @return the Interaction for chaining
     */
    public Interaction setGlobalCD(final long global_cooldown_sec) {
        if (global_cooldown_sec < NO_COOLDOWN) {
            throw new IllegalArgumentException("Cooldown cant be lower than -1");
        }
        this.globalCooldown = global_cooldown_sec;
        return this;
    }

    /**
     * This method sets the global cooldown for the interaction.
     * <br><br>
     * It takes a value in seconds as an argument and converts it to nanoseconds before storing it.
     * <br>
     * This method throws an {@link IllegalArgumentException} if given value is less than {@link Interaction#NO_COOLDOWN}
     * @param user_cooldown_sec the specified time in seconds
     * @return the Interaction for chaining
     */
    public Interaction setUserCD(final long user_cooldown_sec) {
        if (user_cooldown_sec < NO_COOLDOWN) {
            throw new IllegalArgumentException("Cooldown cant be lower than -1");
        }
        this.userCooldown = user_cooldown_sec;
        return this;
    }

    /**
     * This method sets the channel restriction of the Interaction.
     * <br><br>
     * If this method doest get called, it can be triggered from anywhere
     * @param channelRestriction the specified list of channelIDs
     * @return
     */
    public Interaction setChannelRestriction(final List<String> channelRestriction) {
        this.channelRestriction.addAll(channelRestriction);
        return this;
    }

    /**
     * Add the {@link EventHandler#PREFIX} to any standard message identifier
     * @param identifier the specified unique string
     * @return the unique string, starting with the prefix
     */
    private String addPrefix(final String identifier) {
        if (this.type.equals(Type.PRIVATE_CHANNEL) || this.type.equals(Type.PUBLIC_CHANNEL)) {
            return EventHandler.PREFIX + identifier;
        }
        return identifier;
    }

    /**
     * @return the type of the interaction
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the identifier of the interaction
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the channel restriction list of the interaction
     */
    public List<String> getChannelRestriction() {
        return channelRestriction;
    }

    /**
     * @return the global cooldown of the interaction
     */
    public long getGlobalCooldown() {
        return globalCooldown;
    }

    /**
     * @return the user cooldown of the interaction
     */
    public long getUserCooldown() {
        return userCooldown;
    }
}
