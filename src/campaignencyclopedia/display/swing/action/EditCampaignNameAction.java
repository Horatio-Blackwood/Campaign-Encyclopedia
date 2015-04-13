package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.EditCampaignNameDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 *
 * @author adam
 */
public class EditCampaignNameAction extends AbstractAction {

    private Frame m_parent;
    private CampaignDataManager m_cdm;
    private UserDisplay m_display;

    public EditCampaignNameAction(Frame parent, CampaignDataManager cdm, UserDisplay display) {
        super("Edit Name...");
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' can't be null.");
        }
        if (display == null) {
            throw new IllegalArgumentException("Parameter 'display' can't be null.");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parameter 'parent' can't be null.");
        }
        m_parent = parent;
        m_cdm = cdm;
        m_display = display;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final EditCampaignNameDialogContent dc = new EditCampaignNameDialogContent(m_cdm.getData().getName());
        Runnable ok = new Runnable() {
            @Override
            public void run() {
                Campaign original = m_cdm.getData();
                Campaign campaign = new Campaign(dc.getCampaignName(), original.getEntities(), original.getAllRelationships(), original.getTimelineEntries(), original.getCalendar());
                DisplayCampaignHelper.displayCampaign(m_display, m_cdm, campaign);
                SaveHelper.autosave(m_parent, m_cdm, true);
            }
        };
        DialogCommitManager dcm = new OkCancelCommitManager(ok);
        DialogFactory.buildDialog(m_parent, "Edit Campaign Name", true, dc, dcm);
    }
}