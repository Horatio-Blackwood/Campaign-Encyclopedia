package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.CampaignDataManagerListener;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
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
 * An viewer for canvas displays.
 * @author adam
 */
public class CanvasViewer implements CampaignDataManagerListener {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(CanvasViewer.class.getName());

    /** THe title for the window */
    private String m_title;

    /** The JFrame of this display. */
    protected JFrame m_frame;

    /** The canvas to display in this viewer. */
    protected CanvasDisplay m_canvas;

    /** A Campaign Data Manager to use to get data. */
    protected CampaignDataManager m_cdm;

    /** The dimension of this window. */
    private Dimension m_size;

    /**
     * Constructor.
     * @param canvas the canvas.
     * @param cdm a campaign data manager for getting data.
     * @param title the title of the window.
     * @param size the size this viewer should be.
     */
    public CanvasViewer(CanvasDisplay canvas, CampaignDataManager cdm, String title, Dimension size) {
        if (canvas == null) {
            throw new IllegalArgumentException("Parameter 'canvas' cannot be null.");
        }
        if (title == null) {
            throw new IllegalArgumentException("Parameter 'title' cannot be null.");
        }
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' cannot be null.");
        }
        if (size == null) {
            throw new IllegalArgumentException("Parameter 'size' cannot be null.");
        }
        m_cdm = cdm;
        m_canvas = canvas;
        m_title = title;
        m_size = size;

        initialize();
    }

    /** Launches this viewer. */
    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, m_size);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex)     {
            LOGGER.log(Level.CONFIG, "Unable to load application icon.", ex);
        }
        m_frame.setVisible(true);
        m_cdm.addListener(this);
    }

    /** Initializes the components. */
    private void initialize() {
        m_frame = new JFrame(m_title);
        m_frame.setLayout(new BorderLayout());
        m_frame.setPreferredSize(m_size);
        m_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        m_frame.add(m_canvas.getComponent(), BorderLayout.CENTER);
        m_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                m_cdm.removeListener(CanvasViewer.this);
            }
        });
    }

    @Override
    public void dataRemoved(UUID id) {
        m_canvas.dataRemoved(id);
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        m_canvas.dataAddedOrUpdated(entity);
    }

    @Override
    public void timelineEntryAddedOrUpdated(TimelineEntry tle) {
        m_canvas.timelineEntryAddedOrUpdated(tle);
    }

    @Override
    public void timelineEntryRemoved(UUID id) {
        m_canvas.timelineEntryRemoved(id);
    }

    @Override
    public void clearAllData() {
        m_canvas.clearAllData();
    }
}