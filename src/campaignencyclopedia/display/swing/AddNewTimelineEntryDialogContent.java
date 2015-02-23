package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Season;
import campaignencyclopedia.data.TimelineEntry;
import core.display.text.LimitedLengthIntegerDocument;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 *
 * @author adam
 */
public class AddNewTimelineEntryDialogContent implements DialogContent {

    private final CampaignDataManager m_cdm;

    private JPanel m_content;
    private JTextField m_titleField;
    private JComboBox<Season> m_seasonSelector;
    private JTextField m_yearField;
    private JComboBox<Entity> m_entitySelector;

    private EditListener m_editListener;

    public AddNewTimelineEntryDialogContent(CampaignDataManager cdm) {
        m_cdm = cdm;
        initailize();
    }

    private void edited() {
        if (m_editListener != null) {
            m_editListener.edited();
        }
    }

    private void initailize() {
        // Build 'em Up
        m_content = new JPanel(new GridBagLayout());
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                edited();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                edited();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                edited();
            }
        };
        m_titleField = new JTextField(25);
        m_titleField.getDocument().addDocumentListener(dl);
        m_yearField = new JTextField(15);
        m_yearField.setDocument(new LimitedLengthIntegerDocument(8));
        m_yearField.getDocument().addDocumentListener(dl);

        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                edited();
            }
        };

        m_seasonSelector = new JComboBox<>();
        m_seasonSelector.addItem(Season.UNSET);
        m_seasonSelector.addItem(Season.SPRING);
        m_seasonSelector.addItem(Season.SUMMER);
        m_seasonSelector.addItem(Season.FALL);
        m_seasonSelector.addItem(Season.WINTER);
        m_seasonSelector.setSelectedItem(Season.UNSET);
        m_seasonSelector.addItemListener(itemListener);
        m_seasonSelector.setRenderer(new ListCellRenderer<Season>(){
            @Override
            public Component getListCellRendererComponent(JList<? extends Season> jlist, Season e, int i, boolean bln, boolean bln1) {
                return new JLabel(e.getDisplayString());
            }
        });

        m_entitySelector = new JComboBox<>();
        for (Entity e : m_cdm.getAllEntities()) {
            m_entitySelector.addItem(e);
        }
        m_entitySelector.addItemListener(itemListener);

        // Lay 'em Out
        // Column One - Labels
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        JLabel title = new JLabel("Title:");
        title.setHorizontalAlignment(JLabel.LEFT);
        m_content.add(title, gbc);

        gbc.gridy = 1;
        JLabel season = new JLabel("Season:");
        season.setHorizontalAlignment(JLabel.LEFT);
        m_content.add(season, gbc);

        gbc.gridy = 2;
        JLabel year = new JLabel("Year:");
        year.setHorizontalAlignment(JLabel.LEFT);
        m_content.add(year, gbc);

        gbc.gridy = 3;
        JLabel linkTo = new JLabel("Link To:");
        linkTo.setHorizontalAlignment(JLabel.LEFT);
        m_content.add(linkTo, gbc);

        // Column Two - Fields
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0f;
        m_content.add(m_titleField, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0f;
        m_content.add(m_seasonSelector, gbc);

        gbc.gridy = 2;
        m_content.add(m_yearField, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0f;
        m_content.add(m_entitySelector, gbc);

        // Column Three - Optional Label
        gbc.gridx = 3;
        gbc.gridy = 0;
        m_content.add(new JLabel("(optional)"), gbc);
    }

    public TimelineEntry getData() {
        return new TimelineEntry(m_titleField.getText(),
                                 (Season)m_seasonSelector.getSelectedItem(),
                                 Integer.valueOf(m_yearField.getText()),
                                 ((Entity)m_entitySelector.getSelectedItem()).getId());
    }

    @Override
    public Component getContent() {
        return m_content;
    }

    @Override
    public void setDialogEditListener(EditListener el) {
        m_editListener = el;
    }

    @Override
    public boolean isDataCommittable() {
        if (m_yearField.getText().isEmpty()) {
            return false;
        }
        if (m_entitySelector.getSelectedItem() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCommitPermitted() {
        return isDataCommittable();
    }
}