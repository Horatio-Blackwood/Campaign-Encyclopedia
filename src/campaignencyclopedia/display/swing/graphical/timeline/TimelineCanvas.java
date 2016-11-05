package campaignencyclopedia.display.swing.graphical.timeline;

import campaignencyclopedia.data.CampaignCalendar;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.swing.graphical.CanvasDisplay;
import campaignencyclopedia.display.swing.graphical.Colors;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 *
 * @author adam
 */
public class TimelineCanvas extends JComponent implements CanvasDisplay {

    // BASIC DATA
    /** The TimelineEntries to render. */
    private Map<TimelineDate, List<TimelineEntry>> m_data;
    /** A data accessor. */
    private DataAccessor m_da;
    /** The JScrollPane containing this component. */
    private JScrollPane m_scrollPane;

    
    // RENDERING CONSTANTS
    /** A pad value for padding other values. */
    private static final int PAD = 50;
    /** The number of pixels up from the bottom of the display that the timeline should be rendered. */
    private static final int TIMELINE_BOTTOM_OFFSET = 100;
    /** The Distance between each Date rendered on the timeline (Segment). */
    private static final int SEGMENT_X_VALUES = 100;
    /** The height of the timeline fencepost at each date (before adding any leaders/events) */
    private static final int FENCE_POST_HEIGHT = 30;
    /** The Default length of the leader lines (lines between the dots). */
    private static final int LEADER_LINE_LENGTH = 10;
    /** The diameter of the dots. */
    private static final int DOT_DIAMETER = 20;
    /** The color to render the lines in, currently a dark grey. */
    private static final Color LINE_COLOR = new Color(64, 64, 64);
    /** The Stroke to use to render the lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.5f);
    /** 
     * The increment value used to step up the Y-axis offset values when performing a timeline declutter operation.  The smaller 
     * this value is the more recursive calls are required to complete declutter, although finer steps may achieve a more attractive
     * decluttered image after processing.
     */
    private static final int Y_OFFSET_INCRFEMENT = 20;
    
    // PROCESSED OR CALCULATED DATA OR DATA USED FOR THE PURPOSE OF PROCESSING OR CALCULATING
    /** A map of TimelineDates to the key location information useful for rendering at that location. */
    private final Map<TimelineDate, DateLocationData> m_dateLocationData;
    /** A map of TimelineEntry IDs to their associated Rendering Configurations. */
    private final Map<UUID, RenderingConfig> m_renderingConfigs;
    /** The number of segments to render (timeline dates) on the timeline. */
    private int m_segmentCount = 0;
    /** The number of bonus segments to include at the end of the timeline (into the 'future') after all required dates are included. */
    private static final int SEGMENT_PAD = 5;
    /** A flag set to true if timeline data has been modified. */
    private boolean m_dataChanged = true;
    /** A flag set to true to indicate that the zoom level has changed. */
    private boolean m_zoomChanged = true;
    /** A flag set to true to indicate that the preferred size has been recalculated. */
    private boolean m_viewChanged = true;
    
    // RENDERING FILTER/FORMAT DATA
    /** A flag set to indicate if secret entries should also be rendered on this display. */
    private boolean m_includeSecretEntries = true;
    /** */
    private int m_earliestYear;
    /** */
    private int m_latestYear;
    /** The current zoom level.  The default is YEAR. */
    private ZoomLevel m_zoomLevel = ZoomLevel.YEAR;
    
    /** A comparator that sorts TimelineEntry objects by title. */
    private static final Comparator<TimelineEntry> ENTRY_COMPARATOR = new Comparator<TimelineEntry>() {
        @Override
        public int compare(TimelineEntry first, TimelineEntry second) {
            return first.getTitle().compareTo(second.getTitle());
        }
    };
    

    
    /**
     * Creates a new instance of TimelineCanvas.
     * 
     * @param da a DataAccessor for the accessing of data, must not be null.
     * @throws IllegalArgumentException if 'da' is null.
     */
    public TimelineCanvas(DataAccessor da) {
        if (da == null) {
            throw new IllegalArgumentException("Parameter 'da' cannot be null.");
        }
        m_da = da;
        
        // Init Data Maps
        m_data = new HashMap<>();
        m_dateLocationData = new HashMap<>();
        m_renderingConfigs = new HashMap<>();

        // Init scroll pane.
        m_scrollPane = new JScrollPane(this);
        m_scrollPane.getHorizontalScrollBar().setUnitIncrement(30);   
        m_scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        
        initialize();
    }
    
