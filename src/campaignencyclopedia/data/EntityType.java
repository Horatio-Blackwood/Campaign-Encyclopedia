package campaignencyclopedia.data;

import campaignencyclopedia.display.swing.graphical.Colors;
import java.awt.Color;

/**
 * An enumeration of EntityTypes.
 * @author adam
 */
public enum EntityType implements ColoredDisplayable {
    NON_PLAYER_CHARACTER("NPC"),
    PLAYER_CHARACTER("PC"),
    PLACE("Place"),
    ITEM("Item"),
    EVENT("Event"),
    ORGANIZATION("Organization");

    /** The display String for the EntityType. */
    private final String m_displayString;

    /**
     * Creates a new EntityType.
     * @param display the display string.
     */
    private EntityType(String display) {
        m_displayString = display;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return m_displayString;
    }

    @Override
    public Color getColor() {
        return Colors.getColor(this);
    }
}