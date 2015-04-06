package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.TimelineEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import toolbox.file.persistence.json.JsonArray;
import toolbox.file.persistence.json.JsonException;
import toolbox.file.persistence.json.JsonObject;

/**
 * A JSON translator for persisting Campaign data.
 * @author adam
 */
public class CampaignTranslator {

    /** The JSON tag for the name of the campaign. */
    private static final String NAME = "campaign-name";
    /** The JSON tag for the Entities in the campaign. */
    private static final String ENTITIES = "entities";
    /** The JSON tag for the time line data. */
    private static final String TIMELINE_ENTRIES = "timeline-entries";
    /** The JSON tag for the Campaign Calendar. */
    private static final String CAMPAIGN_CALENDAR = "calendar";


    public static String toJson(Campaign campaign, boolean includeSecrets) throws JsonException {
        JsonObject json = new JsonObject();
        json.put(NAME,campaign.getName());

        // Collect the secret entities for use later during translation.
        Set<UUID> secretEntities = new HashSet<>();

        // Translate and store Entities in sorted order.
        // --- Sort them to ensure a consistent output order (useful for diffs)
        List<Entity> allEntities = new ArrayList<>(campaign.getEntities());
        Collections.sort(allEntities);
        // --- Translate and add them to JSON structure.
        List<JsonObject> entities = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (entity.isSecret()) {
                secretEntities.add(entity.getId());
                if (!includeSecrets) {
                    continue;
                }
            }
            entities.add(EntityTranslator.toJsonObject(entity, includeSecrets));
        }
        json.put(ENTITIES, entities);

        // Translate the timeline entires in sorted order.
        List<TimelineEntry> timeline = new ArrayList<>(campaign.getTimelineEntries());
        Collections.sort(timeline);
        List<JsonObject> timelineEntries = new ArrayList<>();
        for (TimelineEntry te : timeline) {
            UUID associatedEntity = te.getAssociatedId();
            if (associatedEntity != null && secretEntities.contains(associatedEntity) && !includeSecrets) {
                continue;
            }
            timelineEntries.add(TimelineEntryTranslator.toJsonObject(te));
        }
        json.put(TIMELINE_ENTRIES, timelineEntries);

        List<Month> months = new ArrayList<>(campaign.getCalendar().getMonths());
        Collections.sort(months);
        List<JsonObject> calendarMonths = new ArrayList<>();
        for (Month month : months) {
            if (!month.equals(Month.UNSPECIFIED)) {
                calendarMonths.add(MonthTranslator.toJson(month));
            }
        }
        json.put(CAMPAIGN_CALENDAR, calendarMonths);

        return json.toString(4);
    }


    public static Campaign fromJson(String jsonString) throws JsonException {
        JsonObject json = new JsonObject(jsonString);
        String name = "unnamed campaign";
        if (json.has(NAME)) {
            name = json.getString(NAME);
        }

        Set<Entity> entitySet = new HashSet<>();
        JsonArray entities = json.getJsonArray(ENTITIES);
        for (int i = 0; i < entities.length(); i++) {
            entitySet.add(EntityTranslator.fromJson(entities.get(i).toString()));
        }

        Set<TimelineEntry> timelineData = new HashSet<>();
        if (json.has(TIMELINE_ENTRIES)) {
            JsonArray teArray = json.getJsonArray(TIMELINE_ENTRIES);
            for (int i = 0; i < teArray.length(); i++) {
                timelineData.add(TimelineEntryTranslator.fromJson(teArray.get(i).toString()));
            }
        }

        CampaignCalendar cal = new CampaignCalendar();
        if (json.has(CAMPAIGN_CALENDAR)) {
            JsonArray months = json.getJsonArray(CAMPAIGN_CALENDAR);
            List<Month> translated = new ArrayList<>();
            for (int i = 0; i < months.length(); i++) {
                translated.add(MonthTranslator.fromJson(months.getJSONObject(i)));
            }
            cal.updateMonths(translated);
        }

        return new Campaign(name, entitySet, timelineData, cal);
    }
}