package it.unibas.spicygui.controllo.mapping;

import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.json.DAOJson;
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
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

public class ActionExportTranslatedInstancesJson extends CallableSystemAction implements Observer {
    
    private static Log logger = LogFactory.getLog(ActionExportTranslatedInstances.class);
    private LastActionBean lastActionBean;
    private Modello modello;
    private Vista vista;

    public ActionExportTranslatedInstancesJson() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TRANSLATED_INSTANCES_JSON));
        this.setEnabled(false);
        registraAzione();
    }

    public void update(Observable o, Object stato) {
        if (stato.equals(LastActionBean.TRANSLATE) || stato.equals(LastActionBean.SOLVE_AND_TRANSLATE)) {
            this.setEnabled(true);
        } else if (stato.equals(LastActionBean.CLOSE) || (stato.equals(LastActionBean.NO_SCENARIO_SELECTED))) {
            this.setEnabled(false);
        }
    }

    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }

    @Override
    public void performAction() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTask mappingTask = scenario.getMappingTask();
        JFileChooser chooser = vista.getFileChooserSalvaFolder();
        File file;
        int returnVal = chooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(Costanti.class, Costanti.EXPORT));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                file = chooser.getSelectedFile();
                DAOJson daoJson = new DAOJson();
                daoJson.exportTranslatedJsonInstances(mappingTask, file.getAbsolutePath(), scenario.getNumber());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK)));
            } catch (DAOException ex) {
                logger.error(ex);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));                
            } 
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

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TRANSLATED_INSTANCES_JSON);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

//    @Override
//    protected String iconResource() {
//        return Costanti.ICONA_EXPORT_TRANSLATED_INSTANCES;
//    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
