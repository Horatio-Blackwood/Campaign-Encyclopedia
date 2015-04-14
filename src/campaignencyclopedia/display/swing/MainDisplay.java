package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.display.EntityDisplayFilter;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.action.SaveHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import toolbox.display.DisplayUtilities;
import toolbox.display.EditListener;

/**
 * The top level display class of this application.
 * @author adam
 */
public class MainDisplay implements EditListener, UserDisplay {

    // TOP LEVEL WINDOW COMPONENTS, DATA.
    /** The top-level window of this application. */
    private JFrame m_frame;

    /** The starting dimensions of the top-level window. */
    private static final Dimension m_windowSize = new Dimension(1000, 700);

    /** A MenuManager for building menus as needed. */
    private MenuManager m_menuManager;

    /** The JList of all of the Entities in the campaign. */
    private JList<Entity> m_entityList;

    /** The Entity JList Model, for storing the data and filter. */
    private SortableListModel<Entity> m_entityModel;

    /** The campaign title label. */
    private JLabel m_campaignTitleLabel;

    /** The Quick Search box */
    private JTextField m_searchBox;

    /** The quick search check box. */
    private JCheckBox m_filterCheckBox;


    // COMPONENTS FOR THE ENTITY VIEW/EDIT DISPLAY
    /** The text field for entering the name of an Entity. */
    private JTextField m_entityNameField;

    /** The Entity Type combobox selector. */
    private JComboBox<EntityType> m_typeSelector;

    /** A button for creating a  new entity. */
    private JButton m_newEntityButton;

    /** A button for adding/updating the currently displayed entity. */
    private JButton m_commitEntityButton;

    /** The EntityData Display for public data. */
    private EntityDataEditor m_public;

    /** An EntityDataDisplay for secret data. */
    private EntityDataEditor m_secret;
    
    /** An editor for Entity Relationship data. */
    private EntityRelationshipEditor m_relationshipEditor;

    /** The JCheckBox for making this Entity secret (or not). */
    private JCheckBox m_secretEntityCheckbox;


    // BACKING DATA
    /** The ID of the currently displayed Entity, if it exists or the entity displayed has one.  If not, this value is null. */
    private UUID m_displayedEntityId;

    /** A campaign data manager, which keeps track of the current data. */
    private final CampaignDataManager m_cdm;

    /** The blue Color used throughout this application. */
    public static final Color BLUE = new Color(96, 128, 192);

    /** The text Color used on the blue background. */
    public static final Color SILVER = new Color(248, 248, 248);

    /** The current release version number. */
    public static final String VERSION = "v1.2.0-Alpha";

    /** The date this release was created. */
    public static final String DATE = "April 9, 2015";

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(MainDisplay.class.getName());

    /**
     * Creates a new Main Display instance.
     * @param cdm the campaign data manager.
     */
    public MainDisplay(CampaignDataManager cdm) {
        m_cdm = cdm;
        initialize();
    }

    /** {@inheritDoc} */
    @Override
    public void edited() {
        if (isEntityContentCommittable()) {
            m_commitEntityButton.setEnabled(true);
        } else {
            m_commitEntityButton.setEnabled(false);
        }
    }

    /**
     * Returns true if the content of the displayed Entity is valid to be added to the encyclopedia, false otherwise.
     * @return true if the content of the displayed Entity is valid to be added to the encyclopedia, false otherwise.
     */
    private boolean isEntityContentCommittable() {
        return !m_entityNameField.getText().trim().isEmpty();
    }

