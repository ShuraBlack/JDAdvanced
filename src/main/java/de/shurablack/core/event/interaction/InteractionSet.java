package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
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
    public static InteractionSet create(final EventWorker worker, final Interaction... interactions) {
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

    /**
     * This static method creates a list of InteractionSets from a JSON file.
     * <br><br>
     * Check out the GitHub Wiki for more information's, on how to structure the JSON file.
     *
     * @param path the path to the JSON file
     * @return the list of InteractionSets
     */
    public static List<InteractionSet> fromJson(final String path) {
        JSONObject jsonObject = FileUtil.loadJson(path);
        if (jsonObject == null) {
            return new ArrayList<>();
        }
        List<InteractionSet> interactionSets = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("interactionSets");
        try {
            for (int idx = 0; idx < jsonArray.length(); idx++) {
                JSONObject interactionSet = jsonArray.getJSONObject(idx);
                Class<?> cls = Class.forName(interactionSet.getString("worker"));
                Object worker = cls.getDeclaredConstructor().newInstance();

                List<Interaction> interactionsList = new ArrayList<>();
                JSONArray interactions = interactionSet.getJSONArray("interactions");
                for (int subidx = 0 ; subidx < interactions.length(); subidx++) {
                    JSONObject interaction = interactions.getJSONObject(subidx);
                    Type type = Type.valueOf(interaction.getString("type"));
                    String identifier = interaction.getString("identifier");
                    long globalCooldown = interaction.getLong("globalCooldown");
                    long userCooldown = interaction.getLong("userCooldown");
                    JSONArray channelRestriction = interaction.getJSONArray("channelRestriction");
                    List<String> channelRestrictionList = new ArrayList<>();
                    for (int i = 0; i < channelRestriction.length(); i++) {
                        channelRestrictionList.add(channelRestriction.getString(i));
                    }
                    Interaction interactionObject = Interaction.create(type, identifier)
                            .setGlobalCD(globalCooldown)
                            .setUserCD(userCooldown)
                            .setChannelRestriction(channelRestrictionList);
                    interactionsList.add(interactionObject);
                }
                InteractionSet interactionSetObject = InteractionSet.create(
                        (EventWorker) worker,
                        interactionsList.toArray(new Interaction[0])
                );
                interactionSets.add(interactionSetObject);
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException
                 | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return interactionSets;
    }

}
