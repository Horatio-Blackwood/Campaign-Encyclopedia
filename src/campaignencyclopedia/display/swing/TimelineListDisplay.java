package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.DataFilter;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.action.SaveHelper;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * A display that shows TimelineEntry objects in a list, and provides for editing them.
 * @author adam
 */
public class TimelineListDisplay {

    /** The parent dialog of this dialog content.  Used to position dialogs launched by user actions taken in this display. */
    private Frame m_parent;

    /** The main content of the dialog */
    private JPanel m_content;

    /** The list of TimelineEvents.  */
    private JList<TimelineEntry> m_eventList;

    /** The backing model for the TimelineEvent list. */
    private SortableListModel<TimelineEntry> m_listModel;
    
    /** A check box for hiding secret timeline entries. */
    private JCheckBox m_hideSecretEntriesCheckbox;

    /** The data accessor to fetch data for display and update with new data. */
    private CampaignDataManager m_cdm;

    /** A display for showing entities. */
    private EntityDisplay m_display;
    
    /** An edit listener. */
    private EditListener m_ediListener;

    public TimelineListDisplay(Set<TimelineEntry> entries, EntityDisplay display, CampaignDataManager cdm) {
        if (entries == null) {
            throw new IllegalArgumentException("Parameter 'events' cannot be null.");
        }
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' cannot be null.");
        }

        m_listModel = new SortableListModel<>();
        m_listModel.addAllElements(entries);
        m_cdm = cdm;
        m_display = display;
        initialize();
    }

    private void initialize() {
        m_content = new JPanel(new GridBagLayout());
        
        m_eventList = new JList<>();
        m_eventList.setModel(m_listModel);
        m_eventList.setCellRenderer(new TimelineEventCellRenderer(m_cdm));
        
        m_hideSecretEntriesCheckbox = new JCheckBox("Hide Secret Items");
        m_hideSecretEntriesCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (m_hideSecretEntriesCheckbox.isSelected()) {
                    m_listModel.setFilter(new DataFilter<TimelineEntry>() {
                        @Override
                        public boolean accept(TimelineEntry item) {
                            return (!item.isSecret());
                        }
                    });                    
                } else {
                    m_listModel.setFilter(null);
                }
                alertEditListener();
            }
        });
        
        // Define reusable showEntity runnable (for both key and mouse listeners)
        final Runnable showEntity = new Runnable() {
            @Override
            public void run() {
                TimelineEntry selected = m_listModel.getElementAt(m_eventList.getSelectedIndex());
                if (selected != null) {
                    m_display.showEntity(selected.getAssociatedId());
                }
            }
        };
        
        m_eventList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                m_eventList.setSelectedIndex(m_eventList.locationToIndex(me.getPoint()));
                int selectedIndex = m_eventList.getSelectedIndex();
                
                // Double Click - navigate to associated entity to edit
                if (me.getClickCount() > 1 && selectedIndex >= 0) {
                    showEntity.run();
                } else if (SwingUtilities.isRightMouseButton(me)) {
                    TimelineEntry entry = m_listModel.getElementAt(selectedIndex);
                    JPopupMenu menu = getTimelineEntryContextMenu(entry);
                    menu.show(m_eventList, me.getX(), me.getY());
                }
            }
        });
        
        // Setup Key Listener
        m_eventList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
                // Ignored
            }
            @Override
            public void keyPressed(KeyEvent ke) {
                int selectedIndex = m_eventList.getSelectedIndex();
                if (ke.getKeyChar() == KeyEvent.VK_ENTER && selectedIndex >= 0) {
                    showEntity.run();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {
                // Ignored
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(3, 3, 3, 3);
        JLabel message = new JLabel("Significant Campaign Events");
        message.setHorizontalAlignment(JLabel.LEFT);
        m_content.add(message, gbc);

        gbc.weightx = 0.0f;
        gbc.gridx = 1;
        JButton addButton = new JButton("Add Item");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final TimelineEntryDialogContent dc = new TimelineEntryDialogContent(m_cdm);
                Runnable commit = new Runnable() {
                    @Override
                    public void run() {
                        TimelineEntry data = dc.getData();
                        m_listModel.addElement(data);
                        m_cdm.addOrUpdateTimelineEntry(data);
                        SaveHelper.autosave(m_parent, m_cdm, true);
                    }
                };
                DialogCommitManager dcm = new OkCancelCommitManager(commit);
                DialogFactory.buildDialog(m_parent, "New Timeline Entry", false, dc, dcm);
            }
        });
        m_content.add(addButton, gbc);
        
        // Hide Secrets
        gbc.gridx = 2;
        m_content.add(m_hideSecretEntriesCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0f;
        gbc.weighty = 1.0f;
        gbc.fill = GridBagConstraints.BOTH;
        m_content.add(new JScrollPane(m_eventList), gbc);
    }
    
    /**
     * Sets the parent for this display.
     * @param parent the new parent window for this display.
     */
    public void setParent(Frame parent) {
        m_parent = parent;
    }    
    
    public JPopupMenu getTimelineEntryContextMenu(final TimelineEntry entry) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                TimelineEntry tle = m_eventList.getSelectedValue();
                if (tle != null) {
                    String msg = "Are you sure?";
                    if (tle.getTitle() != null && !tle.getTitle().isEmpty()) {
                        msg = "Are you sure you want to remove '" + tle.getTitle() + "' from your campaign?";
                    }
                    int result = JOptionPane.showConfirmDialog(m_parent, msg, "Remove Timeline Entry", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        m_listModel.removeElement(tle);
                        m_cdm.removeTimelineEntry(tle.getId());
                        SaveHelper.autosave(m_parent, m_cdm, true);
                        alertEditListener();
                    }
                }
            }
        });
        
        menu.add(new AbstractAction("Edit") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final TimelineEntryDialogContent dc = new TimelineEntryDialogContent(m_cdm);
                dc.setData(entry);
                Runnable commit = new Runnable() {
                    @Override
                    public void run() {
                        TimelineEntry data = dc.getData();
                        
                        // Remove the existing entry.
                        List<TimelineEntry> allEntries = m_listModel.getAllElements();
                        for (TimelineEntry tle : allEntries) {
                            if (tle.getId().equals(data.getId())) {
                                m_listModel.removeElement(tle);
                                break;
                            }
                        }
                        
                        // Add the new one to the display and the data manager.
                        m_listModel.addElement(data);
                        m_cdm.addOrUpdateTimelineEntry(data);
                        
                        // Save changes
                        SaveHelper.autosave(m_parent, m_cdm, true);
                        alertEditListener();
                    }
                };

                DialogCommitManager dcm = new OkCancelCommitManager(commit);
                DialogFactory.buildDialog(m_parent, "Edit Timeline Entry", false, dc, dcm);
            }
        });
        return menu;
    }

    /**
     * Returns the primary display component of this class.
     * @return the primary display component of this class.
     */
    public Component getContent() {
        return m_content;
    }

    /**
     * Sets an edit listener on this display.
     * @param el the edit listener to set.
     */
    public void setDialogEditListener(EditListener el) {
        m_ediListener = el;
    }

    /** If not null, this call alerts the edit listener of changes. */
    private void alertEditListener() {
        if (m_ediListener != null) {
            m_ediListener.edited();
        }
    }
    
    /**
     * Returns true if the dialog is currently showing secret TimelineEntries.
     * @return true if the dialog is currently showing secret TimelineEntries, false otherwise.
     */
    public boolean isShowingSecretEntries() {
        return !m_hideSecretEntriesCheckbox.isSelected();
    }
}