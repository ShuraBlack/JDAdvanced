package de.shurablack.core.event;

import de.shurablack.core.event.interaction.InteractionSet;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
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
 * {@link EventWorker} cant be instantiated directly and should be extended
 * by an event proccessing class.
 * <br><br>
 * Override the desired event process function and register your worker with the {@link EventHandler#registerEvent(InteractionSet...)}.
 * <br><br>
 * The class will throw a {@link ImplementationException} if the method gets called, but isnt implemented probably
 * or stil calls the <b>super()</b> function.
 * </p>
 * <pre>{@code
 * public Worker extends EventWorker {
 *      @Override
 *      public void processButtonEvent(...) {
 *          // Your processing
 *      }
 * }
 * }</pre>
 *
 * @version core-1.0.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public abstract class EventWorker {
    
    private static final String MSG = "Function isnt implemented or stil use super function call";

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link ButtonInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link ButtonInteractionEvent Event} got called
     * @param compID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.components.buttons.Button Button} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processButtonEvent(final Member member, final MessageChannelUnion channel, final String compID, final ButtonInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link SlashCommandInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param user is a independent discord user object
     * @param channel is the private channel of the given user
     * @param name is the command name of {@link SlashCommandInteractionEvent#getName()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalSlashEvent(final User user, final PrivateChannel channel, final String name, final SlashCommandInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link SlashCommandInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link SlashCommandInteractionEvent Event} got called
     * @param name is the command name of {@link SlashCommandInteractionEvent#getName()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildSlashEvent(final Member member, final MessageChannelUnion channel, final String name, final SlashCommandInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link ModalInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link SlashCommandInteractionEvent Event} got called
     * @param compID is the custom ID you set on an {@link ModalInteractionEvent} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processModalEvent(final Member member, final MessageChannelUnion channel, final String compID, final ModalInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageReceivedEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param user is a independent discord user object
     * @param channel is the private channel of the given user where the {@link MessageReceivedEvent Event} got called
     * @param message is the raw content given by {@link Message#getContentRaw()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPrivateChannelEvent(final User user, final PrivateChannel channel, final String message, final MessageReceivedEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageReactionAddEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param user is a independent discord user object
     * @param channel is the private channel of the given user where the {@link MessageReactionAddEvent Event} got called
     * @param name is the unicode or custom name of the {@link Emoji#getName()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPrivateReactionEvent (final User user, final PrivateChannel channel, final String name, final MessageReactionAddEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageReceivedEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link MessageReceivedEvent Event} got called
     * @param message is the raw content given by {@link Message#getContentRaw()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPublicChannelEvent(final Member member, final MessageChannelUnion channel, final String message, final MessageReceivedEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageReactionAddEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link MessageReceivedEvent Event} got called
     * @param name is the unicode or custom name of the {@link Emoji#getName()}
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processPublicReactionEvent (final Member member, final MessageChannelUnion channel, final String name, final MessageReactionAddEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link StringSelectInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param member is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction Event} got called
     * @param compID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.components.selections.SelectMenu SelectMenu} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processStringSelectEvent(final Member member, final MessageChannelUnion channel, final String compID, final StringSelectInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    public void processEntitySelectEvent(final Member member, final MessageChannelUnion channel, final String compID, final EntitySelectInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link UserContextInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param source is a independent discord user object, which called the context
     * @param target is a independent discord user object, which is the target of the call
     * @param textID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction UserContextInteraction} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalUserContextEvent(final User source, final User target, final String textID, final UserContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link UserContextInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param source is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents the calling discord user
     * @param target is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents the targeted discord user
     * @param textID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction UserContextInteraction} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildUserContextEvent(final Member source, final Member target, final String textID, final UserContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageContextInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param user is a independent discord user object, which called the context
     * @param channel is were the {@link MessageContextInteractionEvent Event} got called
     * @param textID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction MessageContextInteraction} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGlobalMessageContextEvent(final User user, final MessageChannelUnion channel, final String textID, final MessageContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }

    /**
     * <p>
     * Get called by the {@link EventHandler} when the {@link net.dv8tion.jda.api.JDA JDA} calls an {@link MessageContextInteractionEvent}
     * <br><br>
     * Override this function if you want an worker for the {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     * </p>
     * @param source is a {@link net.dv8tion.jda.api.entities.Guild Guild} object which represents a discord user
     * @param channel is were the {@link MessageContextInteractionEvent Event} got called
     * @param textID is the custom ID you set on an {@link net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction MessageContextInteraction} object
     * @param event is the original Event from {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void processGuildMessageContextEvent(final Member source, final MessageChannelUnion channel, final String textID, final MessageContextInteractionEvent event) {
        throw new ImplementationException(MSG);
    }
}
