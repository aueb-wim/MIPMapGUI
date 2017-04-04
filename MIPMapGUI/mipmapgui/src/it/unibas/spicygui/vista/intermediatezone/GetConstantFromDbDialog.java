/*
    Copyright (C) 2007-2011  Database Group - Universita' della Basilicata
    Giansalvatore Mecca - giansalvatore.mecca@unibas.it
    Salvatore Raunich - salrau@gmail.com
    Marcello Buoncristiano - marcello.buoncristiano@yahoo.it

    This file is part of ++Spicy - a Schema Mapping and Data Exchange Tool
    
    ++Spicy is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    ++Spicy is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ++Spicy.  If not, see <http://www.gnu.org/licenses/>.
 */
 
package it.unibas.spicygui.vista.intermediatezone;

import it.unibas.spicy.model.correspondence.GetIdFromDb;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.controllo.FormValidation;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterConst;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.jdesktop.beansbinding.Binding;
import org.openide.windows.WindowManager;

public class GetConstantFromDbDialog extends javax.swing.JDialog {

    private String driverValue, uriValue, schemaValue, usernameValue, passwordValue, tableValue, columnValue, functionValue;
    private CaratteristicheWidgetInterConst caratteristiche;
    private Binding binding;
    private FormValidation formValidation /*= new FormValidation(false)*/;
    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;
    private ButtonGroup group = new ButtonGroup();
    public GetConstantFromDbDialog(java.awt.Frame parent, CaratteristicheWidgetInterConst caratteristiche, boolean modal) {
        super(parent, modal);
        this.caratteristiche = caratteristiche;
        formValidation = this.caratteristiche.getFormValidation();
        this.setLocationRelativeTo(parent);
        initComponents();
        initBinding();
        initListener();
    }
    
    private void initBinding() {
        
        String seq = "";
        if(caratteristiche.getCostante() != null){
            for(int k=1; k<caratteristiche.getCostante().toString().split("_").length;k++){
                seq += caratteristiche.getCostante().toString().split("_")[k]+"_";
            }
        }
        if(!seq.equals(""))
            seq = seq.substring(0, seq.length()-1);
        
        GetIdFromDb dbConnectionProperties = SpicyEngineConstants.GET_ID_FROM_DB.get(seq);
        if (dbConnectionProperties != null) {
            getDriver().setText(dbConnectionProperties.getDriver());
            getUri().setText(dbConnectionProperties.getUri());
            if(dbConnectionProperties.getSchema() != null)
                getSchema().setText(dbConnectionProperties.getSchema());
            getUsername().setText(dbConnectionProperties.getLogin());
            getPassword().setText(dbConnectionProperties.getPassword());
            getTable().setText(dbConnectionProperties.getTable());
            getColumn().setText(dbConnectionProperties.getColumn());
        } else {
            getDriver().setText("");
            getUri().setText("");
            getSchema().setText("");
            getUsername().setText("");
            getPassword().setText("");
            getTable().setText("");
            getColumn().setText("");
        }
     }

