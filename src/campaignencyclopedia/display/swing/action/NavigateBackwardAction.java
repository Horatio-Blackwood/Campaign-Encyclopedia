package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.display.UserDisplay;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author adam
 */
public class NavigateBackwardAction extends AbstractAction {

    private final UserDisplay m_display;
    
    public NavigateBackwardAction(UserDisplay display) {
        super("View Last Item");
        if (display == null) {
            throw new IllegalArgumentException("path cannot be null.");
        }
        m_display = display;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        m_display.navigateBackward();
    }
}