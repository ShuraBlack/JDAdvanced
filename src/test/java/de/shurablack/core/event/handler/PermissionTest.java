package de.shurablack.core.event.handler;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.Validation;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionTest {

    private final EventWorker worker = mock(EventWorker.class);

    // errorFeedback off, so failed validations dont need a reply mock
    private final NativeEventHandler handler = NativeEventHandler.create("!", true, false, false)
            .registerEvent(InteractionSet.create(worker,
                    Interaction.create(Type.BUTTON, "ban").setRequiredPermissions(Permission.BAN_MEMBERS)));

    private ButtonInteractionEvent click(boolean allowed) {
        final GuildChannel channel = mock(GuildChannel.class);
        final Member member = mock(Member.class, RETURNS_DEEP_STUBS);
        when(member.getUser().isBot()).thenReturn(false);
        when(member.getGuild().getGuildChannelById("channel")).thenReturn(channel);
        when(member.hasPermission(eq(channel), anyCollection())).thenReturn(allowed);

        final ButtonInteractionEvent event = mock(ButtonInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getMember()).thenReturn(member);
        when(event.getChannel().getId()).thenReturn("channel");
        handler.onButtonEvent("ban", event);
        return event;
    }

    @Test
    void memberNeedsThePermissionInTheChannel() {
        click(false);
        verifyNoInteractions(worker);

        final ButtonInteractionEvent allowed = click(true);
        verify(worker).processButtonEvent(any(), any(), any(), eq(allowed));
    }

    @Test
    void deniedOutsideOfGuilds() {
        final User user = mock(User.class);
        assertEquals(Validation.NO_PERMISSION, handler.getEventWorker("ban", Type.BUTTON, user, "dm").getValidation());
    }
}
