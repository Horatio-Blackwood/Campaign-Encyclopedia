package campaignencyclopedia.data;

import java.util.List;

/**
 * An accessor for configurable data.
 * @author adam
 * @param <T> The Type of Configurable data this accessor provides.
 */
public interface ConfigurableDataAccessor<T extends Comparable> {

    /**
     * Returns the default data for the configurable data.
     * @return the default data for the configurable data.
     */
    public List<T> getDefaultData();
}
