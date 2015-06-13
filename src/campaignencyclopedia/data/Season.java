package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adam
 */
public enum Season implements Displayable, Comparable<Season> {
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall"),
    WINTER("Winter");

    /** The Display Name. */
    private final String m_displayName;

    /**
     * Creates a new Seson.
     * @param displayName the display name.
     */
    private Season(String displayName) {
        m_displayName = displayName;
    }

    /**
     * Returns an ordered list of the Seasons.
     * @return an ordered list of the Seasons.
     */
    public static List<Month> getAsMonths() {
        List<Month> months = new ArrayList<>();
        int i = 0;
        for (Season season : values()) {
            months.add(new Month(season.getDisplayString(), i));
            i++;
        }
        return months;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return m_displayName;
    }
}
