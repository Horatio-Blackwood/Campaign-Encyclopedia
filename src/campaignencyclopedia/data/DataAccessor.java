package campaignencyclopedia.data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author adam
 */
public interface DataAccessor {

    /**
     * Returns the entity associated with the supplied ID.  If no entity is associated with the supplied ID, or the ID
     * is null, null is returned.
     *
     * @param id the ID associated with the Entity desired.
     * @return the entity associated with the supplied ID or null.
     */
    public Entity getEntity(UUID id);

    /**
     * Returns all Entities available to the data accessor.
     * @return all Entities available to the data accessor.
     */
    public List<Entity> getAllEntities();


    /**
     * Returns the TimelineEntry data for this Campaign.
     * @return the TimelineEntry data for this Campaign.
     */
    public Set<TimelineEntry> getTimelineData();

    /**
     * Adds the supplied Entity to the system.  If an entity with a matching ID exists, that Entity is replaced by the
     * supplied one.
     * @param entity the Entity to add.
     */
    public void addOrUpdateEntity(Entity entity);


    public void addOrKUpdateTimelineEntry(TimelineEntry entry);

    public void removeTimelineEntry(UUID id);

    /**
     * Removes the Entity associated with the supplied ID.
     * @param id the ID of the Entity to remove.
     */
    public void removeEntity(UUID id);
}
