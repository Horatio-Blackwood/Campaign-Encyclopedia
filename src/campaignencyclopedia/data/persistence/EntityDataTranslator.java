package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import java.util.HashSet;
import java.util.Set;
import toolbox.file.persistence.json.JsonArray;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 * The translator for EntityData objects.
 * @author adam
 */
public class EntityDataTranslator {

    /** The JSON key for the tags. */
    public static final String TAGS = "tags";

    /** The JSON key for the relationships. */
    public static final String RELATIONSHIPS = "relationships";

    /** The JSON key for the description. */
    public static final String DESCRIPTION = "description";

    /**
     * Translates the supplied EntityData to the JSON Object that will represent it on disk.
     * @param data the object to translate.
     * @return the JSON Object that represented it.
     * @throws JsonException if an error occurs during translation.
     */
    public static JsonObject toJson(EntityData data) throws JsonException {
        JsonObject json = new JsonObject();

        // Description
        json.put(DESCRIPTION, data.getDescription());

        // Tags
        JsonArray tags = new JsonArray(new HashSet<Object>(data.getTags()));
        json.put(TAGS, tags);

        // Relationships
        Set<JsonObject> relationships = new HashSet<>();
        for (Relationship rel : data.getRelationships()) {
            relationships.add(RelationshipTranslator.toJson(rel));
        }
        json.put(RELATIONSHIPS, relationships);

        return json;
    }

    /**
     * Translates the supplied JSON String into an EntityData object.
     * @param jsonString the string to translate to an EntityData.
     * @return the EntityData object that is represented by the supplied string.
     * @throws JsonException if an error occurs during translation.
     */
    public static EntityData fromJson(String jsonString) throws JsonException {
        JsonObject json = new JsonObject(jsonString);

        Set<String> tags = new HashSet<>();
        if (json.has(TAGS)) {
            JsonArray jsonTags = json.getJsonArray(TAGS);
            for (int i = 0; i < jsonTags.length(); i++) {
                tags.add(jsonTags.getString(i));
            }
        }

        Set<Relationship> relationships = new HashSet<>();
        if (json.has(RELATIONSHIPS)) {
            JsonArray relations = json.getJsonArray(RELATIONSHIPS);
            for (int i = 0; i < relations.length(); i++) {
                JsonObject rel = relations.getJSONObject(i);
                relationships.add(RelationshipTranslator.fromJson(rel.toString()));
            }
        }

        String description = "";
        if (json.has(DESCRIPTION)) {
            description = json.getString(DESCRIPTION);
        }

        return new EntityData(description, tags, relationships);
    }
}