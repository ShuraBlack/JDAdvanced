package de.shurablack.core.event.interaction.workers;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.annotation.DisabledWorker;
import de.shurablack.core.event.annotation.EventProcess;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@DisabledWorker
public class OffWorker extends EventWorker {

    @EventProcess(identifier = "off")
    @Override
    public void processButtonEvent(Member member, MessageChannelUnion channel, String compID, ButtonInteractionEvent event) { }
}
