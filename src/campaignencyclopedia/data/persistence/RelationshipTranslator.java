package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Relationship;
import java.util.UUID;
import toolbox.file.persistence.json.JsonObject;

/**
 * Translator for Relationships.
 * @author adam
 */
public class RelationshipTranslator {

    /** The JSON Key for the relationship type. */
    private static final String RELATIONSHIP_TYPE = "relationship-type";

    /** The JSON Key for the UUID of the Entity that the owning Entity is related to. */
    private static final String RELATION_ID = "relation-id";

    /** The JSON Key for the UUID of the Entity that owns this relationship. */
    private static final String ENTITY_ID = "entity-id";

    /** The JSON Key for whether or not the Relationship is secret. */
    private static final String IS_SECRET = "is-secret";

    /**
     * Translates the supplied relationship to the JSON String that represents it.
     * @param rel the Relationship to translate.
     * @return the JSON Object that represents the supplied Relationship.
     */
    public static JsonObject toJson(Relationship rel) {
        JsonObject json = new JsonObject();
        json.put(RELATION_ID, rel.getRelatedEntity().toString());
        json.put(ENTITY_ID, rel.getEntityId().toString());
        json.put(RELATIONSHIP_TYPE, rel.getRelationshipText());
        json.put(IS_SECRET, rel.isSecret());

        return json;
    }

    /**
     * Returns the Relationship represented by the supplied JSON String.
     * @param jsonString the JSON string to translate to a Relationship.
     * @return the Relationship represented by the supplied JSON String.
     */
    public static Relationship fromJson(String jsonString) {
        JsonObject json = new JsonObject(jsonString);
        UUID entity = UUID.fromString(json.getString(ENTITY_ID));
        UUID relation = UUID.fromString(json.getString(RELATION_ID));
        String relType = json.getString(RELATIONSHIP_TYPE);
        boolean isSecret = json.getBoolean(IS_SECRET);

        return new Relationship(entity, relation, relType, isSecret);
    }
}