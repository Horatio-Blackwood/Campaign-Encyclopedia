package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JOptionPane;

/**
 * A data management object that is used at run time to provide a mutable object containing the state of the entire
 * Campaign.  As changes are made, this manager is updated and when asked to save the data this data is dumped to file.
 *
 * @author adam
 */
public class CampaignDataManager implements DataAccessor {

    private String m_campaignName;
    private final Map<UUID, Entity> m_entities;
    private final Map<UUID, TimelineEntry> m_timelineData;
    private String m_filename;
    private CampaignCalendar m_cal;

    public CampaignDataManager() {
        m_campaignName = "New Campaign";
        m_entities = new HashMap<>();
        m_timelineData = new HashMap<>();
        m_filename = null;
        m_cal = new CampaignCalendar();
    }

    /** {@inheritDoc} */
    @Override
    public Entity getEntity(UUID id) {
        if (id != null) {
            return m_entities.get(id);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Entity> getAllEntities() {
        List<Entity> list = new ArrayList<>(m_entities.values());
        Collections.sort(list);
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public void addOrUpdateEntity(Entity entity) {
        if (entity != null) {
            m_entities.put(entity.getId(), entity);
        }
    }


    @Override
    public void addOrKUpdateTimelineEntry(TimelineEntry entry) {
        if (entry != null) {
            m_timelineData.put(entry.getId(), entry);
        }
    }

    @Override
    public void removeTimelineEntry(UUID id) {
        if (id != null) {
            m_timelineData.remove(id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeEntity(UUID id) {
        if (id != null) {
            m_entities.remove(id);
        }
    }

    /**
     * Updates the calendar in this CDM using the supplied one.
     * @param cal the new calendar.
     */
    public void updateCalendar(CampaignCalendar cal) {
        m_cal.updateMonths(cal.getMonths());
        for (UUID id : m_timelineData.keySet()) {
            TimelineEntry entry = m_timelineData.get(id);
            if (!m_cal.hasMonth(entry.getMonth())) {
                TimelineEntry updated = new TimelineEntry(entry.getTitle(), Month.UNSPECIFIED, entry.getYear(), entry.getAssociatedId(), entry.getId());
                m_timelineData.put(id, updated);
            }
        }
    }

    /**
     * Creates and returns a Campaign that is represented by all of the data in the CampaignDataManager.  The CDM is
     * not modified in any way.  Each time this method is called, a new Campaign object is instantiated and returned.
     *
     * @return a Campaign that contains all of the data in the CampaignDataManager.
     */
    public Campaign getData() {
        return new Campaign(m_campaignName, new HashSet<>(m_entities.values()), new HashSet<>(m_timelineData.values()), m_cal);
    }

    /**
     * Clears all old data and sets the supplied campaign data on this display.
     * @param campaign the new data to set.
     */
    public void setData(Campaign campaign) {
        m_entities.clear();
        m_timelineData.clear();
        m_campaignName = campaign.getName();
        m_cal = campaign.getCalendar();

        // Set to collect all of the previously saved relationships.
        Set<String> relationships = new HashSet<>();
        for (Entity e : campaign.getEntities()) {
            m_entities.put(e.getId(), e);

            // Collect all of the previously saved relationships and add them to our Set above.
            for (Relationship r : e.getPublicData().getRelationships()) {
                relationships.add(r.getRelationship());
            }
            for (Relationship r : e.getSecretData().getRelationships()) {
                relationships.add(r.getRelationship());
            }
        }

        // Ensure that all of the relationships previously saved are in the local
        // relationships file, and indeed the Relationship Data Manager as well.
        RelationshipDataManager.addRelationships(new ArrayList<>(relationships));
        
        // Roll through each of the timeline entries for this campaign and ensure that the months all exist in the 
        // campaign.  If any are missing, add them to the Calendar and alert the user with a popup message.
        boolean monthsAdded = false;
        for (TimelineEntry tle : campaign.getTimelineEntries()) {
            m_timelineData.put(tle.getId(), tle);
            if (!m_cal.hasMonth(tle.getMonth())) {
                m_cal.addMonth(tle.getMonth());
                monthsAdded = true;
            }
        }
        if (monthsAdded) {
            JOptionPane.showMessageDialog(null,
                                          "One or more 'months' were added to your campaign\n"
                                        + "calendar based on stored campaign timeline data.\n"
                                        + "You may review this change to your calendar in its\n"
                                        + "configuration dialog.",
                                          "Missing Months",
                                          JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Returns the save file name.
     * @return the save file name.
     */
    public String getSaveFileName() {
        return m_filename;
    }

    /**
     * Sets the save file name.
     * @param filename the file name of the campaign.
     */
    public void setFileName(String filename) {
        if (filename != null && !filename.endsWith(".campaign")) {
            filename += ".campaign";
        }
        m_filename = filename;
    }

    /**
     * Returns the timeline data.
     * @return the timeline data.
     */
    @Override
    public Set<TimelineEntry> getTimelineData() {
        return new HashSet<>(m_timelineData.values());
    }
}