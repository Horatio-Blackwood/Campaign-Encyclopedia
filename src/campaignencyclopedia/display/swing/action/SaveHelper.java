package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.persistence.CampaignTranslator;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import toolbox.file.FileTools;
import toolbox.file.persistence.json.JsonException;

/**
 * A class that helps to support saving data.
 * @author adam
 */
public class SaveHelper {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SaveHelper.class.getName());

    /**
     * A helper function to save the campaign.  If a filename has been specified, this method simply uses it,
     * otherwise, 'save' will be called which will prompt the user for a destination file name.
     *
     * @param parent the top-level window to position dialogs launched by this static method when called.
     * @param cdm the CampaignDataManager to get the data to save from.
     * @param includeSecrets true if secrets should be included in the saved file resulting from this call, false if
     * they should not be included.
     */
    public static void save(final Frame parent, final CampaignDataManager cdm, final boolean includeSecrets) {
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

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        File selectedFile = chooser.getSelectedFile();
                        String path = selectedFile.getAbsolutePath().trim();
                        String campaign = CampaignTranslator.toJson(cdm.getData(), cdm, includeSecrets);
                        if (!path.endsWith(".campaign")) {
                            path = path + ".campaign";
                        }
                        FileTools.writeFile(path, campaign);
                        cdm.setFileName(path);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Failed to save the campaign.", ex);
                    } catch (JsonException jex) {
                        LOGGER.log(Level.SEVERE, "Failed to translate the campaign.", jex);
                    }
                }
            }).start();
        }
    }

    /**
     * A helper function to autosave the campaign.  If a filename has been specified, this method simply uses it,
     * otherwise, 'save' will be called which will prompt the user for a destination file name.
     *
     * @param frame the top-level window to position dialogs launched by this static method when called.
     * @param cdm the CampaignDataManager to get the data to save from.
     * @param includeSecrets true if secrets should be included in the saved file resulting from this call, false if
     * they should not be included.
     */
    public static void autosave(Frame frame, CampaignDataManager cdm, boolean includeSecrets) {
        if (cdm.getSaveFileName() != null) {
            Campaign campaign = cdm.getData();
            try {
                FileTools.writeFile(cdm.getSaveFileName(), CampaignTranslator.toJson(campaign, cdm, true));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to save campaign.", ex);
            } catch (JsonException jex) {
                LOGGER.log(Level.SEVERE, "Failed to translate the campaign.", jex);
            }
        } else {
            SaveHelper.save(frame, cdm, includeSecrets);
        }
    }
}