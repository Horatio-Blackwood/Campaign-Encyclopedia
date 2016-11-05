package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.Dimension;
import java.util.UUID;

/**
 * A graphical, Entity viewer that shows Entities and their relationships in an orbital fashion.
 * @author adam
 */
public class OrbitalEntityViewer extends CanvasViewer {

    /**
     * Creates a new OrbitalEntityViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param cdm the data manager to fetch data to view.
     * @param toView the ID of the initial Entity to view.
     */
    public OrbitalEntityViewer(EntityDisplay display, CampaignDataManager cdm, UUID toView ) {
        super(new OrbitalEntityCanvas(display, cdm, toView), cdm, "Orbital Viewer", new Dimension(900, 675), false);
    }
    
    /** Closes this viewer frame.s */
    @Override
    public void clearAllData() {
        m_frame.dispose();
    }
}
