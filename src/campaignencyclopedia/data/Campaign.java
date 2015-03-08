package campaignencyclopedia.data;

import java.util.HashSet;
import java.util.Set;

/**
 * The Top-Level data object of this application.
 * @author adam
 */
public class Campaign {

    /** The name of the Campaign. */
    private final String m_name;

    /** The Entity data that makes up this Campaign's primary content.  */
    private final Set<Entity> m_entities;

    /** A set of TimelineEntry objects for this Campaign. */
    private final Set<TimelineEntry> m_timelineEntries;

    /** The Calendar for this campaign. */
    private final CampaignCalendar m_calendar;

    /**
     * Constructor.
     * @param name The name of the Campaign.
     * @param entities The Entity data that makes up this Campaign's primary content.
     * @param timelineData the TimelineEntry data for the Campaign.
     * @param cal the campaign calendar.
     */
    public Campaign(String name, Set<Entity> entities, Set<TimelineEntry> timelineData, CampaignCalendar cal) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'name' must not be null or empty.");
        }
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' must not be null.");
        }
        if (timelineData == null) {
            throw new IllegalArgumentException("Parameter 'timelineData' must not be null.");
        }
        if (cal == null) {
            throw new IllegalArgumentException("Parameter 'cal' must not be null.");
        }
        m_name = name;
        m_entities = new HashSet<>(entities);
        m_timelineEntries = new HashSet<>(timelineData);
        m_calendar = cal;
    }

    /**
     * Returns the Entities contained in this Campaign.
     * @return the Entities contained in this Campaign.
     */
    public Set<Entity> getEntities() {
        return m_entities;
    }

    /**
     * Returns the timeline entries in this campaign.
     * @return the timeline entries in this campaign.
     */
    public Set<TimelineEntry> getTimelineEntries() {
        return m_timelineEntries;
    }

    /**
     * Returns the name of the Campaign.
     * @return the name of the Campaign.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the calendar for this Campaign.
     * @return the calendar for this Campaign.
     */
    public CampaignCalendar getCalendar() {
        return m_calendar;
    }
}