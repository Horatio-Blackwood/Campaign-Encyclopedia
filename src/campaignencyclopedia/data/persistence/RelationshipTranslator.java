package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipType;
import java.util.UUID;
import toolbox.file.persistence.json.JsonObject;

/**
 * Translator for Relationships.
 * @author adam
 */
public class RelationshipTranslator {

    private static final String RELATIONSHIP_TYPE = "relationship-type";
    private static final String ID = "id";

    /**
     * Translates the supplied relationship to the JSON String that represents it.
     * @param rel the Relationship to translate.
     * @return the JSON String that represents the supplied Relationship.
     */
    public static String toJson(Relationship rel) {
        JsonObject json = new JsonObject();
        json.put(ID, rel.getIdOfRelation().toString());
        json.put(RELATIONSHIP_TYPE, rel.getType().name());

        return json.toString(4);
    }

    public static Relationship fromJson(String jsonString) {
        JsonObject json = new JsonObject(jsonString);
        UUID id = UUID.fromString(json.getString(ID));
        RelationshipType relType = RelationshipType.valueOf(json.getString(RELATIONSHIP_TYPE));

        return new Relationship(id, relType);
    }
}
