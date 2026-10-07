package de.shurablack.testdata;

import de.shurablack.core.event.annotation.command.Option;
import de.shurablack.core.event.annotation.command.CommandHandler;

/** Broken handlers, outside of the scanned test packages on purpose */
public class InvalidHandlers {

    @CommandHandler(command = "music", sub = "play")
    void undeclaredOption(@Option("artist") String artist) { }

    @CommandHandler(command = "music", sub = "play")
    void wrongType(@Option("title") Integer title) { }

    @CommandHandler(command = "music", sub = "play")
    void primitiveOptional(@Option("volume") int volume) { }

    @CommandHandler(command = "music", sub = "play")
    void notInjectable(StringBuilder builder) { }

    @CommandHandler(command = "music", sub = "stop")
    void undeclaredSubCommand() { }

    @CommandHandler(command = "music")
    void missingSub() { }

    @CommandHandler(command = "ping")
    void undeclaredTopLevelOption(@Option("target") String target) { }
}