    private void initListener() {
        validatoreConstantStrNum.setFormValidation(formValidation);
        validatoreConstantFun.setFormValidation(formValidation);
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        validatoreConstantFun = new it.unibas.spicygui.controllo.validators.ValidatoreConstantFun();
        validatoreConstantStrNum = new it.unibas.spicygui.controllo.validators.ValidatoreConstantStrNum();
        buttonGroup1 = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfUri = new javax.swing.JTextField();
        tfUsername = new javax.swing.JTextField();
        tfPassword = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tfTable = new javax.swing.JTextField();
        tfColumn = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        lFunction = new javax.swing.JList<>();
        jLabel8 = new javax.swing.JLabel();
        tfSchema = new javax.swing.JTextField();
        tfDriver = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText(org.openide.util.NbBundle.getMessage(Costanti.class, "Ok_button")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(Costanti.class, "Cancel_button")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel1.toolTipText")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel2.toolTipText")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel3.toolTipText")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel4.toolTipText")); // NOI18N

        tfUri.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfUri.text")); // NOI18N
        tfUri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUriActionPerformed(evt);
            }
        });

        tfUsername.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfUsername.text")); // NOI18N
        tfUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUsernameActionPerformed(evt);
            }
        });

        tfPassword.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfPassword.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel5.toolTipText")); // NOI18N

        jLabel6.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel6.toolTipText")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel7.toolTipText")); // NOI18N

        tfTable.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfTable.text")); // NOI18N
        tfTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTableActionPerformed(evt);
            }
        });

        tfColumn.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfColumn.text")); // NOI18N
        tfColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfColumnActionPerformed(evt);
            }
        });

        lFunction.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "max" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lFunction);

        jLabel8.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel8.text")); // NOI18N
        jLabel8.setToolTipText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.jLabel8.toolTipText")); // NOI18N

        tfSchema.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfSchema.text")); // NOI18N
        tfSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSchemaActionPerformed(evt);
            }
        });

        tfDriver.setText(org.openide.util.NbBundle.getMessage(GetConstantFromDbDialog.class, "GetConstantFromDbDialog.tfDriver.text")); // NOI18N
        tfDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfDriverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 143, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(tfColumn, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(tfTable, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton))
                                    .addComponent(tfPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jScrollPane1))
                            .addComponent(tfUsername, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tfDriver, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tfUri, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tfSchema, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfUri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(tfSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tfColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 27, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void tfUriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUriActionPerformed

    private void tfUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUsernameActionPerformed

    private void tfTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTableActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTableActionPerformed

    private void tfColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfColumnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfColumnActionPerformed

    private void tfSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSchemaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfSchemaActionPerformed

    private void tfDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfDriverActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfDriverActionPerformed

    public void doClose(int retStatus){
        returnStatus = retStatus;
        if(returnStatus == RET_OK){
            driverValue = tfDriver.getText().trim();
            uriValue = tfUri.getText().trim();
            schemaValue = tfSchema.getText().trim();
            usernameValue = tfUsername.getText().trim();
            passwordValue = tfPassword.getText().trim();
            tableValue = tfTable.getText().trim();
            columnValue = tfColumn.getText().trim();
            try {
                functionValue = lFunction.getSelectedValue().trim();
            } catch(NullPointerException ex) {
                functionValue="";
            }
            if(!driverValue.equals("") && !uriValue.equals("") && !usernameValue.equals("") 
                    && !passwordValue.equals("") && !tableValue.equals("") && !columnValue.equals("") && !functionValue.equals("")){
                SpicyEngineConstants.TEMP_DB_PROPERTIES 
                    = new GetIdFromDb(driverValue, uriValue, schemaValue, usernameValue, passwordValue, tableValue, columnValue, functionValue);               
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), 
                        "Please complete all the necessary fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            setVisible(false);
            dispose();
        }
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> lFunction;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField tfColumn;
    private javax.swing.JTextField tfDriver;
    private javax.swing.JPasswordField tfPassword;
    private javax.swing.JTextField tfSchema;
    private javax.swing.JTextField tfTable;
    private javax.swing.JTextField tfUri;
    private javax.swing.JTextField tfUsername;
    private it.unibas.spicygui.controllo.validators.ValidatoreConstantFun validatoreConstantFun;
    private it.unibas.spicygui.controllo.validators.ValidatoreConstantStrNum validatoreConstantStrNum;
    // End of variables declaration//GEN-END:variables
    private int returnStatus;

    public CaratteristicheWidgetInterConst getCaratteristiche() {
        return caratteristiche;
    }

    public FormValidation getFormValidation() {
        return formValidation;
    }
    
    public JTextField getDriver() {
        return tfDriver;
    }
    
    public JTextField getUri() {
        return tfUri;
    }
    
    public JTextField getSchema() {
        return tfSchema;
    }
    
    public JTextField getUsername() {
        return tfUsername;
    }
    
    public JPasswordField getPassword() {
        return tfPassword;
    }
    
    public JTextField getTable() {
        return tfTable;
    }
    
    public JTextField getColumn() {
        return tfColumn;
    }
    
    public JList getFunctionList() {
        return lFunction;
    }
    
    public JButton getBtnOk(){
        return okButton;
    }

    
}
