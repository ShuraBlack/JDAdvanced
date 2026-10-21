package de.shurablack.core.event.annotation.command;


/**
 * <p>
 * Declares a sub command of a {@link SlashCommand}, e.g. <code>play</code> of <code>/music play</code>.
 * Only usable as element of {@link SlashCommand#subCommands()}, handled by a {@link CommandHandler} with
 * {@link CommandHandler#sub()} set to its name.
 * </p>
 *
 * @author ShuraBlack
 */
public @interface SubCommand {

    /**
     * @return the name of the sub command, must not be blank
     */
    String name();

    /**
     * @return the description of the sub command, must not be blank
     */
    String description();

    /**
     * @return the localized names of the sub command, empty (default) for none
     */
    LocalizedText[] localizedNames() default {};

    /**
     * @return the localized descriptions of the sub command, empty (default) for none
     */
    LocalizedText[] localizedDescriptions() default {};

    /**
     * @return the options of the sub command, empty (default) for none
     */
    CommandOption[] options() default {};
}
