package campaignencyclopedia.display.swing.orbital;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
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
 *
 * @author adam
 */
public class OrbitalEntityCanvas extends JComponent {
    /** The color to render places in. */
    private static final Color PLACE_COLOR = new Color(64, 160, 64);

    /** The Color to render PCs. */
    private static final Color PC_COLOR = new Color(72, 72, 232);

    // Gold
    private static final Color NPC_COLOR = new Color(180, 64, 255);

    // Orange
    private static final Color ITEM_COLOR = new Color(255, 128, 64);

    // Crimson
    private static final Color EVENT_COLOR = new Color(160, 64, 64);

    // Grey
    private static final Color ORG_COLOR = new Color(128, 128, 128);

    private static final Color LINE_COLOR = new Color(84, 84, 84);

    private static final int DOT_LINE_LENGTH = 225;
    private static final int TEXT_LINE_LENGTH = 265;
    private static final int CIRCLE_RADIUS = 40;
    private static final int PAD = 5;
    private static final int BIG_PAD = 15;
    
    private static final Font PRIMARY_ENTITY_FONT = new Font("Arial", Font.BOLD, 20);
    
    private static final String RELATIONSHIPS = "Relationships:";
    
    private static final Shape BACK_BUTTON = new Rectangle2D.Double(0, 0, 40, 20);
    private static final Shape FWD_BUTTON = new Rectangle2D.Double(40, 0, 40, 20);

    // Current Data (should be smarter than this)
    // The path is the navigation history of how the user got to the current screen, and is navigable via mouse clicks.
    private NavigationPath m_path;

    // Clickable Points
    // here should be some sort of map of locations on this canvas that you can click and navigate, and a mouse listener
    // should check wit this data structure to see if the user clicked in a region that should result in an action
    private final Map<UUID, RenderingConfig> m_renderingConfigMap;

    // Current Entity
    private Entity m_currentEntity;
    
    private Entity m_hoveredEntity;
    private Point2D.Double m_hoverPoint;

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
        m_currentEntity = entity;
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // RENDER ENTITY
        if (m_currentEntity != null) {
            // Rendering stuff
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics orignalFontMetrics = g2.getFontMetrics();
            Font originalFont = g2.getFont();
            Font boldFont = originalFont.deriveFont(Font.BOLD);

            // Clear the location map
            m_renderingConfigMap.clear();

            // Fetch some required values
            Set<Relationship> relationships = new HashSet<>(m_currentEntity.getPublicData().getRelationships());
            relationships.addAll(m_currentEntity.getSecretData().getRelationships());

            Set<UUID> uniqueIds = new HashSet<>();
            for (Relationship rel : relationships) {
                uniqueIds.add(rel.getIdOfRelation());
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
                    g2.setPaint(LINE_COLOR);
                    g2.draw(new Line2D.Double(center.x, center.y, rf.dotPoint.x, rf.dotPoint.y));

                    // Then Dots
                    g2.setPaint(getColor(relatedTo.getType()));
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

            // Render Central Entity Dot
            g2.setPaint(getColor(m_currentEntity.getType()));
            g2.fill(new Ellipse2D.Double(center.x - dotRadius, center.y - dotRadius, dotRadius * 2, dotRadius * 2));

            // Render Central Entity License Plate
            // - The background
            g2.setFont(PRIMARY_ENTITY_FONT);
            FontMetrics bigFontMetrics = g2.getFontMetrics();
            g2.setPaint(Color.WHITE);
            int width = bigFontMetrics.stringWidth(m_currentEntity.getName());
            int height = bigFontMetrics.getHeight();
            double x = center.x - (width / 2) - PAD;
            double y = center.y - (height / 2);
            g2.fill(new Rectangle2D.Double(x, y, (width + PAD * 2), height));
            // - The Border
            g2.setPaint(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Rectangle2D.Double(x, y, (width + PAD * 2), height));

            // Render Central Entity Text
            g2.setFont(PRIMARY_ENTITY_FONT);
            g2.drawString(m_currentEntity.getName(), (float)center.x - (width / 2), (float)center.y + (height / 2) - PAD);
            
            
            
            // RENDER RELATIONSHIP HOVER DATA (IF VALID TO DO SO)
            if (m_hoveredEntity != null) {
                UUID hoveredEntityId = m_hoveredEntity.getId();
                int maxWidth = orignalFontMetrics.stringWidth(RELATIONSHIPS);
                List<String> hoverRelationships = new ArrayList<>();
                hoverRelationships.add(RELATIONSHIPS);
                for (Relationship rel : m_currentEntity.getPublicData().getRelationships()) {
                    if (rel.getIdOfRelation().equals(hoveredEntityId)) {
                        String line = "\n  - " + rel.getRelationship() + " " + m_accessor.getEntity(rel.getIdOfRelation()).getName();
                        hoverRelationships.add(line);
                        int stringWidth = orignalFontMetrics.stringWidth(line);
                        if (maxWidth < stringWidth) {
                            maxWidth = stringWidth;
                        }
                    }
                }
                for (Relationship rel : m_currentEntity.getSecretData().getRelationships()) {
                    if (rel.getIdOfRelation().equals(hoveredEntityId)) {
                        String line = "\n  - " + rel.getRelationship() + " " + m_accessor.getEntity(rel.getIdOfRelation()).getName() + " (Secret)";
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
                
            }
            
            // RENDER BACK / FWD Buttons
            g2.setFont(originalFont);
            g2.setPaint(Color.WHITE);
            g2.fill(BACK_BUTTON);
            g2.setPaint(Color.BLACK);
            g2.draw(BACK_BUTTON);
            g2.drawString("Back", 10.0f, 15.0f);

            g2.setPaint(Color.WHITE);
            g2.fill(FWD_BUTTON);
            g2.setPaint(Color.BLACK);
            g2.draw(FWD_BUTTON);
            g2.drawString("Fwd", 50.0f, 15.0f);      
            
            // Render Navigation History
            RecentHistory recentHistory = m_path.getRecentHistory();
            g2.setFont(boldFont);
            float historyYpos = 20.0f + (recentHistory.m_recent.size() * orignalFontMetrics.getHeight());
            for (int i = 0; i < recentHistory.m_recent.size(); i++) {
                String name = m_accessor.getEntity(recentHistory.m_recent.get(i)).getName();
                if (i == recentHistory.m_current) {
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

    private Color getColor(EntityType type) {
        switch (type) {
            case PLAYER_CHARACTER:
                return PC_COLOR;
            case NON_PLAYER_CHARACTER:
                return NPC_COLOR;
            case PLACE:
                return PLACE_COLOR;
            case ITEM:
                return ITEM_COLOR;
            case ORGANIZATION:
                return ORG_COLOR;
            case EVENT:
                return EVENT_COLOR;
        }
        throw new IllegalStateException("Received request to get color for unknown entity type:  " + type.name());
    }
    
    private void initializeMouseListener() {
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                Point click = me.getPoint();
                if (BACK_BUTTON.contains(click)) {
                    m_path.back();
                    show(m_accessor.getEntity(m_path.getCurrentId()));
                } else if (FWD_BUTTON.contains(click)) {
                    m_path.forward();
                    show(m_accessor.getEntity(m_path.getCurrentId()));
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
                        m_hoveredEntity = m_accessor.getEntity(id);
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

    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private Point2D.Double dotPoint;
        private Point2D.Double textPoint;
        private Shape dot;
    }
    
    

    /** Launches an orbital canvas window */
    public void launch() {

    }
}