    /** Adds the currently displayed Entity to the CampaignDataManager and clears the display. */
    private void commitDisplayedDataToCdm() {
        // Get shown Entity
        Entity entity = getDisplayedEntity();
        
        // Get Displayed Relationships and add them.
        RelationshipManager relMgr = new RelationshipManager();
        for (Relationship rel : m_relationshipEditor.getData()) {
            // If the entity is secret and it has any public relationships, they must now be secret, so update them.
            if (entity.isSecret() && !rel.isSecret()) {
                relMgr.addRelationship(new Relationship(rel.getEntityId(), rel.getRelatedEntity(), rel.getRelationshipText(), true));
            } else {
                relMgr.addRelationship(rel);
            }
        }
        
        // If entity is secret, relationships owned by other entities pointing to it must be secret, so update them.
        if (entity.isSecret()) {
            for (Entity otherEntity : m_cdm.getAllEntities()) {
                RelationshipManager otherRelMgr = m_cdm.getRelationshipsForEntity(otherEntity.getId());
                Set<Relationship> requireUpdate = new HashSet<>();
                for (Relationship rel : new HashSet<>(otherRelMgr.getPublicRelationships())) {
                    if (!rel.isSecret() && rel.getRelatedEntity().equals(entity.getId())) {
                        otherRelMgr.remove(rel);
                        requireUpdate.add(new Relationship(rel.getEntityId(), rel.getRelatedEntity(), rel.getRelationshipText(), true));
                    }
                }
                // Clear the public data from the relationship manager and add in the newly updated stuff.
                otherRelMgr.addAllRelationships(requireUpdate);
                m_cdm.addOrUpdateAllRelationships(otherEntity.getId(), otherRelMgr);
            }   
        }

        // Check to see if the Entity is already in our data manager
        // If it is, remove it from the SortedListModel.
        Entity cdmEntity = m_cdm.getEntity(entity.getId());
        if (cdmEntity != null) {
            m_entityModel.removeElement(cdmEntity);
        }
        
        // Add the new or updated Enitty to both the CDM and the SortedListModel
        m_cdm.addOrUpdateEntity(entity);
        m_entityModel.addElement(entity);
        m_entityList.setSelectedValue(entity, true);
        // Add/Update the Relationships
        m_cdm.addOrUpdateAllRelationships(entity.getId(), relMgr);
        m_displayedEntityId = entity.getId();

        // Force Update of display for relationship changes.
        m_relationshipEditor.clearData();
        m_relationshipEditor.setData(relMgr.getAllRelationships());
    }

    /**
     * Returns the currently displayed entity.
     * @return the currently displayed entity.
     */
    private Entity getDisplayedEntity() {
        UUID id;
        String name = m_entityNameField.getText().trim();
        EntityType type = (EntityType)m_typeSelector.getSelectedItem();
        EntityData publicData = m_public.getEntityData();
        EntityData secretData = m_secret.getEntityData();
        if (m_displayedEntityId == null) {
            id = UUID.randomUUID();
        } else {
            id = m_displayedEntityId;
        }
        boolean isSecret = m_secretEntityCheckbox.isSelected();

        return new Entity(id, name, type, publicData, secretData, isSecret);
    }


    /** {@inheritDoc} */
    @Override
    public UUID getShownEntity() {
        return m_displayedEntityId;
    }

