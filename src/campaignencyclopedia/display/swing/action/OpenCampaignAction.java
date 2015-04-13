package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.persistence.CampaignTranslator;
import campaignencyclopedia.display.UserDisplay;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import toolbox.file.FileTools;

/**
 * An action for opening Campaign files.
 * @author adam
 */
public class OpenCampaignAction extends AbstractAction {

    /** The CampaignDataManager to load the data into. */
    private CampaignDataManager m_cdm;

    /** The UserDispaly to show the data on. */
    private UserDisplay m_userDisplay;

    /** The parent window to center dialogs launched over. */
    private Frame m_window;

    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(SaveCampaignAction.class.getName());

    /**
     * Creates an instance of the OpenCampaignAction.
     * @param window a window to center the dialogs launched by this action.
     * @param userDisplay a reference to the user display
     * @param campaignManager a reference to the campaign data manager.
     */
    public OpenCampaignAction(Frame window, UserDisplay userDisplay, CampaignDataManager campaignManager) {
        super("Open...");
        if (campaignManager == null) {
            throw new IllegalArgumentException("Parameter 'campaign' cannot be null.");
        }
        m_cdm = campaignManager;
        m_userDisplay = userDisplay;
        m_window = window;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        final JFileChooser chooser = new JFileChooser("./campaigns");
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".campaign")){
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Campaign files";
            }
        });
        if (chooser.showOpenDialog(m_window) == JFileChooser.APPROVE_OPTION) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File selectedFile = chooser.getSelectedFile();
                        String jsonCampaign = FileTools.readFile(selectedFile.getAbsolutePath());
                        Campaign campaign = CampaignTranslator.fromJson(jsonCampaign);
                        m_cdm.setFileName(selectedFile.getAbsolutePath());
                        DisplayCampaignHelper.displayCampaign(m_userDisplay, m_cdm, campaign);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Failed to save the campaign.", ex);
                    }
                }
            }).start();
        }
    }
}