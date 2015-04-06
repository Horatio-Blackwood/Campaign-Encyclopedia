package campaignencyclopedia.display.swing.orbital;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import traer.physics.Particle;
import traer.physics.ParticleSystem;
import traer.physics.Spring;
import traer.physics.Vector3D;

/**
 *
 * @author adam
 */
public class DynamicOrbitalEntityCanvas extends JComponent {
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

    private static final int DOT_LINE_LENGTH = 175;
    private static final int CIRCLE_RADIUS = 20;
    private static final int PAD = 4;
    private static final int BIG_PAD = 15;
    
    private static final Font PRIMARY_ENTITY_FONT = new Font("Arial", Font.PLAIN, 14);
    
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

    
    private Entity m_hoveredEntity;
    private Point2D.Double m_hoverPoint;

    private final DataAccessor m_accessor;
    
    /** An EntityDisplay to show Entity data on. */
    private final EntityDisplay m_display;
    
    
    private final ScheduledExecutorService m_ses = Executors.newSingleThreadScheduledExecutor();
    private long m_previousUpdateTime;
    
    private ParticleSystem m_particleSystem;
    private Particle m_currentParticle = null;
    
    private final Random m_rand = new Random();

    
    /**
     * Creates a new instance of Orbital Entity Canvas.
     * @param display an entity display to show Entity data on.
     * @param accessor a data accessor to fetch Entity data from.
     * @param initialId the ID of the initial Entity to show.
     */
    public DynamicOrbitalEntityCanvas(EntityDisplay display, DataAccessor accessor, UUID initialId) {
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
        
        
        //Initialize physics
        m_particleSystem = new ParticleSystem(0.0f, 2.5f);
        m_particleSystem.setIntegrator(ParticleSystem.RUNGE_KUTTA);
        
        initializeEntities();
        
        //Set up update loop
        m_previousUpdateTime = System.currentTimeMillis();
        m_ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                update(currentTime - m_previousUpdateTime);
                m_previousUpdateTime = currentTime;
                repaint();
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
        
        //show(m_accessor.getEntity(initialId));
        initializeMouseListener();
    }

    public final void initializeEntities() {
        List<Entity> allEntities = m_accessor.getAllEntities();
        
        //Entities
        for (Entity e : allEntities) {
            Particle p = createParticle(300 + m_rand.nextInt(100), 200 + m_rand.nextInt(100));
            
            int r = getDotRadius();
            RenderingConfig rc = new RenderingConfig();
            rc.text = e.getName();
            rc.dot = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
            rc.particle = p;
            rc.color = getColor(e.getType());
            m_renderingConfigMap.put(e.getId(), rc);
        }
        
        //Relationship Springs
        for (Entity e : allEntities) {
            EntityData data = e.getPublicData();
            Set<Relationship> relationships = data.getRelationships();
            
            for (Relationship r : relationships) {
                Particle a = m_renderingConfigMap.get(e.getId()).particle;
                Particle b = m_renderingConfigMap.get(r.getIdOfRelation()).particle;
                
                m_particleSystem.makeSpring(a, b, 0.3f, 0.2f, getDotLineLength());
            }
        }
    }

    
    private void update(long dt) {
        m_particleSystem.tick();
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
        
        g2.setColor(Color.BLACK);
        //Draw Springs
        for (int i = 0; i < m_particleSystem.numberOfSprings(); i++) {
            Spring s = m_particleSystem.getSpring(i);
            drawSpring(s, g2);
        }
        
        
        for (UUID id : m_renderingConfigMap.keySet()) {
            RenderingConfig rc = m_renderingConfigMap.get(id);
            drawRenderingConfig(rc, g2);
        }

    }
    
    private Particle createParticle(int x, int y) {
        Particle newParticle = m_particleSystem.makeParticle(5, x, y, 0);
        
        for (int i = 0; i < m_particleSystem.numberOfParticles(); i++) {
            Particle p = m_particleSystem.getParticle(i);
            if (p.equals(newParticle)) {
                System.out.println("Skipping self");
                continue;
            }
            m_particleSystem.makeAttraction(p, newParticle, -200, 10);
        }
        
        return newParticle;
    }
    
    private void drawRenderingConfig(RenderingConfig rc, Graphics2D g2) {
        g2.setColor(rc.color);
        drawParticle(rc.particle, g2);
        Point2D center = new Point2D.Double((int)rc.particle.position().x(), (int)rc.particle.position().y());
        
        // Render Central Entity License Plate
        // - The background
        g2.setFont(PRIMARY_ENTITY_FONT);
        FontMetrics bigFontMetrics = g2.getFontMetrics();
        g2.setPaint(Color.WHITE);
        
        int stringWidth = bigFontMetrics.stringWidth(rc.text);
        int stringHeight = bigFontMetrics.getHeight();
        
        double anchorX = center.getX() - ((double)stringWidth / 2.0) - PAD;
        double anchorY = center.getY() - ((double)stringHeight / 2.0);
        g2.fill(new Rectangle2D.Double(anchorX, anchorY, (stringWidth + PAD * 2.0f), stringHeight));
        // - The Border
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new Rectangle2D.Double(anchorX, anchorY, (stringWidth + PAD * 2), stringHeight));

        // Render Central Entity Text
        g2.setFont(PRIMARY_ENTITY_FONT);
        g2.drawString(rc.text, (int)anchorX + PAD, (int)anchorY + (stringHeight) - PAD);
    }
    
    private void drawSpring(Spring s, Graphics2D g2d) {
        Particle a = s.getOneEnd();
        Particle b = s.getTheOtherEnd();
        
        g2d.drawLine((int)a.position().x(), (int)a.position().y(), (int)b.position().x(), (int)b.position().y());
    }
    
    private void drawParticle(Particle p, Graphics2D g2d) {
        float r = getDotRadius();
        g2d.fillOval((int)(p.position().x() - r), (int)(p.position().y() - r), (int)(r*2.0f), (int)(r*2.0f));
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
                Vector3D clickVector = new Vector3D(click.x, click.y, 0);
                int r = getDotRadius();
                int r2 = r*r;
                Particle clickedParticle = null;
                
                for (int i = 0; i < m_particleSystem.numberOfParticles(); i++) {
                    Particle p = m_particleSystem.getParticle(i);
                    
                    if (p.position().distanceSquaredTo(clickVector) <= (r2)) {
                        clickedParticle = p;
                        System.out.println("Clicked Particle: " + clickedParticle);
                        break;
                    }
                }
                
                if (clickedParticle != null) {
                    clickedParticle.makeFixed();
                    m_currentParticle = clickedParticle;
                }
                
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                if (m_currentParticle != null) {
                    m_currentParticle.makeFree();
                    m_currentParticle = null;
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
            
            @Override
            public void mouseDragged(MouseEvent me) {
                if (m_currentParticle != null) {
                    m_currentParticle.position().set(me.getX(), me.getY(), 0);
                }
            }
        });
    }

    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private String text;
        private Shape dot;
        private Color color;
        private Particle particle;
    }
    
    

    /** Launches an orbital canvas window */
    public void launch() {
        
    }

}