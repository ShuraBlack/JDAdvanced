package de.shurablack.core.builder;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import de.shurablack.core.util.ClassScan;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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

    @Test
    void uploadsTopLevelOptions() {
        ClassScan.setPackages("de.shurablack.core.event.interaction.sample");
        try {
            final SlashCommandData ping = (SlashCommandData) CommandUploadBuilder.buildCommands().get("GLOBAL").stream()
                    .filter(c -> c.getName().equals("ping")).findFirst().orElseThrow();
            assertEquals("target", ping.getOptions().get(0).getName());
            assertEquals(OptionType.USER, ping.getOptions().get(0).getType());
            // Context commands need runtime retention, otherwise the whole build fails and nothing gets uploaded
            assertTrue(CommandUploadBuilder.buildCommands().get("GLOBAL").stream()
                    .anyMatch(c -> c.getName().equals("Report") && c.getType() == Command.Type.MESSAGE));
        } finally {
            ClassScan.setPackages();
        }
    }

    @Test
    void invalidDeclarationUploadsNothing() {
        // A partial list would delete the missing commands at Discord
        ClassScan.setPackages("de.shurablack.testdata.invalidcommand");
        try {
            assertTrue(CommandUploadBuilder.buildCommands().isEmpty());
        } finally {
            ClassScan.setPackages();
        }
    }

    @Test
    void withoutGlobalCommandsTheRegisteredOnesGetRemoved() {
        // Package without any command declarations, e.g. after removing the last global command
        ClassScan.setPackages("de.shurablack.core.scheduling.sample");
        try {
            final CommandListUpdateAction update = mock(CommandListUpdateAction.class, RETURNS_SELF);
            final JDA jda = mock(JDA.class);
            when(jda.updateCommands()).thenReturn(update);

            CommandUploadBuilder.uploadInteractionCommands(jda, false);

            verify(update).addCommands(List.<CommandData>of());
            verify(update).queue(any(), any());
        } finally {
            ClassScan.setPackages();
        }
    }

    @Test
    void invalidDeclarationDoesntTouchTheRegisteredCommands() {
        ClassScan.setPackages("de.shurablack.testdata.invalidcommand");
        try {
            final JDA jda = mock(JDA.class);

            CommandUploadBuilder.uploadInteractionCommands(jda, false);

            verify(jda, never()).updateCommands();
        } finally {
            ClassScan.setPackages();
        }
    }
}
