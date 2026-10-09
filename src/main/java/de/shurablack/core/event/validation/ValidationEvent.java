package de.shurablack.core.event.validation;

import de.shurablack.core.event.Event;

/**
 * <p>
 * Result of the validation of an incoming event in the {@link de.shurablack.core.event.handler.EventHandler}:
 * the {@link Validation} state and, if the validation succeeded, the matching {@link Event}.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public class ValidationEvent {

    /**
     * The validated event, null if the validation failed.
     */
    private final Event event;

    /**
     * The validation state.
     */
    private final Validation validation;

    /**
     * Private constructor for creating a ValidationEvent.
     *
     * @param event      the validated event, null if the validation failed
     * @param validation the validation state
     */
    private ValidationEvent(final Event event, final Validation validation) {
        this.event = event;
        this.validation = validation;
    }

    /**
     * Creates a successful validation event.
     *
     * @param event the validated event
     * @return a new {@link ValidationEvent} with the state {@link Validation#SUCCESS}
     */
    public static ValidationEvent success(final Event event) {
        return new ValidationEvent(event, Validation.SUCCESS);
    }

    /**
     * Creates a failed validation event.
     *
     * @param validation the validation state representing the failure reason
     * @return a new {@link ValidationEvent} with the given state and without an event
     */
    public static ValidationEvent fail(final Validation validation) {
        return new ValidationEvent(null, validation);
    }

    /**
     * Retrieves the event associated with this validation event.
     *
     * @return the validated {@link Event}, null if the validation failed
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Retrieves the validation state associated with this validation event.
     *
     * @return the {@link Validation} state
     */
    public Validation getValidation() {
        return validation;
    }

    /**
     * Checks if the validation event represents a successful validation.
     *
     * @return true if the validation state is {@link Validation#SUCCESS}, false otherwise
     */
    public boolean isSuccess() {
        return validation == Validation.SUCCESS;
    }
}