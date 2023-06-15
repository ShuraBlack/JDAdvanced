package de.shurablack.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * The CommandMapper class is a utility for parsing a string input into a primary command and
 * a list of sub-commands.<br><br>It provides methods for checking the presence of a primary and sub-command,
 * getting the primary and sub-commands, and getting the sizes of the sub-command list and the entire input string
 * </p>
 *
 * @version mapping-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class CommandMapper {

    /** Primary Commands String */
    private String primary;

    /** List of sub commands */
    private final List<String> sub = new ArrayList<>();

    /**
     * The CommandMapper standard constructor
     * @param input the user input/message
     */
    public CommandMapper(final String input) {
        if (input.isEmpty()) {
            return;
        }
        String[] args = input.split(" ");
        this.primary = args[0];

        sub.addAll(Arrays.asList(args).subList(1, args.length));
    }

    /**
     * Checks if a primary command is present
     * @return true if it is present
     */
    public boolean isPresent() {
        return this.primary != null;
    }

    /**
     * Checks if sub commands exists
     * @return true if there are sub commands
     */
    public boolean isSubPresent() {
        return !this.sub.isEmpty();
    }

    /**
     * @return the primary command
     */
    public String getPrimary() {
        return this.primary;
    }

    /**
     * @param index the position of the sub command
     * @return the sub command or null
     */
    public String getSub(final int index) {
        if (index >= this.sub.size()) {
            return null;
        }
        return this.sub.get(index);
    }

    /**
     * @return the total length of the command
     */
    public int totalSize() {
        return sub.size()+1;
    }

    /**
     * @return the length of the sub commands
     */
    public int subSize() {
        return sub.size();
    }
}
