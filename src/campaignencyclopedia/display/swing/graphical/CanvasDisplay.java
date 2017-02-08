package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManagerListener;
import javax.swing.JComponent;

/**
 *
 * @author adam
 */
public interface CanvasDisplay extends CampaignDataManagerListener {

    public JComponent getComponent();
}
