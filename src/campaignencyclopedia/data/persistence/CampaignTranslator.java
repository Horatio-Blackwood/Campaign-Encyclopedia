package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    /** The JSON tag for the name of the campaign. */
    private static final String NAME = "campaign-name";
    /** The JSON tag for the Entities in the campaign. */
    private static final String ENTITIES = "entities";
    /** The JSON tag for the time line data. */
    private static final String TIMELINE_ENTRIES = "timeline-entries";
    /** The JSON tag for the Campaign Calendar. */
    private static final String CAMPAIGN_CALENDAR = "calendar";
    /** The JSON key for the relationships. */
    private static final String RELATIONSHIPS = "relationships";
    /** The JSON key for the version this file was created with. */
    private static final String VERSION_TAG = "version";
    /** The version value. */
    private static final String VERSION = "1.2.0";


    /**
     * Translates the supplied Campaign to JSON for storage to file.
     * 
     * @param campaign the Campaign to translate.
     * @param da a DataAccessor for accessing any required data.
     * @param includeSecrets true if Secrets should be included in the save file.
     * 
     * @return a JSON string that represents the supplied Campaign.
     * 
     * @throws JsonException if an error occurs during translation.
     */
    public static String toJson(Campaign campaign, DataAccessor da, boolean includeSecrets) throws JsonException {
        JsonObject json = new JsonObject();
        json.put(NAME, campaign.getName());
        
        json.put(VERSION_TAG, VERSION);

        // Collect the secret entities for use later during translation.
        Set<UUID> secretEntities = new HashSet<>();

        // ENTITIES
        // Translate and store Entities in sorted order.
        // --- Sort them to ensure a consistent output order (useful for diffs)
        List<Entity> allEntities = new ArrayList<>(campaign.getEntities());
        Collections.sort(allEntities);
        // --- Translate and add them to JSON structure.
        List<JsonObject> entities = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (entity.isSecret()) {
                secretEntities.add(entity.getId());
                if (!includeSecrets) {
                    continue;
                }
            }
            entities.add(EntityTranslator.toJsonObject(entity, da, includeSecrets));
        }
        json.put(ENTITIES, entities);

        // TIMELINE ENTRIES
        List<TimelineEntry> timeline = new ArrayList<>(campaign.getTimelineEntries());
        Collections.sort(timeline);
        List<JsonObject> timelineEntries = new ArrayList<>();
        for (TimelineEntry te : timeline) {
            UUID associatedEntity = te.getAssociatedId();
            if (associatedEntity != null && secretEntities.contains(associatedEntity) && !includeSecrets) {
                continue;
            }
            timelineEntries.add(TimelineEntryTranslator.toJsonObject(te));
        }
        json.put(TIMELINE_ENTRIES, timelineEntries);

        // CALENDAR
        List<Month> months = new ArrayList<>(campaign.getCalendar().getMonths());
        Collections.sort(months);
        List<JsonObject> calendarMonths = new ArrayList<>();
        for (Month month : months) {
            calendarMonths.add(MonthTranslator.toJson(month));
        }
        json.put(CAMPAIGN_CALENDAR, calendarMonths);

        // RELATIONSHIPS
        List<Relationship> relationships = new ArrayList<>();
        for (RelationshipManager rels : campaign.getAllRelationships().values()) {
            relationships.addAll(rels.getAllRelationships());
        }
        Collections.sort(relationships);
        List<JsonObject> jsonRels = new ArrayList<>();
        for (Relationship rel : relationships) {
            jsonRels.add(RelationshipTranslator.toJson(rel));
        }
        json.put(RELATIONSHIPS, jsonRels);

        return json.toString(4);
    }


    /**
     * Translates the JSON String that represents a Campaign into the associated Campaign object.
     * @param jsonString the Campaign JSON string to translate.
     * @return the Campaign object translated from the JSON String.
     *
     * @throws JsonException if an error occurs translating the Campaign.
     */
    public static Campaign fromJson(String jsonString) throws JsonException {
        JsonObject json = new JsonObject(jsonString);

        // Name
        String name = "unnamed campaign";
        if (json.has(NAME)) {
            name = json.getString(NAME);
        }

        // Entities
        Set<Entity> entitySet = new HashSet<>();
        JsonArray entities = json.getJsonArray(ENTITIES);
        for (int i = 0; i < entities.length(); i++) {
            entitySet.add(EntityTranslator.fromJson(entities.get(i).toString()));
        }

        // Timeline Entries
        Set<TimelineEntry> timelineData = new HashSet<>();
        if (json.has(TIMELINE_ENTRIES)) {
            JsonArray teArray = json.getJsonArray(TIMELINE_ENTRIES);
            for (int i = 0; i < teArray.length(); i++) {
                timelineData.add(TimelineEntryTranslator.fromJson(teArray.get(i).toString()));
            }
        }

        // Campaign Calendar
        CampaignCalendar cal = new CampaignCalendar();
        if (json.has(CAMPAIGN_CALENDAR)) {
            JsonArray months = json.getJsonArray(CAMPAIGN_CALENDAR);
            List<Month> translated = new ArrayList<>();
            for (int i = 0; i < months.length(); i++) {
                translated.add(MonthTranslator.fromJson(months.getJSONObject(i)));
            }
            cal.updateMonths(translated);
        }

        // Relationships
        Map<UUID, RelationshipManager> relationships = new HashMap<>();
        if (json.has(RELATIONSHIPS)) {
            JsonArray rels = json.getJsonArray(RELATIONSHIPS);
            for (int i = 0; i <rels.length(); i++) {
                Relationship rel = RelationshipTranslator.fromJson(rels.getJSONObject(i).toString());
                if (relationships.get(rel.getEntityId()) == null) {
                    relationships.put(rel.getEntityId(), new RelationshipManager());
                }
                relationships.get(rel.getEntityId()).addRelationship(rel);
            }
        }

        return new Campaign(name, entitySet, relationships, timelineData, cal);
    }
}