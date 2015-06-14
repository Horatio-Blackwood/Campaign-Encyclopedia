package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.display.RecentHistory;
import campaignencyclopedia.display.NavigationPath;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JComponent;

/**
 * A custom component that implements CanvasDisplay for displaying an entity and its adjacent relationships.
 * @author adam
 */
public class OrbitalEntityCanvas extends JComponent implements CanvasDisplay  {

    // RENDERING VALUES
    private static final int DOT_LINE_LENGTH = 225;
    private static final int TEXT_LINE_LENGTH = 265;
    private static final int CIRCLE_RADIUS = 40;
    private static final int PAD = 5;
    private static final int BIG_PAD = 15;
    private static final Font PRIMARY_ENTITY_FONT = new Font("Arial", Font.BOLD, 20);
    private static final String RELATIONSHIPS = "Relationships:";
    private static final Shape BACK_BUTTON = new Rectangle2D.Double(0, 0, 40, 20);
    private static final Shape FWD_BUTTON = new Rectangle2D.Double(40, 0, 40, 20);

    /** The user's navigation history.  Used to aid in navigating around the orbital display. */
    private NavigationPath m_path;

    /** The map of Entity IDs to rendering configuration objects.  Used to both render and handle user mouse interaction. */
    private final Map<UUID, RenderingConfig> m_renderingConfigMap;

    /** The shapes rendered for the current entity. Used to determine if the user has selected to edit this Entity. */
    private Shape m_currentEntityShape;

    /** Current Entity */
    private UUID m_currentEntity;

    /** The currently hovered over entity. */
    private UUID m_hoveredEntity;

    /** The position where the user is currently hovering. */
    private Point2D.Double m_hoverPoint;

    /** A data accessor. */
    private final DataAccessor m_accessor;

    /** An EntityDisplay to show Entity data on. */
    private final EntityDisplay m_display;



    /**
     * Creates a new instance of Orbital Entity Canvas.
     * @param display an entity display to show Entity data on.
     * @param accessor a data accessor to fetch Entity data from.
     * @param initialId the ID of the initial Entity to show.
     */
    public OrbitalEntityCanvas(EntityDisplay display, DataAccessor accessor, UUID initialId) {
        if (initialId == null) {
            throw new IllegalArgumentException("Parameter 'initialId' cannot be null.");
        }
        if (display == null) {
            throw new IllegalArgumentException("Parameter 'initialId' cannot be null.");
        }
        m_accessor = accessor;
        m_display = display;
        m_path = new NavigationPath(initialId);
        m_renderingConfigMap = new HashMap<>();

        initializeMouseListener();

        show(m_accessor.getEntity(initialId));
    }