    /**
     * Sets the zoom level.
     * @param level the level to change to.
     */
    void setZoomLevel(ZoomLevel level) {
        m_zoomLevel = level;
        m_zoomChanged = true;
    }
    
    /**
     * Called to update the rendering of this display to indicate if secret TimelineEntry values should be rendered or not.
     * @param include true to include them, false to hide them.
     */
    void showSecretEntries(boolean include) {
        m_includeSecretEntries = include;
        m_dataChanged = true;
    }
    
    /** Initializes all required values, populates data maps and does any pre-processing required for rendering the timeline. */
    private void initialize() {
        // Initialize Data Map and all offsets to zero.
        m_data.clear();
        for (TimelineEntry tle : m_da.getTimelineData()) {
            if (!m_includeSecretEntries && tle.isSecret()) {
                continue;
            }
            if (tle.getYear() < m_earliestYear) {
                continue;
            }
            if (tle.getYear() > m_latestYear) {
                continue;
            }
            
            TimelineDate date = new TimelineDate(tle.getMonth(), tle.getYear());
            switch (m_zoomLevel) {
                case CENTURY:
                    date = date.getCenturyDate();
                    break;
                case DECADE:
                    date = date.getDecadeDate();
                    break;
                case YEAR:
                    date = date.getYearDate();
                    break;
                case MONTH:
                    // do nothing.
                    break;
            }
            
            if (m_data.get(date) == null) {
                m_data.put(date, new ArrayList<TimelineEntry>());
            }
            m_data.get(date).add(tle);
        }
        
        // Initialize Segment Count
        List<TimelineDate> dates = new ArrayList<>(m_data.keySet());
        Collections.sort(dates);
        calculateSegmentCount(dates);
        
        // Initialize Vertical Line Post Map
        calculateDateLocationData(m_segmentCount);
        
        // Initialize Rendering Configurations
        initRenderingConfigs();
        
        // Revalidate and repaint the component.
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        // Calculate the Preferred Size
        int currentYSpan = 0;
        for (RenderingConfig rc : m_renderingConfigs.values()) {
            // Fetch related offset
            int offset = m_dateLocationData.get(rc.date).dateYOffset;
            // Timeline Y Location - Dot Location, + date Y Offset + PAD value.
            int dotSpan = (getTimelineY() - (int)rc.dot.getY()) + offset + (3 * PAD);
            if (dotSpan > currentYSpan) {
                currentYSpan = dotSpan;
            }
        }
        
        // Determine new Width and Height (Y Span)
        int width  = (2 * PAD) + (m_segmentCount * SEGMENT_X_VALUES);

        // Indicate that the preferred size has been recalculated
        m_viewChanged = true;
        
        return new Dimension(width, currentYSpan);
    }
    
