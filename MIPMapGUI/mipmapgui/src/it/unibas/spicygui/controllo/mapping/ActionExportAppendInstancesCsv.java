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


import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.csv.AppendCsvInstancesMainFrame;
import java.util.HashMap;
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
public class ActionExportAppendInstancesCsv extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionExportTranslatedInstances.class);
    private LastActionBean lastActionBean;
    private Modello modello;
    private Vista vista;

    public ActionExportAppendInstancesCsv() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_APPEND_INSTANCES_CSV));
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
        IDataSourceProxy dataSource = mappingTask.getTargetProxy();
        AppendCsvInstancesMainFrame appendFrame = new AppendCsvInstancesMainFrame(dataSource);
        //jd.getResponse() returns the table name (key) and the file path (value)
        HashMap<String,String> absolutePaths = appendFrame.getResponse();
        if (!absolutePaths.isEmpty()){
            try{
                DAOCsv daoCsv = new DAOCsv();
                daoCsv.appendTranslatedCSVinstances(mappingTask, absolutePaths, scenario.getNumber());
                //daoCsv.appendTranslatedCSVinstances(mappingTask.getMappingData().getSolution().getDataSource(), absolutePaths);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK)));
                //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK));
            }catch (DAOException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                logger.error(ex);
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
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_APPEND_INSTANCES_CSV);
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
