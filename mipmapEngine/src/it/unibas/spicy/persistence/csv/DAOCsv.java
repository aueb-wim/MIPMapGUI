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

package it.unibas.spicy.persistence.csv;

import com.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.datasource.DataSource;
import it.unibas.spicy.model.datasource.nodes.AttributeNode;
import it.unibas.spicy.model.datasource.nodes.LeafNode;
import it.unibas.spicy.model.datasource.nodes.SetNode;
import it.unibas.spicy.model.datasource.nodes.TupleNode;
import it.unibas.spicy.model.datasource.values.IOIDGeneratorStrategy;
import it.unibas.spicy.model.datasource.values.IntegerOIDGenerator;
import it.unibas.spicy.model.datasource.values.OID;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.proxies.ConstantDataSourceProxy;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.Types;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
//import au.com.bytecode.opencsv.CSVReader;
//import au.com.bytecode.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.relational.DAORelationalUtility;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//giannisk
public class DAOCsv {
    
    private static Log logger = LogFactory.getLog(DAOCsv.class);
    private static IOIDGeneratorStrategy oidGenerator = new IntegerOIDGenerator();
    private static final String TUPLE_SUFFIX = "Tuple";
    private static final String TRANSLATED_INSTANCE_SUFFIX = "-translatedInstances";
    private static final String CANONICAL_INSTANCE_SUFFIX = "-canonicalInstances";
    private static final String PK_CONSTRAINT_VIOLATED_INSTANCE_SUFFIX ="-PKConstraintViolatedInstances";
    private static final int NUMBER_OF_SAMPLE = 100;
    private static final int BATCH_SIZE = 500;
    
    //////////////////////////////////////////////////////////
    //////////////////////// SCHEMA
    //////////////////////////////////////////////////////////   
   
