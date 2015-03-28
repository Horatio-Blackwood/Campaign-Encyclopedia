package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityDataBuilder;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.UserDisplay;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * An action for deleting Entities.
 * @author adam
 */
public class DeleteEntityAction extends AbstractAction {

    private final Entity m_entity;
    private final CampaignDataManager m_cdm;
    private final UserDisplay m_display;
    private final Frame m_parent;

    /**
     * Creates a new DeleteEntityAction.
     * @param parent the parent display to use for positioning any dialogs launched by this display.
     * @param toDelete the Entity to delete.
     * @param cdm the CampaignDataManager to update.
     * @param display the UserDisplay to update.
     */
    public DeleteEntityAction(Frame parent, Entity toDelete, CampaignDataManager cdm, UserDisplay display) {
        super("Delete");
        m_entity = toDelete;
        m_cdm = cdm;
        m_display = display;
        m_parent = parent;
    }


    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UUID id = m_entity.getId();
                Entity toDelete = m_cdm.getEntity(id);
                List<Entity> toBeUpdated = new ArrayList<>();
                boolean updateRequired = false;

                String message = "Are you sure you want to delete '" + toDelete.getName() + "' from your campaign?";
                int result = JOptionPane.showConfirmDialog(m_parent, message, "Are you sure?", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    // Remove from backing data structures and display
                    m_cdm.removeEntity(id);
                    m_display.removeEntity(m_entity);
                    if (m_display.getShownEntity() != null && m_display.getShownEntity().equals(id)) {
                        m_display.clearDisplayedEntity();
                    }

                    // Figure out what other Entities are affected by this removal (those which have Relationships with
                    // the one being removed, for instance).
                    for (Entity entity : m_cdm.getAllEntities()) {
                        updateRequired = false;
                        EntityData publicData = entity.getPublicData();
                        EntityData secretData = entity.getSecretData();

                        Set<Relationship> pubRels = publicData.getRelationships();
                        Set<Relationship> secRels = secretData.getRelationships();

                        Set<Relationship> filteredPublicRels = filterRelationships(id, pubRels);
                        Set<Relationship> filteredSecretRels = filterRelationships(id, secRels);

                        // Check Public Data
                        if (pubRels.size() != filteredPublicRels.size()) {
                            updateRequired = true;
                        }
                        if (secRels.size() != filteredSecretRels.size()) {
                            updateRequired = true;
                        }

                        if (updateRequired) {
                            EntityData newPublic = new EntityDataBuilder(publicData).relationships(filteredPublicRels).build();
                            EntityData newSecret = new EntityDataBuilder(secretData).relationships(filteredSecretRels).build();
                            toBeUpdated.add(new Entity(entity.getId(), entity.getName(), entity.getType(), newPublic, newSecret, entity.isSecret()));
                        }
                    }

                    for (Entity entity : toBeUpdated) {
                        m_cdm.addOrUpdateEntity(entity);
                    }

                    // Then Save.
                    SaveHelper.autosave(m_parent, m_cdm, updateRequired);
                }
            }
        }).run();
    }

    /**
     * Filters the relationships.
     * @param entity the Entity ID of the entity being deleted.
     * @param relationships the relationships to check for links to the ID of the Entity being deleted.
     * @return all relationships that don't link up to the ID of the Entity being deleted.
     */
    private Set<Relationship> filterRelationships(UUID entity, Set<Relationship> relationships) {
        Set<Relationship> rels = new HashSet<>();
        for (Relationship r : relationships) {
            if (!r.getIdOfRelation().equals(entity)) {
                rels.add(r);
            }
        }
        return rels;
    }
}
