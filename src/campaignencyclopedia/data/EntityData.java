package campaignencyclopedia.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class representing
 * @author adam
 */
public class EntityData {

    /** Tags about this entity.  Used for searching. */
    private final Set<String> m_tags;

    /** The description of the Entity that owns this EntityData. */
    private final String m_description;

    /**
     * Creates a new instance of EntityData with the supplied attributes.
     * @param description the EntityData description.
     * @param tags the set of tags for this Entity Data.
     */
    public EntityData(String description, Set<String> tags) {
        if (description == null) {
            throw new IllegalArgumentException("Parameter description must not be null.");
        }
        if (tags == null) {
            throw new IllegalArgumentException("Parameter tags must not be null.");
        }
        m_tags = new HashSet<>(tags);
        m_description = description;
    }

    /**
     * Returns the Set of tags for this EntityData.
     * @return the Set of tags for this EntityData.
     */
    public Set<String> getTags() {
        return m_tags;
    }

    /**
     * Returns the description for this EntityData.
     * @return the description for this EntityData.s
     */
    public String getDescription() {
        return m_description;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.m_tags);
        hash = 13 * hash + Objects.hashCode(this.m_description);
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
        final EntityData other = (EntityData) obj;
        if (!Objects.equals(this.m_tags, other.m_tags)) {
            return false;
        }
        if (!Objects.equals(this.m_description, other.m_description)) {
            return false;
        }
        return true;
    }
}