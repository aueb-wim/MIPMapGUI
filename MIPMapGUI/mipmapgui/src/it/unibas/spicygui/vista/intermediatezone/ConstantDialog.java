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

import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.controllo.FormValidation;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterConst;
import it.unibas.spicygui.controllo.validators.ValidatoreCampoTesto;
import it.unibas.spicygui.controllo.validators.ValidazioneBindingListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ConstantDialog extends javax.swing.JDialog {

    private CaratteristicheWidgetInterConst caratteristiche;
    private Binding binding;
    private FormValidation formValidation /*= new FormValidation(false)*/;
    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;
    private ButtonGroup group = new ButtonGroup();
    private GetConstantFromDbDialog dialog;
    
    public ConstantDialog(java.awt.Frame parent, CaratteristicheWidgetInterConst caratteristiche, boolean modal) {
        super(parent, modal);
        this.caratteristiche = caratteristiche;
        formValidation = this.caratteristiche.getFormValidation();
        this.setLocationRelativeTo(parent);
        initComponents();
        initBinding();
        initListener();
        newIdOffsetVisibility();
        
    }

    
    public void newIdOffsetVisibility(){
        this.getJComboBoxFunction().addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                Object selected = comboBox.getSelectedItem();
                if(selected != null) {
                    if (selected.equals("newId()")) {
                        offsetPanel.setVisible(true);
                    } else {
                        offsetPanel.setVisible(false);
                    }
                }
            }
        });
    }
    
    public void clean() {
        bindingGroup.unbind();
        bindingGroup.bind();
    }

    private void initBinding() {
        offsetPanel.setVisible(false);
        String seq = "";
        if(caratteristiche.getCostante() != null){
            for(int k=1; k<caratteristiche.getCostante().toString().split("_").length;k++){
                seq += caratteristiche.getCostante().toString().split("_")[k]+"_";
            }
        }
        if(!seq.equals("")){
            textSequenceName.setText(seq.substring(0, seq.length()-1));
            offsetText.setText(SpicyEngineConstants.OFFSET_MAPPING.get(seq.substring(0, seq.length()-1)));
        } 
        if(caratteristiche.getType() != null && caratteristiche.getCostante().toString().split("_")[0].equals("newId()")){
            jComboBoxFunction.setSelectedIndex(2);
            offsetPanel.setVisible(true);
            if(caratteristiche.getType().equals("getId()")){
                rbDatabase.setSelected(true);
            } else if (caratteristiche.getType().equals("constant")){
                rbConstant.setSelected(true);
            }
        }

        org.jdesktop.beansbinding.Binding binding1 = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${formValidation.textFieldState}"), jTextFieldConstant, org.jdesktop.beansbinding.BeanProperty.create("enabled"), "textBinding");
        bindingGroup.addBinding(binding1);
        org.jdesktop.beansbinding.Binding binding2 = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${formValidation.comboBoxState}"), jComboBoxFunction, org.jdesktop.beansbinding.BeanProperty.create("enabled"), "comboBinding");
        bindingGroup.addBinding(binding2);
        org.jdesktop.beansbinding.Binding binding3 = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${formValidation.buttonState}"), okButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"), "buttonBinding");
        bindingGroup.addBinding(binding3);
//        Binding binding1 = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, jTextFieldConstant, org.jdesktop.beansbinding.BeanProperty.create("text"), this.caratteristicheWidgetInterConst, ELProperty.create("${costante}"));
        this.binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, this.getCaratteristiche(), ELProperty.create("${costante}"), jTextFieldConstant, ELProperty.create("${text}"), "costantBinding");
