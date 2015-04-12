package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.TimelineEntry;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import traer.physics.Particle;
import traer.physics.ParticleSystem;
import traer.physics.Spring;
import traer.physics.Vector3D;

/**
 * The canvas for the connected graph view of campaign entities.
 * @author keith
 * @author adam
 */
public class GraphicalTimelineCanvas extends JComponent implements Scrollable, CampaignDataManagerListener {

    // RENDERING PARAMETERS
    /** How long to draw the lines between the dots. */
    private static final int EVENT_LEADER_LINE_LENGTH = 30;
    /** The width of the timeline ticks. */
    private static final int TICK_WIDTH = 2;
    /** The height of the timeline ticks. */
    private static final int TICK_HEIGHT = 30;
    /** The thickness of the timeline. */
    private static final int TIMELINE_THICKNESS = 4;
    /** The initial vertical position of the timeline itself. */
    private static final int TIMELINE_VERTICAL_POSITION = 300;
    /** Value used to determine initial X position. */
    private static final int X_RANGE = 300;
    /** Value used to determine initial how wide a year is on the display. */
    private static final int m_yearScaleFactor = 30;
    /** A pad value for how many years extra to draw on either end of the timeline. */
    private static final int EXTRA_YEAR_PAD = 10;
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
    private static final float GRAVITY = -10.0f;
    /** Amount of drag. */
    private static final float DRAG = 30.0f;
    /** The amount of repulsive force between graph nodes. */
    private static final float REPULSIVE_FORCE = -400;
    /** The minimum repulsive distance. */
    private static final float MIN_REPULSIVE_DISTANCE = 30;
    /** The strength of the springs which hold the nodes together. */
    private static final float SPRING_STRENGTH = 0.3f;
    /** The amount of spring dampening. */
    private static final float SPRING_DAMPENING = 0.4f;
    /** The current particle that has been clicked, used for dragging. */
    private Particle m_currentParticle = null;    
    /** The mass of the particle. */
    private static final int PARTICLE_MASS = 20;
    /** Whether or not the nodes are frozen */
    private boolean m_onLockdown = false;



    // GENERAL MEMBERS
    /** A map of Entity UUIDs to their rendering configurations. */
    private final Map<UUID, RenderingConfig> m_renderingConfigMap;
    /** A map of years to the particle representing that year. */
    private final Map<Integer, Particle> m_yearParticleMap;
    /** The earliest year being displayed (i.e. the far left) */
    private int m_earliestYear;
    /** The latest year being displayed (i.e. the far right) */
    private int m_latestYear;
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
    private static final Logger LOGGER = Logger.getLogger(GraphicalTimelineCanvas.class.getName());
    
