package de.shurablack.core.event.interaction;

import de.shurablack.core.event.ImplementationException;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.interaction.sample.MusicCommands;
import de.shurablack.core.event.interaction.sample.PingCommand;
import de.shurablack.core.util.ClassScan;
import de.shurablack.testdata.PublicHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstanceFactoryTest {

    private static final SlashCommand MUSIC = MusicCommands.class.getAnnotation(SlashCommand.class);
    private static final SlashCommand PING = PingCommand.class.getAnnotation(SlashCommand.class);

    @AfterEach
    void resetScan() {
        ClassScan.setPackages();
    }

    @Test
    void classesWithoutInstanceAreSkipped() {
        ClassScan.setPackages("de.shurablack.core.event.interaction.sample");
        assertTrue(InteractionSet.fromAnnotation(cls -> null).isEmpty());

        final List<InteractionSet> sets = InteractionSet.fromAnnotation(cls -> cls == PingCommand.class ? new PingCommand() : null);
        assertEquals(1, sets.size());
        assertEquals("ping", sets.get(0).getInteractions().get(0).getIdentifier());
    }

    @Test
    void proxiesNeedPublicMethods() throws NoSuchMethodException {
        final Method play = MusicCommands.class.getDeclaredMethod("play", net.dv8tion.jda.api.entities.Member.class, String.class, Integer.class);

        // Subclass like a Spring CGLIB proxy, wouldnt delegate the package-private method
        final ImplementationException cglib = assertThrows(ImplementationException.class,
                () -> SlashCommandInvoker.create(new MusicCommands() { }, play, MUSIC));
        assertTrue(cglib.getMessage().contains("make the method public"), cglib.getMessage());

        // Interface based proxy, not an instance of the class at all
        final ImplementationException jdk = assertThrows(ImplementationException.class,
                () -> SlashCommandInvoker.create(new Object(), play, MUSIC));
        assertTrue(jdk.getMessage().contains("Interface based proxies"), jdk.getMessage());

        // Public methods work through a subclass proxy
        final Method ping = PublicHandler.class.getDeclaredMethod("ping");
        assertNotNull(SlashCommandInvoker.create(new PublicHandler() { }, ping, PING));
    }
}
