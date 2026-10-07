package de.shurablack.testdata;

import de.shurablack.core.event.annotation.command.Option;
import de.shurablack.core.event.annotation.command.SubCommandHandler;

/** Broken handlers, outside of the scanned test packages on purpose */
public class InvalidHandlers {

    @SubCommandHandler(command = "music", name = "play")
    void undeclaredOption(@Option("artist") String artist) { }

    @SubCommandHandler(command = "music", name = "play")
    void wrongType(@Option("title") Integer title) { }

    @SubCommandHandler(command = "music", name = "play")
    void primitiveOptional(@Option("volume") int volume) { }

    @SubCommandHandler(command = "music", name = "play")
    void notInjectable(StringBuilder builder) { }

    @SubCommandHandler(command = "music", name = "stop")
    void undeclaredSubCommand() { }
}