//        bindingGroup.addBinding(binding1);

        ValidatoreCampoTesto validatoreCampoTesto = new it.unibas.spicygui.controllo.validators.ValidatoreCampoTesto();
        validatoreCampoTesto.setFormValidation(formValidation);
        this.binding.setValidator(validatoreCampoTesto);
        this.binding.bind();
    }

    private void initListener() {
        validatoreConstantStrNum.setFormValidation(formValidation);
        validatoreConstantFun.setFormValidation(formValidation);
        this.binding.addBindingListener(new ValidazioneBindingListener(this.errorLabel, "costantBinding", formValidation));
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup = new javax.swing.ButtonGroup();
        validatoreConstantFun = new it.unibas.spicygui.controllo.validators.ValidatoreConstantFun();
        validatoreConstantStrNum = new it.unibas.spicygui.controllo.validators.ValidatoreConstantStrNum();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jTextFieldConstant = new javax.swing.JTextField();
        jComboBoxFunction = new javax.swing.JComboBox();
        jRadioButtonString = new javax.swing.JRadioButton();
        jRadioButtonNumber = new javax.swing.JRadioButton();
        jRadioButtonFunction = new javax.swing.JRadioButton();
        errorLabel = new javax.swing.JLabel();
        offsetPanel = new javax.swing.JPanel();
        offsetText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        textSequenceName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnGetOffset = new javax.swing.JButton();
        rbDatabase = new javax.swing.JRadioButton();
        rbConstant = new javax.swing.JRadioButton();

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

        jTextFieldConstant.setText("");
        jTextFieldConstant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldConstantActionPerformed(evt);
            }
        });

        jComboBoxFunction.setModel(new DefaultComboBoxModel(new String[] { NbBundle.getMessage(Costanti.class, Costanti.INPUT_TEXT_CONSTANT_FUNCTION1), NbBundle.getMessage(Costanti.class, Costanti.INPUT_TEXT_CONSTANT_FUNCTION3),NbBundle.getMessage(Costanti.class, Costanti.INPUT_TEXT_CONSTANT_FUNCTION2)}));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, this, org.jdesktop.beansbinding.ELProperty.create("${caratteristiche.costante}"), jComboBoxFunction, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        jComboBoxFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFunctionActionPerformed(evt);
            }
        });

        buttonGroup.add(jRadioButtonString);
        jRadioButtonString.setText(org.openide.util.NbBundle.getMessage(Costanti.class, "Input_text_constant_radio_string")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${caratteristiche.tipoStringa}"), jRadioButtonString, org.jdesktop.beansbinding.BeanProperty.create("selected"), "radio Stringa");
        binding.setValidator(validatoreConstantStrNum);
        bindingGroup.addBinding(binding);

        buttonGroup.add(jRadioButtonNumber);
        jRadioButtonNumber.setText(org.openide.util.NbBundle.getMessage(Costanti.class, "Input_text_constant_radio_number")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${caratteristiche.tipoNumero}"), jRadioButtonNumber, org.jdesktop.beansbinding.BeanProperty.create("selected"), "radio numero");
        binding.setValidator(validatoreConstantStrNum);
        bindingGroup.addBinding(binding);

        buttonGroup.add(jRadioButtonFunction);
        jRadioButtonFunction.setText(org.openide.util.NbBundle.getMessage(Costanti.class, "Input_text_constant_radio_function")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${caratteristiche.tipoFunzione}"), jRadioButtonFunction, org.jdesktop.beansbinding.BeanProperty.create("selected"), "radio funzione");
        binding.setValidator(validatoreConstantFun);
        bindingGroup.addBinding(binding);

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        errorLabel.setText("");

        offsetText.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.offsetText.text")); // NOI18N
        offsetText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offsetTextActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.jLabel1.text")); // NOI18N

        textSequenceName.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.textSequenceName.text")); // NOI18N
        textSequenceName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textSequenceNameActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.jLabel2.text")); // NOI18N

        btnGetOffset.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.btnGetOffset.text")); // NOI18N
        btnGetOffset.setToolTipText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.btnGetOffset.toolTipText")); // NOI18N

        buttonGroup2.add(rbDatabase);
        rbDatabase.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.rbDatabase.text")); // NOI18N
        rbDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbDatabaseActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbConstant);
        rbConstant.setText(org.openide.util.NbBundle.getMessage(ConstantDialog.class, "ConstantDialog.rbConstant.text")); // NOI18N
        rbConstant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbConstantActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout offsetPanelLayout = new javax.swing.GroupLayout(offsetPanel);
        offsetPanel.setLayout(offsetPanelLayout);
        offsetPanelLayout.setHorizontalGroup(
            offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(offsetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(offsetPanelLayout.createSequentialGroup()
                        .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textSequenceName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnGetOffset)
                            .addGroup(offsetPanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(offsetText, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, offsetPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, offsetPanelLayout.createSequentialGroup()
                        .addComponent(rbConstant)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        offsetPanelLayout.setVerticalGroup(
            offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, offsetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnGetOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(offsetText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textSequenceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(offsetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbDatabase)
                    .addComponent(rbConstant))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldConstant, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButtonString)
                            .addComponent(jRadioButtonFunction)
                            .addComponent(jRadioButtonNumber))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(offsetPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxFunction, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(errorLabel, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 54, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jRadioButtonString)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonNumber))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonFunction)
                .addGap(9, 9, 9)
                .addComponent(jComboBoxFunction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(offsetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addComponent(errorLabel))
                .addContainerGap())
        );

        bindingGroup.bind();

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

    private void jTextFieldConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldConstantActionPerformed
        if (this.okButton.isEnabled()) {
            doClose(RET_OK);
        }  
    }//GEN-LAST:event_jTextFieldConstantActionPerformed

    private void jComboBoxFunctionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFunctionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxFunctionActionPerformed

    private void rbDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbDatabaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbDatabaseActionPerformed

    private void textSequenceNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textSequenceNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textSequenceNameActionPerformed

    private void offsetTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offsetTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_offsetTextActionPerformed

    private void rbConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbConstantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbConstantActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGetOffset;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JComboBox jComboBoxFunction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton jRadioButtonFunction;
    private javax.swing.JRadioButton jRadioButtonNumber;
    private javax.swing.JRadioButton jRadioButtonString;
    private javax.swing.JTextField jTextFieldConstant;
    private javax.swing.JPanel offsetPanel;
    private javax.swing.JTextField offsetText;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton rbConstant;
    private javax.swing.JRadioButton rbDatabase;
    private javax.swing.JTextField textSequenceName;
    private it.unibas.spicygui.controllo.validators.ValidatoreConstantFun validatoreConstantFun;
    private it.unibas.spicygui.controllo.validators.ValidatoreConstantStrNum validatoreConstantStrNum;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;

    public javax.swing.JRadioButton getJRadioButtonFunction() {
        return jRadioButtonFunction;
    }

    public javax.swing.JComboBox getJComboBoxFunction() {
        return jComboBoxFunction;
    }

    public CaratteristicheWidgetInterConst getCaratteristiche() {
        return caratteristiche;
    }

    public FormValidation getFormValidation() {
        return formValidation;
    }

    public javax.swing.JRadioButton getJRadioButtonNumber() {
        return jRadioButtonNumber;
    }

    public javax.swing.JRadioButton getJRadioButtonString() {
        return jRadioButtonString;
    }
    
    public javax.swing.JRadioButton getJRadioButtonConstant() {
        return rbConstant;
    }
    
    public javax.swing.JRadioButton getJRadioButtonDatabase() {
        return rbDatabase;
    }
    
    public javax.swing.JPanel getOffsetPanel(){
        return offsetPanel;
    }
    
    public JTextField getOffsetText(){
        return offsetText;
    }
    
    public JTextField getTextSequenceName(){
        return textSequenceName;
    }
    
    public JButton getOffsetButton(){
        return btnGetOffset;
    }
}
