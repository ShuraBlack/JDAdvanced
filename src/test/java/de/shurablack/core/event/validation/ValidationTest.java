package de.shurablack.core.event.validation;

import de.shurablack.core.event.Event;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ValidationTest {

    @Test
    void everyFailureHasAMessageAndAnEmbed() {
        for (Validation validation : Validation.values()) {
            if (validation == Validation.SUCCESS) {
                assertNull(validation.getMessage());
                continue;
            }
            assertNotNull(validation.getMessage(), validation.name());
            // Without a translation the english default is used
            assertEquals(validation.getMessage(), validation.getEmbed(DiscordLocale.JAPANESE).getDescription());
        }
    }

    @Test
    void validationEvent() {
        final Event event = mock(Event.class);
        final ValidationEvent success = ValidationEvent.success(event);
        assertTrue(success.isSuccess());
        assertSame(event, success.getEvent());

        final ValidationEvent fail = ValidationEvent.fail(Validation.ON_COOLDOWN);
        assertFalse(fail.isSuccess());
        assertNull(fail.getEvent());
        assertEquals(Validation.ON_COOLDOWN, fail.getValidation());
    }
}
