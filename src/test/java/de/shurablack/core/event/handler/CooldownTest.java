package de.shurablack.core.event.handler;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.Validation;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CooldownTest {

    private static User user(String id) {
        final User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private static Validation call(AbstractEventHandler handler, String identifier, User user) {
        return handler.getEventWorker(identifier, Type.BUTTON, user, "channel").getValidation();
    }

    @Test
    void userAndGlobalCooldowns() {
        final AbstractEventHandler handler = NativeEventHandler.createDefault().registerEvent(InteractionSet.create(mock(EventWorker.class),
                Interaction.create(Type.BUTTON, "user").setUserCD(60),
                Interaction.create(Type.BUTTON, "global").setGlobalCD(60),
                Interaction.create(Type.BUTTON, "free")));
        final User alice = user("1");
        final User bob = user("2");

        assertEquals(Validation.SUCCESS, call(handler, "user", alice));
        assertEquals(Validation.ON_COOLDOWN, call(handler, "user", alice));
        assertEquals(Validation.SUCCESS, call(handler, "user", bob));

        assertEquals(Validation.SUCCESS, call(handler, "global", alice));
        assertEquals(Validation.ON_COOLDOWN, call(handler, "global", bob));

        assertEquals(Validation.SUCCESS, call(handler, "free", alice));
        assertEquals(Validation.SUCCESS, call(handler, "free", alice));
    }

    @Test
    void blockedCallDoesntStartTheOtherCooldown() {
        final AbstractEventHandler handler = NativeEventHandler.createDefault().registerEvent(InteractionSet.create(mock(EventWorker.class),
                Interaction.create(Type.BUTTON, "both").setGlobalCD(60).setUserCD(60)));
        final User alice = user("1");

        assertEquals(Validation.SUCCESS, call(handler, "both", alice));
        // Blocked by the user cooldown, must not count as a new call
        assertEquals(Validation.ON_COOLDOWN, call(handler, "both", alice));
        assertEquals(Validation.ON_COOLDOWN, call(handler, "both", user("2")));
    }
}
