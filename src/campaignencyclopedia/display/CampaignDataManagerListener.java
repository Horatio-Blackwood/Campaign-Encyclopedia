package campaignencyclopedia.display;

import campaignencyclopedia.data.Entity;
import java.util.UUID;

/**
 *
 * @author adam
 */
public interface CampaignDataManagerListener {

    public void dataRemoved(UUID id);

    public void dataAddedOrUpdated(Entity entity);
}
