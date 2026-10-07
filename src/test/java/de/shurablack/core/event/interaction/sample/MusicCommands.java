package de.shurablack.core.event.interaction.sample;

import de.shurablack.core.event.annotation.command.CommandOption;
import de.shurablack.core.event.annotation.command.Option;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.annotation.command.SubCommand;
import de.shurablack.core.event.annotation.command.SubCommandHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@SlashCommand(name = "music", description = "Music", subCommands = {
        @SubCommand(name = "play", description = "Plays a song", options = {
                @CommandOption(type = OptionType.STRING, name = "title", description = "The title", required = true),
                @CommandOption(type = OptionType.INTEGER, name = "volume", description = "The volume")
        })
})
public class MusicCommands {

    public static Member member;
    public static String title;
    public static Integer volume;

    @SubCommandHandler(command = "music", name = "play")
    void play(Member member, @Option("title") String title, @Option("volume") Integer volume) {
        MusicCommands.member = member;
        MusicCommands.title = title;
        MusicCommands.volume = volume;
    }
}
