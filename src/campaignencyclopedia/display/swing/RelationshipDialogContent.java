package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipOptionManager;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.UUID;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 * A dialog for adding a relationship between two entities.
 * @author adam
 */
public class RelationshipDialogContent implements DialogContent {

    /** The main content of this dialog content. */
    private JPanel m_panel;

    /** The relationship type selector. */
    private JComboBox<String> m_typeSelector;

    /** The Entity selector. */
    private JComboBox<Entity> m_entitySelector;

    /** A check box for selecting whether or not the relationship is secret. */
    private JCheckBox m_isSecretCheckbox;

    /** The Data Accessor for getting at any necessary data. */
    private final DataAccessor m_accessor;

    /** An edit listener. */
    private EditListener m_editListener;

    /** The UUID of the entity that owns this relationship. */
    private UUID m_entityId;

    /**
     * Creates a new AddRelationshipDialogContent.s
     * @param accessor the DataAccessor for getting at any needed data.
     * @param entity the UUID of the Entity that owns the relationship.
     */
    public RelationshipDialogContent(DataAccessor accessor, UUID entity) {
        if (accessor == null) {
            throw new IllegalArgumentException("Parameter 'accessor' cannot be null.");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null.");
        }
        m_accessor = accessor;
        m_entityId = entity;
        initialize();
    }

    /** Do necessary initialization work. */
    private void initialize() {
        // Initialize
        m_panel = new JPanel(new GridBagLayout());
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED && m_editListener != null) {
                    m_editListener.edited();
                    updateEnabledStateOfSecretCheckbox();
                }
            }
        };

        m_typeSelector = new JComboBox<>();
        for (String relationship : RelationshipOptionManager.getRelationships()) {
            m_typeSelector.addItem(relationship);
        }
        m_typeSelector.addItemListener(itemListener);
        m_typeSelector.setMaximumRowCount(20);

        m_entitySelector = new JComboBox<>();
        for (Entity entity : m_accessor.getAllEntities()) {
            m_entitySelector.addItem(entity);
        }
        m_entitySelector.addItemListener(itemListener);
        m_entitySelector.setMaximumRowCount(20);

        m_isSecretCheckbox = new JCheckBox("Is Secret");

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 2, 2, 2);
        m_panel.add(m_typeSelector, gbc);

        gbc.gridx = 1;
        m_panel.add(m_entitySelector, gbc);

        gbc.gridx = 2;
        m_panel.add(m_isSecretCheckbox, gbc);
    }

    /**
     * Returns the Relationship displayed based on the user's selections.
     * @return the Relationship displayed based on the user's selections.
     */
    public Relationship getDisplayedRelationship() {
        UUID relatedTo = ((Entity)m_entitySelector.getSelectedItem()).getId();
        String relationshipType = (String)m_typeSelector.getSelectedItem();
        return new Relationship(m_entityId, relatedTo, relationshipType, m_isSecretCheckbox.isSelected());
    }
    
    /**
     * Sets the supplied relationship on this display.
     * @param relationship the relationship to set.
     */
    public void setRelationship(Relationship relationship) {
        m_entityId = relationship.getEntityId();
        m_typeSelector.setSelectedItem(relationship.getRelationshipText());
        m_entitySelector.setSelectedItem(m_accessor.getEntity(relationship.getRelatedEntity()));
        m_isSecretCheckbox.setSelected(relationship.isSecret());
        updateEnabledStateOfSecretCheckbox();
    }
    
    /**
     * Checks to see if the secret checkbox should be enabled or disabled based on the selected entities.  It is not 
     * valid to have non-secret relationships between two entities where at least one of them is secret.
     */
    private void updateEnabledStateOfSecretCheckbox() {
        // Check state
        if (m_accessor.getEntity(m_entityId).isSecret() || m_accessor.getEntity(((Entity)m_entitySelector.getSelectedItem()).getId()).isSecret()) {
            m_isSecretCheckbox.setEnabled(false);
            m_isSecretCheckbox.setSelected(true);
            m_isSecretCheckbox.setToolTipText("One or both items in this relationship are secret, so this relationship must be.");
        } else {
            m_isSecretCheckbox.setEnabled(true);
            m_isSecretCheckbox.setToolTipText("");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Component getContent() {
        return m_panel;
    }

    /** {@inheritDoc} */
    @Override
    public void setDialogEditListener(EditListener el) {
        m_editListener = el;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataCommittable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return true;
    }
}