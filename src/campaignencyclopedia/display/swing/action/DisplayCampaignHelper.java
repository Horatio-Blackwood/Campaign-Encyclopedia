package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.UserDisplay;

/**
 *
 * @author adam
 */
public class DisplayCampaignHelper {

    static void displayCampaign(UserDisplay display, CampaignDataManager cdm, Campaign campaign) {
        display.clearAllData();
        cdm.setData(campaign);
        display.displayCampaign(campaign);
    }
}
