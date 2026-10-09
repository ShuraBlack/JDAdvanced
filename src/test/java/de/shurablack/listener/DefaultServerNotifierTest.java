package de.shurablack.listener;

import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.shurablack.core.util.ServerUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultServerNotifierTest {

    private MockedStatic<ServerUtil> serverUtil;

    @BeforeEach
    void mockWebhook() {
        // No real webhook requests, only capture what would be sent
        serverUtil = mockStatic(ServerUtil.class);
    }

    @AfterEach
    void close() {
        serverUtil.close();
    }

    private static User user() {
        final User user = mock(User.class);
        when(user.getAsMention()).thenReturn("@User");
        when(user.getName()).thenReturn("User");
        when(user.getEffectiveAvatarUrl()).thenReturn("https://example.com/avatar.png");
        return user;
    }

    private String sentDescription() {
        final ArgumentCaptor<WebhookEmbedBuilder> captor = ArgumentCaptor.forClass(WebhookEmbedBuilder.class);
        serverUtil.verify(() -> ServerUtil.sendWebHookMessage(eq("hook"), captor.capture()));
        return captor.getValue().build().getDescription();
    }

    private static GuildMemberRemoveEvent leave() {
        final User user = user();
        final GuildMemberRemoveEvent event = mock(GuildMemberRemoveEvent.class);
        when(event.getUser()).thenReturn(user);
        return event;
    }

    @Test
    void leaveUsesLeaveMessagesEvenWithoutJoinMessages() {
        new DefaultServerNotifier("hook", null, List.of("bye"), true, true, true, true, true)
                .onGuildMemberRemove(leave());
        assertEquals("@User, bye", sentDescription());
    }

    @Test
    void missingOrEmptyMessagesUseTheDefault() {
        new DefaultServerNotifier("hook", List.of("hi"), Collections.emptyList(), true, true, true, true, true)
                .onGuildMemberRemove(leave());
        assertEquals("@User, see you later", sentDescription());
    }

    @Test
    void joinPicksAConfiguredMessage() {
        final User user = user();
        final GuildMemberJoinEvent event = mock(GuildMemberJoinEvent.class);
        when(event.getUser()).thenReturn(user);

        new DefaultServerNotifier("hook", List.of("hi"), null, true, true, true, true, true).onGuildMemberJoin(event);
        assertEquals("@User, hi", sentDescription());
    }

    @Test
    void disabledAnnouncementsSendNothing() {
        final User user = user();
        final GuildBanEvent ban = mock(GuildBanEvent.class);
        when(ban.getUser()).thenReturn(user);

        final DefaultServerNotifier notifier = new DefaultServerNotifier("hook", false, false, false, false, false);
        notifier.onGuildMemberRemove(leave());
        notifier.onGuildBan(ban);

        serverUtil.verify(() -> ServerUtil.sendWebHookMessage(any(), any()), never());
    }
}
