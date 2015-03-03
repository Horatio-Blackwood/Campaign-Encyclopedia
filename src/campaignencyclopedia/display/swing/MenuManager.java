package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.action.ConfigureRelationshipsAction;
import campaignencyclopedia.display.swing.action.DeleteEntityAction;
import campaignencyclopedia.display.swing.action.ExportCampaignToPdfAction;
import campaignencyclopedia.display.swing.action.ExportEntityToPdf;
import campaignencyclopedia.display.swing.action.NewCampaignAction;
import campaignencyclopedia.display.swing.action.OpenCampaignAction;
import campaignencyclopedia.display.swing.action.SaveCampaignAction;
import campaignencyclopedia.display.swing.action.ShowTimelineAction;
import java.awt.Frame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

        m_showTimelineAction = new ShowTimelineAction(m_frame, m_display, cdm);
        m_configureRelationships = new ConfigureRelationshipsAction(m_frame);
    }


    /**
     * Returns the campaign menu for the application menu bar.
     * @return the campaign menu for the application menu bar.
     */
    public JMenu getCampaignMenu() {
        JMenu campaignMenu = new JMenu("Campaign");

        // Create Actions
        JMenuItem newCampaign  = new JMenuItem(m_newAction);
        newCampaign.setAccelerator(KeyStroke.getKeyStroke('N', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        JMenuItem openCampaign = new JMenuItem(m_openAction);
        openCampaign.setAccelerator(KeyStroke.getKeyStroke('O', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        JMenuItem saveCampaign = new JMenuItem(m_saveAction);
        saveCampaign.setAccelerator(KeyStroke.getKeyStroke('S', java.awt.event.InputEvent.CTRL_DOWN_MASK));

        // Add Actions
        campaignMenu.add(newCampaign);
        campaignMenu.add(openCampaign);
        campaignMenu.add(saveCampaign);

        return campaignMenu;
    }

    /**
     * Returns the Export menu.
     * @return the Export menu for the application.
     */
    public JMenu getExportMenu() {
        JMenu export = new JMenu("Export");

        JMenuItem exportPdfWith = new JMenuItem(m_pdfWithSecretsAction);
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
    public JMenu getDataMenu() {
        JMenu dataMenu = new JMenu("Data");

        JMenuItem showTimeline = new JMenuItem(m_showTimelineAction);
        showTimeline.setAccelerator(KeyStroke.getKeyStroke('T', java.awt.event.InputEvent.CTRL_DOWN_MASK));

        JMenuItem configureRelationships = new JMenuItem(m_configureRelationships);

        dataMenu.add(showTimeline);
        dataMenu.add(configureRelationships);

        return dataMenu;
    }

    /**
     * Returns the Entity context menu for the supplied entity.
     * @param entity the Entity to build the context menu for.
     * @return the Entity context menu for the supplied entity.
     */
    public JPopupMenu getEntityContextMenu(Entity entity) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(new DeleteEntityAction(m_frame, entity, m_cdm, m_display));
        menu.add(new ExportEntityToPdf(m_frame, entity, m_cdm, "Export to PDF w/Secrets", true));
        if (!entity.isSecret()) {
            menu.add(new ExportEntityToPdf(m_frame, entity, m_cdm, "Export to PDF w/o Secrets", false));
        }
        return menu;
    }
}