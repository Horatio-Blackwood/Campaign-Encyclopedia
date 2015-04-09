package campaignencyclopedia.display.swing.graphical;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author adam
 */
class RecentHistory {
    
    List<UUID> m_recent;
    int m_current;
    
    RecentHistory(List<UUID> recent, int current) {
        m_recent = recent;
        m_current = current;
    }
}