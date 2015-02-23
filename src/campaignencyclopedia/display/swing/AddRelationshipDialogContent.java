package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipType;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.UUID;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 *
 * @author adam
 */
public class AddRelationshipDialogContent implements DialogContent {
    
    private JPanel m_panel;
    private JComboBox<RelationshipType> m_typeSelector;
    private JComboBox<Entity> m_entitySelector;
    private final DataAccessor m_accessor;
    private EditListener m_editListener;
    
    public AddRelationshipDialogContent(DataAccessor accessor) {
        m_accessor = accessor;
        initialize();
    }
    
    /** Do necessary initialization work. */
    private void initialize() {
        m_panel = new JPanel();
        
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED && m_editListener != null) {
                    m_editListener.edited();
                }
            }
        };
        
        m_typeSelector = new JComboBox<>();
        m_typeSelector.setRenderer(new DisplayableCellRenderer());
        for (RelationshipType type : RelationshipType.values()) {
            m_typeSelector.addItem(type);
        }
        m_typeSelector.addItemListener(itemListener);
        m_typeSelector.setMaximumRowCount(20);
        
        m_entitySelector = new JComboBox<>();
        //m_entitySelector.setRenderer(new EntityIdReferenceCellRenderer(m_accessor));
        for (Entity entity : m_accessor.getAllEntities()) {
            m_entitySelector.addItem(entity);
        }
        m_entitySelector.addItemListener(itemListener);
        m_entitySelector.setMaximumRowCount(20);
        
        m_panel.add(m_typeSelector);
        m_panel.add(m_entitySelector);
    }
    
    /**
     * Returns the Relationship displayed based on the user's selections.
     * @return the Relationship displayed based on the user's selections.
     */
    public Relationship getDisplayedRelationship() {
        return new Relationship(((Entity)m_entitySelector.getSelectedItem()).getId(), (RelationshipType)m_typeSelector.getSelectedItem());
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