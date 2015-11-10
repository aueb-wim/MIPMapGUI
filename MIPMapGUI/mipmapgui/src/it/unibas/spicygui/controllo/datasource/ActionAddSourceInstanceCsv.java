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
 
package it.unibas.spicygui.controllo.datasource;

import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.InstancesTopComponent;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.csv.LoadCsvInstancesMainFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
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

//giannisk
public class ActionAddSourceInstanceCsv extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionAddSourceInstanceCsv.class);
    private Vista vista;
    private Modello modello;
    private LastActionBean lastActionBean;

    public ActionAddSourceInstanceCsv() {
        executeInjection();
        registraAzione();
        this.setEnabled(false);
    }

    @Override
    public void performAction() {
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTask mappingTask = scenario.getMappingTask();
        IDataSourceProxy dataSource = mappingTask.getSourceProxy();
        LoadCsvInstancesMainFrame jd = new LoadCsvInstancesMainFrame(dataSource);
        InstancesTopComponent viewInstancesTopComponent = scenario.getInstancesTopComponent();
        //jd.getResponse() returns the file path and the table name
        HashMap<String,String> absolutePaths = jd.getResponse();
        if (!absolutePaths.isEmpty())
            if(!scenario.getMappingTask().getSourceProxy().getType().equalsIgnoreCase("XML")){ 
                try {
                    //pathHashMap is a multimap set with the file path String as key
                    //and an arraylist with two values: a)the tablename
                    //b)a boolean value that represents if the file contains column names and
                    //c)a boolean that contains the info if the instance file has been already loaded
                    HashMap<String,ArrayList<Object>> pathHashMap = new HashMap<String,ArrayList<Object>>();
                    for (Map.Entry<String, String> entry : absolutePaths.entrySet()){
                        ArrayList<Object> valSet = new ArrayList<Object>();
                        //table name
                        valSet.add(entry.getValue());
                        valSet.add(jd.getColNames());
                        valSet.add(false);
                        pathHashMap.put(entry.getKey(),valSet);
                    }
                    //dataSource.getInstances().clear();
                    //dataSource.getOriginalInstances().clear();
                    DAOCsv daoCsv = new DAOCsv();
                    daoCsv.addInstances(dataSource, pathHashMap);

                    if (!viewInstancesTopComponent.isRipulito()) {
                        viewInstancesTopComponent.clearSource();
                        viewInstancesTopComponent.createSourceInstanceTree();
                        viewInstancesTopComponent.requestActive();
                    }
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.ADD_INSTANCE_OK));
                } catch (DAOException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.OPEN_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                    logger.error(ex);
                }
            }
        else{
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.CSV_INST_NOTIF), DialogDescriptor.ERROR_MESSAGE));
        }
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
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_ADD_SOURCE_INSTANCE_CSV);
    }

    @Override
    protected String iconResource() {
        return Costanti.ICONA_ADD_TARGET_INSTANCE;
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
