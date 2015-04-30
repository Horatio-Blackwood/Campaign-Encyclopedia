package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.RelationshipOptionManager;
import campaignencyclopedia.display.swing.RelationshipConfigEditorDialogContent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import toolbox.display.dialog.DialogCommitManager;
import toolbox.display.dialog.DialogFactory;
import toolbox.display.dialog.OkCancelCommitManager;

/**
 * The action for configuring the relationships available in this campaign.
 * @author adam
 */
public class ConfigureRelationshipsAction extends AbstractAction {

    /** The top-level window to center dialogs created by this action over. */
    private final Frame m_frame;

    /**
     * Creates a new instance of the Configure Relationships Action.
     * @param parent the top-level window to center dialogs created by this action over.
     */
    public ConfigureRelationshipsAction(Frame parent) {
        super("Configure Relationships...");
        m_frame = parent;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        final RelationshipConfigEditorDialogContent dc = new RelationshipConfigEditorDialogContent();
        Runnable commitRunnable = new Runnable() {
            @Override
            public void run() {
                RelationshipOptionManager.replaceAllRelationships(dc.getRelationships());
            }
        };
        DialogCommitManager dcm = new OkCancelCommitManager(commitRunnable);
        DialogFactory.buildDialog(m_frame, "Configure Relationships", false, dc, dcm);
    }
}