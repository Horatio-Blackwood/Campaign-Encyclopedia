/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.ColoredDisplayable;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 *
 * @author adam
 */
public class ColoredDisplayableComboBoxEditor extends BasicComboBoxEditor {
    
    private ColoredDisplayable m_cd = null;
    private static final Insets INSETS = new Insets(3, 1, 3, 1);
    private final JPanel m_cell;
    private final JLabel m_label;
    private final Dot m_dot;
    
    public ColoredDisplayableComboBoxEditor() {
        // INITIALIZE
        // Panel
        m_cell = new JPanel(new GridBagLayout());
        m_cell.setOpaque(false);

        // Label
        m_label = new JLabel();
        m_label.setOpaque(false);
        m_label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        m_label.setHorizontalAlignment(JLabel.LEFT);

        // Dot
        m_dot = new Dot(Color.BLACK);
        
        // LAYOUT
        // Dot
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.NONE;
        m_cell.add(m_dot, gbc);

        // Label
        gbc.gridx = 1;
        gbc.weightx = 1.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        m_cell.add(m_label, gbc);
    }
    
    @Override
    public Component getEditorComponent() {
        return m_cell;
    }
    
    @Override
    public Object getItem() {
        System.out.println("getting item.");
        return m_cd;
    }
    
    @Override
    public void setItem(Object item) {
        System.out.println("setting item.");
        if (item instanceof ColoredDisplayable) {
            ColoredDisplayable cd = (ColoredDisplayable)item;
            if (!cd.equals(m_cd)) {
                System.out.println("--item actually set.");
                m_cd = cd;
                m_label.setText(m_cd.getDisplayString());
                m_dot.setColor(m_cd.getColor());
            }            
        } else {
            throw new IllegalArgumentException("Parameter item must be a ColoredDisplayable.");
        }
    }
}