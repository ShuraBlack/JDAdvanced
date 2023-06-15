package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * The InteractionSet class is a utility class that stores a list of {@link Interaction} objects and an associated {@link EventWorker} object.
 * <br><br>
 * It provides methods for creating and setting the interactions and for accessing the stored interactions and worker
 * </p>
 *
 * @see Interaction Interaction
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class InteractionSet {


    /** The EventWorker object associated with all {@link Interaction Interactions} */
    private EventWorker worker;

    /** The list of Interaction objects stored in the InteractionSet */
    private final List<Interaction> interactions = new ArrayList<>();

    private InteractionSet() { }

    /**
     * This is a static factory method that creates a new InteractionSet object.
     * @param worker the associated {@link EventWorker}
     * @param interactions the set of {@link Interaction Options}
     * @return the built InteractionSet
     */
    public InteractionSet create(final EventWorker worker, final Interaction... interactions) {
        InteractionSet interactionSet = new InteractionSet();
        interactionSet.worker = worker;
        interactionSet.interactions.addAll(Arrays.asList(interactions));
        return interactionSet;
    }

    /**
     * @return the Options stored in the Set
     */
    public List<Interaction> getInteractions() {
        return interactions;
    }

    /**
     * @return the associated {@link EventWorker}
     */
    public EventWorker getWorker() {
        return worker;
    }
}
