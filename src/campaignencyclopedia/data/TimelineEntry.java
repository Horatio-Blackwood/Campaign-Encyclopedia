package campaignencyclopedia.data;

import java.util.UUID;

/**
 *
 * @author adam
 */
public class TimelineEntry implements Comparable<TimelineEntry> {

    private final String m_title;
    private final Season m_season;
    private final int m_year;
    private final UUID m_entryId;
    private final UUID m_associatedEntity;

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param season the Season, may be null or UNSET.
     * @param year the year.
     * @param associatedEntity an Entity associated with this TimelineEntity, if any.  May be null, but only if
     * the title is not empty or null.
     */
    public TimelineEntry(String title, Season season, int year, UUID associatedEntity) {
        this(title, season, year, associatedEntity, UUID.randomUUID());
    }

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param season the Season, may be null or UNSET.
     * @param year the year.
     * @param associatedEntity an Entity associated with this TimelineEntity.
     * @param entryId the unique ID of this TimelineEntry.
     */
    public TimelineEntry(String title, Season season, int year, UUID associatedEntity, UUID entryId) {
        if (associatedEntity == null) {
            throw new IllegalArgumentException("associatedEnttiy must not be null.");
        }
        m_title = title;
        m_season = season;
        m_year = year;
        m_associatedEntity = associatedEntity;
        m_entryId = entryId;
    }

    public String getTitle() {
        return m_title;
    }

    public Season getSeason() {
        return m_season;
    }

    public int getYear() {
        return m_year;
    }

    public UUID getAssociatedId() {
        return m_associatedEntity;
    }

    public UUID getId() {
        return m_entryId;
    }

    @Override
    public int compareTo(TimelineEntry t) {
        // Compare by Year
        if (m_year > t.getYear()) {
            return 1;
        } else if (m_year < t.getYear()) {
            return -1;
        }

        // If the years are the same, return ordered by season.
        return(m_season.compareTo(t.getSeason()));
    }
}