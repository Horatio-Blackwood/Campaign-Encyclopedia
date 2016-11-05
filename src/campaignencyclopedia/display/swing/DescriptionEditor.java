package campaignencyclopedia.display.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;

/**
 * An editor for Entity descriptions.
 * @author adam
 */
public class DescriptionEditor {

    /** The description editor's label. */
    private final JLabel m_label;

    /** the description editor's text area. */
    private final JTextArea m_textArea;

    /** An edit listener, for changes. */
    private final EditListener m_editListener;

    /**
     * Creates a new instance of description editor.
     * @param title the title of the description editor, must not be null or empty.
     * @param editListener an edit listener.
     */
    public DescriptionEditor(String title, EditListener editListener) {
        if (title == null) {
            throw new IllegalArgumentException("Parameter 'title' can't be null.");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'title' can't be empty.");
        }
        m_label = new JLabel(title);
        m_editListener = editListener;

        m_textArea = new JTextArea(10, 30);
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

    /**
     * Returns the description currently displayed in this editor.
     * @return the description currently displayed in this editor.
     */
    public String getDescription() {
        return m_textArea.getText().trim();
    }

    /**
     * Sets the supplied description on this editor.  The description must not be null, empty is okay.
     * @param text The description to set.  Must not be null, empty is okay.
     */
    public void setDescription(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Parameter 'text' can't be null.");
        }
        m_textArea.setText(text.trim());
        m_textArea.setCaretPosition(0);
    }

    /**
     * Returns the title component.
     * @return the title component.
     */
    public Component getTitle() {
        return m_label;
    }

    /**
     * Returns the description entry/edit component.
     * @return the description entry/edit component.
     */
    public Component getDescriptionComponent() {
        return m_textArea;
    }

    /** Clears the text from the description editor. */
    void clear() {
        m_textArea.setText("");
    }
}
