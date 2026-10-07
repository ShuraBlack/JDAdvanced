package de.shurablack.core.builder;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandUploadBuilderTest {

    private static CommandData play(String description, String germanName) {
        return Commands.slash("play", "Plays music")
                .setNameLocalization(DiscordLocale.GERMAN, germanName)
                .addSubcommands(new SubcommandData("song", description)
                        .addOption(OptionType.STRING, "title", "The title", true, true));
    }

    private static CommandData ping() {
        return Commands.slash("ping", "Pong");
    }

    @Test
    void sameCommandsInOtherOrderAreUpToDate() {
        assertTrue(CommandUploadBuilder.isUpToDate(
                List.of(play("Plays a song", "spielen"), ping()),
                List.of(ping(), play("Plays a song", "spielen"))));
    }

    @Test
    void changesAreDetected() {
        final List<CommandData> registered = List.of(play("Plays a song", "spielen"), ping());

        assertFalse(CommandUploadBuilder.isUpToDate(registered, List.of(play("Plays any song", "spielen"), ping())));
        assertFalse(CommandUploadBuilder.isUpToDate(registered, List.of(play("Plays a song", "abspielen"), ping())));
        assertFalse(CommandUploadBuilder.isUpToDate(registered, List.of(play("Plays a song", "spielen"))));
        assertFalse(CommandUploadBuilder.isUpToDate(registered, List.of(play("Plays a song", "spielen"), ping(),
                Commands.slash("stop", "Stops"))));
    }
}
