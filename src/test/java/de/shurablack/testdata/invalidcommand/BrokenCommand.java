package de.shurablack.testdata.invalidcommand;

import de.shurablack.core.event.annotation.command.CommandOption;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.annotation.command.SubCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/** Discord doesn't allow options and sub commands on the same command */
@SlashCommand(name = "broken", description = "Broken",
        options = @CommandOption(type = OptionType.STRING, name = "text", description = "Text"),
        subCommands = @SubCommand(name = "sub", description = "Sub"))
public class BrokenCommand {
}
