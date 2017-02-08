package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.CampaignDataManagerListener;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.TimelineEntry;
import java.awt.BorderLayout;
import java.awt.Component;
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
    
    /** True if this CanvasViewer should put its canvas in a scroll pane, false otherwise. */
    private boolean m_putCanvasInScrollPane;

    /**
     * Constructor.
     * @param canvas the canvas.
     * @param cdm a campaign data manager for getting data.
     * @param title the title of the window.
     * @param size the size this viewer should be.
     * @param putCanvasInScrollPane true if this CanvasViewer should put its canvas in a scroll pane.
     */
    public CanvasViewer(CanvasDisplay canvas, CampaignDataManager cdm, String title, Dimension size, boolean putCanvasInScrollPane) {
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
        m_putCanvasInScrollPane = putCanvasInScrollPane;

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
        Component canvasComponent = m_canvas.getComponent();
        canvasComponent.setFocusable(true);
        canvasComponent.requestFocusInWindow();
        
        if (m_putCanvasInScrollPane) {
            m_frame.add(new JScrollPane(canvasComponent), BorderLayout.CENTER);
        } else {
            m_frame.add(canvasComponent, BorderLayout.CENTER);
        }
        
        
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