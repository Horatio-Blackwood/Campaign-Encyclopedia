package campaignencyclopedia.display;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.Entity;

/**
 * An interface which defines t he contract of a UserDisplay.
 * @author adam
 */
public interface UserDisplay extends EntityDisplay {

    /**
     * Removes the supplied Entity .
     * @param entity the Entity to remove.
     */
    public void removeEntity(Entity entity);

    /** Clears all data from the display.  NOTE:  This method does NOT clear out the data in the CampaignDataManager. */
    public void clearAllData();

    /**
     * Sets the supplied campaign on this display.
     * @param campaign the data to set.
     */
    public void displayCampaign(Campaign campaign);
    
    /** Navigates to the forward to the next displayed Entity. */
    public void navigateForward();
    
    /** Navigates to the forward to the previously displayed Entity. */
    public void navigateBackward();
}
