package campaignencyclopedia.display.swing.orbital;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import toolbox.display.DisplayUtilities;

/**
 * A graphical, Entity viewer that shows Entities and their relationships in an orbital fashion.
 * @author adam
 */
public class DynamicOrbitalEntityViewer {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(DynamicOrbitalEntityViewer.class.getName());
    
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
     */
    public DynamicOrbitalEntityViewer(EntityDisplay display, DataAccessor da) {
        m_canvas = new DynamicOrbitalEntityCanvas(display, da);
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
        
        m_canvas.setFocusable(true);
        m_canvas.requestFocusInWindow();
        JScrollPane canvasScroller = new JScrollPane(m_canvas);
        
        m_frame.add(canvasScroller, BorderLayout.CENTER);
    }
    
}
