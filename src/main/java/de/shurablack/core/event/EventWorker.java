package de.shurablack.core.event;


import de.shurablack.core.event.interaction.InteractionSet;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * <p>
 * Base class of every event processing class. It can't be instantiated directly and has to be extended.
 * <br><br>
 * Override the process methods of the events you want to handle and declare the interactions via annotations:
 * {@link de.shurablack.core.event.annotation.EventProcess @EventProcess} on the overridden methods or
 * {@link de.shurablack.core.event.annotation.RedirectedProcess @RedirectedProcess} on the class.
 * {@link InteractionSet#fromAnnotation()} discovers the worker, its result gets registered with
 * {@link de.shurablack.core.event.handler.EventHandler#registerEvent(java.util.List)}.
 * Slash commands can also be handled without a worker, see
 * {@link de.shurablack.core.event.annotation.command.CommandHandler @CommandHandler}.
 * <br><br>
 * Every process method throws an {@link ImplementationException} if it gets called, but isn't overridden
 * or still calls the <b>super</b> method.
 * </p>
 * <pre>{@code
 * public class Worker extends EventWorker {
 *      @Override
 *      @EventProcess(identifier = "confirm")
 *      public void processButtonEvent(Member member, MessageChannelUnion channel, String compID, ButtonInteractionEvent event) {
 *          event.reply("Confirmed").queue();
 *      }
 * }
 *
 * handler.registerEvent(InteractionSet.fromAnnotation());
 * }</pre>
 *
 * @version core-1.0.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public abstract class EventWorker {

    /** Message of the {@link ImplementationException} thrown by the default implementations */
    private static final String MSG = "Function isn't implemented or still use super function call";

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link ButtonInteractionEvent}.
     * <br><br>
     * The identifier is the custom ID of the button.
     * </p>
     * @param member the {@link Member} who clicked the button, null outside of guilds
     * @param channel the channel where the {@link ButtonInteractionEvent event} got fired
     * @param compID the custom ID of the {@link net.dv8tion.jda.api.components.buttons.Button Button}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processButtonEvent(final Member member, final MessageChannelUnion channel, final String compID, final ButtonInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link SlashCommandInteractionEvent}
     * outside of a guild.
     * <br><br>
     * The identifier is the command name, or the full command name (e.g. "music play") for
     * {@link de.shurablack.core.event.annotation.command.CommandHandler @CommandHandler} registrations.
     * </p>
     * @param user the {@link User} who used the command
     * @param channel the private channel where the command got used
     * @param name the command name of {@link SlashCommandInteractionEvent#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalSlashEvent(final User user, final PrivateChannel channel, final String name, final SlashCommandInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link SlashCommandInteractionEvent}
     * inside of a guild.
     * <br><br>
     * The identifier is the command name, or the full command name (e.g. "music play") for
     * {@link de.shurablack.core.event.annotation.command.CommandHandler @CommandHandler} registrations.
     * </p>
     * @param member the {@link Member} who used the command
     * @param channel the channel where the {@link SlashCommandInteractionEvent event} got fired
     * @param name the sub command name of {@link SlashCommandInteractionEvent#getSubcommandName()}, null if the command has no sub command
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildSlashEvent(final Member member, final MessageChannelUnion channel, final String name, final SlashCommandInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link ModalInteractionEvent}.
     * <br><br>
     * The identifier is the custom ID of the modal.
     * </p>
     * @param member the {@link Member} who submitted the modal, null outside of guilds
     * @param channel the channel where the {@link ModalInteractionEvent event} got fired
     * @param compID the custom ID of the modal, see {@link ModalInteractionEvent#getModalId()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processModalEvent(final Member member, final MessageChannelUnion channel, final String compID, final ModalInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageReceivedEvent}
     * in a private channel.
     * <br><br>
     * The identifier is the first word of the message, starting with the
     * {@link de.shurablack.core.event.handler.EventHandler#getPREFIX() prefix}.
     * </p>
     * @param user the {@link User} who sent the message
     * @param channel the private channel where the {@link MessageReceivedEvent event} got fired
     * @param message the raw content of {@link Message#getContentRaw()} with the identifier removed
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPrivateChannelEvent(final User user, final PrivateChannel channel, final String message, final MessageReceivedEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageReactionAddEvent}
     * in a private channel.
     * <br><br>
     * The identifier is the one stored for the reacted message.
     * </p>
     * @param user the {@link User} who added the reaction
     * @param channel the private channel where the {@link MessageReactionAddEvent event} got fired
     * @param name the unicode or custom name of the emoji, see {@link Emoji#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPrivateReactionEvent (final User user, final PrivateChannel channel, final String name, final MessageReactionAddEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageReceivedEvent}
     * in a guild.
     * <br><br>
     * The identifier is the first word of the message, starting with the
     * {@link de.shurablack.core.event.handler.EventHandler#getPREFIX() prefix}.
     * </p>
     * @param member the {@link Member} who sent the message, null for webhook messages
     * @param channel the channel where the {@link MessageReceivedEvent event} got fired
     * @param message the raw content of {@link Message#getContentRaw()} with the identifier removed
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPublicChannelEvent(final Member member, final MessageChannelUnion channel, final String message, final MessageReceivedEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageReactionAddEvent}
     * in a guild.
     * <br><br>
     * The identifier is the one stored for the reacted message.
     * </p>
     * @param member the {@link Member} who added the reaction
     * @param channel the channel where the {@link MessageReactionAddEvent event} got fired
     * @param name the unicode or custom name of the emoji, see {@link Emoji#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPublicReactionEvent (final Member member, final MessageChannelUnion channel, final String name, final MessageReactionAddEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link StringSelectInteractionEvent}.
     * <br><br>
     * The identifier is the custom ID of the select menu.
     * </p>
     * @param member the {@link Member} who used the select menu, null outside of guilds
     * @param channel the channel where the {@link StringSelectInteractionEvent event} got fired
     * @param compID the custom ID of the {@link net.dv8tion.jda.api.components.selections.StringSelectMenu StringSelectMenu}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processStringSelectEvent(final Member member, final MessageChannelUnion channel, final String compID, final StringSelectInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires an {@link EntitySelectInteractionEvent}.
     * <br><br>
     * The identifier is the custom ID of the select menu.
     * </p>
     * @param member the {@link Member} who used the select menu, null outside of guilds
     * @param channel the channel where the {@link EntitySelectInteractionEvent event} got fired
     * @param compID the custom ID of the {@link net.dv8tion.jda.api.components.selections.EntitySelectMenu EntitySelectMenu}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processEntitySelectEvent(final Member member, final MessageChannelUnion channel, final String compID, final EntitySelectInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link UserContextInteractionEvent}
     * outside of a guild.
     * <br><br>
     * The identifier is the name of the context command.
     * </p>
     * @param source the {@link User} who used the context command
     * @param target the {@link User} the context command got used on
     * @param compID the name of the context command, see {@link UserContextInteractionEvent#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalUserContextEvent(final User source, final User target, final String compID, final UserContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link UserContextInteractionEvent}
     * inside of a guild.
     * <br><br>
     * The identifier is the name of the context command.
     * </p>
     * @param source the {@link Member} who used the context command
     * @param target the {@link Member} the context command got used on, null if the target isn't a member of the guild
     * @param compID the name of the context command, see {@link UserContextInteractionEvent#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildUserContextEvent(final Member source, final Member target, final String compID, final UserContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageContextInteractionEvent}
     * outside of a guild.
     * <br><br>
     * The identifier is the name of the context command.
     * </p>
     * @param user the {@link User} who used the context command
     * @param channel the channel where the {@link MessageContextInteractionEvent event} got fired
     * @param compID the name of the context command, see {@link MessageContextInteractionEvent#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalMessageContextEvent(final User user, final MessageChannelUnion channel, final String compID, final MessageContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link MessageContextInteractionEvent}
     * inside of a guild.
     * <br><br>
     * The identifier is the name of the context command.
     * </p>
     * @param member the {@link Member} who used the context command
     * @param channel the channel where the {@link MessageContextInteractionEvent event} got fired
     * @param compID the name of the context command, see {@link MessageContextInteractionEvent#getName()}
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildMessageContextEvent(final Member member, final MessageChannelUnion channel, final String compID, final MessageContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Gets called by the {@link de.shurablack.core.event.handler.EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} fires a {@link CommandAutoCompleteInteractionEvent}
     * while a user types into an option with enabled auto complete. The identifier is the command name (without sub command).
     * <br><br>
     * Reply with {@link CommandAutoCompleteInteractionEvent#replyChoices} (max. 25 choices) within 3 seconds.
     * There are no cooldowns, only the channel restriction applies. The choices are only suggestions,
     * validate the submitted value in the slash command itself.
     * </p>
     * @param user the {@link User} who types into the option
     * @param channel the channel where the {@link CommandAutoCompleteInteractionEvent event} got fired
     * @param option the name of the focused option
     * @param event the original event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processAutoCompleteEvent(final User user, final MessageChannelUnion channel, final String option, final CommandAutoCompleteInteractionEvent event) {
        throw new ImplementationException(MSG);
    }
}
