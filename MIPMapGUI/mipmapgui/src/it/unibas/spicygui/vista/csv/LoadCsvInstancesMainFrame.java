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
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicygui.vista.csv;

import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicygui.Costanti;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;

//giannisk
public class LoadCsvInstancesMainFrame extends javax.swing.JDialog{
    
    IDataSourceProxy dataSource;
    HashMap<String,String> response = new HashMap();
    boolean colNames;
    List<LoadCsvInstancesPanel> instanceArray;
    
    public  LoadCsvInstancesMainFrame(IDataSourceProxy dataSource){
        this.dataSource = dataSource;
        
        Image imageDefault = ImageUtilities.loadImage(Costanti.ICONA_MIPMAP, true);
        setIconImage(imageDefault);
        setTitle(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.LOAD2));
        setSize(400, 300);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new GridLayout(0,1));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //for each table in schema
        INode schema = dataSource.getSchema();
        instanceArray = new ArrayList<LoadCsvInstancesPanel>();
        for (INode tableName : schema.getChildren()){
            LoadCsvInstancesPanel instancePanel = new LoadCsvInstancesPanel(tableName.getLabel());
            add(instancePanel);
            instanceArray.add(instancePanel);
        }        
        JPanel instanceMain=new LoadCsvInstancesBottomPanel(this);
        add(instanceMain);  
        pack();
        setVisible(true); 
    }
    
    public void setResponse(HashMap<String,String> response){
        this.response=response;
    }
    
    public HashMap<String,String> getResponse(){
        return response;
    }
    
    public void setColNames(boolean colNames){
        this.colNames=colNames;
    }
    
    public boolean getColNames(){
        return colNames;
    }
    
    public HashMap<String,String> getAllPaths(){
        HashMap<String,String> paths = new HashMap();
        for (LoadCsvInstancesPanel panel : instanceArray){
            if (!panel.getTablePathField().getText().equals("")){
                //add the table name and the path to the file containing the instances
                paths.put(panel.getTablePathField().getText(),panel.getTableNameLabel().getText());
            }
        }
        return paths;
    }
    
}
