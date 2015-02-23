package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityDataBuilder;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import toolbox.file.persistence.json.JsonArray;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 *
 * @author adam
 */
public class EntityDataTranslator {

    public static final String TAGS = "tags";
    public static final String RELATIONSHIPS = "relationships";
    public static final String DESCRIPTION = "description";

    public static String toJson(EntityData data) throws JsonException {
        JsonObject json = new JsonObject();

        json.put(DESCRIPTION, data.getDescription());
        json.put(TAGS, data.getTags());

        Set<String> relationships = new HashSet<>();
        for (Relationship rel : data.getRelationships()) {
            relationships.add(RelationshipTranslator.toJson(rel));
        }
        json.put(RELATIONSHIPS, relationships);

        return json.toString(4);
    }

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
                String rel = relations.getString(i);
                relationships.add(RelationshipTranslator.fromJson(rel));
            }
        }

        String description = "";
        if (json.has(DESCRIPTION)) {
            description = json.getString(DESCRIPTION);
        }

        return new EntityData(description, tags, relationships);
    }
}