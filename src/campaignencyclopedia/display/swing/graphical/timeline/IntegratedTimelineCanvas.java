package campaignencyclopedia.display.swing.graphical.timeline;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.TimelineDialogContent;
import campaignencyclopedia.display.swing.graphical.CanvasDisplay;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import toolbox.display.EditListener;

/**
 *
 * @author adam
 */
public class IntegratedTimelineCanvas implements CanvasDisplay {

    private JPanel m_content;
    private final TimelineCanvas m_canvas;
    private final TimelineDialogContent m_timelineListDialogContent;
    private boolean m_showSecretEntries = true;
    
    public IntegratedTimelineCanvas(Frame parent, EntityDisplay display, CampaignDataManager cdm) {
        m_canvas = new TimelineCanvas(cdm);
        m_timelineListDialogContent = new TimelineDialogContent(parent, cdm.getTimelineData(), display, cdm);
        initialize();
    }
    
    private void initialize() {
        m_content = new JPanel(new BorderLayout());
        m_content.add(m_canvas.getComponent(), BorderLayout.CENTER);
        m_content.add(m_timelineListDialogContent.getContent(), BorderLayout.LINE_START);
        m_content.add(createControlPanel(), BorderLayout.PAGE_END);
        m_timelineListDialogContent.setDialogEditListener(new EditListener() {
            @Override
            public void edited() {
                if (m_timelineListDialogContent.isShowingSecretEntries() != m_showSecretEntries) {
                    m_showSecretEntries = m_timelineListDialogContent.isShowingSecretEntries();
                    m_canvas.showSecretEntries(m_showSecretEntries);
                }
            }
        });
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        final JComboBox<ZoomLevel> selector = new JComboBox<>(ZoomLevel.values());
        selector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    m_canvas.setZoomLevel((ZoomLevel)selector.getSelectedItem());
                }
            }
        });
        selector.setRenderer(new ListCellRenderer<ZoomLevel>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends ZoomLevel> jlist, ZoomLevel e, int i, boolean bln, boolean bln1) {
                return new JLabel(e.getDisplayName());
            }
        });
        selector.setSelectedItem(ZoomLevel.YEAR);
        
        JLabel label = new JLabel("Zoom Level:");
        label.setHorizontalAlignment(JLabel.RIGHT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selector, gbc);
        
        return panel;
    }
    
    @Override
    public Component getComponent() {
        return m_content;
    }

    @Override
    public void dataRemoved(UUID id) {
        m_canvas.dataRemoved(id);
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        m_canvas.dataAddedOrUpdated(entity);
    }

    @Override
    public void timelineEntryAddedOrUpdated(TimelineEntry tle) {
        m_canvas.timelineEntryAddedOrUpdated(tle);
    }

    @Override
    public void timelineEntryRemoved(UUID id) {
        m_canvas.timelineEntryRemoved(id);
    }
}