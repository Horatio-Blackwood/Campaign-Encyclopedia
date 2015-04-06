package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityDataBuilder;
import campaignencyclopedia.data.EntityType;
import java.util.UUID;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 * The JSON translator for Entity objects.
 * @author adam
 */
public class EntityTranslator {

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

    /**
     * Returns the JSON object that represents the supplied Entity object.
     * @param entity the Entity to translate.
     * @param da a data accessor.
     * @param includeSecrets true if secrets should be included, false otherwise.
     * @return the JSON Object that represents the supplied Entity object.
     * @throws JsonException if an error occurs during translation.
     */
    public static JsonObject toJsonObject(Entity entity, DataAccessor da, boolean includeSecrets) throws JsonException {
        JsonObject json = new JsonObject();
        if (includeSecrets) {
            json.put(SECRET_DATA, EntityDataTranslator.toJsonObject(entity.getSecretData(), da, includeSecrets));
        }
        json.put(PUBLIC_DATA, EntityDataTranslator.toJsonObject(entity.getPublicData(), da, includeSecrets));
        json.put(ID, entity.getId().toString());
        json.put(TYPE, entity.getType().name());
        json.put(NAME, entity.getName());
        json.put(IS_SECRET, entity.isSecret());

        return json;
    }

    /**
     * Returns the Entity represented by the JSON String supplied.
     * @param jsonString the JSON String to translate.
     * @return the Entity that is represented by the supplied JSON String.
     * @throws JsonException if an error occurs during translation.
     */
    public static Entity fromJson(String jsonString) throws JsonException {
        JsonObject json = new JsonObject(jsonString);

        EntityType type = null;
        if (json.has(TYPE)) {
            type = EntityType.valueOf(json.getString(TYPE));
        }

        UUID id = null;
        if (json.has(ID)) {
            id = UUID.fromString(json.getString(ID));
        }

        EntityData pd = null;
        if (json.has(PUBLIC_DATA)) {
            pd = EntityDataTranslator.fromJson(json.getJsonObject(PUBLIC_DATA).toString());
        }

        EntityData sd = new EntityDataBuilder().build();
        if (json.has(SECRET_DATA)) {
            sd = EntityDataTranslator.fromJson(json.getJsonObject(SECRET_DATA).toString());
        }

        boolean isSecret = false;
        if (json.has(IS_SECRET)) {
            isSecret = json.getBoolean(IS_SECRET);
        }

        String name = json.getString(NAME);

        return new Entity(id, name, type, pd, sd, isSecret);
    }
}