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
 
package it.unibas.spicygui.controllo.file;


import it.unibas.spicy.model.datasource.ForeignKeyConstraint;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.relational.DAORelational;
import it.unibas.spicy.persistence.relational.DBFragmentDescription;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.persistence.xml.DAOXsd;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicy.persistence.sql.DAOSql;
import it.unibas.spicy.persistence.DAOHandleDB;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.AbstractScenario;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenarios;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.controllo.datasource.ActionViewSchema;
import it.unibas.spicygui.controllo.window.ActionProjectTree;
import it.unibas.spicygui.vista.treepm.TreeTopComponentAdapter;
import it.unibas.spicygui.vista.wizard.NewMappingTaskWizardPanel1;
import it.unibas.spicygui.vista.wizard.NewMappingTaskWizardPanel2;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import it.unibas.spicygui.vista.wizard.pm.NewMappingTaskPM;
import it.unibas.spicygui.vista.wizard.pm.XMLConfigurationPM;
import it.unibas.spicygui.vista.wizard.pm.RelationalConfigurationPM;
import it.unibas.spicygui.vista.wizard.pm.CSVConfigurationPM;
import it.unibas.spicygui.vista.wizard.pm.SQLConfigurationPM;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.Lookups;

public final class ActionNewMappingTask extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionNewMappingTask.class);
    private WizardDescriptor.Panel[] panels;
    private Modello modello;
    private LastActionBean lastActionBean;
    private ActionViewSchema actionViewSchema;
    private ActionProjectTree actionProjectTree;
    private DAORelational daoRelational = new DAORelational();
    private DAOSql daoSql = new DAOSql();
    private DAOXsd daoXsd = new DAOXsd();
    private DAOCsv daoCsv = new DAOCsv();
    private DAOHandleDB daoCreateDB = new DAOHandleDB();

    public ActionNewMappingTask() {
        executeInjection();
        registraAzione();
    }

    public void performAction() {
        insertBeanForBinding();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Choose input");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            try {
                //giannisk
                Scenarios.getNextFreeNumber();
                NewMappingTaskPM newMappingTaskPM = (NewMappingTaskPM) this.modello.getBean(Costanti.NEW_MAPPING_TASK_PM);
                IDataSourceProxy source = loadDataSource(newMappingTaskPM.getSourceElement(), true);
                IDataSourceProxy target = loadDataSource(newMappingTaskPM.getTargetElement(), false);
                MappingTask mappingTask = new MappingTask(source, target, SpicyEngineConstants.LINES_BASED_MAPPING_TASK);
                mappingTask.setModified(true);
                
                //giannisk 
                //prompt for automatic loading of foreign keys disabled
                if (!source.getForeignKeyConstraints().isEmpty()) {
                    confirmAddForeignKeyToJoin(source, true);
                }
                if (!target.getForeignKeyConstraints().isEmpty()) {
                    confirmAddForeignKeyToJoin(target, false);
                }
                
                gestioneScenario(mappingTask);
                enableActions();
                actionViewSchema.performAction();
                //giannisk open scenarios on the tree panel when creating a new mapping task
                actionProjectTree.performAction();
            } catch (Exception ex) {
                logger.error(ex);
                Scenarios.releaseNumber();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NEW_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
            } 
        }
    }
    
    private void confirmAddForeignKeyToJoin(IDataSourceProxy dataSource, boolean source) {
        NotifyDescriptor notifyDescriptor = null;
        if (source) {
            notifyDescriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Costanti.class, Costanti.CREATE_AUTOMATIC_JOINCONDITION_SOURCE), DialogDescriptor.YES_NO_OPTION);
        } else {
            notifyDescriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Costanti.class, Costanti.CREATE_AUTOMATIC_JOINCONDITION_TARGET), DialogDescriptor.YES_NO_OPTION);
        }
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue().equals(NotifyDescriptor.YES_OPTION)) {
            List<ForeignKeyConstraint> foreignKeyConstraints = new ArrayList<ForeignKeyConstraint>(dataSource.getForeignKeyConstraints());
            for (ForeignKeyConstraint foreignKey : foreignKeyConstraints) {
                dataSource.addJoinForForeignKey(foreignKey);
            }
        }

    }

    private void gestioneScenario(MappingTask mappingTask) {
        Scenarios scenarios = (Scenarios) modello.getBean(Costanti.SCENARIOS);
        if (scenarios == null) {
            scenarios = new Scenarios("PROGETTO DI PROVA");
            scenarios.addObserver(this.actionProjectTree);
            modello.putBean(Costanti.SCENARIOS, scenarios);
            actionProjectTree.performAction();
        }
//        modello.putBean(Costanti.ACTUAL_SAVE_FILE, null);
        AbstractScenario scenario = new Scenario("SCENARIO DI PROVA", mappingTask, true);
        scenario.addObserver(this.actionProjectTree);
        scenarios.addScenario(scenario);
        Scenario scenarioOld = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        if (scenarioOld != null) {
            LastActionBean lab = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
            scenarioOld.setStato(lab.getLastAction());
        }
        modello.putBean(Costanti.CURRENT_SCENARIO, scenario);
    }

    private void updateScenario(MappingTask mappingTask){
        Scenarios scenarios = (Scenarios) modello.getBean(Costanti.SCENARIOS);
        AbstractScenario scenario = new Scenario("SCENARIO DI PROVA", mappingTask, true);
        scenario.addObserver(this.actionProjectTree);
        scenarios.addScenario(scenario);
        Scenario scenarioOld = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        scenarioOld.getMappingTaskTopComponent().forceClose();
        modello.putBean(Costanti.CURRENT_SCENARIO, scenario);
    }
    private IDataSourceProxy loadDataSource(String type, boolean source) throws DAOException, SQLException {
        if (type.equals(NbBundle.getMessage(Costanti.class, Costanti.DATASOURCE_TYPE_RELATIONAL))) {
            if (source) {
                return loadRelationalDataSource((RelationalConfigurationPM) modello.getBean(Costanti.RELATIONAL_CONFIGURATION_SOURCE), source);
            }
            return loadRelationalDataSource((RelationalConfigurationPM) modello.getBean(Costanti.RELATIONAL_CONFIGURATION_TARGET), source);
        }
        else if (type.equals(NbBundle.getMessage(Costanti.class, Costanti.DATASOURCE_TYPE_SQL))) {
            if (source) {
                return loadSQLDataSource((SQLConfigurationPM) modello.getBean(Costanti.SQL_CONFIGURATION_SOURCE), source);
            }
            return loadSQLDataSource((SQLConfigurationPM) modello.getBean(Costanti.SQL_CONFIGURATION_TARGET), source);
        }
        else if (type.equals(NbBundle.getMessage(Costanti.class, Costanti.DATASOURCE_TYPE_XML))) {
            if (source) {
                return loadXMLDataSource((XMLConfigurationPM) modello.getBean(Costanti.XML_CONFIGURATION_SOURCE));
            }
            return loadXMLDataSource((XMLConfigurationPM) modello.getBean(Costanti.XML_CONFIGURATION_TARGET));
        }
        if (source) {
            return loadCSVDataSource((CSVConfigurationPM) modello.getBean(Costanti.CSV_CONFIGURATION_SOURCE), source);
        }
        return loadCSVDataSource((CSVConfigurationPM) modello.getBean(Costanti.CSV_CONFIGURATION_TARGET), source);  
    }

    private IDataSourceProxy loadRelationalDataSource(RelationalConfigurationPM configuration, boolean source) throws DAOException, SQLException {
        DBFragmentDescription dataDescription = new DBFragmentDescription();
        IConnectionFactory dataSourceDB = new SimpleDbConnectionFactory();
        IDataSourceProxy dataSource = daoRelational.loadSchema(Scenarios.lastScenarioNo, configuration.getAccessConfiguration(), dataDescription, dataSourceDB, source);
        daoRelational.loadInstanceSample(configuration.getAccessConfiguration(), dataSource, dataDescription, dataSourceDB, null, false);
        return dataSource;
    }
    
    private IDataSourceProxy loadSQLDataSource(SQLConfigurationPM configuration, boolean source) throws DAOException, SQLException {       
        IDataSourceProxy dataSource = daoSql.loadSchema(Scenarios.lastScenarioNo, configuration.getDBName(), configuration.getSchemaPath(), source);
        daoSql.loadInstanceSample(dataSource, configuration.getDBName(), configuration.getSchemaPath());
        return dataSource;
    }

    private IDataSourceProxy loadXMLDataSource(XMLConfigurationPM configuration) throws DAOException {
        IDataSourceProxy dataSource = daoXsd.loadSchema(configuration.getSchemaPath());
        if (configuration.getInstancePath() != null && !("".equals(configuration.getInstancePath()))) {
            daoXsd.loadInstance(dataSource, configuration.getInstancePath());
        }
        return dataSource;
    }
    
    //giannisk  
    private IDataSourceProxy loadCSVDataSource(CSVConfigurationPM configuration, boolean source) throws DAOException, SQLException {        
        HashMap<String,ArrayList<Object>> instancePathList = new HashMap<String,ArrayList<Object>>();
        if(!configuration.getSchemaOnly()){            
            for (String path : configuration.getSchemaPathList()){
                //tablename is the name of the csv schema file 
                //since schema and instance are loaded from the same file in this case
                File userFile = new File(path);
                String tableName = userFile.getName();
                //exclude filename extension
                if (tableName.indexOf(".") > 0) {
                    tableName = tableName.substring(0, tableName.lastIndexOf("."));
                }                
                ArrayList<Object> valSet = new ArrayList<Object>();
                valSet.add(tableName);
                valSet.add(true);
                valSet.add(false);
                instancePathList.put(path,valSet);
            }
        }
        IDataSourceProxy dataSource = daoCsv.loadSchema(Scenarios.lastScenarioNo, configuration.getSchemaPathList(), configuration.getDBName(), source, instancePathList);
        daoCsv.loadInstanceSample(dataSource, instancePathList, configuration.getDBName());        
        return dataSource;        
    }

    public void update(Observable o, Object stato) {
//        if (stato.equals(LastActionBean.CLOSE)) {
//            this.setEnabled(true);
//        } else {
//            this.setEnabled(false);
//        }
    }

    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }

    private void enableActions() {
        this.lastActionBean.setLastAction(LastActionBean.NEW);
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.actionViewSchema == null) {
            this.actionViewSchema = Lookups.forPath("Azione").lookup(ActionViewSchema.class);
        }
        if (this.actionProjectTree == null) {
            this.actionProjectTree= Lookups.forPath("Azione").lookup(ActionProjectTree.class);
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
//        if (panels == null) {
        panels = new WizardDescriptor.Panel[]{
                    new NewMappingTaskWizardPanel1(),
                    new NewMappingTaskWizardPanel2()
                };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }

        }
