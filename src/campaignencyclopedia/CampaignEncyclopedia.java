package campaignencyclopedia;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.swing.MainDisplay;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main class of the Campaign Encyclopedia.
 * @author adam
 */
public class CampaignEncyclopedia {

    // Set up logging.
    static {
        System.setProperty("java.util.logging.config.file", "./config/logging.properties");
    }

    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(CampaignEncyclopedia.class.getName());

    /**
     * Main method.
     * @param args the command line arguments (ignored)
     */
    public static void main(String[] args) {
        swingMain(args);
    }

    /**
     * Runs the application using the Swing GUI toolkit.
     * @param args command line arguments (ignored for swing main).
     */
    private static void swingMain(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException ex) {
            LOGGER.log(Level.FINER, "Error setting look and feel.", ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.FINER, "Illegal access when setting look and feel.", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.FINER, "Unsupported Look and Feel provided by UIManager.", ex);
        }

        CampaignDataManager cdm = new CampaignDataManager();
        MainDisplay display = new MainDisplay(cdm);
        display.launch();
    }
}