package campaignencyclopedia.data;

import java.util.Objects;
import java.util.UUID;

/**
 * A class that represents a Relationship between the Entity that has this relationship and the relation, which may be
 * any other Entity.  All Relationships are considered to be FROM the Entity holding the Relationship to the Entity that
 * is the relation.  If necessary, or helpful Relationships may be defined in both directions.
 *
 * @author adam
 */
public class Relationship implements Comparable<Relationship> {

    private final RelationshipType m_type;
    private final UUID m_id;


    /**
     * Constructor.
     * @param relation the UUID of the Entity that is the relation.
     * @param type the type of this Relationship.
     */
    public Relationship(UUID relation, RelationshipType type) {
        m_type = type;
        m_id = relation;
    }

    /**
     * Returns the type of this Relationship.
     * @return the type of this Relationship.
     */
    public RelationshipType getType() {
        return m_type;
    }

    /**
     * Returns the UUID of the Entity that is the relation.
     * @return the UUID of the Entity that is the relation.
     */
    public UUID getIdOfRelation() {
        return m_id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.m_type);
        hash = 67 * hash + Objects.hashCode(this.m_id);
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
        final Relationship other = (Relationship) obj;
        if (this.m_type != other.m_type) {
            return false;
        }
        if (!Objects.equals(this.m_id, other.m_id)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Relationship t) {
        return getType().getDisplayString().compareTo(t.getType().getDisplayString());
    }
    
    @Override
    public String toString() {
        return m_type.getDisplayString() + m_id.toString();
    }
}
