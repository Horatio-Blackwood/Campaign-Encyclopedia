package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.swing.CampaignStatisticsDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCommitManager;

/**
 *
 * @author adam
 */
public class ShowCampaignStatisticsAction extends AbstractAction {

    /** The CampaignDataManager to load the data into. */
    private CampaignDataManager m_cdm;

    /** The parent window to center dialogs launched over. */
    private Frame m_window;
    
    public ShowCampaignStatisticsAction(Frame window, CampaignDataManager campaignManager) {
        super("Statistics...");
        if (campaignManager == null) {
            throw new IllegalArgumentException("Parameter 'campaign' cannot be null.");
        }
        m_cdm = campaignManager;
        m_window = window;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        final CampaignStatisticsDialogContent dc = new CampaignStatisticsDialogContent(m_cdm);
        DialogCommitManager dcm = new OkCommitManager();
        DialogFactory.buildDialog(m_window, "Campaign Statistics", false, dc, dcm);
    }
    
}
