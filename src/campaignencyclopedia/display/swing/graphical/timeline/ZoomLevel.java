package campaignencyclopedia.display.swing.graphical.timeline;

/**
 *
 * @author adam
 */
enum ZoomLevel {
    MONTH("Month"),
    YEAR("Year"),
    DECADE("Decade"),
    CENTURY("Century");
    
    private String m_name;
    
    private ZoomLevel(String name) {
        m_name = name;
    }
    
    String getDisplayName() {
        return m_name;
    }
}