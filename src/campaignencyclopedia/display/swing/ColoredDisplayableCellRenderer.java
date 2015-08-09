package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.ColoredDisplayable;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A renderer for Displayables that also have a Color associated with them.  In this renderer, a simple colored
 * dot will be rendered to the left of the text.
 * @author adam
 */
public class ColoredDisplayableCellRenderer implements ListCellRenderer<ColoredDisplayable>{

    /** An Insets instance. */
    private static final Insets INSETS = new Insets(3, 0, 3, 0);

    @Override
    public Component getListCellRendererComponent(JList<? extends ColoredDisplayable> jlist, ColoredDisplayable e, int i, boolean isSelected, boolean hasFocus) {

        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(false);

        JLabel label = new JLabel(e.getDisplayString());
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        label.setHorizontalAlignment(JLabel.LEFT);

        Color deselectedBackground = cell.getBackground();
        Color deselectedTextColor = cell.getForeground();

        // LAYOUT COMPONENTS
        // Dot
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.NONE;
        cell.add(new Dot(e.getColor()), gbc);

        // Label
        gbc.gridx = 1;
        gbc.weightx = 1.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cell.add(label, gbc);


        if (isSelected){
            cell.setOpaque(true);
            cell.setBackground(MetalLookAndFeel.getTextHighlightColor());
        } else {
            cell.setBackground(deselectedBackground);
            cell.setForeground(deselectedTextColor);
        }
        
        return cell;
    }
}