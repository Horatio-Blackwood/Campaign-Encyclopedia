package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.action.SaveHelper;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogContent;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * The dialog content for viewing the Timeline.
 * @author adam
 */
public class TimelineDialogContent implements DialogContent {

    /** */
    private Frame m_parent;

    /** The main content of the dialog */
    private JPanel m_content;

    private JList<TimelineEntry> m_eventList;

    private SortableListModel<TimelineEntry> m_listModel;

    /** The data accessor to fetch data for display and update with new data. */
    private CampaignDataManager m_cdm;

    /** A display for showing entities. */
    private EntityDisplay m_display;

    public TimelineDialogContent(Frame parent, Set<TimelineEntry> entries, EntityDisplay display, CampaignDataManager cdm) {
        if (entries == null) {
            throw new IllegalArgumentException("Parameter 'events' cannot be null.");
        }
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' cannot be null.");
        }
        m_parent = parent;
        m_listModel = new SortableListModel<>();
        m_listModel.addAllElements(entries);
        m_cdm = cdm;
        m_display = display;
        initialize();
    }

    private void initialize() {
        m_content = new JPanel(new GridBagLayout());
        m_content.setPreferredSize(new Dimension(400, 500));
        m_eventList = new JList<>();
        m_eventList.setModel(m_listModel);
        m_eventList.setCellRenderer(new TimelineEventCellRenderer(m_cdm));
        
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
                if (me.getClickCount() > 1 && selectedIndex >= 0) {
                    showEntity.run();
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
                final NewTimelineEntryDialogContent dc = new NewTimelineEntryDialogContent(m_cdm);

                Runnable commit = new Runnable() {
                    @Override
                    public void run() {
                        TimelineEntry data = dc.getData();
                        m_listModel.addElement(data);
                        m_cdm.addOrKUpdateTimelineEntry(data);
                        SaveHelper.autosave(m_parent, m_cdm, true);
                    }
                };

                DialogCommitManager dcm = new OkCancelCommitManager(commit);
                DialogFactory.buildDialog(m_parent, "Add Timeline Entry", false, dc, dcm);
            }
        });
        m_content.add(addButton, gbc);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                TimelineEntry tle = m_eventList.getSelectedValue();
                if (tle != null) {
                    m_listModel.removeElement(tle);
                    m_cdm.removeTimelineEntry(tle.getId());
                }
                SaveHelper.autosave(m_parent, m_cdm, true);
            }
        });
        gbc.gridx = 2;
        m_content.add(removeButton, gbc);



        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0f;
        gbc.weighty = 1.0f;
        gbc.fill = GridBagConstraints.BOTH;
        m_content.add(new JScrollPane(m_eventList), gbc);
    }

    /** {@inheritDoc} */
    @Override
    public Component getContent() {
        return m_content;
    }

    /** {@inheritDoc} */
    @Override
    public void setDialogEditListener(EditListener el) {
        // Do nothing.
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