package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.handler.DispatchEventHandler;
import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Validation of {@link de.shurablack.core.event.handler.AbstractEventHandler}: bots, channel restriction,
 * admin bypass, {@link de.shurablack.core.event.annotation.BypassGuildValidity} and the error feedback
 */
class HandlerValidationTest {

    private final EventWorker worker = mock(EventWorker.class);

    @AfterEach
    void resetPrefix() {
        EventHandler.setPREFIX(EventHandler.DEFAULT_PREFIX);
    }

    private NativeEventHandler handler(boolean adminBypass, boolean errorFeedback, Interaction... interactions) {
        return NativeEventHandler.create("!", true, errorFeedback, adminBypass)
                .registerEvent(List.of(InteractionSet.create(worker, interactions)));
    }

    private NativeEventHandler handler(Interaction... interactions) {
        return handler(false, true, interactions);
    }

    private NativeEventHandler adminBypassHandler(Interaction... interactions) {
        return handler(true, true, interactions);
    }

    private static Member member(boolean bot, boolean admin) {
        final Member member = mock(Member.class, RETURNS_DEEP_STUBS);
        when(member.getUser().isBot()).thenReturn(bot);
        when(member.getId()).thenReturn(bot ? "bot" : "user");
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(admin);
        return member;
    }

    private static ButtonInteractionEvent click(Member member, String channel) {
        final ButtonInteractionEvent event = mock(ButtonInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getMember()).thenReturn(member);
        when(event.getChannel().getId()).thenReturn(channel);
        when(event.getUserLocale()).thenReturn(DiscordLocale.ENGLISH_US);
        return event;
    }

    private void assertWorkerCalled(int times) {
        verify(worker, times(times)).processButtonEvent(any(), any(), any(), any());
    }

    @Test
    void botsAreIgnored() {
        final NativeEventHandler handler = handler(Interaction.create(Type.BUTTON, "confirm"));

        handler.onButtonEvent("confirm", click(member(true, false), "channel"));
        assertWorkerCalled(0);
        handler.onButtonEvent("confirm", click(member(false, false), "channel"));
        assertWorkerCalled(1);
    }

    @Test
    void channelRestrictionAndAdminBypass() {
        final Interaction restricted = Interaction.create(Type.BUTTON, "confirm").setChannelRestriction(List.of("allowed"));

        final NativeEventHandler handler = handler(restricted);
        handler.onButtonEvent("confirm", click(member(false, true), "other"));
        assertWorkerCalled(0);
        handler.onButtonEvent("confirm", click(member(false, false), "allowed"));
        assertWorkerCalled(1);

        // Administrators skip the channel restriction if the bypass is enabled
        final NativeEventHandler bypass = adminBypassHandler(
                Interaction.create(Type.BUTTON, "confirm").setChannelRestriction(List.of("allowed")));
        bypass.onButtonEvent("confirm", click(member(false, true), "other"));
        assertWorkerCalled(2);
        bypass.onButtonEvent("confirm", click(member(false, false), "other"));
        assertWorkerCalled(2);
    }

    @Test
    void bypassGuildValidityIgnoresTheChannelRestriction() {
        final NativeEventHandler handler = handler(
                Interaction.create(Type.BUTTON, "confirm", true).setChannelRestriction(List.of("allowed")));

        handler.onButtonEvent("confirm", click(member(false, false), "other"));
        assertWorkerCalled(1);
    }

    @Test
    void failedValidationRepliesWithAnEphemeralEmbed() {
        final NativeEventHandler handler = handler(Interaction.create(Type.BUTTON, "confirm"));
        final ButtonInteractionEvent unknown = click(member(false, false), "channel");

        handler.onButtonEvent("unknown", unknown);

        verify(unknown).replyEmbeds(any(MessageEmbed.class));
        assertWorkerCalled(0);
    }

    @Test
    void errorFeedbackCanBeDisabled() {
        final NativeEventHandler handler = handler(false, false, Interaction.create(Type.BUTTON, "confirm"));
        final ButtonInteractionEvent unknown = click(member(false, false), "channel");

        handler.onButtonEvent("unknown", unknown);

        verify(unknown, never()).replyEmbeds(any(MessageEmbed.class));
    }

    @Test
    void messageEventsUseThePrefixAndPassTheRemainingText() {
        final NativeEventHandler handler = NativeEventHandler.create("?", true, true, false);
        // The prefix is added on creation of the interaction, so after the handler configured it
        handler.registerEvent(List.of(InteractionSet.create(worker, Interaction.create(Type.PUBLIC_CHANNEL, "ping"))));

        final MessageReceivedEvent event = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        final Member member = member(false, false);
        when(event.getMember()).thenReturn(member);
        // Only the leading identifier is stripped, later occurrences stay in the text
        when(event.getMessage().getContentRaw()).thenReturn("?ping hello ?ping");

        handler.onPublicChannelEvent("?ping", event);

        verify(worker).processPublicChannelEvent(eq(member), any(), eq(" hello ?ping"), eq(event));
    }

    @Test
    void globalSlashCommandsOutsideOfPrivateChannels() {
        final NativeEventHandler handler = handler(Interaction.create(Type.GLOBAL_SLASH, "ping"));

        // Group DMs and user installed commands arent private channels
        final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        final User user = mock(User.class);
        when(event.getUser()).thenReturn(user);
        when(event.getName()).thenReturn("ping");
        when(event.getFullCommandName()).thenReturn("ping");
        when(event.getChannelType()).thenReturn(ChannelType.GROUP);
        when(event.getChannel().asPrivateChannel()).thenThrow(new IllegalStateException("Cannot convert channel of type GROUP"));

        handler.onGlobalSlashEvent("ping", event);

        verify(worker).processGlobalSlashEvent(eq(user), isNull(), eq("ping"), eq(event));
    }

    @Test
    void dispatchHandlerRunsTheWorkerOnThePool() throws InterruptedException {
        Dispatcher.start();
        final CountDownLatch called = new CountDownLatch(1);
        final Thread caller = Thread.currentThread();
        final Thread[] workerThread = new Thread[1];
        doAnswer(invocation -> {
            workerThread[0] = Thread.currentThread();
            called.countDown();
            return null;
        }).when(worker).processButtonEvent(any(), any(), any(), any());

        DispatchEventHandler.createDefault()
                .registerEvent(List.of(InteractionSet.create(worker, Interaction.create(Type.BUTTON, "confirm"))))
                .onButtonEvent("confirm", click(member(false, false), "channel"));

        assertTrue(called.await(5, TimeUnit.SECONDS));
        assertTrue(workerThread[0] != caller, "the worker must not run on the calling (JDA) thread");
    }

    @Test
    void cooldownSecondsAreStoredAsMilliseconds() {
        final Interaction interaction = Interaction.create(Type.BUTTON, "confirm").setUserCD(5).setGlobalCD(2);
        assertEquals(5_000, interaction.getUserCooldown());
        assertEquals(2_000, interaction.getGlobalCooldown());
        assertEquals(Interaction.NO_COOLDOWN, Interaction.create(Type.BUTTON, "x").getUserCooldown());
    }

    @Test
    void cooldownsBelowMinusOneAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Interaction.create(Type.BUTTON, "confirm").setUserCD(-2));
    }
}
