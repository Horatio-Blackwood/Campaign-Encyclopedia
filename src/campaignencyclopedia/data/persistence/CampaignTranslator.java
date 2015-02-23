package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import toolbox.file.persistence.json.JsonArray;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 * A JSON translator for persisting Campaign data.
 * @author adam
 */
public class CampaignTranslator {

    private static final String NAME = "campaign-name";
    private static final String ENTITIES = "entities";
    private static final String TIMELINE_ENTRIES = "timeline-entries";


    public static String toJson(Campaign campaign, boolean includeSecrets) throws JsonException {
        JsonObject json = new JsonObject();
        json.put(NAME,campaign.getName());

        // Collect the secret entities for use later during translation.
        Set<UUID> secretEntities = new HashSet<>();

        Set<JsonObject> entities = new HashSet<>();
        for (Entity entity : campaign.getEntities()) {
            if (entity.isSecret()) {
                secretEntities.add(entity.getId());
                if (!includeSecrets) {
                    continue;
                }
            }
            entities.add(EntityTranslator.toJsonObject(entity, includeSecrets));
        }
        json.put(ENTITIES, entities);

        Set<JsonObject> timelineEntries = new HashSet<>();
        for (TimelineEntry te : campaign.getTimelineEntries()) {
            UUID associatedEntity = te.getAssociatedId();
            if (associatedEntity != null && secretEntities.contains(associatedEntity) && !includeSecrets) {
                continue;
            }
            timelineEntries.add(TimelineEntryTranslator.toJsonObject(te));
        }
        json.put(TIMELINE_ENTRIES, timelineEntries);

        return json.toString(4);
    }


    public static Campaign fromJson(String jsonString) throws JsonException {
        JsonObject json = new JsonObject(jsonString);
        String name = "unnamed campaign";
        if (json.has(NAME)) {
            name = json.getString(NAME);
        }

        Set<Entity> entitySet = new HashSet<>();
        JsonArray entities = json.getJsonArray(ENTITIES);
        for (int i = 0; i < entities.length(); i++) {
            entitySet.add(EntityTranslator.fromJson(entities.get(i).toString()));
        }

        Set<TimelineEntry> timelineData = new HashSet<>();
        if (json.has(TIMELINE_ENTRIES)) {
            JsonArray teArray = json.getJsonArray(TIMELINE_ENTRIES);
            for (int i = 0; i < teArray.length(); i++) {
                timelineData.add(TimelineEntryTranslator.fromJson(teArray.get(i).toString()));
            }
        }

        return new Campaign(name, entitySet, timelineData);
    }
}