package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * <p>
 * Declares an option of a {@link SlashCommand} or {@link SubCommand}. Only usable as element of those annotations,
 * the value can be injected into a {@link CommandHandler} parameter via {@link Option}.
 * <br><br>
 * The constraints are passed to JDA on upload. Invalid combinations (e.g. a max length on a non STRING option)
 * make the whole upload fail, so no command gets uploaded.
 * </p>
 * <pre>{@code
 * @CommandOption(type = OptionType.INTEGER, name = "amount", description = "How many", minValue = 1, maxValue = 10, required = true)
 * }</pre>
 *
 * @author ShuraBlack
 */
public @interface CommandOption {

    /**
     * @return the type of the option
     */
    OptionType type();

    /**
     * @return the name of the option, must not be blank
     */
    String name();

    /**
     * @return the description of the option, must not be blank
     */
    String description();

    /**
     * @return the localized names of the option, empty (default) for none
     */
    LocalizedText[] localizedNames() default {};

    /**
     * @return the localized descriptions of the option, empty (default) for none
     */
    LocalizedText[] localizedDescriptions() default {};

    /**
     * @return the maximum value of an INTEGER or NUMBER option, -1 (default) for none
     */
    long maxValue() default -1;

    /**
     * @return the minimum value of an INTEGER or NUMBER option, -1 (default) for none
     */
    long minValue() default -1;

    /**
     * @return the maximum length of a STRING option, -1 (default) for none
     */
    int maxLength() default -1;

    /**
     * @return true if the option has to be provided, false (default) otherwise
     */
    boolean required() default false;

    /**
     * Predefined values the user has to choose from. They are added as string choices, so they only fit STRING options.
     * @return the choices, empty (default) for a free input
     */
    OptionChoice[] choices() default {};

    /**
     * Dynamic suggestions via {@link de.shurablack.core.event.EventWorker#processAutoCompleteEvent EventWorker#processAutoCompleteEvent},
     * can't be combined with {@link #choices()}.
     * @return true to enable auto complete, false (default) otherwise
     */
    boolean autoComplete() default false;
}
