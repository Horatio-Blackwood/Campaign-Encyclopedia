package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.display.UserDisplay;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * 
 * @author adam
 */
public class NavigateForwardAction extends AbstractAction {

    private final UserDisplay m_display;
    
    public NavigateForwardAction(UserDisplay display) {
        super("View Next Item");
        if (display == null) {
            throw new IllegalArgumentException("path cannot be null.");
        }
        m_display = display;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        m_display.navigateForward();
    }
}
