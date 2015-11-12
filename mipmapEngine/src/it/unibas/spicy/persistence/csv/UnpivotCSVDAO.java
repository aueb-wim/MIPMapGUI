/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.csv;

import au.com.bytecode.opencsv.CSVReader;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.DAOHandleDB;
import it.unibas.spicy.persistence.Types;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//giannisk
public class UnpivotCSVDAO {
    
    private final DAOHandleDB daoHandleDB = new DAOHandleDB();
    private static final int BATCH_SIZE = 500;
    private String[] csvTableColumns;
     
    public void unpivotTable(List<String> keepColNames, List<String> colNames, String newColName, File file) throws DAOException, SQLException, IOException{
        //create temp database "mipmaptask0" for storing values temporarily
        int databaseNo = 0;
        daoHandleDB.createSchema(databaseNo);
        
        IConnectionFactory connectionFactory;
        Connection connection = null;
        AccessConfiguration accessConfiguration = getAccessConfigurationForUnpivotDB();
        
        String tableName = file.getName();                
        //exclude filename extension
        if (tableName.indexOf(".") > 0) {
            tableName = tableName.substring(0, tableName.lastIndexOf("."));
        }
        
        String createTableQuery = createTableQuery(tableName, databaseNo);
        String insertToTableQuery = insertToTableQuery(file, tableName, databaseNo);
        String createviewQuery = createUnpivotQuery(keepColNames, colNames, newColName, tableName, databaseNo);
        try
        {
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableQuery);
            statement.executeUpdate(insertToTableQuery);
            statement.executeUpdate(createviewQuery);        
            //export un-pivoted table to csv
            ExportCSVInstances export = new ExportCSVInstances();
            String[] newTableColumns = new String [keepColNames.size()+2];
            int i=0;
            for (String keepColumn: keepColNames){
                newTableColumns[i] = keepColumn;
                i++;
            }
            newTableColumns[keepColNames.size()] = newColName;
            newTableColumns[keepColNames.size()+1] = "Value";
            export.createCSVDocument("unpivoted_"+tableName, SpicyEngineConstants.TARGET_SCHEMA_NAME+databaseNo, null, file.getParent(), statement, newTableColumns);
        }     
        finally
        {
            if(connection != null)
              connection.close();
        } 
        //drop temporary database
        daoHandleDB.dropSchema(databaseNo);
    }
    
    private AccessConfiguration getAccessConfigurationForUnpivotDB(){
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        return accessConfiguration;
    }
    
    private String createTableQuery(String tableName, int no) throws IOException{
        StringBuilder query = new StringBuilder();
        query.append("create table ").append(SpicyEngineConstants.SOURCE_SCHEMA_NAME).append(no).append(".\"").append(tableName).append("\" (");        
        String columns = "";
        String[] firstline = this.csvTableColumns;
        for (int i=0; i<firstline.length; i++){
            String typeOfColumn = Types.POSTGRES_STRING;
            columns += "\""+firstline[i] + "\" " + typeOfColumn + ",";
        }
        //take out the last ',' character
        columns = columns.substring(0, columns.length()-1);
        
        query.append(columns);        
        query.append(");");
        return query.toString();
    }
    
    private String insertToTableQuery(File originalTableFile, String tableName, int no) throws IOException{
        StringBuilder query = new StringBuilder();
        ArrayList<String> stmnt_list = getCsvTabledata(originalTableFile);
        for (String stmnmt : stmnt_list){
            query.append("insert into ").append(SpicyEngineConstants.SOURCE_SCHEMA_NAME).append(no).append(".\"").append(tableName).append("\" values ").append(stmnmt).append(";");
        }      
        return query.toString();
    }
    
    private String createUnpivotQuery(List<String> keepColNames, List<String> colNames, String newColName, String tableName,int no){
        StringBuilder query = new StringBuilder();
        query.append("create view ").append(SpicyEngineConstants.TARGET_SCHEMA_NAME).append(no).append(".\"unpivoted_").append(tableName).append("\" as (SELECT ");
        for (String keepcolumn : keepColNames){
            query.append("\"").append(keepcolumn).append("\", ");
        }
        query.append("UNNEST(ARRAY[");
        for (String column : colNames){
            query.append("'").append(column).append("', ");
        }
        //delete the last comma character
        query.delete(query.length() - ", ".length(), query.length());
        query.append("]) AS ").append(newColName).append(", UNNEST(ARRAY[");
        for (String column : colNames){
            query.append("\"").append(column).append("\", ");
        }
        //delete the last comma character
        query.delete(query.length() - ", ".length(), query.length());
        query.append("]) AS Value FROM ").append(SpicyEngineConstants.SOURCE_SCHEMA_NAME).append(no).append(".\"").append(tableName).append("\");");
        return query.toString();
    }
    
    public String[] getCsvTableColumns(File csvFile) throws FileNotFoundException, IOException{
        String [] firstLine;
        //read only first line
        CSVReader reader = new CSVReader(new FileReader(csvFile.getAbsolutePath()));
        //read only first line
        firstLine = reader.readNext();        
        this.csvTableColumns = firstLine;
        reader.close();
        return firstLine;
    }
    
    public ArrayList<String> getCsvTabledata(File csvFile) throws FileNotFoundException, IOException{
        ArrayList<String> stmnt_list = new ArrayList<String>();
        String sql_insert_stmnt;
        CSVReader reader = new CSVReader(new FileReader(csvFile.getAbsolutePath())); 
        reader.readNext();
        String [] nextLine;
        String values;
        sql_insert_stmnt = "";
        int line = 0;
        while ((nextLine = reader.readNext()) != null) {//for each line in the file
            line++;
            //insert into batches (of 500 rows)
            if (line%BATCH_SIZE==0){
                //take out the last ',' character
                sql_insert_stmnt = sql_insert_stmnt.substring(0, sql_insert_stmnt.length()-1);
                stmnt_list.add(sql_insert_stmnt);
                sql_insert_stmnt = "";
            }
            values = "";
            for (String nextLine1 : nextLine) {
                //replace double quotes with single quotes
                //while first escape the character ' for SQL (the "replaceAll" method call)
                if (nextLine1!=null && !nextLine1.equalsIgnoreCase("null")){
                    values += "'" + nextLine1.replaceAll("'", "''") + "',";
                }
                //if value is null or string 'null' insert null value to the database
                else{
                    values += nextLine1+ ",";
                }
            }
            //take out the last ',' character
            values = values.substring(0, values.length()-1);
            sql_insert_stmnt += "("+values+"),";
        }
    reader.close();
    //take out the last ',' character
    if(!sql_insert_stmnt.equals(""))
        sql_insert_stmnt = sql_insert_stmnt.substring(0, sql_insert_stmnt.length()-1);
    stmnt_list.add(sql_insert_stmnt);
    return stmnt_list;
    }
}


