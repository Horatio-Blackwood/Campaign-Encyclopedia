package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.CampaignDataManagerListener;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import traer.physics.Attraction;
import traer.physics.Particle;
import traer.physics.ParticleSystem;
import traer.physics.Spring;
import traer.physics.Vector3D;

/**
 * The canvas for the connected graph view of campaign entities.
 * @author keith
 * @author adam
 */
public class CampaignEntityGraphCanvas extends JComponent implements Scrollable, CampaignDataManagerListener {

    // RENDERING PARAMETERS
    /** How long to draw the lines between the dots. */
    private static final int DOT_LINE_LENGTH = 200;
    /** THe radius of the circles. */
    private static final int DOT_RADIUS = 20;
    /** A pad value. */
    private static final int PAD = 4;
    /** The scroll pad. */
    private static final int SCROLL_PAD = 100;
    /** The font to render entity names in. */
    private static final Font ENTITY_NAME_FONT = new Font("Arial", Font.PLAIN, 14);
    
    /** The max scroll increment for being scrollable. */
    private final int m_maxUnitIncrement = 5;
    /** The vertical translation of coordinates in the particle system to the viewer 
     *  system since you can't scroll to or use negative coordinates in Swing/Scrollable. */
    private float m_verticalScrollTranslation = 0.0f;
    /** The horizontal translation of coordinates in the particle system to the viewer 
     *  system since you can't scroll to or use negative coordinates in Swing/Scrollable. */
    private float m_horizontalScrollTranslation = 0.0f;
    /** The scale factor used for zooming. */
    private float m_scaleFactor = 1.0f;
    
    
    // PHYSICS PARAMETERS
    /** The particle physics system. */
    private ParticleSystem m_particleSystem;
    /** The last time the system ticked, in miliseconds. */
    private long m_previousUpdateTime;
    /** The gravity value. */
    private static final float GRAVITY = 0.0f;
    /** Amount of drag. */
    private static final float DRAG = 10.0f;
    /** The amount of repulsive force between graph nodes. */
    private static final float REPULSIVE_FORCE = -1000;
    /** The minimum repulsive distance. */
    private static final float MIN_REPULSIVE_DISTANCE = 30;
    /** The strength of the springs which hold the nodes together. */
    private static final float SPRING_STRENGTH = 0.3f;
    /** The amount of spring dampening. */
    private static final float SPRING_DAMPENING = 0.4f;
    /** The current particle that has been clicked, used for dragging. */
    private Particle m_currentParticle = null;
    /** Value used to determine initial X position. */
    private static final int X_RANGE = 300;
    /** Value used to determine initial Y position. */
    private static final int Y_RANGE = 300;
    /** The mass of the particle. */
    private static final int PARTICLE_MASS = 20;
    /** Whether or not the nodes are frozen */
    private boolean m_onLockdown = false;


    // GENERAL MEMBERS
    /** A map of Entity UUIDs to their rendering configurations. */
    private final Map<UUID, RenderingConfig> m_renderingConfigMap;
    /** The entity currently hovered over. */
    private Entity m_hoveredEntity;
    /** The point currently hovered over. */
    private Point2D.Double m_hoverPoint;
    /** A data accessor for fetching data. */
    private final DataAccessor m_accessor;
    /** An EntityDisplay to show/edit Entity data on/with. */
    private final EntityDisplay m_display;
    /** Random number generator. */
    private final Random m_rand = new Random();
    /** Executor for tasks such as the update loop. */
    private final ScheduledExecutorService m_ses = Executors.newSingleThreadScheduledExecutor();
    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(CampaignEntityGraphCanvas.class.getName());
    
