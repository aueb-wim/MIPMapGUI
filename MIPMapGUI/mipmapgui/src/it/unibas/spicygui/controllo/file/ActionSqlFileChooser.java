package it.unibas.spicygui.controllo.file;

import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.wizard.pm.SQLConfigurationPM;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class ActionSqlFileChooser extends AbstractAction {
    private static Log logger = LogFactory.getLog(ActionSqlFileChooser.class);
    private Modello modello;
    private Vista vista;    
    private SQLConfigurationPM sqlConfigurationPM;

    public ActionSqlFileChooser() {
        super("...");       
    }    
    
    public void actionPerformed(ActionEvent e) {
        executeInjection();
        JFileChooser chooser = vista.getFileChooserApriSQL();
        File file;
        int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            sqlConfigurationPM.setSchemaPath(file.getAbsolutePath());
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

    public void setBean(SQLConfigurationPM sqlConfigurationPM) {
        this.sqlConfigurationPM = sqlConfigurationPM;
    }  
}
