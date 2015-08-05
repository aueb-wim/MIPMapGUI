/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import it.unibas.spicy.model.algebra.query.operators.sql.GenerateSQL;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.Types;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//giannisk
public class ExportCSVInstances {
    private static Log logger = LogFactory.getLog(ExportCSVInstances.class);
    
    private Connection getConnectionToPostgres(IConnectionFactory connectionFactory, int scenarioNo) throws DAOException{
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME+scenarioNo);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        
        return connectionFactory.getConnection(accessConfiguration);
    }
    
    public void exportCSVInstances(MappingTask mappingTask, String directoryPath, String suffix, int scenarioNo) throws DAOException, SQLException, IOException{          
        String folderPath = generateFolderPath(mappingTask.getTargetProxy().getIntermediateSchema(), directoryPath, suffix, 0);     
        //create CSV Folder
        new File(folderPath).mkdir(); 
        //connection to Postgres
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory, scenarioNo);
        try{
            Statement statement = connection.createStatement();
            
            //get table names from target database
            DatabaseMetaData databaseMetaData = connection.getMetaData();          
            String[] tableTypes = new String[]{"TABLE"};

            ResultSet tableResultSet = databaseMetaData.getTables(SpicyEngineConstants.MAPPING_TASK_DB_NAME+scenarioNo, GenerateSQL.TARGET_SCHEMA_NAME, null, tableTypes);
            while (tableResultSet.next()) { 
                String tableName = tableResultSet.getString("TABLE_NAME");
                createCSVDocument(tableName, GenerateSQL.TARGET_SCHEMA_NAME+".", mappingTask.getTargetProxy().getIntermediateSchema().getChild(tableName), folderPath, statement, null);           
            }  
        }finally{        
            //close connection
            if(connection != null)
              connectionFactory.close(connection); 
        }
    }
    
    private String generateFolderPath(INode rootNode, String directoryPath, String suffix, int i) {
        String folderPath = "";
        if (directoryPath.endsWith(File.separator)) {
            folderPath = directoryPath + rootNode.getLabel() + suffix + i;
        } else {
            folderPath = directoryPath + File.separator + rootNode.getLabel() + suffix + i;
        }
        return folderPath;
    }
    
    public void createCSVDocument(String tableName, String schema, INode tableNode, String folderPath, Statement statement, String[] columnNames) throws SQLException, IOException{
        File file = new File(folderPath+File.separator+tableName+".csv"); 
        ResultSet allRows = statement.executeQuery("SELECT * FROM "+schema+"\""+tableName+"\";");
        int columnCount = allRows.getMetaData().getColumnCount();                
        
        if(columnNames==null){
            //first write column names
            columnNames = new String[columnCount];
            int i = 0;
            //get column names from the first table tuple only           
            for (INode column: tableNode.getChild(0).getChildren()){
                //columnNames[i] = "\""+column.getLabel()+"\"";
                columnNames[i] = column.getLabel();                
                i++;
            }        
        }
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
        csvWriter.writeNext(columnNames, false);        
        
        try {    
            //then write the data                           
            while (allRows.next()){
                String[] row = new String[columnCount];
                for (int j=1; j<=columnCount; j++){
                    String dataType = allRows.getMetaData().getColumnTypeName(j);
                    String value = allRows.getString(j);                     
                    //if the value is null write null to csv file
                    if (value == null){
                        value = "null";
                    }       
                    //if the type is String/text etc and is not null put the value between double quotes
                    else if(dataType.toLowerCase().startsWith("varchar") || dataType.toLowerCase().startsWith("char") ||
                                dataType.toLowerCase().startsWith("text") || dataType.equalsIgnoreCase("bpchar") ||
                                dataType.equalsIgnoreCase("bit") || dataType.equalsIgnoreCase("mediumtext") ||
                                dataType.equalsIgnoreCase("longtext")||dataType.equalsIgnoreCase("serial") || 
                                dataType.equalsIgnoreCase("enum")){                            
                        value="\""+value+"\"";  
                    } 
                    row[j-1] = value;
                }
                //the following false value means there will not be quotes added, since we have already added double quotes only for string results
                csvWriter.writeNext(row, false);
            } 
        }finally{
            csvWriter.close();
            allRows.close();
        }
    }
    
    public void appendCSVInstances(MappingTask mappingTask, HashMap<String,String>  directoryPaths, int scenarioNo) throws SQLException, DAOException, IOException {         
        //connection to Postgres
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory, scenarioNo);
        try{
            Statement statement = connection.createStatement();

            //get table names from target database
            DatabaseMetaData databaseMetaData = connection.getMetaData();          
            String[] tableTypes = new String[]{"TABLE"}; 
            ResultSet tableResultSet = databaseMetaData.getTables(SpicyEngineConstants.MAPPING_TASK_DB_NAME+scenarioNo, GenerateSQL.TARGET_SCHEMA_NAME, null, tableTypes);
            //for each table
            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString("TABLE_NAME");
                String filePath = directoryPaths.get(tableName);

                if ((filePath!=null)&&(!filePath.equals(""))){
                    ResultSet allRows = statement.executeQuery("SELECT * FROM " +GenerateSQL.TARGET_SCHEMA_NAME+".\""+tableName+"\";");                
                    //no of columns
                    int columnCount = allRows.getMetaData().getColumnCount();                
                    //column names
                    String[] columnNames = new String[columnCount];
                    for (int i=1; i<=columnCount; i++){                    
                        columnNames[i-1]=allRows.getMetaData().getColumnName(i);
                    }                
                    if(checkDocForCSVColumns(columnCount, columnNames, filePath))
                        appendToCSVDocument(allRows, columnCount, filePath);
                    else{
                       throw new DAOException("Column names do not match those of the csv file"); 
                    }
                    allRows.close();
                }
            }
        }
        finally{                
            //close connection
            if(connection != null)
              connectionFactory.close(connection);
        }
    }
    
    private boolean checkDocForCSVColumns(int columnCount, String[] columnNames, String filePath) {
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath));
            // nextLine[] is an array of values from the line
            String [] nextLine;
            //read only first line
            nextLine = reader.readNext();
            
            //check for same length first
            if (nextLine.length!=columnCount)
                return false; 
            
            int i = 0;
            //if table columns are not the same -or the same order- as the ones of the csv file 
            for (String columnName: columnNames){
               String csvColumnName = nextLine[i].replace("\"","");               
               if (!columnName.equalsIgnoreCase(csvColumnName)){
                    return false;
               }
               i++;
            }
            reader.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExportCSVInstances.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(ExportCSVInstances.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
     }
     
     
    private void appendToCSVDocument(ResultSet allRows, int columnCount, String filePath) throws SQLException, IOException{          
        //the flag 'true' means that the writer will go to the end of the file and append the new data
        BufferedWriter bfout = new BufferedWriter(new FileWriter(filePath,true));            
        try { 
            while(allRows.next()) {                        
                String row="";
                for (int j=1; j<=columnCount; j++){
                    String dataType = allRows.getMetaData().getColumnTypeName(j);
                    String value = allRows.getString(j);                     
                    //if the value is null write null to csv file
                    if (value == null){
                        value = "null";
                    }       
                    //if the type is String/text etc and is not null put the value between double quotes
                    else if(dataType.toLowerCase().startsWith("varchar") || dataType.toLowerCase().startsWith("char") ||
                                dataType.toLowerCase().startsWith("text") || dataType.equalsIgnoreCase("bpchar") ||
                                dataType.equalsIgnoreCase("bit") || dataType.equalsIgnoreCase("mediumtext") ||
                                dataType.equalsIgnoreCase("longtext")||dataType.equalsIgnoreCase("serial") || 
                                dataType.equalsIgnoreCase("enum")){                            
                        value = "\"" + value + "\"";  
                    } 
                    row = row + value + ",";
                }
                //take out the last ',' character
                row = row.substring(0, row.length()-1);
                bfout.write(row);
                bfout.newLine();                        
            }
        } finally{
            bfout.close();
        }        
    }
    
    public void exportPKConstraintCSVInstances(MappingTask mappingTask, String directoryPath, HashSet<String> tableNames, String suffix, int scenarioNo) throws DAOException, SQLException, IOException{
        String folderPath = generateFolderPath(mappingTask.getTargetProxy().getIntermediateSchema(), directoryPath, suffix, 0);     
        //create Folder
        new File(folderPath).mkdir();    
        //connection to Postgres
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory, scenarioNo);
        
        try{
            Statement statement = connection.createStatement();
            for (String tableName : tableNames) {
                createCSVDocument(tableName, GenerateSQL.WORK_SCHEMA_NAME+".", mappingTask.getTargetProxy().getIntermediateSchema().getChild(tableName), folderPath, statement, null);           
            }  
        }finally{        
            //close connection
            if(connection != null)
              connectionFactory.close(connection); 
        }
    }
    
}
