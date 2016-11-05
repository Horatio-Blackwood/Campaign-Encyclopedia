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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
public class ConfigureCalendarDialogContent implements DialogContent {

    /** The primary content panel of this dialog. */
    private JPanel m_content;
    
    /** The display component for showing the calendar months as presently constituted. */
    private JList<String> m_list;
    
    /** A model for the JList. */
    private DefaultListModel<String> m_model;
    
    /** A button to add a month to the calendar. */
    private JButton m_addButton;
    
    /** A button to restore the default calendar. */
    private JButton m_restoreDefaultsButton;
    
    /** The dialog content's edit listener. */
    private EditListener m_editListener;
    
    /** The add text field for creating a new month. */
    private JTextField m_addTextField;
    
    /** The campaign calendar currently set on the campaign. */
    private CampaignCalendar m_cal;

    /**
     * Creates a new instance of the MonthConfigEditorDialogContent.
     * @param cal the current Campaign Calendar.  Must not be null.
     */
    public ConfigureCalendarDialogContent(CampaignCalendar cal) {
        if (cal == null) {
            throw new IllegalArgumentException("Parameter 'cal' cannot be null.");
        }
        m_cal = cal;
        initialize();
    }

    /** Initializes and lays out this display's components. */
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
                m_list.setSelectedIndex(m_list.locationToIndex(me.getPoint()));
                int selectedIndex = m_list.getSelectedIndex();
                if (SwingUtilities.isRightMouseButton(me) && selectedIndex >= 0) {
                    if (SwingUtilities.isRightMouseButton(me)) {
                        JPopupMenu menu = new JPopupMenu();
                        menu.add(new AbstractAction("Remove") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                String selected = m_list.getSelectedValue();
                                if (selected != null) {
                                    m_model.removeElement(selected);
                                    alertListener();
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
                                    alertListener();
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
                                    alertListener();
                                }
                            }
                        });
                        menu.show(m_list, me.getX(), me.getY());
                    }
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
        
        // Text Field
        m_addTextField = new JTextField(15);
        final Runnable addRunnable = new Runnable() {
            @Override
            public void run() {
                String month = m_addTextField.getText().trim();
                if (!month.isEmpty()) {
                    m_model.addElement(month);
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
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_addTextField.requestFocus();
            }            
        });
    }

    /**  
     * Updates the add button's enabled status based on the entered value for a new month to add.  If the value 
     * to add is not valid, disables the button, otherwise, re-enables it.
     */
    private void updateAddButtonValidity() {
        String value = m_addTextField.getText().trim();
        // False if:
        //      - supplied value is empty
        //      - value is any existing value.
        if (value.isEmpty() || m_model.contains(value)) {
            m_addButton.setEnabled(false);
        } else {
            m_addButton.setEnabled(true);
        }
    }

    /**
     * Returns the CampaignCalendar that is defined by the user in this Dialog.
     * @return the defined CampaignCalendar.
     */
    public CampaignCalendar getCalendar() {
        CampaignCalendar cal = new CampaignCalendar();
        List<Month> newCalendar = new ArrayList<>();
        for (int i = 0; i < m_model.getSize(); i++) {
            Month month = new Month(m_model.get(i), i);
            newCalendar.add(month);
        }
        cal.update(newCalendar);
        return cal;
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
        CampaignCalendar newCal = getCalendar();
        return !m_cal.equals(newCal);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return true;
    }
}