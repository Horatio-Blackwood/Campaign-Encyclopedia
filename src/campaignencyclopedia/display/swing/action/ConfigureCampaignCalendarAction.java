package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.display.swing.ConfigureCalendarDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * An action for configuring the calendar for this campaign.
 * @author adam
 */
public class ConfigureCampaignCalendarAction extends AbstractAction {

    /** The window to use to center dialogs launched by this action.  */
    private final Frame m_frame;

    /** The campaign data manager to update with new calendar information. */
    private final CampaignDataManager m_cdm;

    /**
     * Creates a new Configure Calendar Action.
     * @param parent the parent component, a top-level window, used for positioning dialogs launched by this action.
     * @param cdm the Campaign Data Manager.
     */
    public ConfigureCampaignCalendarAction(Frame parent, CampaignDataManager cdm) {
        super("Configure Calendar...");
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' may not be null.");
        }
        m_frame = parent;
        m_cdm = cdm;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        final ConfigureCalendarDialogContent dc = new ConfigureCalendarDialogContent(m_cdm.getData().getCalendar());
        Runnable commitRunnable = new Runnable() {
            @Override
            public void run() {
                m_cdm.updateCalendar(dc.getCalendar());
                SaveHelper.autosave(m_frame, m_cdm, enabled);
            }
        };
        DialogCommitManager dcm = new OkCancelCommitManager(commitRunnable);
        DialogFactory.buildDialog(m_frame, "Campaign Calendar Editor", false, dc, dcm);
    }
}
