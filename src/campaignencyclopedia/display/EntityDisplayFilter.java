package campaignencyclopedia.display;

import campaignencyclopedia.data.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 * A display filter for Entity data.
 * @author adam
 */
public class EntityDisplayFilter implements DataFilter<Entity> {
    
    /** A boolean indicating if secret data should pass the filter. */
    private final boolean m_includeSecretData;
    
    /** The search string. */
    private final String m_searchString;
    
    public EntityDisplayFilter(String searchString, boolean includeSecret) {
        if (searchString == null) {
            throw new IllegalArgumentException("Parameter 'searchString' must not be null.");
        }
        m_includeSecretData = includeSecret;
        m_searchString = searchString.trim().toLowerCase();
    }
    
    /**
     * Returns true if the supplied Entity passes the filter.
     * @param entity the Entity to check.
     * @return true if the supplied Entity passes the filter, false otherwise.
     */
    @Override
    public boolean accept(Entity entity) {
        
        // Never accept a null entity
        if (entity == null) {
            return false;
        }
        
        // If secret data is not to be included, and this Entity is secret, return false.
        if (!m_includeSecretData && entity.isSecret()) {
            return false;
        }

        // If the search string is contained in the name, accepted
        if (entity.getName().trim().toLowerCase().contains(m_searchString)) {
            return true;
        }
        
        // If the search string is contained in the tags, accept it.
        Set<String> allTags = new HashSet<>();
        allTags.addAll(entity.getPublicData().getTags());
        // If secret data is to be included, include searching the secret tags
        if (m_includeSecretData) {
            allTags.addAll(entity.getSecretData().getTags());
        }
        for (String tag : allTags) {
            if (tag.trim().toLowerCase().contains(m_searchString)) {
                return true;
            }
        }
        
        // If the user-displayable Entity type string contains the search string, accept the Entity.
        if (entity.getType().getDisplayString().toLowerCase().contains(m_searchString)) {
            return true;
        }
        
        return false;
    }
    
}
