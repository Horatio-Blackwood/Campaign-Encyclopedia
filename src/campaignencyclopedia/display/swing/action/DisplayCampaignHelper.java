package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.UserDisplay;

/**
 * A helper for showing Campaigns.
 * @author adam
 */
public class DisplayCampaignHelper {

    /**
     * Given a campaign, this helper sets the data in the data manager as well as the user display supplied.  Not
     * fully sure why I felt it was so important to have this helper, but I won't change it for now.
     * 
     * @param display the UserDisplay to show the Campaign on.
     * @param cdm the data manager to update with new campaign data.
     * @param campaign the Campaign to display.
     */
    static void displayCampaign(UserDisplay display, CampaignDataManager cdm, Campaign campaign) {
        cdm.setData(campaign);
        display.displayCampaign(campaign);
    }
}
