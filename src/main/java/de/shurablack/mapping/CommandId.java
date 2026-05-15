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
public class CommandId {

    /** Primary Commands String */
    private String primary;

    /** List of sub commands */
    private final String[] sub;

    /**
     * The CommandMapper standard constructor
     * @param input the user input/message
     */
    public CommandId(final String input) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input string is empty");
        }
        String[] args = input.split(" ");
        this.primary = args[0];

        if (args.length > 1) {
            this.sub = new String[0];
        } else {
            this.sub = Arrays.copyOfRange(args, 1, args.length);
        }
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
        return this.sub.length > 0;
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
        if (index >= this.sub.length) {
            return null;
        }
        return this.sub[index];
    }

    /**
     * @return the total length of the command
     */
    public int totalSize() {
        return 1 + this.sub.length;
    }

    /**
     * @return the length of the sub commands
     */
    public int subSize() {
        return this.sub.length;
    }
}