    /**
     * Dynamically get the Y position of the main timeline.
     * @return the main timeline's y position.
     */
    private int getTimelineY() {
        //return m_timelineY;
        return this.getHeight() - TIMELINE_BOTTOM_OFFSET;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        
        FontMetrics fontMetrics = g2d.getFontMetrics();
        
        // PREPARE BASIC VALUES FOR RENDERING
        List<TimelineDate> dates = new ArrayList<>(m_data.keySet());
        Collections.sort(dates);
        
        // Color Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        
        // UPDATE FOR ZOOM CHANGES, OR ANY CHANGED DATA
        // --- If data has changed since last update, recalcualte some key values.
        if (m_zoomChanged || m_dataChanged || m_viewChanged) {
            initialize();
            declutter(fontMetrics, true);
            m_zoomChanged = false;
            m_dataChanged = false;
            m_viewChanged = false;
        }
        
        // RENDER THE TIMELINE
        if (dates.size() > 1) {
            // --- LONG HORIZONTAL LINE
            // --- --- Get First and Last, and figure out how long the line should be.
            g2d.setStroke(LINE_STROKE);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(PAD, getTimelineY(), (SEGMENT_X_VALUES * m_segmentCount) - PAD, getTimelineY());

            // --- DRAW ALL VERTICAL FENCEPOSTS
            for (TimelineDate date : m_dateLocationData.keySet()) {
                DateLocationData dld = m_dateLocationData.get(date);
                int fencepostX = dld.dateXPos;
                g2d.drawLine(fencepostX, getTimelineY(), fencepostX, getTimelineY() - FENCE_POST_HEIGHT - dld.dateYOffset);
                g2d.drawString(dld.label, fencepostX, getTimelineY() + (PAD / 2));
            }
            
            // --- RENDER TIMELINE EVENTS
            // --- --- Render all Lines
            for (RenderingConfig rc : m_renderingConfigs.values()) {
                // --- --- LEADER LINE
                int yOffset = m_dateLocationData.get(rc.date).dateYOffset;
                g2d.drawLine(rc.leaderStart.x, rc.leaderStart.y - yOffset, rc.leaderEnd.x, rc.leaderEnd.y - yOffset);
            }
            
            // --- --- Render All Dots and Text
            for (RenderingConfig rc : m_renderingConfigs.values()) {
                int yOffset = m_dateLocationData.get(rc.date).dateYOffset;
                
                 // --- --- --- DOT
                Ellipse2D newDot = new Ellipse2D.Double(rc.dot.getX(), rc.dot.getY() - yOffset, DOT_DIAMETER, DOT_DIAMETER);
                g2d.setColor(rc.color);
                g2d.fill(newDot);
                g2d.setColor(LINE_COLOR);
                g2d.draw(newDot);
                
                // --- --- TEXT
                g2d.setColor(Color.WHITE);
                // Sorry for the magic number, but it seemed about right to help put the text background in place.
                g2d.fillRect(rc.textPoint.x, rc.textPoint.y - yOffset - fontMetrics.getHeight() - 1, fontMetrics.stringWidth(rc.text), fontMetrics.getHeight() + 8);
                g2d.setColor(LINE_COLOR);
                g2d.drawString(rc.text, rc.textPoint.x, rc.textPoint.y - yOffset);
            }

        } else {
            // Handle a zero or single timeline entry timeline.
        }
    }
    
