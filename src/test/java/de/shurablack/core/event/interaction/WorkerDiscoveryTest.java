package de.shurablack.core.event.interaction;

import de.shurablack.core.event.interaction.workers.GreetWorker;
import de.shurablack.core.event.interaction.workers.RedirectWorker;
import de.shurablack.core.util.ClassScan;
import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Discovery of {@link de.shurablack.core.event.annotation.EventProcess}, {@link de.shurablack.core.event.annotation.RedirectedProcess},
 * {@link de.shurablack.core.event.annotation.DisabledWorker} and {@link de.shurablack.core.event.annotation.BypassGuildValidity}
 */
class WorkerDiscoveryTest {

    private Map<String, Interaction> interactions;

    private List<InteractionSet> sets;

    @BeforeEach
    void discover() {
        ClassScan.setPackages("de.shurablack.core.event.interaction.workers");
        sets = InteractionSet.fromAnnotation();
        interactions = sets.stream().flatMap(set -> set.getInteractions().stream())
                .collect(Collectors.toMap(Interaction::getIdentifier, Function.identity()));
    }

    @AfterEach
    void resetScan() {
        ClassScan.setPackages();
    }

    @Test
    void disabledWorkersAreSkipped() {
        assertEquals(Set.of("greet", "hello", "vote", "poll"), interactions.keySet());
        assertEquals(Set.of(GreetWorker.class, RedirectWorker.class),
                sets.stream().map(set -> set.getWorker().getClass()).collect(Collectors.toSet()));
    }

    @Test
    void eventProcessSettingsAreApplied() {
        final Interaction greet = interactions.get("greet");
        assertEquals(Type.BUTTON, greet.getType());
        assertEquals(3_000, greet.getUserCooldown());
        assertEquals(1_000, greet.getGlobalCooldown());
        assertEquals(List.of("c1"), greet.getChannelRestriction());
        assertEquals(Set.of(Permission.MESSAGE_MANAGE), greet.getRequiredPermissions());
        assertFalse(greet.isBypassGuildValidity());

        final Interaction hello = interactions.get("hello");
        assertEquals(Type.GUILD_SLASH, hello.getType());
        assertTrue(hello.isBypassGuildValidity());
    }

    @Test
    void redirectedProcessesUseTheGivenType() {
        assertEquals(Type.BUTTON, interactions.get("vote").getType());
        assertEquals(2_000, interactions.get("vote").getUserCooldown());
        assertEquals(Type.MODAL, interactions.get("poll").getType());
    }
}
