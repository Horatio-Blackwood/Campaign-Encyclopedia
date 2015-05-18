package campaignencyclopedia.data;

import java.util.Objects;

/**
 *
 * @author adam
 */
public class Month implements Comparable<Month> {

    private final int m_index;
    private final String m_name;

    /**
     * Private constructor for internal use only.  DO NOT CHANGE THIS TO PUBLIC.
     * @param name
     */
    private Month(String name) {
        m_name = name;
        m_index = 0;
    }

    /**
     * Creates a new Month.
     * @param name the name of the month
     * @param index the index ordinal that defines the order this month appears in the year, must be greater than zero.
     * @throws IllegalArgumentException if index is zero or less or the name supplied is empty or null.
     */
    public Month(String name, int index) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'name' must not be null or empty was: '" + name + "'.");
        }
        m_index = index;
        m_name = name;
    }

    /**
     * Returns the Index of this month.
     * @return the Index of this month.
     */
    public int getIndex() {
        return m_index;
    }

    /**
     * Returns the name of this Month.
     * @return the name of this Month.
     */
    public String getName() {
        return m_name;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Month t) {
        if (m_index > t.getIndex()) {
            return 1;
        } else if (m_index < t.getIndex()) {
            return -1;
        }
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.m_index;
        hash = 79 * hash + Objects.hashCode(this.m_name);
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Month other = (Month) obj;
        if (this.m_index != other.m_index) {
            return false;
        }
        if (!Objects.equals(this.m_name, other.m_name)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_name;
    }
}
