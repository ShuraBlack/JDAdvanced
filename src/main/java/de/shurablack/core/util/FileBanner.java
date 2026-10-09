package de.shurablack.core.util;

import de.shurablack.core.builder.CommandAction;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 * Console banner that is printed when the CLI of the bot starts.
 * <br><br>
 * Loads a text template from the classpath and replaces the placeholders <code>${author}</code>,
 * <code>${version}</code>, <code>${jdaVersion}</code>, <code>${color}</code>, <code>${bold}</code>,
 * <code>${style.reset}</code> and <code>${registeredCommands}</code>. The version values are hard-coded.
 * </p>
 *
 * @author ShuraBlack
 */
public class FileBanner {
    
    /** The banner text with all placeholders replaced */
    private String text;

    /**
     * Loads the banner template and fills in the placeholders.
     * @param innerPath the classpath resource path of the template, e.g. "/internal/banner.txt"
     * @param commands the CLI commands listed for <code>${registeredCommands}</code>
     * @throws IOException if the resource can't be read
     * @throws NullPointerException if no resource exists at {@code innerPath}
     */
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

    /** Formats each command as "name -> description", one per line */
    private static String formatCommandActions(List<CommandAction> commands) {
        final StringBuilder s = new StringBuilder();

        for (CommandAction action : commands) {
            s.append(action.getName()).append(" -> ").append(action.getDescription()).append("\n");
        }

        return s.toString();
    }

    /**
     * Prints the banner.
     * @param out the target stream, e.g. {@code System.out}
     */
    public void print(PrintStream out) {
        out.println(text);
    }
}
