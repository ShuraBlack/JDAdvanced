package de.shurablack.core.event.interaction.workers;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.annotation.ExtendedEventProcess;
import de.shurablack.core.event.annotation.RedirectedProcess;
import de.shurablack.core.event.interaction.Type;

@RedirectedProcess({
        @ExtendedEventProcess(identifier = "vote", type = Type.BUTTON, userCooldown = 2),
        @ExtendedEventProcess(identifier = "poll", type = Type.MODAL)
})
public class RedirectWorker extends EventWorker {
}
