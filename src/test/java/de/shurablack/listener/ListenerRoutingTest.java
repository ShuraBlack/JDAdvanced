package de.shurablack.listener;

import de.shurablack.core.event.handler.EventHandler;
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
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The listeners pass the identifier the handler looks up the registered process with.
 * Components must use their custom ID, the interaction ID changes on every click.
 */
class ListenerRoutingTest {

    private final EventHandler handler = mock(EventHandler.class);

    @Test
    void componentsUseTheirCustomId() {
        final DefaultInteractionReceiver receiver = new DefaultInteractionReceiver(handler);

        final ButtonInteractionEvent button = mock(ButtonInteractionEvent.class);
        when(button.getComponentId()).thenReturn("confirm");
        when(button.getId()).thenReturn("123456789");
        receiver.onButtonInteraction(button);
        verify(handler).onButtonEvent("confirm", button);

        final StringSelectInteractionEvent stringSelect = mock(StringSelectInteractionEvent.class);
        when(stringSelect.getComponentId()).thenReturn("color");
        receiver.onStringSelectInteraction(stringSelect);
        verify(handler).onStringSelectionMenuEvent("color", stringSelect);

        final EntitySelectInteractionEvent entitySelect = mock(EntitySelectInteractionEvent.class);
        when(entitySelect.getComponentId()).thenReturn("member");
        receiver.onEntitySelectInteraction(entitySelect);
        verify(handler).onEntitySelectionMenuEvent("member", entitySelect);

        final ModalInteractionEvent modal = mock(ModalInteractionEvent.class);
        when(modal.getModalId()).thenReturn("feedback");
        receiver.onModalInteraction(modal);
        verify(handler).onModalEvent("feedback", modal);
    }

    @Test
    void slashCommandsAreSplitIntoGuildAndGlobal() {
        final DefaultSlashReceiver receiver = new DefaultSlashReceiver(handler);

        final SlashCommandInteractionEvent guild = mock(SlashCommandInteractionEvent.class);
        when(guild.isFromGuild()).thenReturn(true);
        when(guild.getName()).thenReturn("music");
        receiver.onSlashCommandInteraction(guild);
        verify(handler).onGuildSlashEvent("music", guild);

        final SlashCommandInteractionEvent dm = mock(SlashCommandInteractionEvent.class);
        when(dm.getName()).thenReturn("music");
        receiver.onSlashCommandInteraction(dm);
        verify(handler).onGlobalSlashEvent("music", dm);
        verify(handler, never()).onGlobalSlashEvent("music", guild);
    }

    @Test
    void contextCommandsAreRoutedOnlyOnce() {
        final DefaultSlashReceiver receiver = new DefaultSlashReceiver(handler);

        final MessageContextInteractionEvent message = mock(MessageContextInteractionEvent.class);
        when(message.isFromGuild()).thenReturn(true);
        when(message.getName()).thenReturn("Report");
        receiver.onMessageContextInteraction(message);
        verify(handler).onGuildMessageContextEvent("Report", message);
        verify(handler, never()).onGlobalMessageContextEvent(any(), any());

        final UserContextInteractionEvent user = mock(UserContextInteractionEvent.class);
        when(user.getName()).thenReturn("Profile");
        receiver.onUserContextInteraction(user);
        verify(handler).onGlobalUserContextEvent("Profile", user);
        verify(handler, never()).onGuildUserContextEvent(any(), any());
    }

    @Test
    void autoCompleteUsesTheCommandName() {
        final CommandAutoCompleteInteractionEvent event = mock(CommandAutoCompleteInteractionEvent.class);
        when(event.getName()).thenReturn("play");
        new DefaultSlashReceiver(handler).onCommandAutoCompleteInteraction(event);
        verify(handler).onAutoCompleteEvent("play", event);
    }

    @Test
    void messagesUseTheFirstWordAndCanBeDeleted() {
        final MessageReceivedEvent guild = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        when(guild.isFromGuild()).thenReturn(true);
        when(guild.getMessage().getContentRaw()).thenReturn("!ping some arguments");
        new DefaultMessageReceiver(handler, true).onMessageReceived(guild);
        verify(handler).onPublicChannelEvent("!ping", guild);
        verify(guild.getMessage()).delete();

        final MessageReceivedEvent dm = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        when(dm.getMessage().getContentRaw()).thenReturn("!ping");
        new DefaultMessageReceiver(handler, true).onMessageReceived(dm);
        verify(handler).onPrivateChannelEvent("!ping", dm);
        // Messages in DMs cant be deleted by the bot
        verify(dm.getMessage(), never()).delete();
    }

    @Test
    void reactionsAreRoutedByTheRegisteredMessage() {
        final DefaultReactionReceiver receiver = new DefaultReactionReceiver(handler);
        receiver.getMap().put("channel/message", "vote");

        final MessageReactionAddEvent known = mock(MessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(known.getChannel().getId()).thenReturn("channel");
        when(known.getMessageId()).thenReturn("message");
        when(known.isFromGuild()).thenReturn(true);
        receiver.onMessageReactionAdd(known);
        verify(handler).onPublicReactionEvent("vote", known);

        final MessageReactionAddEvent unknown = mock(MessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(unknown.getChannel().getId()).thenReturn("channel");
        when(unknown.getMessageId()).thenReturn("other");
        receiver.onMessageReactionAdd(unknown);
        verify(handler, never()).onPrivateReactionEvent(any(), any());
        verify(handler, times(1)).onPublicReactionEvent(any(), any());
    }
}
