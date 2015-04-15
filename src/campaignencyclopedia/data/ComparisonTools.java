package campaignencyclopedia.data;

/**
 *
 * @author adam
 */
public class ComparisonTools {
    
    /**
     * Trims the word 'The' from the beginning of Entity names so that it does not interfere with sorting.
     * 
     * ////  This should really be made configurable from within the application.  ////
     * 
     * @param toTrim the String to modify.
     * @return a trimmed string for comparison that only uses the parts of the string that matter for sorting.
     */
    public static String trimForSort(String toTrim) {
        if (toTrim.toLowerCase().startsWith("the ")) {
            toTrim = toTrim.substring(4);
        }
        if (toTrim.toLowerCase().startsWith("king ")) {
            toTrim = toTrim.substring(5);
        }
        if (toTrim.toLowerCase().startsWith("lord ")) {
            toTrim = toTrim.substring(5);
        }
        if (toTrim.toLowerCase().startsWith("lady ")) {
            toTrim = toTrim.substring(5);
        }
        return toTrim;
    }
}