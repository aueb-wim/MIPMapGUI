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
 
package it.unibas.spicygui.vista.wizard;

import it.unibas.spicygui.controllo.validators.ValidatoreWizardStep1Source;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class NewMappingTaskWizardPanel1 implements WizardDescriptor.Panel {

    private static Log logger = LogFactory.getLog(NewMappingTaskWizardPanel1.class);
    private Component component;
    private boolean addExtraTables;
    
    public NewMappingTaskWizardPanel1(){this.addExtraTables = false;}
    
    public NewMappingTaskWizardPanel1(boolean addExtraTables){
        this.addExtraTables = addExtraTables;
    }
    
    public boolean getExtraTables(){
        return this.addExtraTables;
                
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new NewMappingTaskVisualPanel1(this, this.addExtraTables);

        }
        return component;
    }

    public Component getMyComponent() {
        component = new NewMappingTaskVisualPanel1(this, this.addExtraTables);
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        if (ValidatoreWizardStep1Source.verifica()) {
            return true;
        }
        return false;
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }


    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        this.fireChangeEvent();
    }

    public void storeSettings(Object settings) {
    }
}

