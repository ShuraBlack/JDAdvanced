package de.shurablack.testdata;

import de.shurablack.core.event.annotation.command.CommandHandler;

/** Proxy friendly handler, outside of the scanned test packages on purpose */
public class PublicHandler {

    @CommandHandler(command = "ping")
    public void ping() { }
}
