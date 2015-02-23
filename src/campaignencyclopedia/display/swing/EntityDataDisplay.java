package campaignencyclopedia.display.swing;

import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.EntityData;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import toolbox.display.EditListener;

/**
 * A display for showing EntityData objects.
 * @author adam
 */
public class EntityDataDisplay {

    /** A DataAccessor for requesting or supplying data to/from the rest of the system. */
    private final DataAccessor m_dataAccessor;
    
    /** A display for showing Entities.*/
    private final EntityDisplay m_display;
    
    /** A listener for edits made to this display. */
    private final EditListener m_editListener;
    
    
    
    private final boolean m_isSecret;

    private JPanel m_content;
    private RelationshipEditor m_relationships;
    private TagsEditor m_tags;
    private DescriptionEditor m_description;
    private final Frame m_parent;


    public EntityDataDisplay(Frame parent, DataAccessor accessor, EntityDisplay display, EditListener editListener, boolean isSecret) {
        m_parent = parent;
        m_dataAccessor = accessor;
        m_display = display;
        m_editListener = editListener;
        m_isSecret = isSecret;
        initialize();
    }

    public void setData(EntityData data) {
        m_description.setDescription(data.getDescription());
        m_tags.setTags(data.getTags());
        m_relationships.setData(data.getRelationships());
    }

    public EntityData getData() {
        return new EntityData(m_description.getDescription(), m_tags.getTags(), m_relationships.getData());
    }

    public Component getDisplayComponent() {
        return m_content;
    }
    
    void clear() {
        m_description.clear();
        m_tags.clear();
        m_relationships.clearData();
    }

    private void initialize() {
        // inti displays
        m_content = new JPanel(new GridBagLayout());
        if (m_isSecret) {
            m_description = new DescriptionEditor("Secret Description", m_editListener);
            m_tags = new TagsEditor("Secret Tags", m_editListener);
            m_relationships = new RelationshipEditor(m_parent, m_dataAccessor, m_display, "Secret Relationships", m_editListener);
        } else {
            m_description = new DescriptionEditor("Description", m_editListener);
            m_tags = new TagsEditor("Tags", m_editListener);
            m_relationships = new RelationshipEditor(m_parent, m_dataAccessor, m_display, "Relationships", m_editListener);
        }

        // Layout display
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        m_content.add(m_description.getTitle(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0f;
        gbc.weighty = 1.0f;
        gbc.gridwidth = 3;
        JScrollPane descriptionScrollPane = new JScrollPane(m_description.getDescriptionComponent());
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_content.add(descriptionScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.0f;
        gbc.weightx = 0.0f;
        m_content.add(m_relationships.getTitle(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0f;
        gbc.weighty = 0.2f;
        gbc.gridwidth = 2;
        JScrollPane relationShipScrollPane = new JScrollPane(m_relationships.getEditorComponent());
        m_content.add(relationShipScrollPane, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0f;
        gbc.weighty = 0.0f;
        gbc.gridwidth = 1;
        m_content.add(m_relationships.getAddRelationshipButton(), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        m_content.add(m_relationships.getDeleteRelationshipButton(), gbc);

        // Column Two, tags
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0f;
        gbc.weighty = 0.0f;
        m_content.add(m_tags.getTitle(), gbc);

        gbc.gridy = 6;
        gbc.weightx = 1.0f;
        gbc.weighty = 0.2f;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        JScrollPane tagScrollPane = new JScrollPane(m_tags.getEditorComponent());
        tagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_content.add(tagScrollPane, gbc);
    }
}
