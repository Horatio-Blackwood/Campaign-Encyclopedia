package campaignencyclopedia.display.swing.orbital;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.display.CampaignDataManagerListener;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class OrbitalEntityViewer implements CampaignDataManagerListener {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(OrbitalEntityViewer.class.getName());

    /** The starting dimensions of the top-level window. */
    private static final Dimension m_windowSize = new Dimension(900, 675);

    /** The JFrame of this display. */
    private JFrame m_frame;
    
    /** A campaign data manager. */
    private final CampaignDataManager m_cdm;

    /** The orbital canvas upon which the data will be rendered. */
    private final OrbitalEntityCanvas m_canvas;

    /**
     * Creates a new OrbitalEntityViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param cdm the data manager to fetch data to view.
     * @param toView the ID of the initial Entity to view.
     */
    public OrbitalEntityViewer(EntityDisplay display, CampaignDataManager cdm, UUID toView ) {
        m_canvas = new OrbitalEntityCanvas(display, cdm, toView);
        m_cdm = cdm;
        initialize();
    }

    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, m_windowSize);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex)     {
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
        m_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                m_cdm.removeListener(OrbitalEntityViewer.this);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void dataRemoved(UUID id) {
        m_canvas.dataRemoved(id);
    }

    /** {@inheritDoc} */
    @Override
    public void dataAddedOrUpdated(Entity entity) {
        m_canvas.dataAddedOrUpdated(entity);
    }

}
