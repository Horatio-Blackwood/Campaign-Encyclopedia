package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Displayable;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A List Cell Renderer for Displayables.
 * @author adam
 */
public class DisplayableCellRenderer implements ListCellRenderer<Displayable> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Displayable> jlist, Displayable e, int i, boolean isSelected, boolean hasFocus) {
        JLabel cell = new JLabel(e.getDisplayString());
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        Color deselectedBackground = cell.getBackground();
        Color deselectedTextColor = cell.getForeground();

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
