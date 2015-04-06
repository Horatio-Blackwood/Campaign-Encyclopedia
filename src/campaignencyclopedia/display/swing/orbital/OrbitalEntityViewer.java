package campaignencyclopedia.display.swing.orbital;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import toolbox.display.DisplayUtilities;

/**
 * A graphical, Entity viewer that shows Entities and their relationships in an orbital fashion.
 * @author adam
 */
public class OrbitalEntityViewer {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(OrbitalEntityViewer.class.getName());
    
    /** The starting dimensions of the top-level window. */
    private static final Dimension m_windowSize = new Dimension(900, 675);
    
    /** The JFrame of this display. */
    private JFrame m_frame;
    
    /** The orbital canvas upon which the data will be rendered. */
    private final DynamicOrbitalEntityCanvas m_canvas;
    
    /**
     * Creates a new OrbitalEntityViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param da the data accessor to fetch data to view.
     * @param toView the ID of the initial Entity to view.
     */
    public OrbitalEntityViewer(EntityDisplay display, DataAccessor da, UUID toView ) {
        m_canvas = new DynamicOrbitalEntityCanvas(display, da, toView);
        initialize();
    }
    
    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, m_windowSize);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex) {
            LOGGER.log(Level.CONFIG, "Unable to load application icon.", ex);
        }
        m_frame.setVisible(true);
    }
    
    private void initialize() {
        m_frame = new JFrame("Orbital Viewer");
        m_frame.setLayout(new BorderLayout());
        m_frame.setPreferredSize(m_windowSize);
        m_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        m_frame.add(m_canvas, BorderLayout.CENTER);
    }
    
}
