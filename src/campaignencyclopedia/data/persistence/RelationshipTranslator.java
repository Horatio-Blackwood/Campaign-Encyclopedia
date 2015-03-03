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

    /** The JSON Key for the UUID. */
    private static final String ID = "id";

    /**
     * Translates the supplied relationship to the JSON String that represents it.
     * @param rel the Relationship to translate.
     * @return the JSON Object that represents the supplied Relationship.
     */
    public static JsonObject toJson(Relationship rel) {
        JsonObject json = new JsonObject();
        json.put(ID, rel.getIdOfRelation().toString());
        json.put(RELATIONSHIP_TYPE, rel.getRelationship());

        return json;
    }

    /**
     * Returns the Relationship represented by the supplied JSON String.
     * @param jsonString the JSON string to translate to a Relationship.
     * @return the Relationship represented by the supplied JSON String.
     */
    public static Relationship fromJson(String jsonString) {
        JsonObject json = new JsonObject(jsonString);
        UUID id = UUID.fromString(json.getString(ID));
        String relType = json.getString(RELATIONSHIP_TYPE);

        return new Relationship(id, relType);
    }
}