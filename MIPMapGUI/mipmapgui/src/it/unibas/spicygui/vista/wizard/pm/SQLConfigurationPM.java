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
