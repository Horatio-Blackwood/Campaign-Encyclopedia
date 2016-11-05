package campaignencyclopedia.data;

import campaignencyclopedia.display.swing.graphical.Colors;
import java.awt.Color;
import java.util.Objects;
import java.util.UUID;

/**
 * The primary data type of this application, Entities can represent Player Characters, Non-Player Characters, Places,
 * or important game items.
 * @author adam
 */
public class Entity implements Comparable<Entity>, ColoredDisplayable {

    private final UUID m_id;
    private final EntityType m_type;
    private final String m_name;
    private final EntityData m_publicData;
    private final EntityData m_secretData;
    private final boolean m_isSecret;

    /**
     * Constructs a new Entity.
     * @param id the ID of the Entity.
     * @param name the name of the Entity.
     * @param type the EntityType of this Entity.
     * @param publicData the Entity's public data .
     * @param secretData the Entity's secret data
     * @param isSecret true if this Entity is a secret, false otherwise.
     */
    public Entity(UUID id, String name, EntityType type, EntityData publicData, EntityData secretData, boolean isSecret) {
        m_id = id;
        m_name = name;
        m_type = type;
        m_publicData = publicData;
        m_secretData = secretData;
        m_isSecret = isSecret;
    }

    public UUID getId() {
        return m_id;
    }

    public EntityType getType() {
        return m_type;
    }

    public EntityData getSecretData() {
        return m_secretData;
    }

    public EntityData getPublicData() {
        return m_publicData;
    }

    /**
     * Returns true if this Entity is secret, false otherwise.
     * @return true if this Entity is secret, false otherwise.
     */
    public boolean isSecret() {
        return m_isSecret;
    }

    /**
     * Returns the name of this Entity.
     * @return the name of this Entity.
     */
    public String getName() {
        return m_name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.m_id);
        hash = 29 * hash + Objects.hashCode(this.m_type);
        hash = 29 * hash + Objects.hashCode(this.m_name);
        hash = 29 * hash + Objects.hashCode(this.m_publicData);
        hash = 29 * hash + Objects.hashCode(this.m_secretData);
        hash = 29 * hash + (this.m_isSecret ? 1 : 0);
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
        final Entity other = (Entity) obj;
        if (!Objects.equals(this.m_id, other.m_id)) {
            return false;
        }
        if (this.m_type != other.m_type) {
            return false;
        }
        if (!Objects.equals(this.m_name, other.m_name)) {
            return false;
        }
        if (!Objects.equals(this.m_publicData, other.m_publicData)) {
            return false;
        }
        if (!Objects.equals(this.m_secretData, other.m_secretData)) {
            return false;
        }
        if (this.m_isSecret != other.m_isSecret) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Entity t) {
        String first = ComparisonTools.trimForSort(this.getName());
        String second = ComparisonTools.trimForSort(t.getName());
        return first.compareTo(second);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Color getColor() {
        return Colors.getColor(m_type);
    }

    @Override
    public String getDisplayString() {
        return m_name;
    }
}