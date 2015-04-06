package campaignencyclopedia.data;

import java.util.UUID;

/**
 * A class which represents a significant campaign event.
 * @author adam
 */
public class TimelineEntry implements Comparable<TimelineEntry> {

    /** The title of the entry. */
    private final String m_title;

    /** The month (or other sub-year time unit) that this timeline entry occurred. */
    private final Month m_month;
    
    /** The year the event took place.  */
    private final int m_year;
    
    /** True if this timeline entry is secret, false otherwise. */
    private final boolean m_isSecret;
    
    /** The unique ID of this Timeline Entry. */
    private final UUID m_entryId;
    
    /** The ID of the Entity associated with this TimelineEntry. */
    private final UUID m_associatedEntity;

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param month the Season/Month, may be null or UNSET.
     * @param year the year.
     * @param isSecret true if this entry is secret, false otherwise.
     * @param associatedEntity an Entity associated with this TimelineEntity, if any.  May be null, but only if
     * the title is not empty or null.
     */
    public TimelineEntry(String title, Month month, int year, boolean isSecret, UUID associatedEntity) {
        this(title, month, year, isSecret, associatedEntity, UUID.randomUUID());
    }

    /**
     * Constructs a new TimelineEntry.  Requires EITHER a valid title (non-null, non-empty) or an associated entity ID,
     * or both.
     *
     * @param title the title of the entry, may be empty or null - only if the associated Entry is not null.
     * @param month the Month, may be null or UNSET.
     * @param year the year.
     * @param isSecret true if this entry is secret, false otherwise.
     * @param associatedEntity an Entity associated with this TimelineEntity.
     * @param entryId the unique ID of this TimelineEntry.
     */
    public TimelineEntry(String title, Month month, int year, boolean isSecret, UUID associatedEntity, UUID entryId) {
        if (associatedEntity == null) {
            throw new IllegalArgumentException("associatedEnttiy must not be null.");
        }
        if (month == null) {
            throw new IllegalArgumentException("month must not be null.");
        }
        m_title = title;
        m_month = month;
        m_year = year;
        m_isSecret = isSecret;
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

    /**
     * Returns the year of this timeline entry.
     * @return the year of this timeline entry.
     */
    public int getYear() {
        return m_year;
    }
    
    /**
     * Returns true if this TimeLine entry is Secret, False otherwise.
     * @return true if this TimeLine entry is Secret, False otherwise.
     */
    public boolean isSecret() {
        return m_isSecret;
    }

    public UUID getAssociatedId() {
        return m_associatedEntity;
    }

    public UUID getId() {
        return m_entryId;
    }

    /** {@Override} */
    @Override
    public int compareTo(TimelineEntry t) {
        // Compare by Year
        if (m_year > t.getYear()) {
            return 1;
        } else if (m_year < t.getYear()) {
            return -1;
        }
        
        // If the years are the same, compare by month.
        // If the months are the same, compare the titles.
        if (m_month.compareTo(t.getMonth()) == 0) {
            return m_title.compareTo(t.getTitle());
        } else {
            return m_month.compareTo(t.getMonth());
        }
    }
}