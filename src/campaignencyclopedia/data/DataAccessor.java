package campaignencyclopedia.data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * An interface that describes the methods that must be provided to access or update data.
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
     * Adds the supplied Entity to the system.  If an entity with a matching ID exists, that Entity is replaced by the
     * supplied one.
     * @param entity the Entity to add.
     */
    public void addOrUpdateEntity(Entity entity);

    /**
     * Removes the Entity associated with the supplied ID.
     * @param id the ID of the Entity to remove.
     */
    public void removeEntity(UUID id);

    /**
     * Called to add or update the supplied TimelineEntry in the data model.
     * @param entry the TimelineEntry to update.
     */
    public void addOrUpdateTimelineEntry(TimelineEntry entry);

    /**
     * Removes the TimelineEntry with the supplied ID.
     * @param id the ID of the TimelineEntry to remove.
     */
    public void removeTimelineEntry(UUID id);

    /**
     * Returns the TimelineEntry data for this Campaign.
     * @return the TimelineEntry data for this Campaign.
     */
    public Set<TimelineEntry> getTimelineData();

    /**
     * Returns the configured Campaign Calendar.
     * @return the configured Campaign Calendar.
     */
    public CampaignCalendar getCalendar();

    /**
     * Adds the supplied relationship.
     * @param entity the UUID of the Entity upon which to place this Relationship.
     * @param rel the Relationship to add.
     */
    public void addRelationship(UUID entity, Relationship rel);

    /**
     * Removes the Relationship associated with the supplied ID.
     * @param entity the UUID of the Entity to remove the Relationship.
     * @param toRemove the relationship to remove.
     */
    public void removeRelationship(UUID entity, Relationship toRemove);

    /**
     * Replaces the RelationshipManager for the supplied entity with the provided one.
     * @param entity the ID of the Entity to update the RelationshipManager for.
     * @param relMgr the new RelationshipManager.
     */
    public void addOrUpdateAllRelationships(UUID entity, RelationshipManager relMgr);

    /**
     * Returns the Relationships for the entity associated with the supplied ID.
     * @param entity the ID of the entity to get relationships for.
     * @return all of the Relationships for the Entity with the supplied ID.
     */
    public RelationshipManager getRelationshipsForEntity(UUID entity);
}
