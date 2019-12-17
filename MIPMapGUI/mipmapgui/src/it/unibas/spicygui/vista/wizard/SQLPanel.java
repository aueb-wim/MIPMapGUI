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
import it.unibas.spicygui.vista.wizard.pm.SQLConfigurationPM;
import it.unibas.spicygui.controllo.file.ActionSqlFileChooser;
import it.unibas.spicygui.vista.Vista;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFileChooser;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class SQLPanel extends javax.swing.JPanel {

    private SQLConfigurationPM sqlConfigurationPM;
    private PropertyChangeListener sqlPropertyChangeListener;
    private PanelWizardImpl panelWizardImpl;
    private ActionSqlFileChooser actionFileChooser;

    public SQLPanel(PanelWizardImpl panelWizardImpl) {
        this.panelWizardImpl = panelWizardImpl;
        initComponents();
        initLabels();
        initActions();
    }

    private void initButtonBean() {
        this.buttonChooser.setAction(this.actionFileChooser);
        actionFileChooser.setBean(sqlConfigurationPM);
    }

    private void initLabels() {
        dbNameLabel.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.DB_NAME_LABEL));
        sqlLabel.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.LabelSqlSchemaSource));
    }

    private void initActions() {
        this.actionFileChooser = new ActionSqlFileChooser();
    }

    public SQLConfigurationPM getSqlConfigurationPM() {
        return sqlConfigurationPM;
    }

    public void setSqlConfigurationPM(SQLConfigurationPM sqlConfigurationPM) {
        this.sqlConfigurationPM = sqlConfigurationPM;
        removeListener();
        initListener();
        initButtonBean();
        ripulisciCampi();
    }

    public void initListener() {
        this.sqlPropertyChangeListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                campoIstanzeTargetPropertyChange(evt);
            }

            private void campoIstanzeTargetPropertyChange(PropertyChangeEvent evt) {
                panelWizardImpl.fireChangeEvent();
            }
        };
        sqlConfigurationPM.addPropertyChangeListener(this.sqlPropertyChangeListener);
    }

    public void removeListener() {
        if (this.sqlPropertyChangeListener != null) {
            sqlConfigurationPM.removePropertyChangeListener(this.sqlPropertyChangeListener);
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
        sqlLabel = new javax.swing.JLabel();
        filePathTextField = new javax.swing.JTextField();
        buttonChooser = new javax.swing.JButton();
        dbNameLabel = new javax.swing.JLabel();
        dbNameTextField = new javax.swing.JTextField();

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sqlConfigurationPM.schemaPath}"), filePathTextField, org.jdesktop.beansbinding.BeanProperty.create("text"), "schemaPath");
        bindingGroup.addBinding(binding);

        buttonChooser.setText("...");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${sqlConfigurationPM.DBName}"), dbNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sqlLabel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(filePathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                        .addGap(13, 13, 13)
                        .addComponent(buttonChooser))
                    .addComponent(dbNameLabel)
                    .addComponent(dbNameTextField))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sqlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonChooser))
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonChooser;
    private javax.swing.JLabel dbNameLabel;
    private javax.swing.JTextField dbNameTextField;
    private javax.swing.JTextField filePathTextField;
    private javax.swing.JLabel sqlLabel;
    private it.unibas.spicygui.controllo.validators.ValidatoreCampoTesto validatoreCampoTesto;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