    /**
     * Creates a new instance of Orbital Entity Canvas.
     * @param display an entity display to show Entity data on.
     * @param accessor a data accessor to fetch Entity data from.
     */
    public CampaignEntityGraphCanvas(EntityDisplay display, DataAccessor accessor) {
        if (display == null) {
            throw new IllegalArgumentException("Parameter 'initialId' cannot be null.");
        }
        if (accessor == null) {
            throw new IllegalArgumentException("Parameter 'accessor' cannot be null.");
        }
        // Init required variables.
        m_accessor = accessor;
        m_display = display;
        m_renderingConfigMap = new HashMap<>();
        
        //Initialize physics
        m_particleSystem = new ParticleSystem(GRAVITY, DRAG);
        m_particleSystem.setIntegrator(ParticleSystem.RUNGE_KUTTA);
        
        //Initialize entities
        initializeEntities();
        
        //Set up rendering update loop
        m_previousUpdateTime = System.currentTimeMillis();
        m_ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                update(currentTime - m_previousUpdateTime);
                m_previousUpdateTime = currentTime;
                revalidate();
                repaint();
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
        
        initializeKeyListener();
        initializeMouseListener();
    }

    /** Load up all of the existing entity data for rendering. */
    public final void initializeEntities() {
        
        List<Entity> allEntities = m_accessor.getAllEntities();
        
        //Entities: create a particle in the system and a configuration for rendering
        for (Entity e : allEntities) {
            Particle p = createParticle(m_rand.nextInt(X_RANGE), m_rand.nextInt(Y_RANGE));
            int r = getDotRadius();
            RenderingConfig rc = new RenderingConfig();
            rc.text = e.getName();
            rc.dot = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
            rc.particle = p;
            rc.color = Colors.getColor(e.getType());
            m_renderingConfigMap.put(e.getId(), rc);
        }
        
        //Relationship Springs: create a spring between entities for every relationship
        for (Entity e : allEntities) {
            // Collect all relationships for this entity
            EntityData pubData = e.getPublicData();
            EntityData secretData = e.getSecretData();
            Set<Relationship> relationships = pubData.getRelationships();
            relationships.addAll(secretData.getRelationships());
            
            // Create a spring between the entity and what it is related to for each relationship.
            for (Relationship r : relationships) {
                Particle a = m_renderingConfigMap.get(e.getId()).particle;
                RenderingConfig otherRc = m_renderingConfigMap.get(r.getIdOfRelation());
                if (otherRc == null) {
                    LOGGER.warning("Found a relationship pointing to a null entity on " + e.getName() + 
                            "(" + e.getId() + ") pointing to:  " + "(" + r.getIdOfRelation().toString() + ")");
                    continue; 
                }
                Particle b = m_renderingConfigMap.get(r.getIdOfRelation()).particle;
                m_particleSystem.makeSpring(a, b, SPRING_STRENGTH, SPRING_DAMPENING, getDotLineLength());
            }
        }
    }
    
    /**
     * Creates a particle at the given x and y coordinates that has the set repulsive force applied against all other particles.
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @return the created particle.
     */
    private Particle createParticle(int x, int y) {
        // Z axis is always zero, as this is a 2D graph.
        Particle newParticle = m_particleSystem.makeParticle(PARTICLE_MASS, x, y, 0);
        
        //Add repulsion against all other particles
        for (int i = 0; i < m_particleSystem.numberOfParticles(); i++) {
            Particle p = m_particleSystem.getParticle(i);
            // Ignore "making attraction" on this particle against itself.
            if (p.equals(newParticle)) {
                continue;
            }
            m_particleSystem.makeAttraction(p, newParticle, REPULSIVE_FORCE, MIN_REPULSIVE_DISTANCE);
        }
        
        return newParticle;
    }

    /**
     * Called to tick the particle system processing by dt. (time delta currently ignored, system ticks by "1" unit)
     * @param dt the delta time since the last call to update.
     */
    private void update(long dt) {
        m_particleSystem.tick();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Rendering stuff
        Graphics2D g2 = (Graphics2D)g;
       
        //Translate by scroll amount
        g2.scale(m_scaleFactor, m_scaleFactor);
        g2.translate(m_horizontalScrollTranslation, m_verticalScrollTranslation);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);        
        
        //Draw Springs
        g2.setPaint(Colors.LINE);
        for (int i = 0; i < m_particleSystem.numberOfSprings(); i++) {
            Spring s = m_particleSystem.getSpring(i);
            drawSpring(s, g2);
        }
        
