package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Season;
import campaignencyclopedia.data.TimelineEntry;
import java.util.UUID;
import toolbox.file.persistence.json.JsonObject;

/**
 *
 * @author adam
 */
public class TimelineEntryTranslator {

    private static final String TITLE = "title";
    private static final String SEASON = "season";
    private static final String YEAR = "year";
    private static final String ID = "id";
    private static final String ASSOCIATED_ENTITY = "associated-entity";

    /**
     *
     * @param entry
     * @return
     * @throws NullPointerException if entry is null.
     */
    public static JsonObject toJsonObject(TimelineEntry entry) {
        JsonObject json = new JsonObject();
        json.put(SEASON, entry.getSeason().name());
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

        return json;
    }

    public static TimelineEntry fromJson(String jsonString) {
        JsonObject json = new JsonObject(jsonString);

        String title = null;
        if (json.has(TITLE)) {
            title = json.getString(TITLE);
        }

        Season season = Season.valueOf(json.getString(SEASON));
        int year = json.getInt(YEAR);
        UUID id = UUID.fromString(json.getString(ID));
        UUID associatedEntity = UUID.fromString(json.getString(ASSOCIATED_ENTITY));

        return new TimelineEntry(title, season, year, associatedEntity, id);
    }
}