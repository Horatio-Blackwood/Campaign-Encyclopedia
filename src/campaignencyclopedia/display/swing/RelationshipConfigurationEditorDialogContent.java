package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.RelationshipDataManager;
import campaignencyclopedia.data.RelationshipType;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 *
 * @author adam
 */
public class RelationshipConfigurationEditorDialogContent implements DialogContent {

    private JPanel m_content;
    private JList<String> m_list;
    private SortedListModel<String> m_model;
    private JButton m_addButton;
    private JButton m_restoreDefaultsButton;
    private JButton m_removeSelectedButton;
    private EditListener m_editListener;
    private JTextField m_addTextField;

    public RelationshipConfigurationEditorDialogContent() {
        initialize();
    }

    private void initialize() {
        // INITIALIZE COMPONENTS
        m_content = new JPanel(new GridBagLayout());

        m_model = new SortedListModel<>();
        m_model.addAllElements(RelationshipDataManager.getRelationships());

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
            }
        });
        m_removeSelectedButton = new JButton("Remove Selected");
        m_removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String selected = m_list.getSelectedValue();
                if (selected != null) {
                    m_model.removeElement(selected);
                }
            }
        });
        m_addTextField = new JTextField(15);
        m_addButton = new JButton("Add");
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String relationship = m_addTextField.getText().trim();
                if (!relationship.isEmpty()) {
                    m_model.addElement(relationship);
                    m_addTextField.setText("");
                }
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
    }


    public List<String> getRelationships() {
        return m_model.getAllElements();
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
        return true;
    }

    @Override
    public boolean isCommitPermitted() {
        return true;
    }
}