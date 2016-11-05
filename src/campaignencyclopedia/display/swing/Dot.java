/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package campaignencyclopedia.display.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;

/**
 *
 * @author adam
 */
public class Dot extends JComponent {

    /** The size of the dot. */
    private static final int SIZE = 10;

    /** The size of the dot. */
    private static final int PAD = 4;

    private static final Dimension DIM = new Dimension(SIZE + PAD, SIZE + PAD);

    /** The Color to render the dot. */
    private Color m_color;

    /** The Dot itself. */
    private static final Ellipse2D.Double DOT = new Ellipse2D.Double(PAD / 2, PAD / 2, SIZE, SIZE);

    /**
     * Creates a dot of the specified color.
     * @param color the color to make the dot.
     */
    public Dot(Color color) {
        m_color = color;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(m_color);
        g2d.fill(DOT);
    }

    @Override
    public Dimension getPreferredSize() {
        return DIM;
    }
    
    public void setColor(Color color) {
        if (color != null) {
            m_color = color;
        }
    }
}
