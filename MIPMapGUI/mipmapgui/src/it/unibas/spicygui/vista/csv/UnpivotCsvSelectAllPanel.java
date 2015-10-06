/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.vista.csv;

import java.awt.Font;

public class UnpivotCsvSelectAllPanel extends javax.swing.JPanel {
    
    UnpivotCsvMainFrame mainFrame;
    
    public UnpivotCsvSelectAllPanel(UnpivotCsvMainFrame mainFrame ,String name) {
        this.mainFrame = mainFrame;
        initComponents();
        initLabels(name);         
    }    
   
    private void initLabels(String name){
        rowNameLabel.setText(name); 
        rowNameLabel.setFont(new Font(rowNameLabel.getFont().getName(), Font.BOLD, rowNameLabel.getFont().getSize()));
    }
    
    public boolean isColumnSelected(){
        return this.rowCheckBox.isSelected();
    }
    
    private void selectAll(boolean val){
        for (UnpivotCsvColumnPanel panel: mainFrame.getColumnsList()){
            panel.setCheckboxSelected(val);
        }
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rowNameLabel = new javax.swing.JLabel();
        rowCheckBox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(100, 76));

        org.openide.awt.Mnemonics.setLocalizedText(rowCheckBox, org.openide.util.NbBundle.getMessage(UnpivotCsvSelectAllPanel.class, "UnpivotCsvColumnPanel.rowCheckBox.text")); // NOI18N
        rowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rowCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rowNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rowCheckBox)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rowNameLabel)
                    .addComponent(rowCheckBox))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rowCheckBoxActionPerformed
        if (this.rowCheckBox.isSelected()){
            selectAll(true);
        }
        else{
            //clear all
            selectAll(false);
        }
    }//GEN-LAST:event_rowCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox rowCheckBox;
    private javax.swing.JLabel rowNameLabel;
    // End of variables declaration//GEN-END:variables
}
