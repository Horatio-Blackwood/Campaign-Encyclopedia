package campaignencyclopedia.data;

import java.util.UUID;

/**
 * A listener for the Campaign Data Manager.
 * @author adam
 */
public interface CampaignDataManagerListener {

    /**
     * Called when data was removed.
     * @param id the ID of the removed values.
     */
    public void dataRemoved(UUID id);

    /**
     * Called when data is added or updated.
     * @param entity the Entity that was added or removed.
     */
    public void dataAddedOrUpdated(Entity entity);
    
    /**
     * Called when data is added or updated.
     * @param tle the TimelineEntry that was added or removed.
     */    
    public void timelineEntryAddedOrUpdated(TimelineEntry tle);
    
    /**
     * Called when a timeline entry is removed.
     * @param id the id of the removed entry.
     */
    public void timelineEntryRemoved(UUID id);
    
    /** Called when all data from the campaign is cleared. */
    public void clearAllData();
}
