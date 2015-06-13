package campaignencyclopedia.data;

/**
 * An interface for data objects that are displayable as a String.  This interface allows for a toSring
 * to be different than its user DisplayString.
 * @author adam
 */
public interface Displayable {

    /**
     * Returns a displayable string for this object.
     * @return a displayable string for this object.
     */
    public String getDisplayString();

}
