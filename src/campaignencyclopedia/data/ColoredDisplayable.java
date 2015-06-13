package campaignencyclopedia.data;

import java.awt.Color;

/**
 * An interface for Displayables that are associated with a single Color value.
 * @author adam
 */
public interface ColoredDisplayable extends Displayable {

    /**
     * Returns the Color associated with this Displayable.
     * @return the Color associated with this Displayable.
     */
    public Color getColor();
}
