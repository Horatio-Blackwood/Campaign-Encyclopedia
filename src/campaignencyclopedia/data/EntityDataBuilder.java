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

    /** Constructor. */
    public EntityDataBuilder() {
        m_description = "";
        m_tags = new HashSet<>();
    }

    /**
     * Constructor.
     * @param data the EntityData to start with.
     */
    public EntityDataBuilder(EntityData data) {
        m_description = data.getDescription();
        m_tags = data.getTags();
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

    public EntityDataBuilder tags(Set<String> tags ) {
        m_tags = tags;
        return this;
    }

    public EntityData build() {
        return new EntityData(m_description, m_tags);
    }
}