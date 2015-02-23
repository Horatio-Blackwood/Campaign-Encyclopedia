package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.TimelineDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCommitManager;

/**
 * An action for displaying the timeline of the Campaign.
 * @author adam
 */
public class ShowTimelineAction extends AbstractAction {
    
    /** A parent frame to center displays launched by this action on. */
    private final Frame m_parent;

    /** An EntityDisplay to show data on when the user views a given Entity. */
    private final EntityDisplay m_display;

    /** A data manager, to update with changed data. */
    private final CampaignDataManager m_cdm;

    /**
     * Creates a new 'Show Timeline Action.'
     * @param parent the parent window of dialogs launched by this action.
     * @param display the EntityDisplay used to show entities when the user requests it.
     * @param cdm a CampaignDataManager for managing any data changes.
     */
    public ShowTimelineAction(Frame parent, EntityDisplay display, CampaignDataManager cdm) {
        super("View Timeline...");
        m_cdm = cdm;
        m_parent = parent;
        m_display = display;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Runnable commit = new Runnable() {
            @Override
            public void run() {
            }
        };

        // Build and Launch Dialaog
        TimelineDialogContent tdc = new TimelineDialogContent(m_parent, m_cdm.getTimelineData(), m_display, m_cdm);
        OkCommitManager cm = new OkCommitManager(commit);
        DialogFactory.buildDialog(m_parent, "Campaign Timeline", false, tdc, cm);
    }
}