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
 
package it.unibas.spicygui.vista.wizard;

import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.vista.wizard.pm.CSVConfigurationPM;
import it.unibas.spicygui.controllo.file.ActionCsvFileChooserSchema;
import it.unibas.spicygui.vista.Vista;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class CSVPanel extends javax.swing.JPanel {

    private CSVConfigurationPM csvConfigurationPM;
    private PropertyChangeListener csvPropertyChangeListener;
    private PanelWizardImpl panelWizardImpl;
    private ActionCsvFileChooserSchema actionFileChooserSS;
    private DefaultListModel listModel;
    private FileNameExtensionFilter filtro = new FileNameExtensionFilter(null, "csv");

    public CSVPanel(PanelWizardImpl panelWizardImpl) {
        this.panelWizardImpl = panelWizardImpl;
        initComponents();
        initLabels();
        initActions();
    }
    
/*
    private void initButtonBean() {
        this.bottoneChooserSchemaSource.setAction(this.actionFileChooserSS);
        actionFileChooserSS.setBean(csvConfigurationPM);
    }*/

    private void initLabels() {
        etichettaSchemaSource.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.LabelCsvSchemaSource));
        dbNameLabel.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.DB_NAME_LABEL));
        schemaOnlyCheckBox.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.SCHEMA_ONLY_TEXTBOX));
        fileButton.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.JFILECHOOSER_FOLDER_TYPE_FILE));
        addButton.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.ADD_TO_LIST));
        removeButton.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.REMOVE_FROM_LIST));
    }

    private void initActions() {
        this.actionFileChooserSS = new ActionCsvFileChooserSchema();
    }

    public CSVConfigurationPM getCsvConfigurationPM() {
        return csvConfigurationPM;
    }

    public void setCsvConfigurationPM(CSVConfigurationPM csvConfigurationPM) {
        this.csvConfigurationPM = csvConfigurationPM;
        removeListener();
        initListener();
        //initButtonBean();
        ripulisciCampi();
    }

    public void initListener() {
        this.csvPropertyChangeListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                campoIstanzeTargetPropertyChange(evt);
            }

            private void campoIstanzeTargetPropertyChange(PropertyChangeEvent evt) {
                panelWizardImpl.fireChangeEvent();
            }
        };
        csvConfigurationPM.addPropertyChangeListener(this.csvPropertyChangeListener);
    }

    public void removeListener() {
        if (this.csvPropertyChangeListener != null) {
            csvConfigurationPM.removePropertyChangeListener(this.csvPropertyChangeListener);
        }
    }

    public void ripulisciCampi() {
        bindingGroup.unbind();
        bindingGroup.bind();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        validatoreCampoTesto = new it.unibas.spicygui.controllo.validators.ValidatoreCampoTesto();
        jFileChooser1 = new javax.swing.JFileChooser();
        etichettaSchemaSource = new javax.swing.JLabel();
        dbNameLabel = new javax.swing.JLabel();
        dbNameSource = new javax.swing.JTextField();
        schemaOnlyCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        listModel = new DefaultListModel();
        tableList = new javax.swing.JList(listModel);
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        filePathTextField = new javax.swing.JTextField();
        fileButton = new javax.swing.JButton();

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${csvConfigurationPM.DBName}"), dbNameSource, org.jdesktop.beansbinding.BeanProperty.create("text"), "DBName");
        bindingGroup.addBinding(binding);

        schemaOnlyCheckBox.setText(org.openide.util.NbBundle.getMessage(CSVPanel.class, "CSVPanel.schemaOnlyCheckBox.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${csvConfigurationPM.schemaOnly}"), schemaOnlyCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        jScrollPane1.setViewportView(tableList);

        addButton.setText(org.openide.util.NbBundle.getMessage(CSVPanel.class, "CSVPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText(org.openide.util.NbBundle.getMessage(CSVPanel.class, "CSVPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        filePathTextField.setEditable(false);

        fileButton.setText(org.openide.util.NbBundle.getMessage(CSVPanel.class, "CSVPanel.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(etichettaSchemaSource)
                    .addComponent(dbNameLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(schemaOnlyCheckBox)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(filePathTextField)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(fileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(dbNameSource, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbNameSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(schemaOnlyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(etichettaSchemaSource)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if(!filePathTextField.getText().equals("")&&filePathTextField.getText()!=null){
            File treeFile = new File(filePathTextField.getText());
            String filename = treeFile.getName();
            //only add it to the list if it is a .csv file
            if (treeFile.isFile()&&this.filtro.accept(treeFile)){
                if (!this.listModel.contains(filename)){
                    this.listModel.addElement(filename);
                    csvConfigurationPM.addToSchemaPathList(treeFile.getAbsolutePath());
                }
            }
        }
        filePathTextField.setText("");
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        String listFile = (String) tableList.getSelectedValue();
        this.listModel.removeElement(tableList.getSelectedValue() );
        csvConfigurationPM.removeFromSchemaPathList(listFile);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        JFileChooser chooser = Lookup.getDefault().lookup(Vista.class).getFileChooserApriCSV();
        int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            filePathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_fileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel dbNameLabel;
    private javax.swing.JTextField dbNameSource;
    private javax.swing.JLabel etichettaSchemaSource;
    private javax.swing.JButton fileButton;
    private javax.swing.JTextField filePathTextField;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JCheckBox schemaOnlyCheckBox;
    private javax.swing.JList tableList;
    private it.unibas.spicygui.controllo.validators.ValidatoreCampoTesto validatoreCampoTesto;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
