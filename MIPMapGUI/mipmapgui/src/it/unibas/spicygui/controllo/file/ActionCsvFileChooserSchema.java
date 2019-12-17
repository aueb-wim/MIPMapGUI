/* Copyright 2015-2016 by the Athens University of Economics and Business (AUEB).

   This file is part of MIPMap.

   MIPMap is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MIPMap is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MIPMap.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicygui.controllo.file;

import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
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
