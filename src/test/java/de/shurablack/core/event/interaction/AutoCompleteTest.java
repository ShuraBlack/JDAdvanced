package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.handler.NativeEventHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutoCompleteTest {

    @Test
    void everyWorkerMethodMapsToAType() {
        for (Method method : EventWorker.class.getDeclaredMethods()) {
            if (method.getName().startsWith("process")) {
                assertNotNull(Type.fromFunctionName(method.getName()), method.getName());
            }
        }
        assertEquals(Type.AUTO_COMPLETE, Type.fromFunctionName("processAutoCompleteEvent"));
    }

    @Test
    void routesToWorkerByCommandName() {
        final EventWorker worker = mock(EventWorker.class);
        final NativeEventHandler handler = NativeEventHandler.createDefault();
        handler.registerEvent(List.of(InteractionSet.create(worker, Interaction.create(Type.AUTO_COMPLETE, "play"))));

        final CommandAutoCompleteInteractionEvent event = mock(CommandAutoCompleteInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getFocusedOption().getName()).thenReturn("song");

        handler.onAutoCompleteEvent("play", event);
        verify(worker).processAutoCompleteEvent(any(), any(), eq("song"), eq(event));

        // Unknown command: answer with no choices instead of letting discord time out
        handler.onAutoCompleteEvent("unknown", event);
        verify(event).replyChoices();
    }
}