    /** {@inheritDoc} */
    @Override
    public void removeEntity(Entity entity) {
        m_entityModel.removeElement(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void clearDisplayedEntity() {
        m_displayedEntityId = null;
        m_entityNameField.setText("");
        m_public.clear();
        m_secret.clear();
        m_secretEntityCheckbox.setSelected(false);
        m_relationshipEditor.clearData();
    }

    /** {@inheritDoc} */
    @Override
    public void clearAllData() {
        clearDisplayedEntity();
        m_entityModel.clear();
        m_campaignTitleLabel.setText("");
        m_relationshipEditor.clearData();
    }

    /** {@inheritDoc} */
    @Override
    public void displayCampaign(Campaign campaign) {
        clearAllData();
        m_campaignTitleLabel.setText(campaign.getName());
        m_entityModel.addAllElements(campaign.getEntities());
    }

    /** {@inheritDoc} */
    @Override
    public void showEntity(UUID id) {
        if (!isCurrentDataSaved()) {
            int response = isSaveDesired();
            if (response == JOptionPane.YES_OPTION) {
                commitDisplayedDataToCdm();
            } else if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        Entity toShow = m_cdm.getEntity(id);
        displayEntity(toShow);
    }

    /**
     * Displays the supplied Entity.
     * @param entity the Entity to display.
     */
    private void displayEntity(Entity entity) {
        if (!isCurrentDataSaved()){
            int response = isSaveDesired();
            if (response == JOptionPane.YES_OPTION) {
                commitDisplayedDataToCdm();
                SaveHelper.autosave(m_frame, m_cdm, true);
            } else if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        // Clear out the old data first
        clearDisplayedEntity();
        
        // If valid data was set, display it.
        if (entity != null) {
            m_displayedEntityId = entity.getId();
            m_entityNameField.setText(entity.getName());
            m_typeSelector.setSelectedItem(entity.getType());
            m_public.setEntityData(entity.getPublicData());
            m_secret.setEntityData(entity.getSecretData());
            m_relationshipEditor.setData(m_cdm.getRelationshipsForEntity(m_displayedEntityId).getAllRelationships());
            m_secretEntityCheckbox.setSelected(entity.isSecret());
        }
    }

    /** Launches the display window of this application. */
    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, m_windowSize);
        m_searchBox.requestFocus();
        m_frame.setVisible(true);
    }

    /** Initialize this display's components. */
    private void initialize() {
        m_frame = new JFrame("Campaign Encyclopedia - " + VERSION);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex) {
            LOGGER.log(Level.CONFIG, "Unable to load application icon.", ex);
        }
        m_frame.setPreferredSize(m_windowSize);
        m_frame.setLayout(new BorderLayout());
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creating the containing panel (have to use this instead of just the JFrame
        // directly in order to support the input map since JFrame is not a JComponent.
        JPanel panel = new JPanel(new BorderLayout());

        // Set up input map action for putting the cursor in the find text box.
        String findHotKey = "find";
        AbstractAction find = new AbstractAction(findHotKey) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m_searchBox.requestFocus();
                m_searchBox.selectAll();
            }
        };
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK), findHotKey);
        panel.getActionMap().put(findHotKey, find);

        // Add Title/Search bar
        panel.add(createTitleBar(), BorderLayout.NORTH);

        // Add entity editor
        panel.add(createEntityDisplay(), BorderLayout.CENTER);

        // Add entity list.
        panel.add(createEntityList(), BorderLayout.WEST);

        // Create and set main menu
        m_menuManager = new MenuManager(m_frame, this, m_cdm);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(m_menuManager.getFileMenu());
        menuBar.add(m_menuManager.getExportMenu());
        menuBar.add(m_menuManager.getCampaignMenu());
        menuBar.add(m_menuManager.getHelpMenu());

        // Add Components to the Frame.
        m_frame.setJMenuBar(menuBar);
        m_frame.add(panel, BorderLayout.CENTER);

    }

    /** Returns true if the currently displayed data is saved. */
    private boolean isCurrentDataSaved() {
        // If a valid entity is shown...
        if (isEntityContentCommittable()) {
            // And if the entity displayed has an ID, get the Entity from from the CDM, and compare the two.
            if (m_displayedEntityId != null) {
                Entity cdmEntity = m_cdm.getEntity(m_displayedEntityId);
                Entity displayedEntity = getDisplayedEntity();

                // If the two are not equal, changes have been made...
                if (!displayedEntity.equals(cdmEntity)) {
                    return false;
                }
                
                // Or if the Relationship Data has changed, return false...
                RelationshipManager rm = m_cdm.getRelationshipsForEntity(m_displayedEntityId);
                if (!rm.getAllRelationships().equals(m_relationshipEditor.getData())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Prompts the user to determine if they would like to save any changes that have been made
     * and returns the user's choice (true if they desire to save, false otherwise).
     * @return true if save is desired, false otherwise.
     */
    private int isSaveDesired() {
        return JOptionPane.showConfirmDialog(m_frame,
                                                     "The displayed data has changed, do\n" +
                                                     "you want to keep these changes?",
                                                     "Save Current Changes",
                                                     JOptionPane.YES_NO_CANCEL_OPTION);
    }

    /**
     * Creates the Entity display.
     * @return a JPanel which contains an Entity display.
     */
    private JPanel createEntityDisplay() {
        // Init Components
        JPanel panel = new JPanel(new GridBagLayout());
        m_entityNameField = new JTextField(20);
        m_entityNameField.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        m_secretEntityCheckbox = new JCheckBox("Secret");
        m_secretEntityCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                edited();
            }
        });

        m_commitEntityButton = new JButton();
        AbstractAction save = new AbstractAction("Save Item") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                commitDisplayedDataToCdm();
                // Always includes secret data.
                SaveHelper.autosave(m_frame, m_cdm, true);
            }
        };
        m_commitEntityButton.setAction(save);
        m_commitEntityButton.setToolTipText("Save this item, (CTRL+S)");
        m_commitEntityButton.setEnabled(false);
        String saveKey = "Save";
        InputMap saveInputMap = m_commitEntityButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        saveInputMap.put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), saveKey);
        m_commitEntityButton.getActionMap().put(saveKey, save);

        m_newEntityButton = new JButton();
        AbstractAction clear = new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!isCurrentDataSaved()) {
                    int response = isSaveDesired();
                    if (response == JOptionPane.YES_OPTION) {
                        commitDisplayedDataToCdm();
                    } else if (response == JOptionPane.CANCEL_OPTION) {
                        // Do Nothing.
                        return;
                    }
                }
                // Finally, clear the displayed contents.
                clearDisplayedEntity();
                m_entityNameField.requestFocus();
            }
        };
        m_newEntityButton.setAction(clear);
        m_newEntityButton.setToolTipText("Clear data for a new item, (CTRL+N)");
        String clearKey = "clearKey";
        InputMap clearInputMap = m_newEntityButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        clearInputMap.put(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK), clearKey);
        m_newEntityButton.getActionMap().put(clearKey, clear);

        m_typeSelector = new JComboBox<>();
        for (EntityType type : EntityType.values()) {
            m_typeSelector.addItem(type);
        }
        m_typeSelector.setRenderer(new DisplayableCellRenderer());

        m_public = new EntityDataEditor(this, false);
        m_secret = new EntityDataEditor(this, true);
        m_relationshipEditor = new EntityRelationshipEditor(m_frame, m_cdm, this, "Relationships", this);

        Insets insets = new Insets(3, 3, 3, 3);


        // Layout display
        // Create Top Row Panel --> Name / Is Secret / Type / Clear Btn / Add Btn
        JPanel topRow = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        topRow.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0f;
        topRow.add(m_entityNameField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0f;
        topRow.add(m_secretEntityCheckbox, gbc);

        gbc.gridx = 3;
        topRow.add(m_typeSelector, gbc);

        gbc.gridx = 4;
        topRow.add(m_newEntityButton, gbc);

        gbc.gridx = 5;
        topRow.add(m_commitEntityButton, gbc);

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.fill = GridBagConstraints.HORIZONTAL;
        mainGbc.gridwidth = 4;
        mainGbc.insets = insets;
        panel.add(topRow, mainGbc);

        // FIRST COLUMN
        // --- Public Description Label
        mainGbc.gridx = 0;
        mainGbc.gridy = 1;
        mainGbc.gridwidth = 2;
        mainGbc.weighty = 0.0f;
        mainGbc.weightx = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.anchor = GridBagConstraints.PAGE_END;
        panel.add(m_public.getDescriptionEditor().getTitle(), mainGbc);
        
        // --- Public Description Editor Component
        mainGbc.gridy = 2;
        mainGbc.weighty = 1.0f;
        JScrollPane publicDescriptionScrollPane = new JScrollPane(m_public.getDescriptionEditor().getDescriptionComponent());
        publicDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        publicDescriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(publicDescriptionScrollPane, mainGbc);
        
        // --- Relationships Label
        mainGbc.gridy = 3;
        mainGbc.weighty = 0.0f;
        panel.add(m_relationshipEditor.getTitle(), mainGbc);
        
        // --- Public Editor Component
        mainGbc.gridy = 4;
        mainGbc.gridheight = 4;
        mainGbc.weighty = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        JScrollPane relationShipScrollPane = new JScrollPane(m_relationshipEditor.getEditorComponent());
        panel.add(relationShipScrollPane, mainGbc);
        
        // --- Add Relationship Button
        mainGbc.gridy = 1;
        mainGbc.gridy = 8;
        mainGbc.weighty = 0.0f;
        mainGbc.gridwidth = 1;
        mainGbc.gridheight = 1;
        mainGbc.fill = GridBagConstraints.NONE;
        mainGbc.anchor = GridBagConstraints.LAST_LINE_START;
        panel.add(m_relationshipEditor.getAddRelationshipButton(), mainGbc);

        // SECOND COLUMN
        // --- Secret Description Label
        mainGbc.gridx = 2;
        mainGbc.gridy = 1;
        mainGbc.gridwidth = 2;
        mainGbc.weighty = 0.0f;
        mainGbc.weightx = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        panel.add(m_secret.getDescriptionEditor().getTitle(), mainGbc);
        
        // --- Secret Description Editor Component
        mainGbc.gridy = 2;
        mainGbc.weighty = 1.0f;
        JScrollPane secretDescriptionScrollPane = new JScrollPane(m_secret.getDescriptionEditor().getDescriptionComponent());
        secretDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        secretDescriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(secretDescriptionScrollPane, mainGbc);
        
        // --- Public Tags Label
        mainGbc.gridy = 3;
        mainGbc.weighty = 0.0f;
        panel.add(m_public.getTagsEditor().getTitle(), mainGbc);
        
        // --- Public Tags Editor
        mainGbc.gridy = 4;
        mainGbc.gridheight = 1;
        mainGbc.weighty = 0.1f;
        mainGbc.fill = GridBagConstraints.BOTH;
        JScrollPane pubTagScrollPane = new JScrollPane(m_public.getTagsEditor().getEditorComponent());
        pubTagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pubTagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(pubTagScrollPane, mainGbc);

        // --- Secret Tags Label
        mainGbc.gridy = 5;
        mainGbc.gridheight = 1;
        mainGbc.weighty = 0.0f;
        panel.add(m_secret.getTagsEditor().getTitle(), mainGbc);

        // --- Secret Tags Editor
        mainGbc.gridy = 6;
        mainGbc.gridheight = 2;
        mainGbc.weighty = 0.1f;
        JScrollPane secretTagScrollPane = new JScrollPane(m_secret.getTagsEditor().getEditorComponent());
        secretTagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        secretTagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(secretTagScrollPane, mainGbc);

        return panel;
    }

    /**
     * Creates and returns the EntityList.
     * @return the Entity List component.
     */
    private Component createEntityList() {
        m_entityModel = new SortableListModel<>();
        m_entityModel.addAllElements(m_cdm.getAllEntities());

        m_entityList = new JList<>();
        m_entityList.setCellRenderer(new EntityListCellRenderer());
        m_entityList.setModel(m_entityModel);

        // Define reusable showEntity runnable (for both key and mouse listeners)
        final Runnable showEntity = new Runnable() {
            @Override
            public void run() {
                int selectedIndex = m_entityList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Entity selected = m_entityModel.getElementAt(selectedIndex);
                    if (selected != null) {
                        displayEntity(selected);
                    }
                }
            }
        };

        // Setup Mouse Listener
        m_entityList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                m_entityList.setSelectedIndex(m_entityList.locationToIndex(me.getPoint()));
                int selectedIndex = m_entityList.getSelectedIndex();
                if (me.getClickCount() > 1 && selectedIndex >= 0) {
                    showEntity.run();
                } else if (SwingUtilities.isRightMouseButton(me)) {

                    if (selectedIndex >= 0) {
                        Entity selectedEntity =  m_entityModel.getElementAt(selectedIndex);
                        JPopupMenu contextMenu = m_menuManager.getEntityContextMenu(selectedEntity);
                        contextMenu.show(m_entityList, me.getX(), me.getY());
                    }
                }
            }

        });

        // Setup Key Listener
        m_entityList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
                // Ignored
            }
            @Override
            public void keyPressed(KeyEvent ke) {
                int selectedIndex = m_entityList.getSelectedIndex();
                if (ke.getKeyChar() == KeyEvent.VK_ENTER && selectedIndex >= 0) {
                    showEntity.run();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {
                // Ignored
            }
        });
        return new JScrollPane(m_entityList);
    }

    /**
     * Create title bar and filter controls.
     * @return a JPanel containing the title bar and filter controls.
     */
    private JPanel createTitleBar() {
        // Init
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(BLUE);
        m_campaignTitleLabel = new JLabel(m_cdm.getData().getName());
        m_campaignTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        m_campaignTitleLabel.setForeground(SILVER);

        m_filterCheckBox = new JCheckBox("Hide Secret Items");
        m_filterCheckBox.setOpaque(true);
        m_filterCheckBox.setBackground(BLUE);
        m_filterCheckBox.setForeground(SILVER);
        m_filterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updateEntityFilter();
            }
        });
        m_searchBox = new JTextField(18);
        m_searchBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
        });
        m_searchBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }
            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (m_entityModel.getSize() > 0) {
                        m_entityList.setSelectedIndex(0);
                    }
                    m_entityList.requestFocus();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0.0f;
        panel.add(m_campaignTitleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        panel.add(new JLabel(), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_filterCheckBox, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_searchBox, gbc);

        // Return
        return panel;
    }


    /**
     * Returns a new EntityDisplayFilter or null if no valid filter is set.
     * @return a new EntityDisplayFilter or null if no valid filter is set.s
     */
    private void updateEntityFilter() {
        String searchString = m_searchBox.getText().trim();
        if (searchString.isEmpty() && !m_filterCheckBox.isSelected()) {
            m_entityModel.setFilter(null);
        } else {
            m_entityModel.setFilter(new EntityDisplayFilter(searchString, !m_filterCheckBox.isSelected()));
        }
    }
}