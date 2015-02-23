package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    public CampaignDataManager() {
        m_campaignName = "New Campaign";
        m_entities = new HashMap<>();
        m_timelineData = new HashMap<>();
        m_filename = null;
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
     * Creates and returns a Campaign that is represented by all of the data in the CampaignDataManager.  The CDM is
     * not modified in any way.  Each time this method is called, a new Campaign object is instantiated and returned.
     *
     * @return a Campaign that contains all of the data in the CampaignDataManager.
     */
    public Campaign getData() {
        return new Campaign(m_campaignName, new HashSet<>(m_entities.values()), new HashSet<>(m_timelineData.values()));
    }

    /**
     * Clears all old data and sets the supplied campaign data on this display.
     * @param campaign the new data to set.
     */
    public void setData(Campaign campaign) {
        m_entities.clear();
        m_timelineData.clear();
        m_campaignName = campaign.getName();
        for (Entity e : campaign.getEntities()) {
            m_entities.put(e.getId(), e);
        }
        for (TimelineEntry tle : campaign.getTimelineEntries()) {
            m_timelineData.put(tle.getId(), tle);
        }
    }

    public String getSaveFileName() {
        return m_filename;
    }

    public void setFileName(String filename) {
        if (filename != null && !filename.endsWith(".campaign")) {
            filename += ".campaign";
        }
        m_filename = filename;
    }

    @Override
    public Set<TimelineEntry> getTimelineData() {
        return new HashSet<>(m_timelineData.values());
    }
}