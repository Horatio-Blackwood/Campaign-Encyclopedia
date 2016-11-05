package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Entity;
import campaignencyclopedia.display.swing.graphical.Colors;
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
 * A renderer for Entities in a JList.
 * @author adam
 */
public class EntityListCellRenderer implements ListCellRenderer<Entity> {

    private static final Insets INSETS = new Insets(3, 1, 3, 1);

    @Override
    public Component getListCellRendererComponent(JList<? extends Entity> jlist, Entity e, int i, boolean isSelected, boolean hasFocus) {
        JPanel cell = new JPanel(new GridBagLayout());

        JLabel label = new JLabel(e.getName());
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
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
        cell.add(new Dot(Colors.getColor(e.getType())), gbc);

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