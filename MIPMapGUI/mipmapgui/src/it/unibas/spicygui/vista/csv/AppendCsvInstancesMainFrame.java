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
public class AppendCsvInstancesMainFrame extends javax.swing.JDialog{
    
    IDataSourceProxy dataSource;
    HashMap<String,String> response = new HashMap();
    List<AppendCsvInstancesPanel> instanceArray;
    
    public AppendCsvInstancesMainFrame(IDataSourceProxy dataSource){
        this.dataSource = dataSource;
        
        Image imageDefault = ImageUtilities.loadImage(Costanti.ICONA_MIPMAP, true);
        setIconImage(imageDefault);
        setTitle(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.APPEND));
        setSize(400, 300);
        setModal(true);
        setLayout(new GridLayout(0,1));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //for each table in schema
        INode schema = dataSource.getSchema();
        instanceArray = new ArrayList<AppendCsvInstancesPanel>();
        for (INode tableName : schema.getChildren()){
            AppendCsvInstancesPanel instancePanel = new AppendCsvInstancesPanel(tableName.getLabel());
            add(instancePanel);
            instanceArray.add(instancePanel);
        }        
        JPanel instanceMain=new AppendCsvInstancesBottomPanel(this);
        add(instanceMain);  
        pack();
        setLocationRelativeTo(null);
        setVisible(true); 
    }
    
    public void setResponse(HashMap<String,String> response){
        this.response=response;
    }
    
    public HashMap<String,String> getResponse(){
        return response;
    }
    

    public HashMap<String,String> getAllPaths(){
        HashMap<String,String> paths = new HashMap();
        for (AppendCsvInstancesPanel panel : instanceArray){
           // if (!panel.getTablePathField().getText().equals("")){
                //add the table name and the path to the file containing the instances
                paths.put(panel.getTableNameLabel().getText(),panel.getTablePathField().getText());
          //  }
        }
        return paths;
    }
    
}
