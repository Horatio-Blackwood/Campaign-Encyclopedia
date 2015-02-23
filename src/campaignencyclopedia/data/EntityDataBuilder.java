package campaignencyclopedia.data;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author adam
 */
public class EntityDataBuilder {

    private String m_description;
    private Set<String> m_tags;
    private Set<Relationship> m_relationships;

    /** Constructor. */
    public EntityDataBuilder() {
        m_description = "";
        m_tags = new HashSet<>();
        m_relationships = new HashSet<>();
    }
    
    /** Constructor. */
    public EntityDataBuilder(EntityData data) {
        m_description = data.getDescription();
        m_tags = data.getTags();
        m_relationships = data.getRelationships();
    }

    /**
     * Sets the description on this builder and returns itself.
     * @param description the description to set.
     * @return this builder.
     */
    public EntityDataBuilder description(String description) {
        m_description = description;
        return this;
    }

    public EntityDataBuilder relationships(Set<Relationship> relationships) {
        m_relationships.clear();
        m_relationships.addAll(relationships);
        return this;
    }

    public EntityDataBuilder tags(Set<String> tags ) {
        m_tags = tags;
        return this;
    }

    public EntityData build() {
        return new EntityData(m_description, m_tags, m_relationships);
    }
}