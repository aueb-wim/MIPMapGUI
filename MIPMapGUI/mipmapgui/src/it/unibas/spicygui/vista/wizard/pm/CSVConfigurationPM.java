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
 
package it.unibas.spicygui.vista.wizard.pm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.filechooser.FileNameExtensionFilter;

//giannisk
public class CSVConfigurationPM {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private HashSet <String> schemaPathList = new HashSet();
    private String dbName;
    private boolean schemaOnly;
    //private String schemaPath;
    //private String instancePath;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public HashSet<String> getSchemaPathList(){
        return this.schemaPathList;
    }
    
    public void addToSchemaPathList(String filePath){  
        HashSet<String> oldPathList = new HashSet<String> (this.schemaPathList);
        this.schemaPathList.add(filePath);
        fireEvents("schemaPathList", oldPathList, this.schemaPathList);       
    }
    
    public void removeFromSchemaPathList(String fileName){   
        HashSet<String> oldPathList = new HashSet<String> (this.schemaPathList);
        //find the file path that ends with the specific filename and remove it from the list
        Iterator<String> iterator = this.schemaPathList.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            File userFile = new File(element);
            String listFileName = userFile.getName();
            if(listFileName.equals(fileName)){
                iterator.remove();            
            }
        }
        fireEvents("schemaPathList", oldPathList, this.schemaPathList);
    }

    public String getDBName() {
        return this.dbName;
    }

    public void setDBName(String dbName) {
        String oldDBName = this.dbName;
        this.dbName = dbName;
        fireEvents("dbName", oldDBName, this.dbName);
    }
    
    public boolean getSchemaOnly() {
        return this.schemaOnly;
    }

    public void setSchemaOnly(boolean schemaOnly) {
        boolean oldSchemaOnly = this.schemaOnly;
        this.schemaOnly = schemaOnly;
        fireEvents("schemaOnly", oldSchemaOnly, this.schemaOnly);
    }
    
    public boolean checkDatabaseNameField() {
        return (this.getDBName()!= null && !this.getDBName().equals(""));    
    }    
    
    public boolean checkFieldsForSource() {
        FileNameExtensionFilter filtro = new FileNameExtensionFilter(null, "csv");
        File percorso1 = null;
        if (this.schemaPathList.isEmpty()){
            return false;
        }
        else{
            try {
                for (String schemaPath : this.schemaPathList){
                    percorso1 = new File(schemaPath);            
                    //if (percorso1 != null)  {
                        if(!filtro.accept(percorso1)){
                            return false;
                        }
                 //   }
                }
                return true;
            } catch (NullPointerException ex) {
                    return false;
            }
        }
    }
    
    public boolean checkFieldsForTarget() {
        FileNameExtensionFilter filtro = new FileNameExtensionFilter(null, "csv");
        File percorso1 = null;
        try {
            for (String schemaPath : this.schemaPathList){
                percorso1 = new File(schemaPath);
            }
        } catch (NullPointerException ex) {
            return false;
        }
        if (percorso1 != null) {
            return filtro.accept(percorso1);
        }
        return false;
    }
    
    /*
    public String getSchemaPath() {
        return this.schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        String oldSchemaPath = this.schemaPath;
        this.schemaPath = schemaPath;
        fireEvents("schemaPath", oldSchemaPath, this.schemaPath);
    }
    
    public void addSchemaPath(String schemaPath) {
        String oldSchemaPath = this.schemaPath;
        //giannisk if this is the first file to add
        if(this.schemaPath==""||this.schemaPath==null){
            this.schemaPath = schemaPath;
        }
        else {
            this.schemaPath = this.schemaPath+File.pathSeparator+schemaPath;
        }
        fireEvents("schemaPath", oldSchemaPath, this.schemaPath);

    } 
    
    public String getInstancePath() {
        return instancePath;
    }

    public void setInstancePath(String instancePath) {
        String oldInstancePath = this.instancePath;
        this.instancePath = instancePath;
        fireEvents("instancePath", oldInstancePath, this.instancePath);
    }
    */
    
    private void fireEvents(String propertyName, HashSet<String> oldValue, HashSet<String> newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    private void fireEvents(String propertyName, String oldValue, String newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    private void fireEvents(String propertyName, boolean oldValue, boolean newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
        
}
        