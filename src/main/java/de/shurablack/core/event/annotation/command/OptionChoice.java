package de.shurablack.core.event.annotation.command;

/**
 * <p>
 * A predefined value of a {@link CommandOption}, see {@link CommandOption#choices()}.
 * </p>
 *
 * @author ShuraBlack
 */
public @interface OptionChoice {

    /**
     * @return the name shown to the user
     */
    String name();

    /**
     * @return the value the option receives if the choice gets selected
     */
    String value();
}
