package campaignencyclopedia.display.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;

/**
 *
 * @author adam
 */
public class DescriptionEditor {

    private final JLabel m_label;
    private final JTextArea m_textArea;
    private final EditListener m_editListener;

    public DescriptionEditor(String title, EditListener editListener) {
        m_label = new JLabel(title);
        m_editListener = editListener;
        
        m_textArea = new JTextArea(5, 30);
        m_textArea.setWrapStyleWord(true);
        m_textArea.setLineWrap(true);
        m_textArea.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent de) {
                m_editListener.edited();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                m_editListener.edited();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                m_editListener.edited();
            }
        });
    }

    public String getDescription() {
        return m_textArea.getText().trim();
    }

    public void setDescription(String text) {
        m_textArea.setText(text.trim());
    }

    public Component getTitle() {
        return m_label;
    }

    public Component getDescriptionComponent() {
        return m_textArea;
    }

    void clear() {
        m_textArea.setText("");
    }
}