//        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_NEW);
    }

    @Override
    public String iconResource() {
        return Costanti.ICONA_NEW;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public static String getSHORT_DESCRIPTION() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_NEW_TOOLTIP);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void insertBeanForBinding() {
        this.modello.putBean(Costanti.RELATIONAL_CONFIGURATION_SOURCE, new RelationalConfigurationPM());
        this.modello.putBean(Costanti.RELATIONAL_CONFIGURATION_TARGET, new RelationalConfigurationPM());
        this.modello.putBean(Costanti.SQL_CONFIGURATION_SOURCE, new SQLConfigurationPM());
        this.modello.putBean(Costanti.SQL_CONFIGURATION_TARGET, new SQLConfigurationPM());
        this.modello.putBean(Costanti.XML_CONFIGURATION_SOURCE, new XMLConfigurationPM());
        this.modello.putBean(Costanti.XML_CONFIGURATION_TARGET, new XMLConfigurationPM());
        this.modello.putBean(Costanti.CSV_CONFIGURATION_SOURCE, new CSVConfigurationPM());
        this.modello.putBean(Costanti.CSV_CONFIGURATION_TARGET, new CSVConfigurationPM());
        this.modello.putBean(Costanti.NEW_MAPPING_TASK_PM, new NewMappingTaskPM());
    }
}
