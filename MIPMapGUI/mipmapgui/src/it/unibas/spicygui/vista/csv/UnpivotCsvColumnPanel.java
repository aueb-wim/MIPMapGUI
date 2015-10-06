/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.vista.csv;

public class UnpivotCsvColumnPanel extends javax.swing.JPanel {
    
    public UnpivotCsvColumnPanel(String name) {
        initComponents();
        initLabels(name);         
    }    
   
    private void initLabels(String name){
        rowNameLabel.setText(name);     
    }
   
    public String getColNameLabel(){
        return this.rowNameLabel.getText();
    }
    
    public boolean isColumnSelected(){
        return this.rowCheckBox.isSelected();
    }
    
    public void setCheckboxSelected(boolean val){
        this.rowCheckBox.setSelected(val);
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

        org.openide.awt.Mnemonics.setLocalizedText(rowCheckBox, org.openide.util.NbBundle.getMessage(UnpivotCsvColumnPanel.class, "UnpivotCsvColumnPanel.rowCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rowNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox rowCheckBox;
    private javax.swing.JLabel rowNameLabel;
    // End of variables declaration//GEN-END:variables
}
