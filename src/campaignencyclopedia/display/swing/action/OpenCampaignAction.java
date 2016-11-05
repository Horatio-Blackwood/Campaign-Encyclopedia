package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
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
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import toolbox.file.FileTools;
import toolbox.file.persistence.json.JsonException;

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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Campaign Files", "campaign");
        chooser.setFileFilter(filter);
        
        if (chooser.showOpenDialog(m_window) == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String jsonCampaign = FileTools.readFile(selectedFile.getAbsolutePath());
                        Campaign campaign = CampaignTranslator.fromJson(jsonCampaign);
                        m_cdm.setFileName(selectedFile.getAbsolutePath());
                        DisplayCampaignHelper.displayCampaign(m_userDisplay, m_cdm, campaign);
                    } catch (JsonException jex) {
                        String msg = "Error openming file:  " + selectedFile.getName() + ".  Is this a valid campaign file?";
                        JOptionPane.showMessageDialog(m_window, msg, "Unable to Open File", JOptionPane.ERROR_MESSAGE);   
                        LOGGER.log(Level.WARNING, "Failed to open the campaign, json error.", jex);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Failed to open the campaign, IO error.", ex);
                    }
                }
            }).start();
        }
    }
}