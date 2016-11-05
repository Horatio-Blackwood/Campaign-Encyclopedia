package campaignencyclopedia.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author adam
 */
public class RecentHistory {
    
    private List<UUID> m_recent;
    private int m_current;
    
    public RecentHistory(List<UUID> recent, int current) {
        if (recent == null) {
            throw new IllegalArgumentException("Param 'recent' cannot be null.");
        }
        if (current < 0) {
            throw new IllegalArgumentException("Param 'current' must be greater than or equal to zero.");
        }
        m_recent = new ArrayList<>(recent);
        m_current = current;
    }
    
    public List<UUID> getRecentHistory() {
        return Collections.unmodifiableList(m_recent);
    }
    
    public int getCurrentIndex() {
        return m_current;
    }
}