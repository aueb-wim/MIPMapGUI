/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.vista.intermediatezone;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
//giannisk
public class FunctionTooltipRenderer extends DefaultListCellRenderer {
    
ArrayList<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);

        if (-1 < index && null != value && null != tooltips) {
                    list.setToolTipText(tooltips.get(index));
                }
        return comp;
    }

    public void setTooltips(ArrayList tooltips) {
        this.tooltips = tooltips;
    }
}
