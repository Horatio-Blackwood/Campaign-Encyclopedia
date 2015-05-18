package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.action.ConfigureCampaignCalendarAction;
import campaignencyclopedia.display.swing.action.ConfigureRelationshipsAction;
import campaignencyclopedia.display.swing.action.DeleteEntityAction;
import campaignencyclopedia.display.swing.action.EditCampaignNameAction;
import campaignencyclopedia.display.swing.action.ExportCampaignToPdfAction;
import campaignencyclopedia.display.swing.action.ExportEntityToPdf;
import campaignencyclopedia.display.swing.action.NavigateBackwardAction;
import campaignencyclopedia.display.swing.action.NavigateForwardAction;
import campaignencyclopedia.display.swing.action.NewCampaignAction;
import campaignencyclopedia.display.swing.action.OpenCampaignAction;
import campaignencyclopedia.display.swing.action.SaveCampaignAction;
import campaignencyclopedia.display.swing.action.ShowCampaignStatisticsAction;
import campaignencyclopedia.display.swing.action.ShowTimelineAction;
import campaignencyclopedia.display.swing.graphical.CampaignEntityGraphViewer;
import campaignencyclopedia.display.swing.graphical.OrbitalEntityViewer;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
 * A manager for menus and actions.
 * @author adam
 */
public class MenuManager {

    /** An action for saving a campaign. */
    private final SaveCampaignAction m_saveAction;

    /** An action for opening a saved campaign. */
    private final OpenCampaignAction m_openAction;

    /** An action for opening a saved campaign. */
    private final NewCampaignAction m_newAction;

    /** An action for exporting the campaign to PDF with secrets. */
    private final ExportCampaignToPdfAction m_pdfWithSecretsAction;

    /** An action for exporting the campaign to PDF without secrets. */
    private final ExportCampaignToPdfAction m_pdfWithoutSecretsAction;

    /** An action for exporting the campaign file without secrets. */
    private final SaveCampaignAction m_exportWithoutSecretsAction;

    /** An action for showing the timeline of the campaign. */
    private final ShowTimelineAction m_showTimelineAction;

    /** An action for configuring the relationships of the campaign. */
    private final ConfigureRelationshipsAction m_configureRelationships;

    /** An action for configuring the calendar in this campaign. */
    private final ConfigureCampaignCalendarAction m_configureCalendar;

    /** The action for showing the statistics dialog. */
    private final ShowCampaignStatisticsAction m_showStats;

    /** The action for editing the name of the Campaign. */
    private final EditCampaignNameAction m_editName;
    
    /** The action for navigating forward. */
    private final NavigateForwardAction m_navForward;
    
    /** The action for navigating backward. */
    private final NavigateBackwardAction m_navBack;

    /** The action for showing the application help. */
    private final AbstractAction m_helpAction;

    /** The action for showing the 'about' display. */
    private final AbstractAction m_aboutAction;

    /** A reference to the application's top level window, used for centering any dialogs launched by actions in this manager. */
    private final Frame m_frame;

    /** The CampaignDataManger for this manager. */
    private final CampaignDataManager m_cdm;

    /** A user display. */
    private final UserDisplay m_display;

