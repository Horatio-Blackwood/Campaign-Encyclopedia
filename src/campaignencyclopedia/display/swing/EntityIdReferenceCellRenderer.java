package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.DataAccessor;
import java.awt.Color;
import java.awt.Component;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author adam
 */
public class EntityIdReferenceCellRenderer implements ListCellRenderer<UUID> {

    private final DataAccessor m_accessor;
    
    public EntityIdReferenceCellRenderer(DataAccessor accessor) {
        m_accessor = accessor;
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList<? extends UUID> jlist, UUID e, int i, boolean isSelected, boolean hasFocus) {
        JLabel relatedEntityLabel = new JLabel(m_accessor.getEntity(e).getName());

        relatedEntityLabel.setOpaque(false);
        relatedEntityLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        Color deselectedTextColor = relatedEntityLabel.getForeground();
        Color deselectedBackground = relatedEntityLabel.getBackground();

        if (isSelected){
            relatedEntityLabel.setOpaque(true);
            relatedEntityLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
        } else {
            relatedEntityLabel.setBackground(deselectedBackground);
            relatedEntityLabel.setForeground(deselectedTextColor);
        }

        return relatedEntityLabel;
    }
    
}
