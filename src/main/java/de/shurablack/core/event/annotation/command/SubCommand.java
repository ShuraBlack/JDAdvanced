package de.shurablack.core.event.annotation.command;


public @interface SubCommand {
    String name();
    String description();
    LocalizedText[] localizedNames() default {};
    LocalizedText[] localizedDescriptions() default {};
    CommandOption[] options() default {};
}
