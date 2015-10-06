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
 
package it.unibas.spicygui.controllo.mapping;


import it.unibas.spicy.model.algebra.query.operators.sql.ExecuteSQL;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicy.persistence.json.DAOJson;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.controllo.datasource.ActionViewInstances;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.InstancesTopComponent;
import it.unibas.spicygui.vista.Vista;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

public class ActionTranslate extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionTranslate.class);
    private Modello modello;
    private Vista vista;
    private LastActionBean lastActionBean;
    private ActionViewInstances actionViewInstances;

    public ActionTranslate() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_TRANSLATE_TOOLTIP));
        this.setEnabled(false);
        registraAzione();
    }

    private void enableActions() {
        this.lastActionBean.setLastAction(LastActionBean.TRANSLATE);
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.actionViewInstances == null) {
            this.actionViewInstances = Lookups.forPath("Azione").lookup(ActionViewInstances.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }

    public void update(Observable o, Object stato) {
        if (stato.equals(LastActionBean.SOLVE)) {
            this.setEnabled(true);
        } else {
            this.setEnabled(false);
        }
    }

    private void openWindows() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        InstancesTopComponent instancesTopComponent = scenario.getInstancesTopComponent();
        instancesTopComponent.clearTranslated();
        this.actionViewInstances.performAction();
    }

    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }
    
    private void translateToDatabase() throws DAOException{
        ExecuteSQL exSQL = new ExecuteSQL();
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        int scenarioNo = scenario.getNumber();
        MappingTask mappingTask = scenario.getMappingTask();
        String sqltext = mappingTask.getMappingData().getSQLScript();            
        exSQL.executeScript(mappingTask, sqltext, null, null, null, null,scenarioNo);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.TRANSLATION_OK)));
        
        //in case there were any instances that violated the Primary Key constraints, ask the user to export those instances
        checkForPKConstraints(mappingTask, exSQL.getPKConstraintsTables(), scenarioNo);        
    }
    
    private void checkForPKConstraints(MappingTask mappingTask, HashSet<String> pkTableNames, int scenarioNo) throws DAOException{
        if (!pkTableNames.isEmpty()){
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_PK_TABLES), DialogDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                String[] format = {NbBundle.getMessage(Costanti.class, Costanti.OUTPUT_TYPE_CSV), 
                    NbBundle.getMessage(Costanti.class, Costanti.OUTPUT_TYPE_JSON)};
                int formatResponse = JOptionPane.showOptionDialog(null, NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_PK_OUTPUT), NbBundle.getMessage(Costanti.class, Costanti.PK_OUTPUT_TITLE),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, format, format[0]);
                if (formatResponse != JOptionPane.CLOSED_OPTION){
                    JFileChooser chooser = vista.getFileChooserSalvaFolder();
                    File file;
                    int returnVal = chooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(Costanti.class, Costanti.EXPORT));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = chooser.getSelectedFile();
                        if (formatResponse == 0){
                            DAOCsv daoCsv = new DAOCsv();
                            daoCsv.exportPKConstraintCSVinstances(mappingTask, pkTableNames, file.getAbsolutePath(), scenarioNo);
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK)));
                        }
                        else if (formatResponse == 1){
                            DAOJson daoJson = new DAOJson();
                            daoJson.exportPKConstraintJsoninstances(mappingTask, pkTableNames, file.getAbsolutePath(), scenarioNo);
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK)));
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void performAction() {
        try {            
            //giannisk
            translateToDatabase();            
            openWindows();
            enableActions();            
        } catch (DAOException ex) {  
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_TRANSLATE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return Costanti.ICONA_TRANSLATE;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
