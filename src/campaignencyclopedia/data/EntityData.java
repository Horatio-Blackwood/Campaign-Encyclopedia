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
    private final String m_description;
    private final Set<Relationship> m_relationships;

    public EntityData(String description, Set<String> tags, Set<Relationship> relationships) {
        if (description == null) {
            throw new IllegalArgumentException("Parameter description must not be null.");
        }
        if (tags == null) {
            throw new IllegalArgumentException("Parameter tags must not be null.");
        }
        if (relationships == null) {
            throw new IllegalArgumentException("Parameter relationships must not be null.");
        }
        m_tags = new HashSet<>(tags);
        m_description = description;
        m_relationships = relationships;
    }

    /**
     * Returns the Set of tags for this EntityData.
     * @return the Set of tags for this EntityData.
     */
    public Set<String> getTags() {
        return m_tags;
    }

    public String getDescription() {
        return m_description;
    }

    public Set<Relationship> getRelationships() {
        return m_relationships;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.m_tags);
        hash = 89 * hash + Objects.hashCode(this.m_description);
        hash = 89 * hash + Objects.hashCode(this.m_relationships);
        return hash;
    }

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
        if (!Objects.equals(this.m_relationships, other.m_relationships)) {
            return false;
        }
        return true;
    }


}