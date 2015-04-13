package campaignencyclopedia.display.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;

/**
 * An editor for the tags associated with an Entity.
 * @author adam
 */
public class TagsEditor {

    /** The label for this editor. */
    private final JLabel m_label;

    /** Text area where Tags will be displayed. */
    private final JTextArea m_tagsArea;

    private EditListener m_editListener;

    public TagsEditor(String title, EditListener editListener) {
        m_label = new JLabel(title);
        m_editListener = editListener;

        m_tagsArea = new JTextArea(4, 30);
        m_tagsArea.setWrapStyleWord(true);
        m_tagsArea.setLineWrap(true);
        m_tagsArea.getDocument().addDocumentListener(new DocumentListener(){
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
     * Returns the tags for this Tag Editor.
     * @return the tags displayed in this tag editor.
     */
    public Set<String> getTags() {
        String tagText = m_tagsArea.getText().trim();
        Set<String> tagSet = new HashSet<>();
        if (!tagText.isEmpty()) {
            String[] tags = tagText.split(",");
            for (String tag : tags) {
                tagSet.add(tag.trim());
            }
        }
        return tagSet;
    }

    public void setTags(Set<String> tags) {
        clear();
        List<String> tagList = new ArrayList<>(tags);
        Collections.sort(tagList);

        StringBuilder bldr = new StringBuilder();
        for (String tag : tagList) {
            bldr.append(tag);
            if (tagList.indexOf(tag) != tagList.size() - 1) {
                bldr.append(", ");
            }
        }
        m_tagsArea.setText(bldr.toString());
    }

    public Component getTitle() {
        return m_label;
    }

    public Component getEditorComponent() {
        return m_tagsArea;
    }

    public void setEditListener(EditListener el) {
        m_editListener = el;
    }

    void clear() {
        m_tagsArea.setText("");
        m_editListener.edited();
    }
}
