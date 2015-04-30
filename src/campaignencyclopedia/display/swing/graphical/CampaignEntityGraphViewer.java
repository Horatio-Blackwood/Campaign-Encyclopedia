package campaignencyclopedia.display.swing.graphical;

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
import javax.swing.JScrollPane;
import toolbox.display.DisplayUtilities;

/**
 * A graphical, Entity viewer that shows Entities and their relationships in an graph.
 * @author adam
 * @author keith
 */
public class CampaignEntityGraphViewer implements CampaignDataManagerListener {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(CampaignEntityGraphViewer.class.getName());

    /** The starting dimensions of the top-level window. */
    private static final Dimension m_windowSize = new Dimension(900, 675);

    /** The JFrame of this display. */
    private JFrame m_frame;

    /** The orbital canvas upon which the data will be rendered. */
    private final CampaignEntityGraphCanvas m_canvas;

    /** A campaign data manager. */
    private final CampaignDataManager m_cdm;

    /**
     * Creates a new OrbitalEntityViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param cdm the data accessor to fetch data to view.
     */
    public CampaignEntityGraphViewer(EntityDisplay display, CampaignDataManager cdm) {
        m_canvas = new CampaignEntityGraphCanvas(display, cdm);
        m_cdm = cdm;
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
        m_frame = new JFrame("Campaign Graph Viewer");
        m_frame.setLayout(new BorderLayout());
        m_frame.setPreferredSize(m_windowSize);
        m_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        m_canvas.setFocusable(true);
        m_canvas.requestFocusInWindow();
        JScrollPane canvasScroller = new JScrollPane(m_canvas);
        m_frame.add(canvasScroller, BorderLayout.CENTER);
        m_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                m_cdm.removeListener(CampaignEntityGraphViewer.this);
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
