/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.controllo.file;

import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.vista.wizard.pm.XMLConfigurationPM;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.wizard.pm.CSVConfigurationPM;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

//giannisk
public class ActionCsvFileChooserSchema extends AbstractAction {
    
    private static Log logger = LogFactory.getLog(ActionCsvFileChooserSchema.class);
    private Modello modello;
    private Vista vista;    
    private CSVConfigurationPM csvConfigurationPM;

    public ActionCsvFileChooserSchema() {
        super("...");       
    }    
    
    public void actionPerformed(ActionEvent e) {
        executeInjection();
        JFileChooser chooser = vista.getFileChooserApriCSV();
        File file;
        int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            //giannisk Add schema file path to previous ones
            csvConfigurationPM.addToSchemaPathList(file.getAbsolutePath());
        }
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }

    public void setBean(CSVConfigurationPM csvConfigurationPM) {
        this.csvConfigurationPM = csvConfigurationPM;
    } 
    
    
    
}
