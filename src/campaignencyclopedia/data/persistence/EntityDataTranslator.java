package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    private static final String TAGS = "tags";

    /** The JSON key for the relationships. */
    private static final String RELATIONSHIPS = "relationships";

    /** The JSON key for the description. */
    private static final String DESCRIPTION = "description";

    /** The translator-only relationship comparator. */
    private static final Comparator<Relationship> REL_COMPARATOR = new Comparator<Relationship>() {
            @Override
            public int compare(Relationship t, Relationship t1) {
                int val = t.compareTo(t1);
                if (val == 0) {
                    // Only for the purpose of sortingfor the translator
                    return t.getIdOfRelation().compareTo(t1.getIdOfRelation());
                }
                return val;
            }
        };

    /**
     * Translates the supplied EntityData to the JSON Object that will represent it on disk.
     * @param data the object to translate.
     * @return the JSON Object that represented it.
     * @throws JsonException if an error occurs during translation.
     */
    public static JsonObject toJsonObject(EntityData data) throws JsonException {
        JsonObject json = new JsonObject();

        // Description
        json.put(DESCRIPTION, data.getDescription());

        // Tags
        List<String> tags = new ArrayList<>(data.getTags());
        Collections.sort(tags);
        JsonArray jTags = new JsonArray(tags);
        json.put(TAGS, jTags);

        // Relationships
        List<Relationship> rels = new ArrayList<>(data.getRelationships());
        Collections.sort(rels, REL_COMPARATOR);
        List<JsonObject> relationships = new ArrayList<>();
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