    public IDataSourceProxy loadSchema(int scenarioNo, HashSet<String> tablefullPathList, String catalog, boolean source, HashMap<String,ArrayList<Object>> instancePathList) throws DAOException {
        INode root = null;
        IDataSourceProxy dataSource = null;
        
        //postgres
        IConnectionFactory connectionFactory = null;
        Connection connection = null;
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        try  {
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            Statement statement = connection.createStatement();
            //postgres create schemas
            if(source){                        
                String createSchemasQuery = "create schema if not exists " + SpicyEngineConstants.SOURCE_SCHEMA_NAME+scenarioNo + ";\n";
                //createSchemasQuery += "create schema if not exists " + GenerateSQL.WORK_SCHEMA_NAME + ";\n";                        
                createSchemasQuery += "create schema if not exists " + SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo + ";";
                statement.executeUpdate(createSchemasQuery);
            }

            root = this.createRootNode(catalog);            
            List<String> tableFiles = new ArrayList<String>();

             for (String tablefullPath : tablefullPathList){
                 //getting the filename from file's full path
                 File userFile = new File(tablefullPath);
                 String filename = userFile.getName();                
                 //exclude filename extension
                 if (filename.indexOf(".") > 0) {
                     filename = filename.substring(0, filename.lastIndexOf("."));
                 }
                 INode setTable = this.createSetNode (filename, root, tablefullPath);
                 dataSource = new ConstantDataSourceProxy(new DataSource(SpicyEngineConstants.TYPE_CSV, root));
                
                 dataSource.addAnnotation(SpicyEngineConstants.CSV_DB_NAME, catalog);
                 dataSource.addAnnotation(SpicyEngineConstants.INSTANCE_PATH_LIST, instancePathList);
                 dataSource.addAnnotation(SpicyEngineConstants.CSV_TABLE_FILE_LIST, tableFiles);
                 
                 tableFiles.add(tablefullPath);                    

                 try{
                     String[] nextLine;        
                     // nextLine[] is an array of values from the line
//avenet 20/7
//                     CSVReader reader = new CSVReader(new FileReader(tablefullPath));
//                     CSVReader reader = new CSVReader(new FileReader(tablefullPath));
                    Reader r = new FileReader(tablefullPath);
                    CSVReader reader = new com.opencsv.CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();
                    
                     //read only first line
                     nextLine = reader.readNext();
                     String columns = "";
                     for (int i=0; i<nextLine.length; i++){
                         //trim and remove quotes                
                         String columnName = nextLine[i].trim();
                         //avenet 28/1/2017
                        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                        if (p.matcher(columnName).find()) {
                            String oldColumnName = columnName;
                            // System.out.println("ColumnName contains strange charachter: - " + columnName);
                            //replacing \" with "" is done because the old version had the startsWith (found below) before this line of code
                            dataSource.putChangedValue(filename+"."+columnName.replaceAll("\"", ""), oldColumnName.replaceAll("\"", ""));
                            columnName = columnName.replaceAll("[\\W]|_", "_");
//                            columnName = '\"' + columnName.substring(1, columnName.length());
//                            columnName = columnName.substring(0, columnName.length()-1) + '\"';

                            // System.out.println("ColumnName contains strange charachter: - " + columnName);
                        }
                         if (!(columnName.startsWith("\"") && columnName.endsWith("\""))){
                             columnName = "\""+columnName+"\"";
                         }
                    
                         //check for extra characters and rename the names of the 
                         //the "-" character is replaced since it cannot be accepted by JEP and MIMap
//                         if (columnName.contains("-") || columnName.contains("(") || columnName.contains(")") || columnName.contains("*") || columnName.contains("[") || columnName.contains("]") || columnName.contains("/") || columnName.contains(" ")){
//                             System.out.println("ColumnName contains strange charachter: - " + columnName);
//                             String oldColumnName = columnName;
//                             columnName = oldColumnName.replace("-","_").replace("(","_").replace(")","_").replace("*","_").replace("[","_").replace("]","_").replace("/","_").replace(" ","");
//                             dataSource.putChangedValue(filename+"."+columnName.replaceAll("\"", ""), oldColumnName.replaceAll("\"", ""));
//                             System.out.println("ColumnName contains strange charachter: - " + columnName);
//                         }
//                         if (columnName.contains("-")){
//                             String oldColumnName = columnName;
//                             columnName = oldColumnName.replace("-","_");
//                             dataSource.putChangedValue(filename+"."+columnName.replaceAll("\"", ""), oldColumnName.replaceAll("\"", ""));
//                         }
                         String typeOfColumn = Types.POSTGRES_STRING;
                         columns += columnName + " " + typeOfColumn + ",";
                     }

                     reader.close();
                     //take out the last ',' character
                     columns = columns.substring(0, columns.length()-1);

                     //giannisk postgres create table
                     String table;
                     if (source){
                         table = SpicyEngineConstants.SOURCE_SCHEMA_NAME+scenarioNo+".\""+filename+"\"";
                     }
                     else{
                         table = SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo+".\""+filename+"\"";    
                     }
                     statement.executeUpdate("drop table if exists "+ table);
                     statement.executeUpdate("create table "+ table +" ("+ columns+ ")");
                     dataSource.addAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG, false);
                 } catch(FileNotFoundException e){
                     e.printStackTrace();
                 } 
             }
            
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
        finally
        {   
            if(connection != null)
              connectionFactory.close(connection);
        }
        return dataSource;
    }
    
    public IDataSourceProxy loadSchemaForWeb(int scenarioNo, HashSet<String> tablefullPathList, String catalog) throws DAOException {
        INode root = null;
        IDataSourceProxy dataSource = null;
        try  {            
            root = this.createRootNode(catalog);            
            List<String> tableFiles = new ArrayList<String>();
            for (String tablefullPath : tablefullPathList){
                //getting the filename from file's full path
                File userFile = new File(tablefullPath);
                String filename = userFile.getName();                
                //exclude filename extension
                if (filename.indexOf(".") > 0) {
                    filename = filename.substring(0, filename.lastIndexOf("."));
                }
                INode setTable = this.createSetNode (filename, root, tablefullPath);
                tableFiles.add(tablefullPath);
            }
            dataSource = new ConstantDataSourceProxy(new DataSource(SpicyEngineConstants.TYPE_CSV, root));
            dataSource.addAnnotation(SpicyEngineConstants.CSV_DB_NAME, catalog);
            dataSource.addAnnotation(SpicyEngineConstants.CSV_TABLE_FILE_LIST, tableFiles);
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }        
        return dataSource;
    }
    
    private INode createRootNode(String catalog) {        
        INode root = new TupleNode(catalog);
        root.setNotNull(true);
        root.setRequired(true);
        root.setRoot(true);
        addNode(catalog, root);
        return root;
    }
    
    private INode createSetNode (String tableLabel, INode root, String filepath) throws IOException{
        INode setTable = new SetNode(tableLabel);    
        setTable.addChild(getTuple(tableLabel, filepath));
        setTable.setNotNull(true);
        setTable.setRequired(false);
        root.addChild(setTable);
        addNode(tableLabel, setTable);        
        return setTable;
    }
    
