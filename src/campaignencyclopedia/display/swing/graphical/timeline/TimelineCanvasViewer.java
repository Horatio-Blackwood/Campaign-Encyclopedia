package campaignencyclopedia.display.swing.graphical.timeline;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.graphical.CanvasViewer;
import java.awt.Dimension;

/**
 * The viewer for the Graphical Timeline.
 * @author adam
 */
public class TimelineCanvasViewer extends CanvasViewer {
    
    /**
     * Creates a new instance of TimelineCanvasViewer.
     * @param display the EntityDisplay for this viewer.
     * @param cdm the CampaignDataManager to get the data from.
     */
    public TimelineCanvasViewer(IntegratedTimelineCanvas canvas, EntityDisplay display, CampaignDataManager cdm) {
       super(canvas, cdm, "Graphical Timeline", new Dimension(950, 650), false);        
        
       canvas.setParent(m_frame);
    }
    
    @Override
    public void clearAllData() {
        m_frame.dispose();
    }
}
