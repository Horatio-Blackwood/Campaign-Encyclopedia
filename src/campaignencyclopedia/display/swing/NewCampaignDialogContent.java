package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 * A dialog content for creating a new Campaign.
 * @author adam
 */
public class NewCampaignDialogContent implements DialogContent {

    /** The primary content panel of this DialogContent. */
    private JPanel m_content;

    /** The name editor field. */
    private JTextField m_nameField;

    /** A dialog edit listener. */
    private EditListener m_editListener;

    /** Constructor */
    public NewCampaignDialogContent() {
        initialize();
    }

    /**
     * Returns the new campaign.
     * @return the new campaign.
     */
    public Campaign getCampaign() {
        return new Campaign(m_nameField.getText(),
                            new HashSet<Entity>(),
                            new HashMap<UUID, RelationshipManager>(),
                            new HashSet<TimelineEntry>(),
                            new CampaignCalendar());
    }

    /** Initialize the display components of this DialogContent. */
    private void initialize() {
        m_content = new JPanel(new BorderLayout());
        m_nameField = new JTextField(15);
        m_nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                alertListener();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                alertListener();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                alertListener();
            }
        });

        m_content.add(new JLabel("Campaign Name:"), BorderLayout.LINE_START);
        m_content.add(m_nameField, BorderLayout.LINE_END);
    }

    /** Alerts the edit listener of changes to the dialog, if the listener is null, this call is a no-op. */
    private void alertListener() {
        if (m_editListener != null) {
            m_editListener.edited();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Component getContent() {
        return m_content;
    }

    /** {@inheritDoc} */
    @Override
    public void setDialogEditListener(EditListener el) {
        m_editListener = el;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataCommittable() {
        if (m_nameField.getText().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return isDataCommittable();
    }
}