    private TupleNode getTuple(String tableName, String filepath) throws FileNotFoundException, IOException{
        if (logger.isDebugEnabled()) logger.debug("\nTable: " + tableName);
        
        TupleNode tupleNode = new TupleNode(tableName + TUPLE_SUFFIX);
        tupleNode.setRequired(false);
        tupleNode.setNotNull(true);
        tupleNode.setVirtual(true);
        addNode(tableName + TUPLE_SUFFIX, tupleNode);
        
        try{
            String[] nextLine;        
            // nextLine[] is an array of values from the line
            //avenet
//          CSVReader reader = new CSVReader(new FileReader(filepath));
            Reader r = new FileReader(filepath);
            CSVReader reader = new com.opencsv.CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

            //read only first line
            nextLine = reader.readNext();
            
            for (int i=0; i<nextLine.length; i++){
                //trim and remove quotes                
                
                String columnName = nextLine[i].trim().replace("\"","");
                
                //avenet 28/1/2017
                Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                if (p.matcher(columnName).find()) {
                    String oldColumnName = columnName;
                    columnName = columnName.replaceAll("[\\W]|_", "_");
//                    columnName = '\"' + columnName.substring(1, columnName.length());
//                    columnName = columnName.substring(0, columnName.length()-1) + '\"';
                }
                    
//                if (columnName.contains("-") || columnName.contains("(") || columnName.contains(")") || columnName.contains("*") || columnName.contains("[") || columnName.contains("]") || columnName.contains("/") || columnName.contains(" ")){
//                    String oldColumnName = columnName;
//                    columnName = oldColumnName.replace("-","_").replace("(","_").replace(")","_").replace("*","_").replace("[","_").replace("]","_").replace("/","_").replace(" ","");
//                }                
                
//                //the "-" character is replaced since it cannot be accepted by JEP and MIMap
//                if (columnName.contains("-")){
//                    columnName = columnName.replace("-","_");
//                }
                String keyColumn = tableName + "." + columnName;
                INode columnNode = new AttributeNode(columnName);
                addNode(keyColumn, columnNode);
                columnNode.setNotNull(false);
                String typeOfColumn = Types.STRING;
                columnNode.addChild(new LeafNode(typeOfColumn));
                tupleNode.addChild(columnNode);
                if (logger.isDebugEnabled()) logger.debug("\n\tColumn Name: " + columnName);
            }
            reader.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }  
        return tupleNode;
    }
    
            
    private void addNode(String key, INode node) {
        if (logger.isDebugEnabled()) logger.debug("Adding to map node: " + node.getLabel() + " with key: " + key);
        DAORelationalUtility.addNode(key, node);
    }  
    
    public static INode getNode(String label) {
        return DAORelationalUtility.getNode(label);
    }
    
    private static OID getOID() {
        return oidGenerator.getNextOID();
    }
    
    /////////////////////////////////////////////////////////////
    //////////////////////// INSTANCE
    /////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public void loadInstance(int scenarioNo, IDataSourceProxy dataSource, boolean source) throws DAOException, SQLException {
        
