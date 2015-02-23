package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * The "Save As" Campaign action.
 * @author adam
 */
public class SaveCampaignAction extends AbstractAction {

    /** The CampaignDataManager to get the data from. */
    private CampaignDataManager m_cdm;

    /** True if secrets are to be included. */
    private boolean m_includeSecrets;

    /** The parent window to center dialogs launched over. */
    private Frame m_window;

    /**
     * Creates a new Save Campaign Action.
     * @param window the parent window to center dialogs in front of.
     * @param campaignManager the campaign data manager to get the data to save from.
     * @param name the action's display name.
     * @param includeSecrets true if secrets should be included in the saved data.
     */
    public SaveCampaignAction(Frame window, CampaignDataManager campaignManager, String name, boolean includeSecrets) {
        super(name);
        if (campaignManager == null) {
            throw new IllegalArgumentException("Parameter 'campaign' cannot be null.");
        }
        m_cdm = campaignManager;
        m_includeSecrets = includeSecrets;
        m_window = window;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        SaveHelper.save(m_window, m_cdm, m_includeSecrets);
    }
}