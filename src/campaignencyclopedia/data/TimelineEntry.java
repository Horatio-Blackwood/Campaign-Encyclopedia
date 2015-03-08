package campaignencyclopedia.data;

import java.util.UUID;

/**
 *
 * @author adam
 */
public class TimelineEntry implements Comparable<TimelineEntry> {

    /** The title of the entry. */
    private final String m_title;

    /** The month (or other sub-year time unit) that this timeline entry occurred. */
    private final Month m_month;
    private final int m_year;
    private final UUID m_entryId;
    private final UUID m_associatedEntity;

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param month the Season/Month, may be null or UNSET.
     * @param year the year.
     * @param associatedEntity an Entity associated with this TimelineEntity, if any.  May be null, but only if
     * the title is not empty or null.
     */
    public TimelineEntry(String title, Month month, int year, UUID associatedEntity) {
        this(title, month, year, associatedEntity, UUID.randomUUID());
    }

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param month the Month, may be null or UNSET.
     * @param year the year.
     * @param associatedEntity an Entity associated with this TimelineEntity.
     * @param entryId the unique ID of this TimelineEntry.
     */
    public TimelineEntry(String title, Month month, int year, UUID associatedEntity, UUID entryId) {
        if (associatedEntity == null) {
            throw new IllegalArgumentException("associatedEnttiy must not be null.");
        }
        if (month == null) {
            throw new IllegalArgumentException("month must not be null.");
        }
        m_title = title;
        m_month = month;
        m_year = year;
        m_associatedEntity = associatedEntity;
        m_entryId = entryId;
    }

    /**
     * Returns the title of this TimelineEntry.
     * @return the title of this TimelineEntry.
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * Returns the Month of this Timeline Entry.
     * @return the Month of this Timeline Entry.
     */
    public Month getMonth() {
        return m_month;
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
        
        return (m_month.compareTo(t.getMonth()));
    }
}