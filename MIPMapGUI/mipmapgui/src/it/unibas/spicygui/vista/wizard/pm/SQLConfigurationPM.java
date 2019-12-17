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
    along with MIPMap.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibas.spicygui.vista.wizard.pm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

//giannisk
public class SQLConfigurationPM {
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String schemaPath;
    private String dbName;
     
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public String getSchemaPath() {
        return this.schemaPath;
    }
    
    public void setSchemaPath(String schemaPath) {
        String oldSchemaPath = this.schemaPath;
        this.schemaPath = schemaPath;
        fireEvents("schemaPath", oldSchemaPath, this.schemaPath);
    }
    
    public String getDBName() {
        return this.dbName;
    }

    public void setDBName(String dbName) {
        String oldDBName = this.dbName;
        this.dbName = dbName;
        fireEvents("dbName", oldDBName, this.dbName);
    }
    
    public boolean checkDatabaseNameField() {
        return (this.dbName!= null && !this.dbName.equals(""));    
    }   
    
    public boolean checkFileFields() {
        FileNameExtensionFilter filtro = new FileNameExtensionFilter(null, "sql");
        File percorso1 = null;
        try {
            percorso1 = new File(this.schemaPath);
        } catch (NullPointerException ex) {
            return false;
        }
        if (percorso1 != null) {
            return (filtro.accept(percorso1));
        }
        return false;
    }
    
    private void fireEvents(String propertyName, String oldValue, String newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
}
