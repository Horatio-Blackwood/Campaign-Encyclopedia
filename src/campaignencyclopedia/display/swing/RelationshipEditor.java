package campaignencyclopedia.display.swing;

import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 *
 * @author adam
 */
public class RelationshipEditor {
    /** The title label of this editor. */
    private final JLabel m_label;
    private final JList<Relationship> m_list;

    /** The Add Relationship button. */
    private final JButton m_addButton;

    /** The Delete Relationship button. */
    private final JButton m_deleteButton;

    /** The List Model that backs the JList. */
    private final SortableListModel<Relationship> m_model;

    private final DataAccessor m_accessor;
    private final EditListener m_editListener;
    private final EntityDisplay m_entityDisplay;

    private Frame m_parent;

    public RelationshipEditor(Frame parent, DataAccessor accessor, EntityDisplay display, String title, EditListener editListener) {
        m_accessor = accessor;
        m_editListener = editListener;
        m_entityDisplay = display;
        m_parent = parent;

        m_label = new JLabel(title);

        m_addButton = new JButton("Add Relationship");
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                List<Entity> entities = m_accessor.getAllEntities();
                if (entities.size() > 0) {
                    final AddRelationshipDialogContent dc = new AddRelationshipDialogContent(m_accessor);
                    Runnable commit = new Runnable() {
                        @Override
                        public void run() {
                            m_model.addElement(dc.getDisplayedRelationship());
                        }
                    };

                    Runnable cancel = new Runnable() {
                        @Override
                        public void run() {
                            // No-op
                        }
                    };

                    DialogFactory.buildDialog(m_parent, "Add Relationship", true, dc, new OkCancelCommitManager(commit, cancel));
                } else {
                    JOptionPane.showMessageDialog(m_parent, "No entities exist in this campaign.", "No Entities to Relate To", JOptionPane.OK_OPTION);
                }

            }
        });

        // Init the delete button.
        m_deleteButton = new JButton("Delete Relationship");
        m_deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Relationship rel = m_model.getElementAt(m_list.getSelectedIndex());
                m_model.removeElement(rel);
            }
        });

        m_model = new SortableListModel<>();
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
        m_list = new JList<>();
        m_list.setCellRenderer(new RelationshipCellRenderer(accessor));
        m_list.setModel(m_model);
        m_list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() > 1) {
                    Relationship selected = m_model.getElementAt(m_list.getSelectedIndex());
                    if (selected != null) {
                        m_entityDisplay.showEntity(selected.getIdOfRelation());
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

    public Set<Relationship> getData() {
        return new HashSet<>(m_model.getAllElements());
    }

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

    /**
     * Returns the delete relationship button.
     * @return the delete relationship button.
     */
    public Component getDeleteRelationshipButton() {
        return m_deleteButton;
    }

    /** Clears the data from this display. */
    void clearData() {
        m_model.clear();
    }
}
