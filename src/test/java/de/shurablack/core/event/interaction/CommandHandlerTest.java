package de.shurablack.core.event.interaction;

import de.shurablack.core.event.ImplementationException;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.event.annotation.command.CommandHandler;
import de.shurablack.core.event.interaction.sample.MusicCommands;
import de.shurablack.core.event.interaction.sample.PingCommand;
import de.shurablack.core.util.ClassScan;
import de.shurablack.testdata.InvalidHandlers;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandHandlerTest {

    @AfterEach
    void resetScan() {
        ClassScan.setPackages();
    }

    @Test
    void discoversAndInjectsOptions() {
        // Scans the sample handler and the internal SlashCommandInvoker, which must be skipped
        ClassScan.setPackages("de.shurablack.core.event.interaction");
        final NativeEventHandler handler = NativeEventHandler.createDefault().registerEvent(InteractionSet.fromAnnotation());

        final Member member = mock(Member.class, RETURNS_DEEP_STUBS);
        final OptionMapping title = mock(OptionMapping.class);
        when(title.getAsString()).thenReturn("Never Gonna Give You Up");

        final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getFullCommandName()).thenReturn("music play");
        when(event.getMember()).thenReturn(member);
        when(event.getOption("title")).thenReturn(title);
        when(event.getOption("volume")).thenReturn(null);

        // The listener passes the command name, the handler prefers the sub command registration
        handler.onGuildSlashEvent("music", event);

        assertSame(member, MusicCommands.member);
        assertEquals("Never Gonna Give You Up", MusicCommands.title);
        assertNull(MusicCommands.volume);
    }

    @Test
    void commandWithoutSubCommands() {
        ClassScan.setPackages("de.shurablack.core.event.interaction");
        final NativeEventHandler handler = NativeEventHandler.createDefault().registerEvent(InteractionSet.fromAnnotation());

        final User target = mock(User.class);
        final OptionMapping option = mock(OptionMapping.class);
        when(option.getAsUser()).thenReturn(target);

        final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getFullCommandName()).thenReturn("ping");
        when(event.getOption("target")).thenReturn(option);

        // DM: no member, routed as global slash event
        handler.onGlobalSlashEvent("ping", event);

        assertSame(target, PingCommand.target);
    }

    @Test
    void invalidHandlersFailOnRegistration() {
        final Method[] methods = InvalidHandlers.class.getDeclaredMethods();
        assertEquals(7, methods.length);

        for (Method method : methods) {
            final String command = method.getAnnotation(CommandHandler.class).command();
            final SlashCommand declaration = (command.equals("ping") ? PingCommand.class : MusicCommands.class).getAnnotation(SlashCommand.class);
            final ImplementationException e = assertThrows(ImplementationException.class,
                    () -> SlashCommandInvoker.create(new InvalidHandlers(), method, declaration), method.getName());
            assertTrue(e.getMessage().contains(method.getName()), e.getMessage());
        }
    }
}
