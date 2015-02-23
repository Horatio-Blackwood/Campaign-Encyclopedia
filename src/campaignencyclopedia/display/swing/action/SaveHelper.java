package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.persistence.CampaignTranslator;
import campaignencyclopedia.display.swing.MainDisplay;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import toolbox.file.FileTools;

/**
 * A class that helps to support saving data.
 * @author adam
 */
public class SaveHelper {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SaveHelper.class.getName());

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
                        String campaign = CampaignTranslator.toJson(cdm.getData(), includeSecrets);
                        if (!path.endsWith(".campaign")) {
                            path = path + ".campaign";
                        }
                        FileTools.writeFile(path, campaign);
                        cdm.setFileName(path);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Failed to save the campaign.", ex);
                    }
                }
            }).start();
        }
    }

    public static void autosave(Frame frame, CampaignDataManager cdm, boolean includeSecrets) {
        if (cdm.getSaveFileName() != null) {
            Campaign campaign = cdm.getData();
            try {
                FileTools.writeFile(cdm.getSaveFileName(), CampaignTranslator.toJson(campaign, true));
            } catch (IOException ex) {
                Logger.getLogger(MainDisplay.class.getName()).log(Level.SEVERE, "Failed to save campaign.", ex);
            }
        } else {
            SaveHelper.save(frame, cdm, includeSecrets);
        }
    }
}
