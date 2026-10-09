package de.shurablack.core.event.annotation;

import de.shurablack.core.event.interaction.Type;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.*;

/**
 * <p>
 * Declares an interaction on an overridden process method of an {@link de.shurablack.core.event.EventWorker EventWorker}.
 * The worker gets discovered by {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()}.
 * <br><br>
 * The {@link Type} is derived from the method name (e.g. <code>processButtonEvent</code> to {@link Type#BUTTON}),
 * methods with another name get skipped with a warning. Only methods declared in the worker class itself are scanned,
 * and the annotation is ignored if the class is annotated with {@link RedirectedProcess}.
 * </p>
 * <pre>{@code
 * public class Worker extends EventWorker {
 *      @Override
 *      @EventProcess(identifier = "confirm", userCooldown = 10)
 *      public void processButtonEvent(Member member, MessageChannelUnion channel, String compID, ButtonInteractionEvent event) {
 *          event.reply("Confirmed").queue();
 *      }
 * }
 * }</pre>
 *
 * @see BypassGuildValidity
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventProcess {

    /**
     * The identifier depends on the type: the custom ID of buttons, modals and select menus, the command name of
     * slash, context and auto complete events, the identifier mapped to a reacted message in the
     * {@link de.shurablack.listener.DefaultReactionReceiver#getMap() DefaultReactionReceiver}, or the first word of a
     * message without the {@link de.shurablack.core.event.handler.EventHandler#getPREFIX() prefix} (it gets added automatically).
     * @return the identifier the interaction gets registered with
     */
    String identifier();

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
     * Channel IDs the event is restricted to, empty (default) for every channel. Intended for message events,
     * but checked for every type. Administrators skip it if the admin bypass of the handler is enabled.
     * @return the channel IDs the event is restricted to
     */
    String[] restrictedChannel() default {};

    /**
     * Permissions the member needs in the channel of the event (channel overrides count). Events without a
     * guild member (e.g. DMs) get denied if any permission is required. Not affected by the bypasses.
     * @return the required permissions, empty (default) for none
     */
    Permission[] requiredPermissions() default {};
}
