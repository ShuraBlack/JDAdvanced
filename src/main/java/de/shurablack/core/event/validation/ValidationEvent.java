package de.shurablack.core.event.validation;

import de.shurablack.core.event.Event;

/**
 * Represents a validation event that associates an {@link Event} with a {@link Validation} state.
 * This class is used to encapsulate the result of a validation process in the {@link de.shurablack.core.event.EventHandler}
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public class ValidationEvent {

    /**
     * The event associated with this validation event.
     */
    private final Event event;

    /**
     * The validation state associated with this validation event.
     */
    private final Validation validation;

    /**
     * Private constructor for creating a ValidationEvent.
     *
     * @param event      The event associated with this validation event.
     * @param validation The validation state associated with this validation event.
     */
    private ValidationEvent(final Event event, final Validation validation) {
        this.event = event;
        this.validation = validation;
    }

    /**
     * Creates a successful validation event.
     *
     * @param event The event associated with the successful validation.
     * @return A new {@link ValidationEvent} with a success validation state.
     */
    public static ValidationEvent success(final Event event) {
        return new ValidationEvent(event, Validation.SUCCESS);
    }

    /**
     * Creates a failed validation event.
     *
     * @param validation The validation state representing the failure reason.
     * @return A new {@link ValidationEvent} with the specified failure validation state.
     */
    public static ValidationEvent fail(final Validation validation) {
        return new ValidationEvent(null, validation);
    }

    /**
     * Retrieves the event associated with this validation event.
     *
     * @return The associated {@link Event}, or null if the validation failed.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Retrieves the validation state associated with this validation event.
     *
     * @return The associated {@link Validation} state.
     */
    public Validation getValidation() {
        return validation;
    }

    /**
     * Checks if the validation event represents a successful validation.
     *
     * @return True if the validation state is {@link Validation#SUCCESS}, false otherwise.
     */
    public boolean isSuccess() {
        return validation == Validation.SUCCESS;
    }
}