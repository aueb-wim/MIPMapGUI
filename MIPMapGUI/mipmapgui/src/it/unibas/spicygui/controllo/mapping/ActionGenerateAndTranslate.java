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
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicy.persistence.json.DAOJson;
import it.unibas.spicy.persistence.relational.DAORelational;
import it.unibas.spicy.persistence.relational.DBFragmentDescription;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.controllo.datasource.ActionViewInstances;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.InstancesTopComponent;
import it.unibas.spicygui.vista.TransformationTopComponent;
import it.unibas.spicygui.vista.Vista;
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
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

public class ActionGenerateAndTranslate extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionGenerateAndTranslate.class);
    private ActionViewTransformation viewTransformationAction;
    private ActionViewInstances actionViewInstances;
    private Modello modello;
    private Vista vista;
    private LastActionBean lastActionBean;

    public ActionGenerateAndTranslate() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_SOLVING_AND_TRANSLATE_TOOLTIP));
        this.setEnabled(false);
        registraAzione();
    }

    private void enableActions() {
        lastActionBean.setLastAction(LastActionBean.SOLVE_AND_TRANSLATE);
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.viewTransformationAction == null) {
            this.viewTransformationAction = Lookups.forPath("Azione").lookup(ActionViewTransformation.class);
        }
        if (this.actionViewInstances == null) {
            this.actionViewInstances = Lookups.forPath("Azione").lookup(ActionViewInstances.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }

    public void update(Observable o, Object stato) {
        if (stato.equals(LastActionBean.CLOSE) || (stato.equals(LastActionBean.SOLVE))
                || (stato.equals(LastActionBean.NO_SCENARIO_SELECTED))) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    }

    private void openTransformationWindows() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        TransformationTopComponent transformationTopComponent = scenario.getTransformationTopComponent();
        if (transformationTopComponent == null) {
            transformationTopComponent = new TransformationTopComponent(scenario);
            scenario.setTransformationTopComponent(transformationTopComponent);
        }
        transformationTopComponent.clear();
        this.viewTransformationAction.performAction();
    }

    private void openInstanceWindows() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        InstancesTopComponent instancesTopComponent = scenario.getInstancesTopComponent();
        instancesTopComponent.clearTranslated();
        this.actionViewInstances.performAction();
    }

    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }
    
    private void translateToDatabase(MappingTask mappingTask, int scenarioNo) throws DAOException{              
        ExecuteSQL exSQL = new ExecuteSQL();
        String sqltext = mappingTask.getMappingData().getSQLScript(scenarioNo);
        exSQL.executeScript(mappingTask, sqltext, null, null, null, null,scenarioNo);
        java.util.Date date= new java.util.Date();
        System.out.println("Translation ended at: "+new Timestamp(date.getTime()));
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.TRANSLATION_OK)));
        //in case there were any instances that violated the Primary Key constraints, ask the user to export those instances
        checkForPKConstraints(mappingTask, exSQL.getPKConstraintsTables(), scenarioNo);  
    }
    
    private void loadSourceInstancesBeforeTranslation(IDataSourceProxy dataSource, int scenarioNo) throws DAOException, SQLException{
        String datasourceType = dataSource.getType();
        if(datasourceType.equalsIgnoreCase(NbBundle.getMessage(Costanti.class, Costanti.DATASOURCE_TYPE_CSV))){
            DAOCsv daoCsv = new DAOCsv();
            daoCsv.loadInstance(scenarioNo, dataSource, true);
        }
        else if(datasourceType.equalsIgnoreCase(NbBundle.getMessage(Costanti.class, Costanti.DATASOURCE_TYPE_RELATIONAL))){
            DAORelational daoRelational = new DAORelational();
            DBFragmentDescription dataDescription = new DBFragmentDescription();
            IConnectionFactory dataSourceDB = new SimpleDbConnectionFactory();
            AccessConfiguration accessConfiguration = (AccessConfiguration) dataSource.getAnnotation(SpicyEngineConstants.ACCESS_CONFIGURATION);
            daoRelational.loadInstance(scenarioNo, accessConfiguration, dataSource, dataDescription, dataSourceDB, true);
        }
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
    public void performAction() {
        java.util.Date date2= new java.util.Date();
        System.out.println("Translation started at: "+new Timestamp(date2.getTime()));
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTask mappingTask = scenario.getMappingTask();
        if (mappingTask.getValueCorrespondences().isEmpty() && mappingTask.getMappingData().getSTTgds().isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EMPTY_CORRESPONDENCES), DialogDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }   
        try {
            //giannisk - load instances for source schema according to datasource type, if they haven't been loaded already
            if(!(Boolean) mappingTask.getSourceProxy().getAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG))
                loadSourceInstancesBeforeTranslation(mappingTask.getSourceProxy(), scenario.getNumber());
            //giannisk - execute translation script on temporary database
            translateToDatabase(mappingTask, scenario.getNumber());            
            openTransformationWindows();
            openInstanceWindows();
            enableActions();
        } catch (DAOException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
        } catch (SQLException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_SOLVING_AND_TRANSLATE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return Costanti.ICONA_GENERATE_AND_TRANSLATE;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
