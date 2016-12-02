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
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibas.spicygui.controllo.mapping;


import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.DAOMappingTaskTgds;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.Vista;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JFileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

//giannisk
public class ActionExportTgds extends CallableSystemAction implements Observer{
    private static Log logger = LogFactory.getLog(ActionExportTranslatedInstances.class);
    private LastActionBean lastActionBean;
    private Modello modello;
    private Vista vista;
    private boolean continua;

    public ActionExportTgds() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TGDS));
        this.setEnabled(false);
        registraAzione();
    }
    
    public void update(Observable o, Object stato) {
    if (stato.equals(LastActionBean.SOLVE) || stato.equals(LastActionBean.SOLVE_AND_TRANSLATE)) {
            this.setEnabled(true);
        } else {
            this.setEnabled(false);
        }
    }

    @Override
    public void performAction() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTask mappingTask = scenario.getMappingTask();
               
        JFileChooser chooser = vista.getFileChooserApriTXT();
        File file;
        continua = true;
        while(continua){
            int returnVal = chooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(Costanti.class, Costanti.EXPORT_TGDS));
            if (returnVal == JFileChooser.APPROVE_OPTION) {   
                file = chooser.getSelectedFile();
                if (!file.exists()) {
                    saveTGDs(mappingTask, file);
                }
                else{
                    confirmSave(mappingTask, file);
                } 
            }
            else{
                continua = false;
            }
        }
        /*    List<FORule> foRules = mappingTask.getMappingData().getSTTgds(); 
        for (FORule foRule : foRules) {
            System.out.println(foRule.toLogicalString(mappingTask));
        }*/
    }
    
    private void confirmSave(MappingTask mappingTask, File file){
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Costanti.class, Costanti.FILE_EXISTS), DialogDescriptor.YES_NO_OPTION);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue().equals(NotifyDescriptor.YES_OPTION)) {
            saveTGDs(mappingTask, file);
        }
    }
    
    private void saveTGDs(MappingTask mappingTask, File file){       
        try {
            DAOMappingTaskTgds daoTgds = new DAOMappingTaskTgds();
            daoTgds.saveMappingTask(mappingTask, file.getAbsolutePath(), true);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_TGDS_OK));
        } catch (DAOException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.SAVE_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
            logger.error(ex);
        }        
        continua = false;
    }
    
    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }
    
    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TGDS);
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
