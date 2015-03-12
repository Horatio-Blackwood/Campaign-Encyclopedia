package campaignencyclopedia.display;

import java.util.UUID;

/**
 * An interface defining a display for Entity data.
 * @author adam
 */
public interface EntityDisplay {

    /**
     * Returns the ID of the currently displayed Entity or null, if no Entity is shown.
     * @return the ID of the currently displayed Entity or null, if no Entity is shown.
     */
    public UUID getShownEntity();

    /**
     * Displays the entity associated with the supplied ID.  If no entity is found, the display is cleared.
     * @param entity the ID of the entity to show.
     */
    public void showEntity(UUID entity);


    /** Clears the entity currently displayed. */
    public void clearDisplayedEntity();

}
