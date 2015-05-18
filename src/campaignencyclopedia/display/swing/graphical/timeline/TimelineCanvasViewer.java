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
    public TimelineCanvasViewer(EntityDisplay display, CampaignDataManager cdm) {
        super(new IntegratedTimelineCanvas(null, display, cdm), cdm, "Graphical Timeline", new Dimension(900, 675));
    }
}
