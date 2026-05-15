package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public @interface CommandOption {
    OptionType type();
    String name();
    String description();
    LocalizedText[] localizedNames() default {};
    LocalizedText[] localizedDescriptions() default {};
    long maxValue() default -1;
    long minValue() default -1;
    int maxLength() default -1;
    boolean required() default false;
    OptionChoice[] choices() default {};
}
