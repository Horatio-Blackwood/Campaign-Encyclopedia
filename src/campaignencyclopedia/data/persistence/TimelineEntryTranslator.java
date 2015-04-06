package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.TimelineEntry;
import java.util.UUID;
import toolbox.file.persistence.json.JsonObject;

/**
 *
 * @author adam
 */
public class TimelineEntryTranslator {

    private static final String TITLE = "title";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String ID = "id";
    private static final String ASSOCIATED_ENTITY = "associated-entity";
    private static final String SECRET = "is-secret";

    /**
     *
     * @param entry
     * @return
     * @throws NullPointerException if entry is null.
     */
    public static JsonObject toJsonObject(TimelineEntry entry) {
        JsonObject json = new JsonObject();
        json.put(MONTH, MonthTranslator.toJson(entry.getMonth()));
        json.put(YEAR, entry.getYear());
        json.put(ID, entry.getId().toString());

        String title = entry.getTitle();
        if (title != null) {
            json.put(TITLE, entry.getTitle());
        }

        // If not null, add the assocated entity
        UUID associatedEntity = entry.getAssociatedId();
        if (associatedEntity != null) {
            json.put(ASSOCIATED_ENTITY, associatedEntity.toString());
        }
        
        // Secret / Not Secret
        json.put(SECRET, entry.isSecret());

        return json;
    }

    public static TimelineEntry fromJson(String jsonString) {
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
}