    /**
     * Creates a new instance of Orbital Entity Canvas.
     * @param display an entity display to show Entity data on.
     * @param accessor a data accessor to fetch Entity data from.
     */
    public GraphicalTimelineCanvas(EntityDisplay display, DataAccessor accessor) {
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
        m_yearParticleMap = new HashMap<>();
        
        //Initialize physics
        m_particleSystem = new ParticleSystem(GRAVITY, DRAG);
        m_particleSystem.setIntegrator(ParticleSystem.RUNGE_KUTTA);
        
        //Initialize entities
        m_earliestYear = Integer.MAX_VALUE;
        m_latestYear = Integer.MIN_VALUE;
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
        Set<TimelineEntry> allEntries = m_accessor.getTimelineData();
        List<TimelineEntry> sortedEntries = new ArrayList<>(allEntries);
        
        //Entities: create a particle in the system and a configuration for rendering
        Iterator<TimelineEntry> it = sortedEntries.listIterator();
        while (it.hasNext()) {
            TimelineEntry e = it.next();
            
            if (e.getYear() < m_earliestYear) {
                m_earliestYear = e.getYear();
            }
            if (e.getYear() > m_latestYear) {
                m_latestYear = e.getYear();
            }
            
            Particle p = createParticle(e.getYear() * m_yearScaleFactor, getTimelineVerticalPosition() - 2 * getTickHeight());
            //p.makeFixed();
            int r = getTickWidth();
            RenderingConfig rc = new RenderingConfig();
            rc.text = e.getTitle();
            rc.tick = new Rectangle2D.Double(0, 0, getTickWidth(), getTickHeight());
            rc.particle = p;
            rc.color = Colors.EVENT;
            m_renderingConfigMap.put(e.getId(), rc);
            
            if (!m_yearParticleMap.containsKey(e.getYear())) {
                //create year particle if one does not yet exist
                Particle yearParticle = createParticle(e.getYear() * m_yearScaleFactor, getTimelineVerticalPosition());
                yearParticle.makeFixed();
                m_yearParticleMap.put(e.getYear(), yearParticle);
            }
            //Link particle to year particle with springs
            m_particleSystem.makeSpring(p, m_yearParticleMap.get(e.getYear()), SPRING_STRENGTH, SPRING_DAMPENING, getEventLeaderLineLength());
        }
        
//        //Relationship Springs: create a spring between entities for every relationship
//        for (Entity e : allEntries) {
//            // Collect all relationships for this entity
//            EntityData pubData = e.getPublicData();
//            EntityData secretData = e.getSecretData();
//            Set<Relationship> relationships = pubData.getRelationships();
//            relationships.addAll(secretData.getRelationships());
//            
//            // Create a spring between the entity and what it is related to for each relationship.
//            for (Relationship r : relationships) {
//                Particle a = m_renderingConfigMap.get(e.getId()).particle;
//                RenderingConfig otherRc = m_renderingConfigMap.get(r.getIdOfRelation());
//                if (otherRc == null) {
//                    LOGGER.warning("Found a relationship pointing to a null entity on " + e.getName() + 
//                            "(" + e.getId() + ") pointing to:  " + "(" + r.getIdOfRelation().toString() + ")");
//                    continue; 
//                }
//                Particle b = m_renderingConfigMap.get(r.getIdOfRelation()).particle;
//                m_particleSystem.makeSpring(a, b, SPRING_STRENGTH, SPRING_DAMPENING, getDotLineLength());
//            }
//        }
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
        
        System.out.println("htrans: " + m_horizontalScrollTranslation + "  vtrans: " + m_verticalScrollTranslation);
        System.out.println("Height: " + getHeight());
        //Draw Timeline line
        g2.setColor(Color.BLACK);
        int timelineStart = (m_earliestYear - EXTRA_YEAR_PAD) * m_yearScaleFactor;
        int timelineEnd = (m_latestYear + EXTRA_YEAR_PAD) * m_yearScaleFactor;
        System.out.println("Drawing timeline at: " + timelineStart +  ", " + (getTimelineVerticalPosition() - getTimelineThickness() / 2));
        g2.fillRect(timelineStart, getTimelineVerticalPosition() - (getTimelineThickness() / 2), timelineEnd, getTimelineThickness());
        //ticks
        for (int i = timelineStart; i <= timelineEnd; i += m_yearScaleFactor) {
            g2.fillRect(i - getTickWidth() / 2, getTimelineVerticalPosition() - (getTickHeight() / 2), getTickWidth(), getTickHeight());
            g2.drawString(String.valueOf(i / m_yearScaleFactor), i, getTimelineVerticalPosition() + (getTickHeight()));
        }
        
        
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
        System.out.println("Drawing RC at: " + rc.particle.position().x() + ", " + rc.particle.position().y());
        
        g2.setColor(rc.color);
        drawParticle(rc.particle, g2);
        Point2D center = new Point2D.Double((int)rc.particle.position().x(), (int)rc.particle.position().y() - getEventLeaderLineLength());
        
        // Render Central Entity License Plate
        // - The background
        g2.setFont(ENTITY_NAME_FONT);
        FontMetrics bigFontMetrics = g2.getFontMetrics();
        g2.setPaint(Color.WHITE);
        
        int licensePlateWidth = TICK_WIDTH * 3;
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
    private int getEventLeaderLineLength() {
        return EVENT_LEADER_LINE_LENGTH;
    }

    /**
     * Returns the timeline thickness, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the thickness of the timeline to be rendered.
     */
    private int getTimelineThickness() {
        return TIMELINE_THICKNESS;
    }
    
    /**
     * Returns the timeline vertical position, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the vertical position of the timeline to be rendered.
     */
    private int getTimelineVerticalPosition() {
        return TIMELINE_VERTICAL_POSITION;
    }

    /**
     * Returns the dot radius, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the radius of the dots to be rendered.
     */
    private int getDotRadius() {
        return getTickWidth() * 2;
    }

    /**
     * Returns the tick width, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the width of the ticks to be rendered.
     */
    private int getTickWidth() {
        return TICK_WIDTH;
    }

    /**
     * Returns the tick height, this method is in place in case dynamic sizes are desired in the future this 
     * method can be updated but reliant code can remain the same.
     * @return the height of the ticks to be rendered.
     */
    private int getTickHeight() {
        return TICK_HEIGHT;
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
                    if (rc.tick.contains(me.getPoint())) {
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
        
        furthestLeft = m_earliestYear * m_yearScaleFactor;
        furthestRight = m_latestYear * m_yearScaleFactor;
        
        //Set the translation adjustment factors, including edge padding
        m_verticalScrollTranslation = 0;    //(-furthestTop + SCROLL_PAD);
        m_horizontalScrollTranslation = (-furthestLeft + SCROLL_PAD);
        
        float horizontalGraphSpan = (furthestRight - furthestLeft);
        float verticalGraphSpan = 2 * getTimelineVerticalPosition();    //(furthestBottom - furthestTop);
        
        int xDim = (int)((horizontalGraphSpan + 2*SCROLL_PAD) * m_scaleFactor);
        int yDim = (int)((verticalGraphSpan) * m_scaleFactor);
        
        System.out.println("width: " + xDim + "  height: " + yDim);
        System.out.println("fleft: " + furthestLeft + "  fright: " + furthestRight);
        
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
        
        //Remove particle
        m_particleSystem.removeParticle(r.particle);
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        if (!m_renderingConfigMap.containsKey(entity.getId())) {
            //New entity for the display, initialize it
            LOGGER.info("Data added to graph display: " + entity.getId());
            //Initialize entity particle and rendering config
            Particle newParticle = createParticle(m_rand.nextInt(X_RANGE), m_rand.nextInt(m_yearScaleFactor));
            if (m_onLockdown) {
                newParticle.makeFixed();
            }
            
            //Create an initial rendering config for the new entity
            int r = getTickWidth();
            RenderingConfig rc = new RenderingConfig();
            rc.particle = newParticle;
            rc.tick
 = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
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

            m_particleSystem.makeSpring(rc.particle, particleForRelatedEntity, SPRING_STRENGTH, SPRING_DAMPENING, getEventLeaderLineLength());
        }
    }
    
    
    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private String text;
        private Shape tick;
        private Color color;
        private Particle particle;
    }
}