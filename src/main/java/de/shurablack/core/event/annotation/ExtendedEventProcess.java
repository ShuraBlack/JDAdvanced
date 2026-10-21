package de.shurablack.core.event.annotation;

import de.shurablack.core.event.interaction.Type;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares a single interaction inside of a {@link RedirectedProcess} annotation.
 * <br><br>
 * Like {@link EventProcess}, but the {@link Type} is set explicitly instead of being derived from a method name.
 * The worker method matching the type gets called. It can't be placed on any element directly and
 * {@link BypassGuildValidity} isn't supported.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ExtendedEventProcess {

    /**
     * @return the identifier the interaction gets registered with, see {@link EventProcess#identifier()}
     */
    String identifier();

    /**
     * @return the type of the interaction, decides which process method of the worker gets called
     */
    Type type();

    /**
     * @return the cooldown per user in seconds, -1 (default) for none. Values below -1 throw an
     *         {@link IllegalArgumentException} on discovery
     */
    int userCooldown() default -1;

    /**
     * @return the cooldown for everyone in seconds, -1 (default) for none. Values below -1 throw an
     *         {@link IllegalArgumentException} on discovery
     */
    int globalCooldown() default -1;

    /**
     * @return the channel IDs the event is restricted to, empty (default) for every channel,
     *         see {@link EventProcess#restrictedChannel()}
     */
    String[] restrictedChannel() default {};

    /**
     * Permissions the member needs in the channel of the event (channel overrides count). Events without a
     * guild member (e.g. DMs) get denied if any permission is required. Not affected by the bypasses.
     * @return the required permissions, empty (default) for none
     */
    Permission[] requiredPermissions() default {};
}