    public final void show(Entity entity) {
        m_currentEntity = entity.getId();
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Rendering stuff
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics orignalFontMetrics = g2.getFontMetrics();
        Font originalFont = g2.getFont();
        Font boldFont = originalFont.deriveFont(Font.BOLD);

        // RENDER ENTITY
        if (m_currentEntity != null) {
            Entity current = m_accessor.getEntity(m_currentEntity);
            if (current != null) {


                // Clear the location map
                m_renderingConfigMap.clear();

                // Fetch some required values
                RelationshipManager currentRelMgr = m_accessor.getRelationshipsForEntity(m_currentEntity);
                Set<Relationship> relationships = new HashSet<>(currentRelMgr.getAllRelationships());

                Set<UUID> uniqueIds = new HashSet<>();
                for (Relationship rel : relationships) {
                    uniqueIds.add(rel.getRelatedEntity());
                }
                int relationshipCount = uniqueIds.size();

                Point2D.Double center = new Point2D.Double(getWidth() / 2, getHeight() / 2);
                int dotRadius = getDotRadius();
                int halfDotRadius = getDotRadius() / 2;
                float angle = 360.0f / relationshipCount;

                // Repopulate the location map.
                float currentAngle = 0;
                for (UUID id : uniqueIds) {
                    RenderingConfig config = new RenderingConfig();
                    config.dotPoint = getPoint(center, currentAngle, getDotLineLength());
                    config.textPoint = getPoint(center, currentAngle, getTextLineLength());
                    m_renderingConfigMap.put(id, config);
                    currentAngle += angle;
                }

                // Draw all of the lines and their relationship dots
                for (UUID id : m_renderingConfigMap.keySet()) {
                    Entity relatedTo = m_accessor.getEntity(id);
                    RenderingConfig rf = m_renderingConfigMap.get(id);
                    if (relatedTo != null) {
                        // Lines first
                        g2.setPaint(Colors.LINE);
                        g2.draw(new Line2D.Double(center.x, center.y, rf.dotPoint.x, rf.dotPoint.y));

                        // Then Dots
                        g2.setPaint(Colors.getColor(relatedTo.getType()));
                        rf.dot = new Ellipse2D.Double(rf.dotPoint.x - halfDotRadius, rf.dotPoint.y - halfDotRadius, dotRadius, dotRadius);
                        g2.fill(rf.dot);

                        // Then Text
                        g2.setPaint(Color.BLACK);
                        double strWidth = orignalFontMetrics.stringWidth(relatedTo.getName());
                        String name = relatedTo.getName();
                        if (rf.textPoint.x < center.x) {
                            g2.drawString(name, (float)(rf.textPoint.x - strWidth), (float)rf.textPoint.y);
                        } else {
                            g2.drawString(name, (float)rf.textPoint.x, (float)rf.textPoint.y);
                        }
                    }
                }

                // RENDER CURRENT PRIMARY ENTITY
                // --- Gather needed values

                // --- DOT
                g2.setPaint(Colors.getColor(current.getType()));
                m_currentEntityShape = new Ellipse2D.Double(center.x - dotRadius, center.y - dotRadius, dotRadius * 2, dotRadius * 2);
                g2.fill(m_currentEntityShape);

                // --- LICENSE PLATE
                // --- The background
                g2.setFont(PRIMARY_ENTITY_FONT);
                FontMetrics bigFontMetrics = g2.getFontMetrics();
                g2.setPaint(Color.WHITE);
                int licensePlateWidth = dotRadius * 3;
                boolean licensePlateUsedMinWidth = true;
                if (bigFontMetrics.stringWidth(current.getName()) > licensePlateWidth) {
                    licensePlateWidth = bigFontMetrics.stringWidth(current.getName());
                    licensePlateUsedMinWidth = false;
                }
                int licensePlateHeight = bigFontMetrics.getHeight();
                double x = center.x - (licensePlateWidth / 2) - PAD;
                double y = center.y - (licensePlateHeight / 2);
                g2.fill(new Rectangle2D.Double(x, y, (licensePlateWidth + PAD * 2), licensePlateHeight));
                // --- The Border
                g2.setPaint(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Rectangle2D.Double(x, y, (licensePlateWidth + PAD * 2), licensePlateHeight));

                // --- License Plate Entity Text
                g2.setFont(PRIMARY_ENTITY_FONT);
                float primaryEntityNameX = (float)center.x - (licensePlateWidth / 2);
                float primaryEntityNameY = (float)center.y + (licensePlateHeight / 2) + 1;
                if (licensePlateUsedMinWidth) {
                    primaryEntityNameX += (licensePlateWidth - bigFontMetrics.stringWidth(current.getName())) / 2.0f;
                }
                g2.drawString(current.getName(), primaryEntityNameX, primaryEntityNameY - PAD);


                // RENDER RELATIONSHIP HOVER DATA (IF VALID TO DO SO)
                if (m_hoveredEntity != null) {
                    Entity hovered = m_accessor.getEntity(m_hoveredEntity);
                    if (hovered != null) {
                        int maxWidth = orignalFontMetrics.stringWidth(RELATIONSHIPS);
                        List<String> hoverRelationships = new ArrayList<>();
                        hoverRelationships.add(RELATIONSHIPS);
                        for (Relationship rel : currentRelMgr.getPublicRelationships()) {
                            if (rel.getRelatedEntity().equals(m_hoveredEntity)) {
                                String line = "\n  - " + rel.getRelationshipText() + " " + m_accessor.getEntity(rel.getRelatedEntity()).getName();
                                hoverRelationships.add(line);
                                int stringWidth = orignalFontMetrics.stringWidth(line);
                                if (maxWidth < stringWidth) {
                                    maxWidth = stringWidth;
                                }
                            }
                        }
                        for (Relationship rel : currentRelMgr.getSecretRelationships()) {
                            if (rel.getRelatedEntity().equals(m_hoveredEntity)) {
                                String line = "\n  - " + rel.getRelationshipText() + " " + m_accessor.getEntity(rel.getRelatedEntity()).getName() + " (Secret)";
                                hoverRelationships.add(line);
                                int stringWidth = orignalFontMetrics.stringWidth(line);
                                if (maxWidth < stringWidth) {
                                    maxWidth = stringWidth;
                                }
                            }
                        }

                        // Background
                        int hoverWidth = maxWidth + BIG_PAD * 2;
                        int hoverHeight = hoverRelationships.size() * orignalFontMetrics.getHeight() + BIG_PAD;
                        g2.setPaint(Color.WHITE);
                        g2.setFont(originalFont);
                        g2.fill(new Rectangle2D.Double(m_hoverPoint.x, m_hoverPoint.y + BIG_PAD, hoverWidth, hoverHeight));

                        // Border
                        g2.setPaint(Color.BLACK);
                        g2.draw(new Rectangle2D.Double(m_hoverPoint.x, m_hoverPoint.y + BIG_PAD, hoverWidth, hoverHeight));

                        // Text
                        float hoverRelTextY = (float)m_hoverPoint.y + BIG_PAD + PAD;
                        for (String relString : hoverRelationships) {
                            hoverRelTextY += orignalFontMetrics.getHeight();
                            g2.drawString(relString, (float)m_hoverPoint.x + BIG_PAD, hoverRelTextY);
                        }
                    } else{
                        m_hoveredEntity = null;
                    }
                }
            } else {
                m_currentEntity = null;
            }
        } else {
            g2.drawString("No Data", this.getWidth() / 2, this.getHeight() / 2);
        }

        // Re BACK / FWD Buttons
        g2.setFont(originalFont);
        g2.setPaint(Color.WHITE);
        g2.fill(BACK_BUTTON);
        g2.setPaint(Color.BLACK);
        g2.draw(BACK_BUTTON);
        if (!m_path.isBackPossible()) {
            g2.setPaint(Color.GRAY);
        }
        g2.drawString("Back", 10.0f, 15.0f);

        g2.setPaint(Color.WHITE);
        g2.fill(FWD_BUTTON);
        g2.setPaint(Color.BLACK);
        g2.draw(FWD_BUTTON);
        if (!m_path.isForwardPossible()) {
            g2.setPaint(Color.GRAY);
        }
        g2.drawString("Fwd", 50.0f, 15.0f);

        // Render Navigation History
        RecentHistory recentHistory = m_path.getRecentHistory();
        g2.setFont(boldFont);
        g2.setPaint(Color.BLACK);
        float historyYpos = 20.0f + (recentHistory.getRecentHistory().size() * orignalFontMetrics.getHeight());
        for (int i = 0; i < recentHistory.getRecentHistory().size(); i++) {
            String name = m_accessor.getEntity(recentHistory.getRecentHistory().get(i)).getName();
            if (i == recentHistory.getCurrentIndex()) {
                g2.setFont(boldFont);
                g2.drawString(name, PAD, historyYpos);
                historyYpos = historyYpos - g2.getFontMetrics().getHeight();
            } else {
                g2.setPaint(Color.BLACK);
                g2.setFont(originalFont);
                g2.drawString(name, PAD, historyYpos);
                historyYpos = historyYpos - g2.getFontMetrics().getHeight();
            }
        }
    }

