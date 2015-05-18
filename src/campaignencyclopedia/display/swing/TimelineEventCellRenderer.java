package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.TimelineEntry;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * A cell renderer for timeline data.
 * @author adam
 */
public class TimelineEventCellRenderer implements ListCellRenderer<TimelineEntry> {

    /** An accessor to assist in rendering data in a user-consumable way. */
    private final DataAccessor m_accessor;

    /**
     * Creates a new TimelineEventCellRenderer
     * @param accessor an accessor to assist in rendering data in a user-consumable way.
     */
    public TimelineEventCellRenderer(DataAccessor accessor) {
        m_accessor = accessor;
    }

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList<? extends TimelineEntry>  jlist, TimelineEntry e, int i, boolean isSelected, boolean hasFocus) {
        JLabel seasonYearLabel = new JLabel();
        JLabel titleLabel = new JLabel();

        // Use Associated Entity name if title is empty
        if (e.getTitle() == null || e.getTitle().isEmpty()) {
            titleLabel.setText(m_accessor.getEntity(e.getAssociatedId()).getName());
        } else {
            // If a valid title has been entered, use it.
            titleLabel.setText(e.getTitle());
        }

        // Set the year and month
        seasonYearLabel.setText(e.getMonth() + " " + e.getYear());

        // Set Opacity & Border
        seasonYearLabel.setOpaque(false);
        titleLabel.setOpaque(false);
        seasonYearLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        // Set Colors
        Color deselectedBackground = seasonYearLabel.getBackground();
        Color deselectedTextColor = seasonYearLabel.getForeground();
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(seasonYearLabel, gbc);
        
        gbc.gridx = 1;
        panel.add(titleLabel, gbc);
        
        if (e.isSecret()) {
            gbc.gridx = 2;
            JLabel secretLabel = new JLabel("(Secret)");
            secretLabel.setForeground(Color.RED);
            secretLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            panel.add(secretLabel, gbc);            
        }
        
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        panel.add(new JLabel(), gbc);

        if (isSelected){
            seasonYearLabel.setOpaque(true);
            titleLabel.setOpaque(true);
            panel.setOpaque(true);
            panel.setBackground(MetalLookAndFeel.getTextHighlightColor());
            seasonYearLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
            titleLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
        } else {
            seasonYearLabel.setBackground(deselectedBackground);
            titleLabel.setBackground(deselectedBackground);
            panel.setBackground(deselectedBackground);
            seasonYearLabel.setForeground(deselectedTextColor);
            panel.setForeground(deselectedTextColor);
        }

        return panel;
    }
}
