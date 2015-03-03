package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.NewCampaignDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * An action for creating a new Campaign.
 * @author adam
 */
public class NewCampaignAction extends AbstractAction {

    private final UserDisplay m_display;
    private final CampaignDataManager m_cdm;
    private final Frame m_frame;

    /**
     * Creates a NewCampaignAction.
     * @param frame the top level application window, used to center dialogs.
     * @param userDisplay the user display to set the new Campaign on, once created.
     * @param cdm the data manager, to set the new campaign data on once created.
     */
    public NewCampaignAction(Frame frame, UserDisplay userDisplay, CampaignDataManager cdm) {
        super("New...");
        m_frame = frame;
        m_cdm = cdm;
        m_display = userDisplay;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        final NewCampaignDialogContent content = new NewCampaignDialogContent();
        Runnable commit = new Runnable() {
            @Override
            public void run() {
                Campaign campaign = content.getCampaign();
                m_cdm.setFileName(null);
                DisplayCampaignHelper.displayCampaign(m_display, m_cdm, campaign);
            }
        };

        DialogCommitManager dcm = new OkCancelCommitManager(commit);
        DialogFactory.buildDialog(m_frame, "New Campaign", true, content, dcm);
    }

}
