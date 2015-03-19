package campaignencyclopedia.display.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 *
 * @author adam
 */
public class EditCampaignNameDialogContent implements DialogContent {

    private String m_originalName = null;
    private JTextField m_editor;
    private JPanel m_content;
    private EditListener m_editListener;

    public EditCampaignNameDialogContent(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'originalName' must not be null.");
        }
        m_originalName = originalName;
        initialize();
    }

    private void initialize() {
        m_editor = new JTextField(20);
        m_editor.setText(m_originalName);
        m_editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                alertListenerOfEdits();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                alertListenerOfEdits();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                alertListenerOfEdits();
            }
        });
        m_content = new JPanel();
        m_content.add(new JLabel("Campaign Name:"));
        m_content.add(m_editor);
    }

    private void alertListenerOfEdits() {
        if (m_editListener != null) {
            m_editListener.edited();
        }
    }

    /**
     * Returns the name in the editor text field.
     * @return the name in the editor text field.
     */
    public String getCampaignName() {
        return m_editor.getText().trim();
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
        String name = m_editor.getText().trim();
        if (!name.isEmpty() && !name.equals(m_originalName)) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return isDataCommittable();
    }

}
