package de.shurablack.testdata.invalidcommand;

import de.shurablack.core.event.annotation.command.SlashCommand;

/** Declared next to a broken command, must not be uploaded alone */
@SlashCommand(name = "valid", description = "Valid")
public class ValidCommand {
}
