/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.controllo.addtable;

/**
 *
 * @author ioannisxar
 */

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.Lookups;

public final class ActionAddTargetTable extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionAddSourceTable.class);
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

    public ActionAddTargetTable() {
        executeInjection();
        registraAzione();
        this.setEnabled(false);
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
                MappingTask mappingTask = (MappingTask) this.modello.getBean(Costanti.MAPPINGTASK_SHOWED);
                NewMappingTaskPM newMappingTaskPM = (NewMappingTaskPM) this.modello.getBean(Costanti.NEW_MAPPING_TASK_PM);
                IDataSourceProxy target = loadDataSource(newMappingTaskPM.getTargetElement(), false);
                mappingTask.addTarget(target);
                updateScenario(mappingTask);
                enableActions();
                actionViewSchema.performAction();
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
    
    
    private IDataSourceProxy loadCSVDataSource(CSVConfigurationPM configuration, boolean source) throws DAOException, SQLException {        
        MappingTask mappingTask = (MappingTask) this.modello.getBean(Costanti.MAPPINGTASK_SHOWED);
        IDataSourceProxy old = mappingTask.getTargetProxy();
        String dbName = (String) old.getAnnotation(SpicyEngineConstants.CSV_DB_NAME);
        ArrayList<String> oldCsvPaths = (ArrayList) old.getAnnotation(SpicyEngineConstants.CSV_TABLE_FILE_LIST);
        for(String csvPath: oldCsvPaths){
            System.out.println("MPIKA -> " + csvPath);
            configuration.addToSchemaPathList(csvPath);
        }
        
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
        IDataSourceProxy dataSource = daoCsv.loadSchema(Scenarios.lastScenarioNo, configuration.getSchemaPathList(), dbName, source, instancePathList);
        daoCsv.loadInstanceSample(dataSource, instancePathList, dbName);        
        return dataSource;        
    }

    public void update(Observable o, Object stato) {
        
        if (stato.equals(LastActionBean.CLOSE) || (stato.equals(LastActionBean.NO_SCENARIO_SELECTED)) ) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
        
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

    private WizardDescriptor.Panel[] getPanels() {
        panels = new WizardDescriptor.Panel[]{
                    new NewMappingTaskWizardPanel2(true)
                };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            steps[i] = c.getName();
        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_ADD_TARGET_TABLE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public static String getSHORT_DESCRIPTION() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_ADD_TARGET_TABLE);
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
