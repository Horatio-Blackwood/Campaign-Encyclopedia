package campaignencyclopedia.data;

/**
 *
 * @author adam
 */
public enum Season implements Displayable, Comparable<Season> {
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall"),
    WINTER("Winter"),
    UNSET("Unset");

    /** The Display Name. */
    private final String m_displayName;

    /**
     * Creates a new Seson.
     * @param displayName the display name.
     */
    private Season(String displayName) {
        m_displayName = displayName;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return m_displayName;
    }
}
