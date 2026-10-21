package de.shurablack.core.event.interaction.sample;

import de.shurablack.core.event.annotation.command.CommandHandler;
import de.shurablack.core.event.annotation.command.CommandOption;
import de.shurablack.core.event.annotation.command.Option;
import de.shurablack.core.event.annotation.command.SlashCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@SlashCommand(name = "ping", description = "Pings a user", options = {
        @CommandOption(type = OptionType.USER, name = "target", description = "Who to ping")
})
public class PingCommand {

    public static User target;

    @CommandHandler(command = "ping")
    void ping(@Option("target") User target) {
        PingCommand.target = target;
    }
}