    private Point2D.Double getPoint(Point2D.Double center, double angle, double distance) {
        // Angles in java are measured clockwise from 3 o'clock.
        double theta = Math.toRadians(angle);
        Point2D.Double p = new Point2D.Double();
        p.x = center.x + distance*Math.cos(theta);
        p.y = center.y + distance*Math.sin(theta);
        return p;
    }

    private int getDotLineLength() {
        return DOT_LINE_LENGTH;
    }

    private int getTextLineLength() {
        return TEXT_LINE_LENGTH;
    }

    private int getDotRadius() {
        return CIRCLE_RADIUS;
    }

    private void initializeMouseListener() {
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                Point click = me.getPoint();
                if (BACK_BUTTON.contains(click)) {
                    if(m_path.back()) {
                        show(m_accessor.getEntity(m_path.getCurrentId()));
                    }
                } else if (FWD_BUTTON.contains(click)) {
                    if (m_path.forward()) {
                        show(m_accessor.getEntity(m_path.getCurrentId()));
                    }
                } else if (m_currentEntity != null &&
                           m_currentEntityShape != null &&
                           m_currentEntityShape.contains(click)) {
                    if (me.isControlDown()) {
                        m_display.showEntity(m_currentEntity);
                    }
                } else {
                    for (UUID id : m_renderingConfigMap.keySet()) {
                        RenderingConfig rc = m_renderingConfigMap.get(id);
                        if (rc.dot.contains(me.getPoint())) {
                            if (me.isControlDown()) {
                                m_display.showEntity(id);
                            } else {
                                m_path.add(id);
                                show(m_accessor.getEntity(id));
                            }

                            // Clear out hover data so we don't have any lingering displays.
                            m_hoveredEntity = null;
                            m_hoverPoint = null;
                            // Quit looking you found what you wanted.
                            break;
                        }
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent me) {

                // Find out if we're hovering over a given entity.
                boolean found = false;
                for (UUID id : m_renderingConfigMap.keySet()) {
                    RenderingConfig rc = m_renderingConfigMap.get(id);
                    if (rc.dot.contains(me.getPoint())) {
                        found = true;
                        m_hoveredEntity = id;
                        m_hoverPoint = new Point2D.Double(me.getX(), me.getY());
                        repaint();
                        break;
                    }
                }

                // Clear out the hovered entity if none exists
                if (found == false) {
                    m_hoveredEntity = null;
                    m_hoverPoint = null;
                    repaint();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void dataRemoved(UUID id) {
        if (id.equals(m_currentEntity)) {
            m_currentEntity = null;
            m_hoveredEntity = null;
        } else if (id.equals(m_hoveredEntity)) {
            m_hoveredEntity = null;
        }
        m_path.removeAll(id);
        repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void dataAddedOrUpdated(Entity entity) {
        repaint();
    }
    
    @Override
    public void timelineEntryAddedOrUpdated(TimelineEntry tle) {
        // ignored
    }

    @Override
    public void timelineEntryRemoved(UUID id) {
        // ignored
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void clearAllData() {
        // Do nothing.
    }

    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private Point2D.Double dotPoint;
        private Point2D.Double textPoint;
        private Shape dot;
    }
}