        IConnectionFactory connectionFactory = null;
        Connection connection = null;
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        try
        {
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            Statement statement = connection.createStatement();
        
            HashMap<String,ArrayList<Object>> strfullPath = (HashMap<String,ArrayList<Object>>) dataSource.getAnnotation(SpicyEngineConstants.INSTANCE_PATH_LIST);
            
            for (Map.Entry<String, ArrayList<Object>> entry : strfullPath.entrySet()) {
               String filePath = entry.getKey();
               //the list entry.getValue() contains a)the table name 
               //b)a boolean that contains the info if the instance file includes column names 
               //and c) a boolean that contains the info if the instance file has been already loaded 
               boolean loaded = (Boolean) entry.getValue().get(2);
               if (!loaded){               
                String tableName = (String) entry.getValue().get(0);
                if (source){
                    tableName =  SpicyEngineConstants.SOURCE_SCHEMA_NAME+scenarioNo+".\""+tableName+"\"";
                }
                else{
                    tableName =  SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo+".\""+tableName+"\"";
                }
                boolean colNames = (Boolean) entry.getValue().get(1);

                //avenet
                //CSVReader reader = new CSVReader(new FileReader(filePath));
                Reader r = new FileReader(filePath);
                CSVReader reader = new com.opencsv.CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

                try{
                     //ignore the first line if file includes column names
                     if (colNames){
                         reader.readNext();
                     }
                     String [] nextLine;
                     String values;

                     ArrayList<String> stmnt_list = new ArrayList<String>();
                     String sql_insert_stmnt ="";
                     int line = 0;
                     while ((nextLine = reader.readNext()) != null) {//for each line in the file   
                         line++;
                         //skip empty lines at the end of the csv file
                         if (nextLine.length != 1 || !nextLine[0].isEmpty()){
                             //insert into batches (of 500 rows)
                             if (line%BATCH_SIZE==0){
                                 //take out the last ',' character           
                                 sql_insert_stmnt = sql_insert_stmnt.substring(0, sql_insert_stmnt.length()-1);
                                 stmnt_list.add(sql_insert_stmnt);
                                 sql_insert_stmnt = "";
                             }
                             values = "";
                             for (int i=0; i<nextLine.length; i++){
                                 //avenet 20/7
                                 if( nextLine[i]!= null) {
//                                    if (!nextLine[i].equalsIgnoreCase("null")){
//                                        //replace double quotes with single quotes
//                                        //while first escape the character ' for SQL (the "replaceAll" method call)
                                        values += "'"+ nextLine[i].trim().replaceAll("'", "''") + "',";
//                                    }
//                                    //do not put quotes if value is the string null
//                                    else{
//                                        values += nextLine[i].trim().replaceAll("'", "''") + ",";   
//                                    }
                                 }
                                 else {
                                     values += "null,";
                                 }
                             }
                             //take out the last ',' character
                             values = values.substring(0, values.length()-1);
                             sql_insert_stmnt += "("+values+"),";
                         }
                    }
                     reader.close();                  
                     if (sql_insert_stmnt != ""){
                         //take out the last ',' character
                         sql_insert_stmnt = sql_insert_stmnt.substring(0, sql_insert_stmnt.length()-1);
                         stmnt_list.add(sql_insert_stmnt);
                         for (String stmnmt : stmnt_list){
                             statement.executeUpdate("insert into "+tableName+" values "+stmnmt+";");
                         } 
                     } 

                     //change the "loaded" value of the entry by replacing it in the hashmap
                     ArrayList<Object> valSet = new ArrayList<Object>();
                     valSet.add(tableName);
                     valSet.add(colNames);
                     valSet.add(true);
                     strfullPath.put(filePath, valSet);

                    }catch (IOException ex) {     
                         Logger.getLogger(DAOCsv.class.getName()).log(Level.SEVERE, null, ex);
                         throw new DAOException(ex);
                    }     
                dataSource.addAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG, true);
                }  
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DAOCsv.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException(ex);
        } finally {
          if(connection != null)
            connection.close();
        }               
    }  
        
    @SuppressWarnings("unchecked")
    public void loadInstanceSample(IDataSourceProxy dataSource, HashMap<String,ArrayList<Object>> strfullPath, String catalog) throws DAOException{
        INode root = null;
        try {
            HashMap<String,ArrayList<Object>> instanceInfoList = (HashMap<String,ArrayList<Object>>) dataSource.getAnnotation(SpicyEngineConstants.CSV_INSTANCES_INFO_LIST);   
            root = new TupleNode(getNode(catalog).getLabel(), getOID());
            root.setRoot(true); 
            for (Map.Entry<String, ArrayList<Object>> entry : strfullPath.entrySet()) {
                 String filePath = entry.getKey();                 
                //the list entry.getValue() contains a)the table name 
                //b)a boolean that contains the info if the instance file includes column names             
                String tableName = (String)entry.getValue().get(0);
                boolean colNames = (Boolean) entry.getValue().get(1);

                SetNode setTable = new SetNode(getNode(tableName).getLabel(), getOID());
                if (logger.isDebugEnabled()) logger.debug("extracting value for table " + tableName + " ....");
                
                getInstanceByTable(tableName, setTable, filePath, colNames);
                root.addChild(setTable);
                if (instanceInfoList == null) {
                    instanceInfoList = new HashMap<String,ArrayList<Object>>();
                    dataSource.addAnnotation(SpicyEngineConstants.CSV_INSTANCES_INFO_LIST, instanceInfoList);
                }
                instanceInfoList.putAll(strfullPath);
            }
            dataSource.addInstanceWithCheck(root);  
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }     
    }
    
    private void getInstanceByTable(String tableName, SetNode setTable, String filepath, Boolean includesColNames) throws DAOException, FileNotFoundException, IOException {

//avenet
//CSVReader reader = new CSVReader(new FileReader(filepath));
        Reader r = new FileReader(filepath);
        CSVReader reader = new com.opencsv.CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

        try{
            //ignore the first line if file includes column names
            if (includesColNames){
                reader.readNext();
            }
            String [] nextLine;
            int sampleSize = 0;
            
            while (((nextLine = reader.readNext()) != null) && (sampleSize < NUMBER_OF_SAMPLE)) {//for each line in the file until the NUMBER_OF_SAMPLE
                //skip empty lines at the end of the csv file
                if (nextLine.length != 1 || !nextLine[0].isEmpty()){
                    TupleNode tupleNode = new TupleNode(getNode(tableName + TUPLE_SUFFIX).getLabel(), getOID());
                    setTable.addChild(tupleNode);
                    int i=0;
                    for (INode attributeNodeSchema : getNode(tableName + TUPLE_SUFFIX).getChildren()) { 
                        AttributeNode attributeNode = new AttributeNode(attributeNodeSchema.getLabel(), getOID());
                        Object columnValue = null;
                        if ( nextLine[i] != null ){
                            columnValue = (Object)nextLine[i].trim();
                        }
                        i++;
                        LeafNode leafNode = createLeafNode(attributeNodeSchema, columnValue);
                        attributeNode.addChild(leafNode);
                        tupleNode.addChild(attributeNode);                   
               }
                    sampleSize++;
                }
            }
            
            int rowCount = sampleSize;
            while ((nextLine = reader.readNext()) != null) {
                rowCount++;
            }
            setTable.setFullSize(rowCount);
            
            reader.close();
        }catch(FileNotFoundException e){
            reader.close();
            e.printStackTrace();
        }     
    }
    
    private LeafNode createLeafNode(INode attributeNode, Object untypedValue) throws DAOException {
        LeafNode leafNodeInSchema = (LeafNode) attributeNode.getChild(0);
        String type = leafNodeInSchema.getLabel();
        Object typedValue = Types.getTypedValue(type, untypedValue);
        return new LeafNode(type, typedValue);
    }
    
    @SuppressWarnings("unchecked")
    public void addInstances(IDataSourceProxy dataSource, HashMap<String,ArrayList<Object>> newFilesMap) throws DAOException{
        //merge previous map with the new one
        HashMap<String,ArrayList<Object>> mergeMap = new HashMap<String,ArrayList<Object>>();
        HashMap<String,ArrayList<Object>> previousMap = (HashMap) dataSource.getAnnotation(SpicyEngineConstants.INSTANCE_PATH_LIST);
        mergeMap.putAll(previousMap);
        mergeMap.putAll(newFilesMap);
        //replace annotation
        dataSource.addAnnotation(SpicyEngineConstants.INSTANCE_PATH_LIST, mergeMap);
        //change flag so that new instances will be loaded to the temporary DB
        dataSource.addAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG, false);        
        String catalog = (String) dataSource.getAnnotation(SpicyEngineConstants.CSV_DB_NAME);
        //load instance sample of the new files
        loadInstanceSample(dataSource, newFilesMap, catalog);
    }
       
    public void exportTranslatedCSVinstances(MappingTask mappingTask, String directoryPath, int scenarioNo) throws DAOException {
        try{
            ExportCSVInstances exporter = new ExportCSVInstances();        
            exporter.exportCSVInstances(mappingTask, directoryPath, TRANSLATED_INSTANCE_SUFFIX, scenarioNo);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
    
    public void appendTranslatedCSVinstances(MappingTask mappingTask, HashMap<String,String> directoryPaths, int scenarioNo) throws DAOException {
        try {
            ExportCSVInstances exporter = new ExportCSVInstances();
            exporter.appendCSVInstances(mappingTask, directoryPaths, scenarioNo);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
    
    public void exportPKConstraintCSVinstances(MappingTask mappingTask, HashSet<String> tableNames, String directoryPath, int scenarioNo) throws DAOException {
        try{
            ExportCSVInstances exporter = new ExportCSVInstances();        
            exporter.exportPKConstraintCSVInstances(mappingTask, directoryPath, tableNames, PK_CONSTRAINT_VIOLATED_INSTANCE_SUFFIX, scenarioNo);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
    
}