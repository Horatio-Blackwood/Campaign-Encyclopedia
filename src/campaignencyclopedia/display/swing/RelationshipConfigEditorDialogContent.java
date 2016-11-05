package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.RelationshipOptionManager;
import campaignencyclopedia.data.RelationshipType;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 * A dialog content for customizing the relationships in the application.
 * @author adam
 */
public class RelationshipConfigEditorDialogContent implements DialogContent {

    /** The primary component of this dialog content. */
    private JPanel m_content;
    
    /** JList to display the Relationships. */
    private JList<String> m_list;
    
    /** The data model to store the data for the JList in. */
    private SortableListModel<String> m_model;
    
    /** The add button. */
    private JButton m_addButton;
    
    /** The Restore defaults button. */
    private JButton m_restoreDefaultsButton;
    
    /** The remove selected defaults button. */
    private JButton m_removeSelectedButton;
    
    /** The edit listener for this dialog content. */
    private EditListener m_editListener;
    
    /** The JTextField for adding new relationships. */
    private JTextField m_addTextField;

    /** Constructor. */
    public RelationshipConfigEditorDialogContent() {
        initialize();
    }

    /** Initializes the components of this display. */
    private void initialize() {
        // INITIALIZE COMPONENTS
        m_content = new JPanel(new GridBagLayout());

        m_model = new SortableListModel<>();
        m_model.addAllElements(RelationshipOptionManager.getRelationships());

        m_list = new JList<>();
        m_list.setModel(m_model);
        JScrollPane jsp = new JScrollPane(m_list);
        jsp.setPreferredSize(new Dimension(250, 500));

        m_restoreDefaultsButton = new JButton("Restore Defaults");
        m_restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m_model.clear();
                m_model.addAllElements(RelationshipType.getStringList());
                alertListener();
            }
        });
        m_removeSelectedButton = new JButton("Remove Selected");
        m_removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String selected = m_list.getSelectedValue();
                if (selected != null) {
                    m_model.removeElement(selected);
                    alertListener();
                }
            }
        });
        
        // Add Text Field
        m_addTextField = new JTextField(15);
        final Runnable addRunnable = new Runnable() {
            @Override
            public void run() {
                String relationship = m_addTextField.getText().trim();
                if (!relationship.isEmpty()) {
                    m_model.addElement(relationship);
                    m_addTextField.setText("");
                    alertListener();
                    m_addTextField.requestFocus();
                }
            }
        };
        m_addTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
                // Ignored
            }
            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    addRunnable.run();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {
                // Ignored
            }
        });
        m_addTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                updateAddButtonValidity();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                updateAddButtonValidity();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                updateAddButtonValidity();
            }
        });
        
        // Add Button
        m_addButton = new JButton("Add");
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                addRunnable.run();
            }
        });

        // LAYOUT COMPONENTS
        // ROW 1
        // -- JList
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0f;
        gbc.weighty = 1.0f;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;
        m_content.add(jsp, gbc);

        // ROW 2
        // -- Remove Selected Button.
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        m_content.add(m_removeSelectedButton, gbc);

        // -- Restore Defaults Button
        gbc.gridx = 2;
        gbc.gridy = 1;
        m_content.add(m_restoreDefaultsButton, gbc);

        // ROW 3
        // -- Editor Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        m_content.add(m_addTextField, gbc);

        // -- Add Button
        gbc.gridx = 2;
        gbc.gridy = 2;
        m_content.add(m_addButton, gbc);
        
        updateAddButtonValidity();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_addTextField.requestFocus();
            }            
        });
    }
    
    /** Checks and updates the enabled status of the add button based on the value in the text field. */
    private void updateAddButtonValidity() {
        String value = m_addTextField.getText().trim();
        if (value.isEmpty() || m_model.contains(value)) {
            m_addButton.setEnabled(false);
        } else {
            m_addButton.setEnabled(true);
        }
    }

    /**
     * Returns the relationship list configured in this Dialog Content.
     * @return the relationship list configured in this Dialog Content.
     */
    public List<String> getRelationships() {
        return m_model.getAllElements();
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
    
    /** Alerts the edit listener, (if one exists) that the contents of the dialog have changed. */
    private void alertListener() {
        if (m_editListener != null) {
            m_editListener.edited();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataCommittable() {
        List<String> displayed = new ArrayList<>(m_model.getAllElements());
        List<String> original = new ArrayList<>(RelationshipOptionManager.getRelationships());
        Collections.sort(displayed);
        Collections.sort(original);
        return !displayed.equals(original);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return true;
    }
}