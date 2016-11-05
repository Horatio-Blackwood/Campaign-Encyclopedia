package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityDataBuilder;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JFileChooser;
import toolbox.file.FileTools;
import toolbox.file.persistence.json.JsonArray;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 * A class to upgrade old save files from v1.1.0 to the new format, currently as defined in the 1.2.0 release.
 * @author adam
 */
public class SaveFileUpgrader {

    /** The JSON tag for the name of the campaign. */
    private static final String CAMPAGN_NAME = "campaign-name";
    /** The JSON tag for the Entities in the campaign. */
    private static final String ENTITIES = "entities";
    /** The JSON tag for the time line data. */
    private static final String TIMELINE_ENTRIES = "timeline-entries";
    /** The JSON tag for the Campaign Calendar. */
    private static final String CAMPAIGN_CALENDAR = "calendar";
    /** The JSON key for the relationships. */
    private static final String RELATIONSHIPS = "relationships";
    /** The JSON tag for the name of the entity. */
    private static final String NAME = "name";
    /** The JSON tag for the Type of Entity. */
    private static final String TYPE = "type";
    /** The JSON tag for the unique identifier of the Entity. */
    private static final String ID = "id";
    /** The JSON tag for the secret data of the entity. */
    private static final String SECRET_DATA = "secret-data";
    /** The JSON tag for the public data of the entity. */
    private static final String PUBLIC_DATA = "public-data";
    /** The JSON tag for the is or is not secret state of this Entity. */
    private static final String IS_SECRET = "is-secret";
    private static final String TITLE = "title";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String ASSOCIATED_ENTITY = "associated-entity";
    private static final String SECRET = "is-secret";
    private static final String INDEX = "index";
    /** The JSON key for the tags. */
    private static final String TAGS = "tags";
    /** The JSON key for the description. */
    private static final String DESCRIPTION = "description";
    /** The JSON Key for the relationship type. */
    private static final String RELATIONSHIP_TYPE = "relationship-type";


    private static Campaign upgrade(String jsonString) {
        Map<UUID, RelationshipManager> relationships = new HashMap<>();

        JsonObject json = new JsonObject(jsonString);
        String name = "unnamed campaign";
        if (json.has(CAMPAGN_NAME)) {
            name = json.getString(CAMPAGN_NAME);
        }

        Set<Entity> entitySet = new HashSet<>();
        JsonArray entities = json.getJsonArray(ENTITIES);
        for (int i = 0; i < entities.length(); i++) {
            entitySet.add(entityFromJson(entities.get(i).toString(), relationships));
        }

        Set<TimelineEntry> timelineData = new HashSet<>();
        if (json.has(TIMELINE_ENTRIES)) {
            JsonArray teArray = json.getJsonArray(TIMELINE_ENTRIES);
            for (int i = 0; i < teArray.length(); i++) {
                timelineData.add(timelineEvenFromJson(teArray.get(i).toString()));
            }
        }

        CampaignCalendar cal = new CampaignCalendar();
        if (json.has(CAMPAIGN_CALENDAR)) {
            JsonArray months = json.getJsonArray(CAMPAIGN_CALENDAR);
            List<Month> translated = new ArrayList<>();
            for (int i = 0; i < months.length(); i++) {
                translated.add(monthFromJson(months.getJSONObject(i)));
            }
            cal.updateMonths(translated);
        }

        return new Campaign(name, entitySet, relationships, timelineData, cal);
    }

    /**
     * Returns the Entity represented by the JSON String supplied.
     * @param jsonString the JSON String to translate.
     * @return the Entity that is represented by the supplied JSON String.
     * @throws JsonException if an error occurs during translation.
     */
    private static Entity entityFromJson(String jsonString, Map<UUID, RelationshipManager> relationshipMap) throws JsonException {
        JsonObject json = new JsonObject(jsonString);

        EntityType type = null;
        if (json.has(TYPE)) {
            type = EntityType.valueOf(json.getString(TYPE));
        }

        // Entity ID
        UUID entityId = null;
        if (json.has(ID)) {
            entityId = UUID.fromString(json.getString(ID));
        }

        // Public Data
        EntityData pd = null;
        if (json.has(PUBLIC_DATA)) {
            pd = entityDataFromJson(json.getJsonObject(PUBLIC_DATA).toString(), relationshipMap, entityId, false);
        }

        // Secret Data
        EntityData sd = new EntityDataBuilder().build();
        if (json.has(SECRET_DATA)) {
            sd = entityDataFromJson(json.getJsonObject(SECRET_DATA).toString(), relationshipMap, entityId, true);
        }

        // Is Secret
        boolean isSecret = false;
        if (json.has(IS_SECRET)) {
            isSecret = json.getBoolean(IS_SECRET);
        }

        String name = json.getString(NAME);

        return new Entity(entityId, name, type, pd, sd, isSecret);
    }