        //Draw entities
        for (UUID id : m_renderingConfigMap.keySet()) {
            RenderingConfig rc = m_renderingConfigMap.get(id);
            drawRenderingConfig(rc, g2);
        }
    }
    
    /**
     * Draws the given rendering configuration on the canvas
     * @param rc The config to draw.
     * @param g2 The graphics instance used to draw.
     */
    private void drawRenderingConfig(RenderingConfig rc, Graphics2D g2) {
        g2.setColor(rc.color);
        drawParticle(rc.particle, g2);
        Point2D center = new Point2D.Double((int)rc.particle.position().x(), (int)rc.particle.position().y());
        
        // Render Central Entity License Plate
        // - The background
        g2.setFont(ENTITY_NAME_FONT);
        FontMetrics bigFontMetrics = g2.getFontMetrics();
        g2.setPaint(Color.WHITE);
        
        int licensePlateWidth = DOT_RADIUS * 3;
        boolean licensePlateUsedMinWidth = true;
        if (bigFontMetrics.stringWidth(rc.text) > licensePlateWidth) {
            licensePlateWidth = bigFontMetrics.stringWidth(rc.text);
            licensePlateUsedMinWidth = false;
        }
        int licensePlateHeight = bigFontMetrics.getHeight();
        
        double anchorX = center.getX() - ((double)licensePlateWidth / 2.0) - PAD;
        double anchorY = center.getY() - ((double)licensePlateHeight / 2.0);
        g2.fill(new Rectangle2D.Double(anchorX, anchorY, (licensePlateWidth + PAD * 2.0f), licensePlateHeight));
        // - The Border
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new Rectangle2D.Double(anchorX, anchorY, (licensePlateWidth + PAD * 2), licensePlateHeight));

        // Render Central Entity Text
        g2.setFont(ENTITY_NAME_FONT);
        //g2.drawString(rc.text, (int)anchorX + PAD, (int)anchorY + (licensePlateHeight) - PAD);
        
        if (licensePlateUsedMinWidth) {
            anchorX += (licensePlateWidth - bigFontMetrics.stringWidth(rc.text)) / 2.0f;
        }
        g2.drawString(rc.text, (int)anchorX + PAD, (int)anchorY + (licensePlateHeight) - PAD);
        
    }
    
    /**
     * Draws the given spring using the supplied Graphics2D object.
     * @param s the Spring to draw.
     * @param g2d the graphics object to draw the spring to.
     */
    private void drawSpring(Spring s, Graphics2D g2d) {
        Particle a = s.getOneEnd();
        Particle b = s.getTheOtherEnd();
        g2d.drawLine((int)a.position().x(), (int)a.position().y(), (int)b.position().x(), (int)b.position().y());
    }
    
    /**
     * Renders a the supplied particle with the supplied Graphics2D object.
     * The particle's position will be in the center of the circle drawn with radius given by {@link #getDotRadius()}.
     * @param p the particle to render.
     * @param g2d the G2D to use.
     */
    private void drawParticle(Particle p, Graphics2D g2d) {
        float r = getDotRadius();
        g2d.fillOval((int)(p.position().x() - r), (int)(p.position().y() - r), (int)(r * 2.0f), (int)(r * 2.0f));
    }

    /**
     * Returns the line length, this method is in place in case dynamic line lengths are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the length of the lines to render.
     */
    private int getDotLineLength() {
        return DOT_LINE_LENGTH;
    }

    /**
     * Returns the dot radius, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the radius of the dots to be rendered.
     */
    private int getDotRadius() {
        return DOT_RADIUS;
    }


    /**
     * Initializes a key listener for the Canvas.  Sets up zoom and lockdown hotkeys.
     */
    private void initializeKeyListener() {
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //Zoom hotkeys
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_PLUS ||
                        e.getKeyCode() == KeyEvent.VK_I ||
                        e.getKeyCode() == KeyEvent.VK_EQUALS) {
                        
                        m_scaleFactor += 0.1f;
                        System.out.println("+ : " + m_scaleFactor);
                    } else if (e.getKeyCode() == KeyEvent.VK_MINUS ||
                               e.getKeyCode() == KeyEvent.VK_K ||
                               e.getKeyCode() == KeyEvent.VK_UNDERSCORE) {
                        
                        m_scaleFactor -= 0.1f;
                        System.out.println("- : " + m_scaleFactor);
                    }
                }
                
                //Toggle lockdown
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    m_onLockdown = !m_onLockdown;
                    for (int i = 0; i < m_particleSystem.numberOfParticles(); i++) {
                        if (m_onLockdown) {
                            m_particleSystem.getParticle(i).makeFixed();
                        } else {
                            m_particleSystem.getParticle(i).makeFree();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }
    
    /**
     * Initializes the mouse listeners for grabbing and dragging nodes.
     */
    private void initializeMouseListener() {
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                //Get point coordinates
                Point click = me.getPoint();
                Vector3D clickVector = new Vector3D((click.x / m_scaleFactor - m_horizontalScrollTranslation),
                                                    (click.y / m_scaleFactor - m_verticalScrollTranslation),
                                                    0);
                int r = getDotRadius();
                int r2 = r * r;
                Particle clickedParticle = null;
                
                //Find if a particle was clicked on
                for (int i = 0; i < m_particleSystem.numberOfParticles(); i++) {
                    Particle p = m_particleSystem.getParticle(i);    
                    if (p.position().distanceSquaredTo(clickVector) <= (r2)) {
                        clickedParticle = p;
                        break;
                    }
                }
                //Fix clicked particle for dragging
                if (clickedParticle != null) {
                    clickedParticle.makeFixed();
                    m_currentParticle = clickedParticle;
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                //Unset the current particle being dragged when the mouse is released
                if (m_currentParticle != null) {
                    //Free the particle, unless the system is on lockdown
                    if (!m_onLockdown) {
                        m_currentParticle.makeFree();
                    }
                    m_currentParticle = null;
                }
            }
        });
        
        // Mouse Wheel Listener
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
                    m_currentParticle.position().set((me.getX() / m_scaleFactor) - m_horizontalScrollTranslation,
                                                     (me.getY() / m_scaleFactor) - m_verticalScrollTranslation,
                                                     0);
                }
            }
        });
        
        // Mouse Wheel Listener
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                if ((mwe.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK) {
                    m_scaleFactor -= mwe.getPreciseWheelRotation() / 100.0f;
                }
            }
        });
        
    }

    @Override
    public Dimension getPreferredSize() {
        int furthestLeft = Integer.MAX_VALUE;
        int furthestRight = Integer.MIN_VALUE;
        int furthestTop = Integer.MAX_VALUE;
        int furthestBottom = Integer.MIN_VALUE;
        
        //Find the furthest extents of the drawn graph
        for (RenderingConfig rc : m_renderingConfigMap.values()) {
            if (rc.particle.position().x() < furthestLeft) {
                furthestLeft = (int)rc.particle.position().x();
            }
            if (rc.particle.position().x() > furthestRight) {
                furthestRight = (int)rc.particle.position().x();
            }
            if (rc.particle.position().y() < furthestTop) {
                furthestTop = (int)rc.particle.position().y();
            }
            if (rc.particle.position().y() > furthestBottom) {
                furthestBottom = (int)rc.particle.position().y();
            }
        }
        
        //Set the translation adjustment factors, including edge padding
        if (m_currentParticle == null) {
            m_verticalScrollTranslation = (-furthestTop + SCROLL_PAD);
            m_horizontalScrollTranslation = (-furthestLeft + SCROLL_PAD);
        } else {
            //TODO figure out a way to not jerk the screen around
        }
        
        float horizontalGraphSpan = (furthestRight - furthestLeft);
        float verticalGraphSpan = (furthestBottom - furthestTop);
        
        int xDim = (int)((horizontalGraphSpan + 2*SCROLL_PAD) * m_scaleFactor);
        int yDim = (int)((verticalGraphSpan + 2*SCROLL_PAD) * m_scaleFactor);
        return new Dimension(xDim, yDim);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition - (currentPosition / m_maxUnitIncrement) * m_maxUnitIncrement;
            return (newPosition == 0) ? m_maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / m_maxUnitIncrement) + 1) * m_maxUnitIncrement - currentPosition;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - m_maxUnitIncrement;
        } else {
            return visibleRect.height - m_maxUnitIncrement;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    
    @Override
    public void dataRemoved(UUID id) {
        LOGGER.log(Level.INFO, "Data removed from graph display: " + id);
        
        RenderingConfig r = m_renderingConfigMap.get(id);
        
        //Remove any linked springs first
        int numSprings = m_particleSystem.numberOfSprings();
        Set<Spring> springsToRemove = new HashSet<>();
        for (int i = 0; i < numSprings; i++) {
            Spring spring = m_particleSystem.getSpring(i);
            if (spring.getOneEnd().equals(r.particle) || spring.getTheOtherEnd().equals(r.particle)) {
                springsToRemove.add(spring);
            }
        }
        for (Spring s : springsToRemove) {
            m_particleSystem.removeSpring(s);
        }
        
        //Remove any linked attractions
        int numAttractions = m_particleSystem.numberOfAttractions();
        Set<Attraction> attractionsToRemove = new HashSet<>();
        for (int i = 0; i < numAttractions; i++) {
            Attraction attraction = m_particleSystem.getAttraction(i);
            if (attraction.getOneEnd().equals(r.particle) || attraction.getTheOtherEnd().equals(r.particle)) {
                attractionsToRemove.add(attraction);
            }
        }
        for (Attraction a : attractionsToRemove) {
            m_particleSystem.removeAttraction(a);
        }
        
        //Remove particle
        m_particleSystem.removeParticle(r.particle);
        
        //Remove the rendering configuration
        m_renderingConfigMap.remove(id);
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        if (!m_renderingConfigMap.containsKey(entity.getId())) {
            //New entity for the display, initialize it
            LOGGER.info("Data added to graph display: " + entity.getId());
            //Initialize entity particle and rendering config
            Particle newParticle = createParticle(m_rand.nextInt(X_RANGE), m_rand.nextInt(Y_RANGE));
            if (m_onLockdown) {
                newParticle.makeFixed();
            }
            
            //Create an initial rendering config for the new entity
            int r = getDotRadius();
            RenderingConfig rc = new RenderingConfig();
            rc.particle = newParticle;
            rc.dot = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
            m_renderingConfigMap.put(entity.getId(), rc);
        }
        
        //Update the entity's relationships.
        LOGGER.info("Updating data in display in graph display: " + entity.getId());
        
        //Get RenderingConfig of the added or updated entity to update it
        RenderingConfig rc = m_renderingConfigMap.get(entity.getId());
        
        //Update general entity data
        rc.text = entity.getName();
        rc.color = Colors.getColor(entity.getType());
        
        //Update Relationship Springs
        //Remove any linked springs first to catch removed relationships TODO: store relationships and springs somehow to prevent having to do this
        int numSprings = m_particleSystem.numberOfSprings();
        Set<Spring> springsToRemove = new HashSet<>();
        for (int i = 0; i < numSprings; i++) {
            Spring spring = m_particleSystem.getSpring(i);
            if (spring.getOneEnd().equals(rc.particle) || spring.getTheOtherEnd().equals(rc.particle)) {
                springsToRemove.add(spring);
            }
        }
        for (Spring s : springsToRemove) {
            m_particleSystem.removeSpring(s);
        }
        
        // Collect all relationships
        EntityData pubData = entity.getPublicData();
        EntityData secretData = entity.getSecretData();
        Set<Relationship> relationships = pubData.getRelationships();
        relationships.addAll(secretData.getRelationships());        
        
        // Create a spring between the new entity's particle and the existing one for each relationship.
        for (Relationship relationship : relationships) {
            RenderingConfig otherRenderingConfig = m_renderingConfigMap.get(relationship.getIdOfRelation());
            if (otherRenderingConfig == null) {
                LOGGER.warning("Found a relationship pointing to a null entity on " + entity.getName() + 
                        "(" + entity.getId() + ") pointing to:  " + "(" + relationship.getIdOfRelation().toString() + ")");
                continue; 
            }
            Particle particleForRelatedEntity = otherRenderingConfig.particle;

            m_particleSystem.makeSpring(rc.particle, particleForRelatedEntity, SPRING_STRENGTH, SPRING_DAMPENING, getDotLineLength());
        }
    }
    
    
    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private String text;
        private Shape dot;
        private Color color;
        private Particle particle;
    }
}