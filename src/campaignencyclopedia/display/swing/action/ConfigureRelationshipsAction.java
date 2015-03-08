package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.RelationshipDataManager;
import campaignencyclopedia.display.swing.RelationshipConfigEditorDialogContent;
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
public class ConfigureRelationshipsAction extends AbstractAction {


    private final Frame m_frame;

    public ConfigureRelationshipsAction(Frame parent) {
        super("Configure Relationships...");
        m_frame = parent;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final RelationshipConfigEditorDialogContent dc = new RelationshipConfigEditorDialogContent();
        Runnable commitRunnable = new Runnable() {
            @Override
            public void run() {
                RelationshipDataManager.replaceAllRelationships(dc.getRelationships());
            }
        };
        DialogCommitManager dcm = new OkCancelCommitManager(commitRunnable);
        DialogFactory.buildDialog(m_frame, "Configure Relationships", true, dc, dcm);
    }
}