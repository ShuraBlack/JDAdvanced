package de.shurablack.core.util;

import de.shurablack.core.builder.CommandAction;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBanner {
    
    private String text;

    public FileBanner(String innerPath, List<CommandAction> commands) throws IOException {
        this.text = new String(
                getClass().getResourceAsStream(innerPath).readAllBytes(),
                StandardCharsets.UTF_8
        );

        this.text = this.text
                .replace("${author}", "ShuraBlack")
                .replace("${version}", "1.0.11")
                .replace("${jdaVersion}", "6.4.1")
                .replace("${color}", "\\033[104m")
                .replace("${bold}", "\\033[1m")
                .replace("${style.reset}", "\\033[0m")
                .replace("${registeredCommands}", formatCommandActions(commands));
    }

    private static String formatCommandActions(List<CommandAction> commands) {
        final StringBuilder s = new StringBuilder();

        for (CommandAction action : commands) {
            s.append(action.getName()).append(" -> ").append(action.getDescription()).append("\n");
        }

        return s.toString();
    }

    public void print(PrintStream out) {
        out.println(text);
    }
}
