package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * A data management object that is used at run time to provide a mutable object containing the state of the entire
 * Campaign.  As changes are made, this manager is updated and when asked to save the data this data is dumped to file.
 *
 * @author adam
 */
public class CampaignDataManager implements DataAccessor {

    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(CampaignDataManager.class.getName());

    /** The name of the campaign.  */
    private String m_campaignName;

    /** A map of UUIDs to their associated Entities. */
    private final Map<UUID, Entity> m_entities;

    /** A map of UUIDs of Entities to Sets of Relationships. */
    private final Map<UUID, RelationshipManager> m_relationships;

    /** A map of UUIDs to their associated Timeline Entries. */
    private final Map<UUID, TimelineEntry> m_timelineData;

    /** The path to the file where the current campaign is stored, or null if no path exists. */
    private String m_filename;

    /** The currently configured campaign calendar. */
    private CampaignCalendar m_cal;

    /** A Set of listeners on the CDM. */
    private final Set<CampaignDataManagerListener> m_listeners;

    public CampaignDataManager() {
        m_campaignName = "New Campaign";
        m_filename = null;

        m_entities = new HashMap<>();
        m_relationships = new HashMap<>();
        m_timelineData = new HashMap<>();
        m_cal = new CampaignCalendar();
        m_listeners = new HashSet<>();
    }

    /**
     * Adds a listener to this data manager.
     * @param listener the listener to be added.
     */
    public void addListener(CampaignDataManagerListener listener) {
        if (listener != null) {
            m_listeners.add(listener);
        }
    }

    /**
     * Removes a listener from this data manager.
     * @param listener the listener to be removed.
     */
    public void removeListener(CampaignDataManagerListener listener) {
        m_listeners.remove(listener);
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

        // Alert Listeners
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataAddedOrUpdated(entity);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void addOrUpdateTimelineEntry(TimelineEntry entry) {
        if (entry != null) {
            m_timelineData.put(entry.getId(), entry);
        }
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.timelineEntryAddedOrUpdated(entry);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeTimelineEntry(UUID id) {
        if (id != null) {
            m_timelineData.remove(id);
        }
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.timelineEntryRemoved(id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeEntity(UUID id) {
        if (id != null) {
            // Remove the Entity
            m_entities.remove(id);

            // Remove relationships for the removed Entity
            m_relationships.remove(id);

            // Check for any relationships that point to the removed Entity and remove them too.
            for (UUID entityId : m_relationships.keySet()) {
                RelationshipManager relationshipManager = m_relationships.get(entityId);
                // Collect all of the relationships that point at the removed Entity
                Set<Relationship> toRemove = new HashSet<>();
                for (Relationship rel : relationshipManager.getAllRelationships()) {
                    if (rel.getRelatedEntity().equals(id)) {
                        toRemove.add(rel);
                    }
                }
                // Remove the bad relations, and set the updated Set back into the map.
                relationshipManager.removeAll(toRemove);
                m_relationships.put(entityId, relationshipManager);
            }
        }

        // Alert Listeners
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataRemoved(id);
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
                TimelineEntry updated = new TimelineEntry(entry.getTitle(), m_cal.getMonthForIndex(0), entry.getYear(), entry.isSecret(), entry.getAssociatedId(), entry.getId());
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
        return new Campaign(m_campaignName, new HashSet<>(m_entities.values()), m_relationships, new HashSet<>(m_timelineData.values()), m_cal);
    }

    /**
     * Clears all old data and sets the supplied campaign data on this display.
     * @param campaign the new data to set.
     */
    public void setData(Campaign campaign) {
        m_entities.clear();
        m_timelineData.clear();
        m_relationships.clear();
        
        // Alert listeners of cleared data.
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.clearAllData();
        }
        
        m_campaignName = campaign.getName();
        m_cal = campaign.getCalendar();


        // Set to collect all of the previously saved relationships.  This is used later to ensure that all established
        // Relationships are in the RelationshipOptionManager.
        Set<String> relationships = new HashSet<>();

        // Add all of the Entities.
        for (Entity e : campaign.getEntities()) {
            UUID entityId = e.getId();
            m_entities.put(entityId, e);

            // Create a RelationshipManager for all Entities in the Campaign
            m_relationships.put(entityId, new RelationshipManager());

            // Collect all of the previously saved relationships and add them to our Set above.
            RelationshipManager entityRelMgr = campaign.getRelationships(entityId);
            if (entityRelMgr != null) {
                for (Relationship r : entityRelMgr.getAllRelationships()) {
                    relationships.add(r.getRelationshipText());
                }
            }
        }

        // Add all of the Relationships.
        m_relationships.putAll(campaign.getAllRelationships());

        // Ensure that all of the relationships previously saved are in the local
        // relationships file, and indeed the Relationship Data Manager as well.
        RelationshipOptionManager.addRelationships(new ArrayList<>(relationships));

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

    /** {@inheritDoc} */
    @Override
    public CampaignCalendar getCalendar() {
        return m_cal;
    }

    /** {@inheritDoc} */
    @Override
    public void addRelationship(UUID entity, Relationship rel) {
        if (m_relationships.get(entity) == null) {
            m_relationships.put(entity, new RelationshipManager());
        }
        m_relationships.get(entity).addRelationship(rel);
    }

    /** {@inheritDoc} */
    @Override
    public void removeRelationship(UUID entity, Relationship toRemove) {
        RelationshipManager relationships = m_relationships.get(entity);
        relationships.remove(toRemove);
        
        // Alert Listeners, data updated because relationship removed
        Entity actualEntity = getEntity(entity);
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataAddedOrUpdated(actualEntity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public RelationshipManager getRelationshipsForEntity(UUID entity) {
        return m_relationships.get(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void addOrUpdateAllRelationships(UUID entity, RelationshipManager relMgr) {
        if (entity != null && relMgr != null) {
            m_relationships.put(entity, relMgr);
            
            // Alert Listeners, data updated because relationship added
            Entity actualEntity = getEntity(entity);
            for (CampaignDataManagerListener cdml : m_listeners) {
                cdml.dataAddedOrUpdated(actualEntity);
            }
        } else {
            LOGGER.warning("Attempted to store a null Entity or RelationshipManager.  Entity was:  " +
                    entity + ", RelationshipManager was:  " + relMgr);
        }
    }
}