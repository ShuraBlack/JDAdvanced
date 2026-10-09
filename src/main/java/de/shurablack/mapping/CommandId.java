package de.shurablack.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Parses a space-separated input like {@code "primary sub1 sub2"} into a primary command and sub-commands.
 * <br><br>
 * It provides methods for checking the presence of a primary and sub-command, getting the primary and
 * sub-commands, and getting the number of sub-commands and of all tokens.
 * </p>
 *
 * @version mapping-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class CommandId {

    /** The first token of the input */
    private String primary;

    /** The sub-commands, never {@code null} */
    private final String[] sub;

    /**
     * Splits the input at single spaces; the first token becomes the primary command, the others the sub-commands.
     * @param input the user input/message, must not be {@code null}
     * @throws IllegalArgumentException if the input is empty
     */
    public CommandId(final String input) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input string is empty");
        }
        String[] args = input.split(" ");
        this.primary = args[0];

        // Empty for a single word
        this.sub = Arrays.copyOfRange(args, 1, args.length);
    }

    /**
     * Checks if a primary command is present. Always true for a constructed instance.
     * @return true if it is present
     */
    public boolean isPresent() {
        return this.primary != null;
    }

    /**
     * Checks if sub-commands exist
     * @return true if there are sub-commands
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
     * @param index the zero-based position of the sub-command, must not be negative
     * @return the sub-command or {@code null} if the index is out of range
     */
    public String getSub(final int index) {
        if (index >= this.sub.length) {
            return null;
        }
        return this.sub[index];
    }

    /**
     * @return the number of tokens, i.e. the primary command plus all sub-commands
     */
    public int totalSize() {
        return 1 + this.sub.length;
    }

    /**
     * @return the number of sub-commands
     */
    public int subSize() {
        return this.sub.length;
    }
}
