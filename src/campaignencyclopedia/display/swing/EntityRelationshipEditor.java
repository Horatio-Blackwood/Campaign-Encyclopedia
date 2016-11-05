package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.ComparisonTools;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * An editor for applying relationship to Entities.
 * @author adam
 */
public class EntityRelationshipEditor {
    /** The title label of this editor. */
    private final JLabel m_label;

    /** Display for relationships associated with a given Entity. */
    private final JList<Relationship> m_list;

    /** The Add Relationship button. */
    private final JButton m_addButton;

    /** The List Model that backs the JList. */
    private final SortableListModel<Relationship> m_model;

    private final DataAccessor m_accessor;
    private final EditListener m_editListener;
    private final EntityDisplay m_entityDisplay;
    
    private final Comparator<Relationship> m_comparator;

    private Frame m_parent;

    /**
     * Creates a new instance of EntityRelationshipEditor.
     * @param parent a parent Frame to center dialogs launched by one of the buttons of this display.
     * @param accessor a data accessor for fetching required information for relationship editing.
     * @param display an EntityDisplay for showing entities if the user chooses to traverse one of the relationships.
     * @param title the title to display for this Relationship Editor.
     * @param editListener an edit listener to alert of changes made to this editor.
     */
    public EntityRelationshipEditor(Frame parent, DataAccessor accessor, EntityDisplay display, String title, EditListener editListener) {
        m_accessor = accessor;
        m_editListener = editListener;
        m_entityDisplay = display;
        m_parent = parent;
        m_label = new JLabel(title);
        
        m_comparator =  new Comparator<Relationship>() {
            @Override
            public int compare(Relationship relationship, Relationship otherRelationship) {
                if (relationship.compareTo(otherRelationship) == 0) {
                    String relationshipName = ComparisonTools.trimForSort(m_accessor.getEntity(relationship.getRelatedEntity()).getName());
                    String otherName = ComparisonTools.trimForSort(m_accessor.getEntity(otherRelationship.getRelatedEntity()).getName());
                    return (relationshipName.compareTo(otherName));
                } else {
                    return relationship.compareTo(otherRelationship);
                }
            }
        };

        m_addButton = new JButton("Add Relationship");
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (m_entityDisplay.getShownEntity() != null) {
                    List<Entity> entities = m_accessor.getAllEntities();
                    if (entities.size() > 0) {
                        final RelationshipDialogContent dc = new RelationshipDialogContent(m_accessor, m_entityDisplay.getShownEntity());
                        Runnable commit = new Runnable() {
                            @Override
                            public void run() {
                                Relationship rel = dc.getDisplayedRelationship();
                                if (!m_model.contains(rel)) {
                                    m_model.addElement(rel);
                                }
                            }
                        };
                        DialogFactory.buildDialog(m_parent, "Add Relationship", true, dc, new OkCancelCommitManager(commit));
                    } else {
                        JOptionPane.showMessageDialog(m_parent, "No entities exist in this campaign.", "No Entities to Relate To", JOptionPane.OK_OPTION);
                    }
                } else {
                    JOptionPane.showMessageDialog(m_parent, "Current item must be saved before adding relationships.", "Current Item Must be Saved", JOptionPane.OK_OPTION);
                }
            }
        });

        // Initialize the list model.
        m_model = new SortableListModel<>(m_comparator);
        m_model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent lde) {
                m_editListener.edited();
            }
            @Override
            public void intervalRemoved(ListDataEvent lde) {
                m_editListener.edited();
            }
            @Override
            public void contentsChanged(ListDataEvent lde) {
                m_editListener.edited();
            }
        });
        
        // Initialize the JList.
        m_list = new JList<>();
        m_list.setCellRenderer(new RelationshipCellRenderer(accessor));
        m_list.setModel(m_model);
        m_list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                m_list.setSelectedIndex(m_list.locationToIndex(me.getPoint()));
                int selectedIndex = m_list.getSelectedIndex();
                
                // Double Click to Navigate
                if (me.getClickCount() > 1) {
                    Relationship selected = m_model.getElementAt(selectedIndex);
                    if (selected != null) {
                        m_entityDisplay.showEntity(selected.getRelatedEntity());
                    }
                 
                // Right-Click, Context Menu
                } else if (SwingUtilities.isRightMouseButton(me)) {
                    if (selectedIndex >= 0) {
                        // Selected Relationship
                        final Relationship relationship = m_model.getElementAt(m_list.getSelectedIndex());
                        
                        // Create Menu
                        JPopupMenu menu = new JPopupMenu();
                        
                        // Make Public / Make Secret
                        if (relationship.isSecret() && 
                                !m_accessor.getEntity(relationship.getEntityId()).isSecret() && 
                                !m_accessor.getEntity(relationship.getRelatedEntity()).isSecret()) {
                            menu.add(new AbstractAction("Make Public") {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    m_model.removeElement(relationship);
                                    m_model.addElement(new Relationship(relationship.getEntityId(), relationship.getRelatedEntity(), relationship.getRelationshipText(), false));
                                }
                            });
                        } else if (!relationship.isSecret()) {
                            menu.add(new AbstractAction("Make Secret") {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    m_model.removeElement(relationship);
                                    m_model.addElement(new Relationship(relationship.getEntityId(), relationship.getRelatedEntity(), relationship.getRelationshipText(), true));
                                }
                            });                            
                        }
                        
                        // Edit Action
                        menu.add(new AbstractAction("Edit") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                if (m_entityDisplay.getShownEntity() != null) {
                                    List<Entity> entities = m_accessor.getAllEntities();
                                    if (entities.size() > 0) {
                                        // Create Dialog
                                        final RelationshipDialogContent dc = new RelationshipDialogContent(m_accessor, m_entityDisplay.getShownEntity());
                                        dc.setRelationship(relationship);
                                        
                                        // OK Runnable
                                        Runnable commit = new Runnable() {
                                            @Override
                                            public void run() {
                                                m_model.removeElement(relationship);
                                                m_model.addElement(dc.getDisplayedRelationship());
                                            }
                                        };
                                        
                                        // Launch Dialog
                                        DialogFactory.buildDialog(m_parent, "Edit Relationship", true, dc, new OkCancelCommitManager(commit));
                                    } else {
                                        JOptionPane.showMessageDialog(m_parent, "No entities exist in this campaign.", "No Entities to Relate To", JOptionPane.OK_OPTION);
                                    }
                                }
                            }
                        });
                        
                        // Remove Action
                        menu.add(new AbstractAction("Delete") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                m_model.removeElement(relationship);
                            }
                        });
                        
                        // Show the context menu
                        menu.show(m_list, me.getX(), me.getY());
                    }
                }
            }
        });
    }

    public void setData(Set<Relationship> relationships) {
        m_model.clear();
        for (Relationship rel : relationships) {
            m_model.addElement(rel);
        }
    }

    /** 
     * Adds the supplied Relationship to this Editor.
     * @param rel the Relationship to add.
     */
    public void addRelationship(Relationship rel) {
        m_model.addElement(rel);
    }

    /**
     * Returns the Relationship data displayed in this Editor.
     * @return the Relationship data displayed in this Editor.
     */
    public Set<Relationship> getData() {
        return new HashSet<>(m_model.getAllElements());
    }

    /**
     * Returns the Title Component of this editor.
     * @return the Title Component of this editor.
     */
    public Component getTitle() {
        return m_label;
    }

    public Component getEditorComponent() {
        return m_list;
    }

    /**
     * Returns the add relationship button.
     * @return the add relationship button.
     */
    public Component getAddRelationshipButton() {
        return m_addButton;
    }

    /** Clears the data from this display. */
    void clearData() {
        m_model.clear();
    }
}
