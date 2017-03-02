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


import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.sql.DAOSql;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.wizard.ExportDBWizardPanel;
import it.unibas.spicygui.vista.wizard.pm.RelationalConfigurationPM;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author ioannisxar
 */
public class ActionExportTranslatedInstancesSQL extends CallableSystemAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionExportTranslatedInstances.class);
    private LastActionBean lastActionBean;
    private Modello modello;
    private Vista vista;
    private WizardDescriptor.Panel[] panels;
    
    public ActionExportTranslatedInstancesSQL() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TRANSLATED_INSTANCES_SQL));
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
        insertBeanForBinding();
        Scenario scenario = (Scenario) modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTask mappingTask = scenario.getMappingTask();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Database Configuration");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled){
            try {
                RelationalConfigurationPM conf = (RelationalConfigurationPM) modello.getBean(Costanti.RELATIONAL_CONFIGURATION_SOURCE);
                String driver = conf.getDriver();
                String uri = conf.getUri();
                String userName = conf.getLogin();
                String password = conf.getPassword();
                DAOSql dao = new DAOSql();
                dao.exportTranslatedSQLInstances(mappingTask, scenario.getNumber(), driver, uri, userName, password);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_COMPLETED_OK)));
            }
            catch (DAOException ex){
            logger.error(ex);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
            }
        }
    }

    private WizardDescriptor.Panel[] getPanels() {
        panels = new WizardDescriptor.Panel[]{ new ExportDBWizardPanel()};
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
        return panels;
    }
    
    private void insertBeanForBinding() {
        this.modello.putBean(Costanti.RELATIONAL_CONFIGURATION_SOURCE, new RelationalConfigurationPM());
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
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_EXPORT_TRANSLATED_INSTANCES_SQL);
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
