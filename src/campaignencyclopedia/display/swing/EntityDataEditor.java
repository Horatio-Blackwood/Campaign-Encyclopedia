package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.EntityData;
import toolbox.display.EditListener;

/**
 * A display for showing EntityData objects.
 * @author adam
 */
public class EntityDataEditor {

    /** A listener for edits made to this display. */
    private final EditListener m_editListener;

    /** True if this entity data display is to be for secret data. */
    private final boolean m_isSecret;

    /** An editor for this entity data's tags. */
    private TagsEditor m_tags;

    /** An editor for this entity data's description. */
    private DescriptionEditor m_description;

    /**
     * Creates a new EntityDataDisplay.
     * @param editListener an edit listener.
     * @param isSecret true if the entity data displayed on this instance is secrete entity data.
     */
    public EntityDataEditor(EditListener editListener, boolean isSecret) {
        m_editListener = editListener;
        m_isSecret = isSecret;
        initialize();
    }

    /**
     * Sets the EntityData on the EntityDisplay.
     * @param data the EntityData to set.
     */
    public void setEntityData(EntityData data) {
        m_description.setDescription(data.getDescription());
        m_tags.setTags(data.getTags());
    }

    /**
     * Returns the EntityData displayed.
     * @return the displayed EntityData.
     */
    public EntityData getEntityData() {
        return new EntityData(m_description.getDescription(), m_tags.getTags());
    }

    /**
     * Returns the description editor.
     * @return the description editor.
     */
    public DescriptionEditor getDescriptionEditor() {
        return m_description;
    }
    
    /**
     * Returns the tags editor.
     * @return the tags editor.
     */
    public TagsEditor getTagsEditor() {
        return m_tags;
    }    

    void clear() {
        m_description.clear();
        m_tags.clear();
    }

    private void initialize() {
        // inti displays
        if (m_isSecret) {
            m_description = new DescriptionEditor("Secret Description", m_editListener);
            m_tags = new TagsEditor("Secret Tags", m_editListener);
        } else {
            m_description = new DescriptionEditor("Description", m_editListener);
            m_tags = new TagsEditor("Tags", m_editListener);
        }
    }
}
