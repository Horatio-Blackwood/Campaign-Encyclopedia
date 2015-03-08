package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.Season;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 * A configuration dialog for the campaign calendar (Months).
 * @author adam
 */
public class MonthConfigEditorDialogContent implements DialogContent {

    private JPanel m_content;
    private JList<String> m_list;
    private DefaultListModel<String> m_model;
    private JButton m_addButton;
    private JButton m_restoreDefaultsButton;
    private EditListener m_editListener;
    private JTextField m_addTextField;
    private CampaignCalendar m_cal;

    public MonthConfigEditorDialogContent(CampaignCalendar cal) {
        if (cal == null) {
            throw new IllegalArgumentException("Parameter 'cal' cannot be null.");
        }
        m_cal = cal;
        initialize();
    }

    private void initialize() {
        // INITIALIZE COMPONENTS
        m_content = new JPanel(new GridBagLayout());

        m_model = new DefaultListModel<>();
        List<Month> calMonths = new ArrayList<>(m_cal.getMonths());
        Collections.sort(calMonths);
        for (Month m : calMonths) {
            m_model.addElement(m.getName());
        }

        m_list = new JList<>();
        m_list.setModel(m_model);
        m_list.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                String month = m_list.getSelectedValue();
                if (SwingUtilities.isRightMouseButton(me) && !month.toLowerCase().equals(Month.UNSPECIFIED.getName().toLowerCase())) {
                    JPopupMenu menu = new JPopupMenu();
                    menu.add(new AbstractAction("Remove") {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            String selected = m_list.getSelectedValue();
                            if (selected != null) {
                                m_model.removeElement(selected);
                            }
                        }
                    });
                    menu.add(new AbstractAction("Move Up") {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            int index = m_list.getSelectedIndex();
                            String selected = m_list.getSelectedValue();
                            if (index > 0) {
                                index = index - 1;
                            }
                            if (selected != null) {
                                m_model.removeElement(selected);
                                m_model.insertElementAt(selected, index);
                            }
                        }
                    });
                    menu.add(new AbstractAction("Move Down") {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            int index = m_list.getSelectedIndex() + 1;
                            String selected = m_list.getSelectedValue();
                            if (selected != null) {
                                m_model.removeElement(selected);
                                m_model.insertElementAt(selected, index);
                            }
                        }
                    });
                    menu.show(m_list, me.getX(), me.getY());
                }
            }
        });
        JScrollPane jsp = new JScrollPane(m_list);
        jsp.setPreferredSize(new Dimension(250, 250));

        m_restoreDefaultsButton = new JButton("Restore Defaults");
        m_restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m_model.clear();
                for (Season s : Season.values()) {
                    m_model.addElement(s.getDisplayString());
                }
            }
        });
        m_addTextField = new JTextField(15);
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
        // ROW 0
        // -- JList
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0f;
        gbc.weighty = 1.0f;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;
        m_content.add(jsp, gbc);

        // ROW 1
        // -- Editor Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        m_content.add(m_addTextField, gbc);

        // -- Add Button
        gbc.gridx = 1;
        gbc.gridy = 1;
        m_content.add(m_addButton, gbc);


        // ROW 2
        // -- Restore Defaults Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        m_content.add(m_restoreDefaultsButton, gbc);

        updateAddButtonValidity();
    }

    private void updateAddButtonValidity() {
        if (m_addTextField.getText().trim().isEmpty() ||
                m_addTextField.getText().trim().toLowerCase().equals(Month.UNSPECIFIED.getName().toLowerCase())) {
            m_addButton.setEnabled(false);
        } else {
            m_addButton.setEnabled(true);
        }
    }

    public CampaignCalendar getCalendar() {
        CampaignCalendar cal = new CampaignCalendar();
        List<Month> newCalendar = new ArrayList<>();
        for (int i = 0; i < m_model.getSize(); i++) {
            String monthName = m_model.get(i);
            // If not The Unspecified Month
            if (!monthName.toLowerCase().equals(Month.UNSPECIFIED.getName().toLowerCase())) {
                Month month = new Month(m_model.get(i), i);
                newCalendar.add(month);
            }
        }
        cal.update(newCalendar);
        return cal;
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