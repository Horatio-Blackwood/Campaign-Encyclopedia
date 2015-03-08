package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.swing.MonthConfigEditorDialogContent;
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
public class ConfigureMonthsAction extends AbstractAction {

    /** The window to use to center dialogs launched by this action.  */
    private final Frame m_frame;

    /** The campaign data manager to update with new calendar information. */
    private final CampaignDataManager m_cdm;

    public ConfigureMonthsAction(Frame parent, CampaignDataManager cdm) {
        super("Configure Months...");
        m_frame = parent;
        m_cdm = cdm;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final MonthConfigEditorDialogContent dc = new MonthConfigEditorDialogContent(m_cdm.getData().getCalendar());
        Runnable commitRunnable = new Runnable() {
            @Override
            public void run() {
                m_cdm.updateCalendar(dc.getCalendar());
                SaveHelper.autosave(m_frame, m_cdm, enabled);
            }
        };
        DialogCommitManager dcm = new OkCancelCommitManager(commitRunnable);
        DialogFactory.buildDialog(m_frame, "Configure Months", true, dc, dcm);
    }
}