    /**
     * Moves each rendered TimelineEntry such that they don't overlap one another.
     * @param metrics
     */
    private void declutter(FontMetrics metrics, boolean clearOffsets) {
        if (clearOffsets) {
            // Clear out all previous yOffsets
            for (DateLocationData dld : m_dateLocationData.values()) {
                dld.dateYOffset = 0;
            }
        }
        
        // Starting at the latest timeline event, work your 
        // way backwards, ensuring that text doesn't overlap.
        List<TimelineDate> dates = new ArrayList<>(m_data.keySet());
        Collections.sort(dates);
        Collections.reverse(dates);
        
        // Generate the entry placards/flags/what-have-you
        List<Rectangle2D> entryFlags = new ArrayList<>();
        for (TimelineDate date : dates) {
            int yOffset = m_dateLocationData.get(date).dateYOffset;
            List<TimelineEntry> entries = m_data.get(date);
            if (entries != null) {
                // --- Build The Rectangle for these Entries
                int maxWidth = 0;
                int totalHeight = 0;
                // For each entry at this date, find out the widest one and 
                // determine the total height of the entries.
                for (TimelineEntry tle : entries) {
                    int width =  DOT_DIAMETER + LEADER_LINE_LENGTH + metrics.stringWidth(tle.getTitle());
                    if (width > maxWidth) {
                        maxWidth = width;
                    }
                    
                    totalHeight += (DOT_DIAMETER + LEADER_LINE_LENGTH);
                }
                Rectangle2D rect = new Rectangle2D.Double(m_dateLocationData.get(date).dateXPos, getTimelineY() - FENCE_POST_HEIGHT - totalHeight - yOffset, maxWidth, totalHeight);
                
                // --- Check all previously constructed placards/flags/what-have-you against the new one.
                for (Rectangle2D other : entryFlags) {
                    if (other.intersects(rect)) {
                        m_dateLocationData.get(date).dateYOffset += Y_OFFSET_INCRFEMENT;
                        declutter(metrics, false);
                        break;
                    }
                }
                
                // Finally, once there are no more collisions, add the new rectangle (so it can be 
                // used to check collisions for the next timeline date).
                entryFlags.add(rect);
            }
        }
    }
    
    
    /** Initializes the rendering configurations for all data. */
    private void initRenderingConfigs() {
        m_renderingConfigs.clear();
        for (TimelineDate date : m_data.keySet()) {
            int currentFencepostHeight = getTimelineY() - FENCE_POST_HEIGHT;
            int fencepostX = m_dateLocationData.get(date).dateXPos;
            
            // Sort the Entries for the supplied TimelineDate so that they'll be alphabetized from top to bottom.
            List<TimelineEntry> entries = m_data.get(date);
            Collections.sort(entries, ENTRY_COMPARATOR);
            Collections.reverse(entries);
            
            for (TimelineEntry entry : entries) {
                RenderingConfig config = new RenderingConfig();
                config.date = date;
                
                // Leader
                config.leaderStart = new Point(fencepostX, currentFencepostHeight);
                currentFencepostHeight = currentFencepostHeight - LEADER_LINE_LENGTH;
                config.leaderEnd = new Point(fencepostX, currentFencepostHeight);
                
                // Dot
                config.color = Colors.getColor(m_da.getEntity(entry.getAssociatedId()).getType());
                config.dot = new Ellipse2D.Double((fencepostX - DOT_DIAMETER / 2), currentFencepostHeight - DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                currentFencepostHeight = currentFencepostHeight - DOT_DIAMETER;
                
                // Text
                config.text = entry.getTitle();
                config.textPoint = new Point(config.leaderEnd.x + DOT_DIAMETER, config.leaderEnd.y - (DOT_DIAMETER / 4));
                
                m_renderingConfigs.put(entry.getId(), config);
            }
        }
    }

    /**
     * Calculates the segment count given a SORTED list of timeline dates.  Unsorted dates will result in errors.
     * @param dates the dates to use to calculate the segment count.
     */
    private void calculateSegmentCount(List<TimelineDate> dates) {
        if (dates.isEmpty()) {
            return;
        }
        switch (m_zoomLevel) {
            case CENTURY:
                // If zoomed to Century Level, segment count is drastically reduced.  
                // Need to ensure that this is carefully handled such that even century segments exist.
                m_segmentCount = dates.get(0).getCenturyDate().difference(dates.get(dates.size() - 1).getCenturyDate()) / (m_da.getCalendar().getMonthCount() * 100);
                break;
            case DECADE:
                // If zoomed to Decade Level, a vertical line post is drawn for each ten years.
                // Need to ensure that this is carefully handled such that even decade segments exist.
                m_segmentCount = dates.get(0).getDecadeDate().difference(dates.get(dates.size() - 1).getDecadeDate()) / (m_da.getCalendar().getMonthCount() * 10);
                break;
            case YEAR:
                // If zoomed to Year Level, a vertical line post is drawn for each year only.
                // Need to ensure that this is carefully handled such that even year segments exist.
                m_segmentCount = dates.get(0).getYearDate().difference(dates.get(dates.size() - 1).getYearDate()) / m_da.getCalendar().getMonthCount();
                break;
            case MONTH:
                // Default is Month.  Do nothing.
                m_segmentCount = dates.get(0).difference(dates.get(dates.size() - 1));
                break;
        }
        m_segmentCount += SEGMENT_PAD;
    }
    
    /**
     * Calculates the vertical line posts for this timeline taking into account zoom level, (if applicable).
     * @param segmentCount the number of segments to be drawn between 
     */
    private void calculateDateLocationData(int segmentCount) {
        m_dateLocationData.clear();
        int monthStep = 1;
        
        List<TimelineDate> dates = new ArrayList<>(m_data.keySet());
        if (dates.size() > 0) {
            Collections.sort(dates);
            TimelineDate first = dates.get(0);

            int currentXposition = PAD;
            TimelineDate currentDate = first;
            for (int i = 0; i < segmentCount; i++) {
                String label = "";
                switch (m_zoomLevel) {
                    case CENTURY:
                        currentDate = currentDate.getCenturyDate();
                        label = String.valueOf(currentDate.year) + "s";
                        monthStep = m_da.getCalendar().getMonthCount() * 100;
                        break;
                    case DECADE:
                        label = String.valueOf(currentDate.year) + "s";
                        monthStep = m_da.getCalendar().getMonthCount() * 10;
                        break;
                    case YEAR:
                        currentDate = currentDate.getYearDate();
                        label = String.valueOf(currentDate.year);
                        monthStep = m_da.getCalendar().getMonthCount();
                        break;
                    case MONTH:
                        // do nothing
                        label = currentDate.toString();
                        monthStep = 1;
                        break;
                }
                
                if (m_dateLocationData.get(currentDate) == null) {
                    DateLocationData dld = new DateLocationData();
                    dld.label = label;
                    dld.dateXPos = currentXposition;
                    dld.dateYOffset = 0;
                    m_dateLocationData.put(currentDate, dld);
                }
                currentDate = currentDate.getFutureDate(monthStep);
                currentXposition += SEGMENT_X_VALUES;
            }
        }
    }

    @Override
    public JComponent getComponent() {
        return m_scrollPane;
    }

    @Override
    public void dataRemoved(UUID id) {
        m_dataChanged = true;
        repaint();
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        m_dataChanged = true;
        repaint();
    }

    @Override
    public void timelineEntryAddedOrUpdated(TimelineEntry tle) {
        m_dataChanged = true;
        repaint();
    }

    @Override
    public void timelineEntryRemoved(UUID id) {
        m_dataChanged = true;
        repaint();
    }

    @Override
    public void clearAllData() {
        // Do nothing.
    }

    void setYearRange(Integer earliestYear, Integer latestYear) {
        m_earliestYear = earliestYear;
        m_latestYear = latestYear;
        m_dataChanged = true;
    }
    
    /** Helper Class for organizing Timeline Data. */
    private class TimelineDate implements Comparable<TimelineDate> {
        Month month;
        int year;

        private TimelineDate(Month month, int year) {
            this.month = month;
            this.year = year;
        }

        /**
         * Returns the absolute value of the difference (in months) between this date and the supplied one.
         * @return the absolute value of the difference (in months) between this date and the supplied one.
         */
        private int difference(TimelineDate date) {
            if (this.compareTo(date) < 0) {
                return difference(date, this, m_da.getCalendar());
            } else {
                return difference(this, date, m_da.getCalendar());
            }
        }
        
        /**
         * Returns the difference between two dates, where bigDate is the date AFTER little date on the time continuum.  This calculation 
         * is inclusive, meaning that the first month and last month will be included in final totals.
         * 
         * @param bigDate a date that occurs after the little date.
         * @param littleDate a date that occurs before the big date.
         * @param calendar the CampaignCalendar to use for calculations.
         * @return the difference between two dates, where bigDate is the date AFTER little date on the time continuum.
         */
        private int difference(TimelineDate bigDate, TimelineDate littleDate, CampaignCalendar calendar) {
            // If the other date is AFTER this date...
            int years = bigDate.year - littleDate.year;
            int months = 0;

            if (littleDate.month.getIndex() < bigDate.month.getIndex()) {
                // If this date's month is BEFORE the other months date, the math is easy.
                // Add one at the end in order to be inclusive of the last month
                months = bigDate.month.getIndex() - littleDate.month.getIndex() + 1;
                return months + (years * calendar.getMonthCount());
            } else {
                // The other date has a month AFTER this object's month in the year.  So we need to count backward a year.
                // Add one at the end in order to be inclusive of the last month
                years -= 1;
                months = (bigDate.month.getIndex() + calendar.getMonthCount()) - littleDate.month.getIndex() + 1;
                return months + years * calendar.getMonthCount();
            }
        }
        
        /**
         * Returns a TimelineDate that is 'n' months ahead of this one.  If necessary, years will also be incremented.
         * @param n the number of months to increment.
         * @return a TimelineDate that is 'n' months ahead of this one.
         */
        private TimelineDate getFutureDate(int n) {
            CampaignCalendar cc = m_da.getCalendar();
            Month newMonth = cc.getMonthForIndex(this.month.getIndex());
            int newYear = this.year;
            for (int i = 0; i < n; i++) {
                newMonth = cc.getMonthAfter(newMonth);
                if (newMonth.getIndex() == 0) {
                    newYear += 1;
                }
            }
            
            return new TimelineDate(newMonth, newYear);
        }

        /**
         * Returns the first date for the year that this date is in.
         * @return the first date for the year that this date is in.
         */
        private TimelineDate getYearDate() {
            return new TimelineDate(m_da.getCalendar().getMonthForIndex(0), this.year);
        }
        
        /**
         * Returns the first date for the decade that this date is in.
         * @return the first date for the decade that this date is in.
         */
        private TimelineDate getDecadeDate() {
            int remainder = this.year % 10;
            return new TimelineDate(m_da.getCalendar().getMonthForIndex(0), this.year - remainder);
        }
        
        /**
         * Returns the first date for the century that this date is in.
         * @return the first date for the century that this date is in.
         */
        private TimelineDate getCenturyDate() {
            int remainder = this.year % 100;
            return new TimelineDate(m_da.getCalendar().getMonthForIndex(0), this.year - remainder);
        }
        
        @Override
        public String toString() {
            return month.getName() + " " + year;
        }

        @Override
        public int compareTo(TimelineDate t) {
            if (year < t.year) {
                return -1;
            } else if (year > t.year) {
                return 1;
            } else {
                return month.compareTo(t.month);
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.month);
            hash = 89 * hash + this.year;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TimelineDate other = (TimelineDate) obj;
            if (!Objects.equals(this.month, other.month)) {
                return false;
            }
            if (this.year != other.year) {
                return false;
            }
            return true;
        }
    }
    
    /** A data bag helper class for managing the position and label of a given date on the timeline. */
    private class DateLocationData {
        private int dateXPos;
        private int dateYOffset;
        private String label;
    }
    
    
    /** A data bag for holding the locations calculated for rendering data for rendering a TimelineEntry. */
    private class RenderingConfig {
        /** The color to render the dot. */
        private Color color;
        /** The Dot to be rendered for this timeline entry. */
        private Ellipse2D dot;
        /** The title text. */
        private String text;
        /** The starting point for the leader (the line from the date fencepost to the dot.) This point is lower on the screen than the end point.*/
        private Point leaderStart;
        /** The ending point for the leader (the line from the date fencepost to the dot.) This point is higher on the screen than the start point.*/
        private Point leaderEnd;
        /** The position where the text is to be rendered. */
        private Point textPoint;
        /** The date where this dot, text, and leader line should be rendered. */
        private TimelineDate date;
    }
    
    /** A helper class that represents a position on the canvas. */
    private class Point {
        /** The X position. */
        private final int x;
        /** The Y position. */
        private final int y;
        
        /**
         * Constructor.
         * @param xPoint The X position.
         * @param yPoint The Y position.
         */
        private Point(int xPoint, int yPoint) {
            x = xPoint;
            y = yPoint;
        }
    }
}
