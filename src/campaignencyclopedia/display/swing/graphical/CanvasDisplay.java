package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManagerListener;
import java.awt.Component;

/**
 *
 * @author adam
 */
public interface CanvasDisplay extends CampaignDataManagerListener {

    public Component getComponent();
}