    /**
     * Translates the supplied JSON String into an EntityData object.
     * @param jsonString the string to translate to an EntityData.
     * @return the EntityData object that is represented by the supplied string.
     * @throws JsonException if an error occurs during translation.
     */
    private static EntityData entityDataFromJson(String jsonString, Map<UUID, RelationshipManager> relationships, UUID entity, boolean isSecret) throws JsonException {
        JsonObject json = new JsonObject(jsonString);

        // Tags
        Set<String> tags = new HashSet<>();
        if (json.has(TAGS)) {
            JsonArray jsonTags = json.getJsonArray(TAGS);
            for (int i = 0; i < jsonTags.length(); i++) {
                tags.add(jsonTags.getString(i));
            }
        }

        // Relationships
        if (json.has(RELATIONSHIPS)) {
            JsonArray relations = json.getJsonArray(RELATIONSHIPS);
            for (int i = 0; i < relations.length(); i++) {
                if (relationships.get(entity) == null) {
                    relationships.put(entity, new RelationshipManager());
                }
                JsonObject rel = relations.getJSONObject(i);
                relationships.get(entity).addRelationship(relationshipFromJson(rel.toString(), entity, isSecret));
            }
        }

        // Description
        String description = "";
        if (json.has(DESCRIPTION)) {
            description = json.getString(DESCRIPTION);
        }

        return new EntityData(description, tags);
    }

    /**
     * Returns the Relationship represented by the supplied JSON String.
     * @param jsonString the JSON string to translate to a Relationship.
     * @param entity the ID of the entity.
     * @param isSecret true if the relationship is a secret relationship, false otherwise.
     * @return the Relationship represented by the supplied JSON String.
     */
    private static Relationship relationshipFromJson(String jsonString, UUID entity, boolean isSecret) {
        JsonObject json = new JsonObject(jsonString);
        UUID id = UUID.fromString(json.getString(ID));
        String relType = json.getString(RELATIONSHIP_TYPE);

        return new Relationship(entity, id, relType, isSecret);
    }

    /**
     * Translates the a JSON string that represents a TimelineEvent.
     * @param jsonString the JSON String.
     * @return the TimelineEvent object translated from the JSON String.
     */
    private static TimelineEntry timelineEvenFromJson(String jsonString) {
        JsonObject json = new JsonObject(jsonString);

        // Title
        String title = null;
        if (json.has(TITLE)) {
            title = json.getString(TITLE);
        }

        // Month
        JsonObject jsonMonth = null;
        if (json.has(MONTH)) {
            jsonMonth = json.getJsonObject(MONTH);
        }

        // Secret
        boolean isSecret = false;
        if (json.has(SECRET)) {
            isSecret = json.getBoolean(SECRET);
        }

        Month month = MonthTranslator.fromJson(jsonMonth);
        int year = json.getInt(YEAR);
        UUID id = UUID.fromString(json.getString(ID));
        UUID associatedEntity = UUID.fromString(json.getString(ASSOCIATED_ENTITY));

        return new TimelineEntry(title, month, year, isSecret, associatedEntity, id);
    }

    /**
     * Translates a month from the JSON string.
     * @param json the string containing the month object.
     * @return The Month object translated from the JSON String.
     */
    private static Month monthFromJson(JsonObject json) {
        String name = json.getString(NAME);
        int index = json.getInt(INDEX);
        return new Month(name, index);
    }


    /**
     * Converts a v1.1.0-style sav file to a v1.2.0+ style save file.
     * @param args
     */
    public static void main(String[] args) throws IOException {
        JFileChooser chooser = new JFileChooser("./campaigns");
        int option = chooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            File toConvert = chooser.getSelectedFile();

            // Convert
            String oldCampaign = FileTools.readFile(toConvert.getAbsolutePath());
            Campaign converted = upgrade(oldCampaign);

            // Create a data accessor.
            CampaignDataManager cdm = new CampaignDataManager();
            cdm.setData(converted);

            // Write out translated file.
            FileTools.writeFile(toConvert.getAbsolutePath() + ".upgraded", CampaignTranslator.toJson(converted, cdm, true));
        }
    }
}