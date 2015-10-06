/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.vista.csv;

import it.unibas.spicygui.Costanti;
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.util.ImageUtilities;

public class UnpivotCsvMainFrame extends javax.swing.JDialog{
    
    List<UnpivotCsvColumnPanel> columnList;
    List<String> columnNamesToUnpivot = new ArrayList<String>();
    List<String> keepColumnNames = new ArrayList<String>();
    String newColumnName;
    
    public UnpivotCsvMainFrame(String[] columnNames){       
        Image imageDefault = ImageUtilities.loadImage(Costanti.ICONA_MIPMAP, true);
        setIconImage(imageDefault);
        setTitle(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.ACTION_UNPIVOT_CSV));
        setPreferredSize(new Dimension(400, 500));        
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //Container Panel
        JPanel mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        //add(mainPanel);   
        JScrollPane scroll = new JScrollPane(mainPanel);
        add(scroll);
        
        ParallelGroup pl = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        SequentialGroup seq = layout.createSequentialGroup();        
        seq.addContainerGap();
        
        //Upper Label Panel
        JPanel upperPanel = new JPanel();
        JLabel rowLabel = new JLabel();
        rowLabel.setText(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.ROW_LABEL));
        upperPanel.add(rowLabel);        
        pl.addComponent(upperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        seq.addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        //Select all panel
        JPanel selectAllPanel = new UnpivotCsvSelectAllPanel(this, org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.SELECT_ALL_LABEL));
        selectAllPanel.setPreferredSize(new Dimension(300, 20));    
        pl.addComponent(selectAllPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        seq.addComponent(selectAllPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        //for each column
        columnList = new ArrayList<UnpivotCsvColumnPanel>();
        for (String columnName : columnNames){
            UnpivotCsvColumnPanel columnPanel = new UnpivotCsvColumnPanel(columnName);
            columnPanel.setPreferredSize(new Dimension(300, 20));
            pl.addComponent(columnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
            seq.addComponent(columnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
            columnList.add(columnPanel);
        }  
        
        //Last Panel with New Column Name textfield
        //and Buttons
        JPanel instanceMain=new UnpivotCsvColumnBottomPanel(this);
        pl.addComponent(instanceMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        seq.addComponent(instanceMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap();            
       
        //GroupLayout
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pl)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seq)
        );      
              
        pack();
        setLocationRelativeTo(null);
        setVisible(true); 
    }
    
    public List<String> setColNames(){
        List<String> columnNames = new ArrayList<String>();
        for (UnpivotCsvColumnPanel panel : columnList){
            if (panel.isColumnSelected()){
                columnNames.add(panel.getColNameLabel());
            }        
        }
        this.columnNamesToUnpivot = columnNames;
        return columnNames;
    }    
    
    public List<String> setKeepColNames(){
        List<String> keepColumnNames = new ArrayList<String>();
        for (UnpivotCsvColumnPanel panel : columnList){
            if (!panel.isColumnSelected()){
                keepColumnNames.add(panel.getColNameLabel());
            }        
        }
        this.keepColumnNames = keepColumnNames;
        return keepColumnNames;
    }            
    
    public void setNewColumnName(String newColumnName){
        this.newColumnName = newColumnName;
    }
    
    public List<String> getColNames(){
        return this.columnNamesToUnpivot;
    }
    
    public List<String> getKeepColNames(){
        return this.keepColumnNames;
    }
    
    public String getNewColName(){
        return this.newColumnName;
    }  
    
    public List<UnpivotCsvColumnPanel> getColumnsList(){
        return this.columnList;
    }
}
