package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.graphical.timeline.IntegratedTimelineCanvas;
import campaignencyclopedia.display.swing.graphical.timeline.TimelineCanvasViewer;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * An action for displaying the timeline of the Campaign.
 * @author adam
 */
public class ShowTimelineAction extends AbstractAction {

    /** An EntityDisplay to show data on when the user views a given Entity. */
    private final EntityDisplay m_display;

    /** A data manager, to update with changed data. */
    private final CampaignDataManager m_cdm;

    /**
     * Creates a new 'Show Timeline Action.'
     * @param display the EntityDisplay used to show entities when the user requests it.
     * @param cdm a CampaignDataManager for managing any data changes.
     */
    public ShowTimelineAction(EntityDisplay display, CampaignDataManager cdm) {
        super("View Timeline...");
        m_cdm = cdm;
        m_display = display;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        // Build and Launch Dialaog
        IntegratedTimelineCanvas canvas = new IntegratedTimelineCanvas(m_display, m_cdm);
        TimelineCanvasViewer tcv = new TimelineCanvasViewer(canvas, m_display, m_cdm);
        tcv.launch();
    }
}