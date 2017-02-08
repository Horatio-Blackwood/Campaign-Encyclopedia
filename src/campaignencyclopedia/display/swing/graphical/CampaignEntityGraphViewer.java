package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.Dimension;

/**
 * A graphical, Entity viewer that shows Entities and their relationships in an graph.
 * @author adam
 * @author keith
 */
public class CampaignEntityGraphViewer extends CanvasViewer {
    /**
     * Creates a new CampaignEntityGraphViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param cdm the data accessor to fetch data to view.
     */
    public CampaignEntityGraphViewer(EntityDisplay display, CampaignDataManager cdm) {
        super(new CampaignEntityGraphCanvas(display, cdm), cdm, "Campaign Graph Viewer", new Dimension(900, 675), true);
    }

    @Override
    public void clearAllData() {
        m_frame.dispose();
    }
}