    /**
     * Creates a new instance of MenuManager.
     * @param parent a parent component for centering dialogs launched by actions in this application.
     * @param display a UserDisplay to make changes on when actions result in changes to the UI.
     * @param cdm a data manager to update when actions on the data occur.
     */
    public MenuManager(Frame parent, UserDisplay display, CampaignDataManager cdm) {
        m_frame = parent;
        m_cdm = cdm;
        m_display = display;

        m_saveAction = new SaveCampaignAction(m_frame, m_cdm, "Save As...", true);
        m_openAction = new OpenCampaignAction(parent, m_display, cdm);
        m_newAction  = new NewCampaignAction(m_frame, m_display, cdm);

        m_pdfWithSecretsAction = new ExportCampaignToPdfAction(m_frame, m_cdm, "Export to PDF w/Secrets", true);
        m_pdfWithoutSecretsAction = new ExportCampaignToPdfAction(m_frame, m_cdm, "Export to PDF w/o Secrets", false);
        m_exportWithoutSecretsAction = new SaveCampaignAction(m_frame, m_cdm, "Export Campaign w/o Secrets", false);

        m_editName = new EditCampaignNameAction(m_frame, m_cdm, m_display);
        m_showTimelineAction = new ShowTimelineAction(m_display, m_cdm);
        m_configureRelationships = new ConfigureRelationshipsAction(m_frame);
        m_configureCalendar = new ConfigureCampaignCalendarAction(m_frame, m_cdm);
        m_showStats = new ShowCampaignStatisticsAction(m_frame, m_cdm);

        m_navBack = new NavigateBackwardAction(m_display);
        m_navForward = new NavigateForwardAction(m_display);
        
        // Help Menu
        m_helpAction = new AbstractAction("Help") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String msg = "Notes:\n" +
                             "    After all data transactions (deletes, adds etc), your campaign is auto-saved.\n" +
                             "    Search only searches on item name, item type and tags.\n\n" +
                             "Hotkeys:\n" +
                             "    CTRL+E - Export the campaign to PDF, with secret data\n" +
                             "    CTRL+F - put the cursor in the quick search box\n" +
                             "    CTRL+N - Clear the currently displayed item to create a new one\n" +
                             "    CTRL+SHIFT+N - create a new camapign\n" +
                             "    CTRL+O - Open a new campaign file\n" +
                             "    CTRL+R - Open the relationships editor\n" +
                             "    CTRL+S - Save the changes to the currently displayed entity\n" +
                             "    CTRL+SHIFT+S - Save the campaign with a new filename\n" +
                             "    CTRL+T - Open the timeline editor\n" +
                             "    ALT+LEFT - Navigate to the previous item in viewing history\n" +
                             "    ALT+RIGHT - Navigate to the next item in viewing history\n\n" +
                             "Orbital View Controls:\n" +
                             "    Hover over an item to view relationship details\n" +
                             "    Click on any outer node to navigate to that node\n" +
                             "    or use the Back and Fwd buttons\n" +
                             "    CTRL+Click on any item to display it in the editort";
                JOptionPane.showMessageDialog(m_frame, msg, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        };
        m_aboutAction = new AbstractAction("About") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String msg = "Campaign Encyclopedia " + MainDisplay.VERSION + "\n" +
                             "Author:  Adam Anderson\n" +
                             "Release Date:  " + MainDisplay.DATE + "\n" +
                             "Home Page:  https://github.com/Horatio-Blackwood/Campaign-Encyclopedia";
                JOptionPane.showMessageDialog(m_frame, msg, "About Campaign Encyclopedia", JOptionPane.PLAIN_MESSAGE);
            }
        };
    }


    /**
     * Returns the campaign menu for the application menu bar.
     * @return the campaign menu for the application menu bar.
     */
    public JMenu getFileMenu() {
        JMenu campaignMenu = new JMenu("File");

        // Create Actions
        JMenuItem newCampaign  = new JMenuItem(m_newAction);
        newCampaign.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        JMenuItem openCampaign = new JMenuItem(m_openAction);
        openCampaign.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        JMenuItem saveCampaign = new JMenuItem(m_saveAction);
        saveCampaign.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

        // Add Actions
        campaignMenu.add(newCampaign);
        campaignMenu.add(openCampaign);
        campaignMenu.add(saveCampaign);

        return campaignMenu;
    }
    
    /**
     * Returns the 'View' menu.
     * @return the 'View' menu.
     */
    public JMenu getViewMenu() {
        JMenu view = new JMenu("View");

        // Items and Accelerators
        JMenuItem forward = new JMenuItem(m_navForward);
        forward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
        JMenuItem backward = new JMenuItem(m_navBack);
        backward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
        
        view.add(backward);
        view.add(forward);
        
        
        return view;
    }

    /**
     * Returns the Export menu.
     * @return the Export menu for the application.
     */
    public JMenu getExportMenu() {
        JMenu export = new JMenu("Export");

        JMenuItem exportPdfWith = new JMenuItem(m_pdfWithSecretsAction);
        exportPdfWith.setAccelerator(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK));
        JMenuItem exportPdfWithout = new JMenuItem(m_pdfWithoutSecretsAction);
        JMenuItem exportCampaignWithout = new JMenuItem(m_exportWithoutSecretsAction);

        export.add(exportPdfWith);
        export.add(exportPdfWithout);
        export.add(exportCampaignWithout);

        return export;
    }

    /**
     * Returns the Data menu.
     * @return the Data menu for the application.
     */
    public JMenu getCampaignMenu() {
        JMenu dataMenu = new JMenu("Campaign");

        JMenuItem editName = new JMenuItem(m_editName);
        JMenuItem showTimeline = new JMenuItem(m_showTimelineAction);
        showTimeline.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK));
        JMenuItem configureRelationships = new JMenuItem(m_configureRelationships);
        configureRelationships.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
        JMenuItem configureCalendar = new JMenuItem(m_configureCalendar);
        JMenuItem showStats = new JMenuItem(m_showStats);

        dataMenu.add(editName);
        dataMenu.add(showTimeline);
        // Commented out until issues are resolved.
        dataMenu.add(new AbstractAction("Launch Graph Viewer") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                CampaignEntityGraphViewer viewer = new CampaignEntityGraphViewer(m_display, m_cdm);
                viewer.launch();
            }
        });
        dataMenu.add(configureRelationships);
        dataMenu.add(configureCalendar);
        dataMenu.add(showStats);

        return dataMenu;
    }

    public JMenu getHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem helpItem = new JMenuItem(m_helpAction);
        JMenuItem aboutItem = new JMenuItem(m_aboutAction);

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        return helpMenu;
    }

    /**
     * Returns the Entity context menu for the supplied entity.
     * @param entity the Entity to build the context menu for.
     * @return the Entity context menu for the supplied entity.
     */
    public JPopupMenu getEntityContextMenu(final Entity entity) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(new AbstractAction("Launch Orbital Viewer") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                OrbitalEntityViewer viewer = new OrbitalEntityViewer(m_display, m_cdm, entity.getId());
                viewer.launch();
            }
        });
        menu.add(new DeleteEntityAction(m_frame, entity, m_cdm, m_display));
        menu.add(new ExportEntityToPdf(m_frame, entity, m_cdm, "Export to PDF w/Secrets", true));
        if (!entity.isSecret()) {
            menu.add(new ExportEntityToPdf(m_frame, entity, m_cdm, "Export to PDF w/o Secrets", false));
        }
        return menu;
    }
}