/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.controllo.preprocessing;

import it.unibas.spicy.persistence.csv.ChangeDelimiterCSV;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.csv.ChangeDelimiterFrame;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JFileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;


public class ActionChangeDelimiter extends CallableSystemAction implements Observer {
    private static Log logger = LogFactory.getLog(ActionUnpivotCSV.class);
    private Modello modello;
    private LastActionBean lastActionBean;
    Vista vista;
    
    public ActionChangeDelimiter() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_CHANGE_DELIMITER));
        registraAzione();
    }
    
    @Override
    public void performAction() {
        JFileChooser chooser = vista.getFileChooserApriCSV();
        File file;
        int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            //file extension
            String ext = file.getPath().substring(file.getPath().lastIndexOf(".")+1);
            if (ext.equalsIgnoreCase("csv")){
                ChangeDelimiterFrame frame = new ChangeDelimiterFrame();
                String sourceDelimiter = frame.getSourceDelimiter();
                String sourceQuotes = frame.getSourceQuotes();
                boolean useTargetQuotes = frame.getTargetQuotes();
//                System.out.println(sourceDelimiter);
//                System.out.println(sourceQuotes);
//                System.out.println(useTargetQuotes);
                if (sourceDelimiter!=null && sourceQuotes!=null)
                    try{
                        ChangeDelimiterCSV changer = new ChangeDelimiterCSV();
                        changer.changeDelimiter(file, sourceDelimiter, sourceQuotes, useTargetQuotes);
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_CHANGED_DELIMITER)+file.getParent()));
                    } catch (IOException ex) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage() , DialogDescriptor.ERROR_MESSAGE));
                    }                
            }        
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_WRONG_FILE_INPUT)+": \""+ext+"\"", DialogDescriptor.ERROR_MESSAGE));
            }
        }
    }
    
    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }
    
    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }
    
    public void update(Observable o, Object stato) {}

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_CHANGE_DELIMITER);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
  
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
}
