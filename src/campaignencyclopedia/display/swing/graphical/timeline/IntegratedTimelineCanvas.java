package campaignencyclopedia.display.swing.graphical.timeline;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.TimelineListDisplay;
import campaignencyclopedia.display.swing.graphical.CanvasDisplay;
import campaignencyclopedia.display.swing.graphical.ScreenshotToolbox;
import core.display.text.LimitedLengthIntegerDocument;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import toolbox.display.EditListener;

/**
 * 
 * @author adam
 */
public class IntegratedTimelineCanvas implements CanvasDisplay {

    private JPanel m_content;
    private final TimelineCanvas m_canvas;
    private final TimelineListDisplay m_timelineListDialogContent;
    private boolean m_showSecretEntries = true;
    private JTextField m_earliestYear;
    private JTextField m_latestYear;
    
    private static final Logger LOGGER = Logger.getLogger(IntegratedTimelineCanvas.class.getName());
    
    public IntegratedTimelineCanvas(EntityDisplay display, CampaignDataManager cdm) {
        m_canvas = new TimelineCanvas(cdm);
        m_timelineListDialogContent = new TimelineListDisplay(cdm.getTimelineData(), display, cdm);
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
        
        // Screenshot Button
        JButton screenshotButton = new JButton("Save Screenshot");
        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                BufferedImage screenshot = ScreenshotToolbox.getScreenShot(m_canvas);
                // Save the image
                JFileChooser chooser = new JFileChooser("./screenshots");
                int result = chooser.showSaveDialog(m_content);
                chooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        ImageIO.write(screenshot, "png", chooser.getSelectedFile());
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Error writing screenshot to file:  " + chooser.getSelectedFile(), ex);
                    }
                }
            }
        });

        // Year Range Filtering
        m_earliestYear = new JTextField(8);
        m_earliestYear.setDocument(new LimitedLengthIntegerDocument(6));
        m_latestYear = new JTextField(8);
        m_latestYear.setDocument(new LimitedLengthIntegerDocument(6));
        DocumentListener yearDocListener = new DocumentListener() {
            void setYearRange() {
                int earliest = Integer.MIN_VALUE;
                int latest = Integer.MAX_VALUE;
                if (!m_earliestYear.getText().isEmpty()) {
                    earliest = Integer.valueOf(m_earliestYear.getText());
                }
                if (!m_latestYear.getText().isEmpty()) {
                    latest = Integer.valueOf(m_latestYear.getText());
                }                
                m_canvas.setYearRange(earliest, latest);
            }
            
            @Override
            public void insertUpdate(DocumentEvent de) {
                setYearRange();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                setYearRange();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                setYearRange();
            }
        };
        m_earliestYear.getDocument().addDocumentListener(yearDocListener);
        m_latestYear.getDocument().addDocumentListener(yearDocListener);
        m_earliestYear.setText("0");
        m_latestYear.setText("999999");
        
        
        // Zoom Selector
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
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(3, 3, 3, 3);
        JLabel label = new JLabel("Earliest Year:");
        label.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(m_earliestYear, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Latest  Year:"), gbc);
        
        gbc.gridx = 3;
        panel.add(m_latestYear, gbc);
        
        gbc.gridx = 4;
        panel.add(new JLabel("Zoom Level:"), gbc);
        
        gbc.gridx = 5;
        panel.add(selector, gbc);
        
        gbc.gridx = 6;
        panel.add(screenshotButton, gbc);
        
        return panel;
    }
    
    @Override
    public JComponent getComponent() {
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
    
    public void setParent(Frame parent) {
        m_timelineListDialogContent.setParent(parent);
    }

    @Override
    public void clearAllData() {
        m_canvas.clearAllData();
    }
}