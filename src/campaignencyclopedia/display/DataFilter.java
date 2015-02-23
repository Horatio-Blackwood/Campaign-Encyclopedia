package campaignencyclopedia.display;

/**
 * A simple, generic filter interface.
 * @author adam
 * @param <T>
 */
public interface DataFilter<T extends Object> {
    
    /**
     * Returns true if the supplied item passes the filter.
     * @param item the item to check.
     * @return true if the supplied item passes the filter, false otherwise.
     */
    public boolean accept(T item);
}
