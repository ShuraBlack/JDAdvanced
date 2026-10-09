package de.shurablack.core.event.interaction.workers;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.annotation.BypassGuildValidity;
import de.shurablack.core.event.annotation.EventProcess;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class GreetWorker extends EventWorker {

    @EventProcess(identifier = "greet", userCooldown = 3, globalCooldown = 1, restrictedChannel = {"c1"},
            requiredPermissions = Permission.MESSAGE_MANAGE)
    @Override
    public void processButtonEvent(Member member, MessageChannelUnion channel, String compID, ButtonInteractionEvent event) { }

    @EventProcess(identifier = "hello")
    @BypassGuildValidity
    @Override
    public void processGuildSlashEvent(Member member, MessageChannelUnion channel, String name, SlashCommandInteractionEvent event) { }
}
