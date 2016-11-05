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

    private final String m_relationship;
    /** The ID of the Entity this Relationship is TO.*/
    private final UUID m_idOfRelatedEntity;
    /** The ID of the Entity that owns this relationship. */
    private final UUID m_idOfEntity;
    /** True if this relationship is secret, false otherwise. */
    private final boolean m_isSecret;


    /**
     * Constructor.
     * @param entity the UUID of the entity that this Entity is FROM.
     * @param relation the UUID of the Entity that this relationship is TO.
     * @param relationship the type of this Relationship.
     * @param isSecret true if this relationship is a secret one.
     */
    public Relationship(UUID entity, UUID relation, String relationship, boolean isSecret) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null.");
        }
        if (relation == null) {
            throw new IllegalArgumentException("Parameter 'relation' cannot be null.");
        }
        if (relationship == null) {
            throw new IllegalArgumentException("Parameter 'relationship' cannot be null.");
        }
        if (relationship.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'relationship' cannot be empty.");
        }
        m_relationship = relationship;
        m_idOfRelatedEntity = relation;
        m_idOfEntity = entity;
        m_isSecret = isSecret;
    }

    /**
     * Returns the type of this Relationship.
     * @return the type of this Relationship.
     */
    public String getRelationshipText() {
        return m_relationship;
    }

    /**
     * Returns the UUID of the Entity that is related to the owner.
     * @return the UUID of the Entity that is related to the owner.
     */
    public UUID getRelatedEntity() {
        return m_idOfRelatedEntity;
    }

    /**
     * Returns the UUID of Entity that owns this relationship.
     * @return the UUID of Entity that owns this relationship.
     */
    public UUID getEntityId() {
        return m_idOfEntity;
    }

    /**
     * Returns true if this relationship is secret, false otherwise.
     * @return true if this relationship is secret, false otherwise.
     */
    public boolean isSecret() {
        return m_isSecret;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.m_relationship);
        hash = 79 * hash + Objects.hashCode(this.m_idOfRelatedEntity);
        hash = 79 * hash + Objects.hashCode(this.m_idOfEntity);
        hash = 79 * hash + (this.m_isSecret ? 1 : 0);
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
        if (!Objects.equals(this.m_relationship, other.m_relationship)) {
            return false;
        }
        if (!Objects.equals(this.m_idOfRelatedEntity, other.m_idOfRelatedEntity)) {
            return false;
        }
        if (!Objects.equals(this.m_idOfEntity, other.m_idOfEntity)) {
            return false;
        }
        if (this.m_isSecret != other.m_isSecret) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Relationship t) {
        return m_relationship.compareTo(t.getRelationshipText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "rel-type:" + m_relationship + ", entity:" + m_idOfEntity + ", relation:" + m_idOfRelatedEntity.toString() + ",isSecret:" + m_isSecret;
    }
}