/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.sql;

import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.ExportCSVInstances;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author ioannisxar
 */
public class ExportSQLInstances {
    
    private Connection getConnectionToPostgres(IConnectionFactory connectionFactory) throws DAOException{
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        
        return connectionFactory.getConnection(accessConfiguration);
    }
    
    private Connection getConnectionToDatabase(IConnectionFactory connectionFactory, String driver, String uri, String login, String pass) throws DAOException{
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(driver);
        accessConfiguration.setUri(uri);
        accessConfiguration.setLogin(login);
        accessConfiguration.setPassword(pass);
        
        return connectionFactory.getConnection(accessConfiguration);
    }
    
    public void exportSQLInstances(MappingTask mappingTask, int scenarioNo, String driver, String uri, 
            String userName, String password) throws DAOException, SQLException, IOException{          
        //connection to Postgres
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory);
        int isExisting = 0;
        if(driver.contains("postgresql")){
            //establish a connection with the selected database
            isExisting = createNewDatabaseIfNotExists(driver, uri, userName, password, 0);
        } else if (driver.contains("mysql")){
            isExisting = createNewDatabaseIfNotExists(driver, uri, userName, password, 1);
        }
        
        IConnectionFactory connectionFactoryCreateTable = new SimpleDbConnectionFactory();
        Connection connectionCreateTable = getConnectionToDatabase(connectionFactoryCreateTable, driver, uri, userName, password );
        DatabaseMetaData exportDatabaseMetaData = connectionCreateTable.getMetaData();
        int selectedDatabase = 0;
        String rootPath = "/tmp/";
        try{
            Statement statement = connection.createStatement();            
            //get table names from target database
            DatabaseMetaData databaseMetaData = connection.getMetaData();          
            String[] tableTypes = new String[]{"TABLE"};

            ResultSet tableResultSet = databaseMetaData.getTables(SpicyEngineConstants.MAPPING_TASK_DB_NAME, 
                    SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo, null, tableTypes);
            Map<String, ArrayList<String>> columnsPerTables;
            Map<String, ArrayList<String>> primaryKeyConstraintsPerTable;
            Map<String, ArrayList<ForeignTableKeyConstraints>> foreignKeyConstraintsPerTable;
            ArrayList<String> cols;
            List<String> createTableScriptList = new ArrayList<>();
            List<String> primaryKeyScriptList = new ArrayList<>();
            List<String> foreignKeyScriptList = new ArrayList<>();
            List<String> tableNames = new ArrayList<>();
            while (tableResultSet.next()) { 
                String tableName = tableResultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
                ResultSet tableColumns = statement.executeQuery("SELECT column_name, data_type, is_nullable "+
                " FROM information_schema.columns WHERE " + " table_schema = '" + SpicyEngineConstants.TARGET_SCHEMA_NAME+String.valueOf(scenarioNo) 
                        + "' AND table_name = '"+ tableName  + "' ORDER BY ordinal_position;");
                
                ResultSet pkConstraints = databaseMetaData.getPrimaryKeys(SpicyEngineConstants.MAPPING_TASK_DB_NAME, 
                        SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo, tableName);
                
                ResultSet fkConstraints = databaseMetaData.getImportedKeys(SpicyEngineConstants.MAPPING_TASK_DB_NAME, 
                        SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo, tableName);
                
                primaryKeyConstraintsPerTable = new LinkedHashMap<>();
                while (pkConstraints.next()) {
                    String pkColumnName = pkConstraints.getString("COLUMN_NAME");
                    if(primaryKeyConstraintsPerTable.get(tableName) == null) {
                        ArrayList<String> l = new ArrayList<>();
                        l.add(pkColumnName);
                        primaryKeyConstraintsPerTable.put(tableName, l);
                    } else {
                        ArrayList<String> l = primaryKeyConstraintsPerTable.get(tableName);
                        l.add(pkColumnName);
                        primaryKeyConstraintsPerTable.remove(tableName);
                        primaryKeyConstraintsPerTable.put(tableName, l);
                    }
                }
                foreignKeyConstraintsPerTable = new LinkedHashMap<>();
                while (fkConstraints.next()) {
                    String fkTableName = fkConstraints.getString("FKTABLE_NAME");
                    String fkColumnName = fkConstraints.getString("FKCOLUMN_NAME");
                    String pkTableName = fkConstraints.getString("PKTABLE_NAME");
                    String pkColumnName = fkConstraints.getString("PKCOLUMN_NAME");
                    if(foreignKeyConstraintsPerTable.get(tableName) == null) {
                       ArrayList<ForeignTableKeyConstraints> l = new ArrayList<>();
                       l.add(new ForeignTableKeyConstraints(fkTableName, fkColumnName, pkTableName, pkColumnName));
                       foreignKeyConstraintsPerTable.put(tableName,  l);
                    } else {
                        ArrayList<ForeignTableKeyConstraints> l = foreignKeyConstraintsPerTable.get(tableName);
                        l.add(new ForeignTableKeyConstraints(fkTableName, fkColumnName, pkTableName, pkColumnName));
                        foreignKeyConstraintsPerTable.remove(tableName);
                        foreignKeyConstraintsPerTable.put(tableName,  l);
                    }
                }
                
                columnsPerTables = new LinkedHashMap<>();
                while(tableColumns.next()){
                    cols = new ArrayList<>();
                    cols.add(tableColumns.getString("data_type"));
                    cols.add(tableColumns.getString("is_nullable"));
                    columnsPerTables.put(tableColumns.getString("column_name"), cols);
                }       
               
//                ResultSet exportTableResultSet = exportDatabaseMetaData.getTables(uri, 
//                "public", null, tableTypes);
//                while (exportTableResultSet.next()) {
//                    System.out.println(tableResultSet.getString("TABLE_NAME"));
//                }
                
                //create the appropriate create table script for the three different supported databases
                //Postgres 0
                //MySql 1
                //Derby 2
                Statement statementCreateAndInsertToTable = connectionCreateTable.createStatement();
                if(driver.contains("postgresql")){
                    //create table scripts
                    statementCreateAndInsertToTable.executeUpdate(createTableSqlScript(columnsPerTables, tableName, 0));
                    if(isExisting == 0) {
                        //insert primary key constraints
                        primaryKeyScriptList.add(insertPrimaryKeyConstraints(primaryKeyConstraintsPerTable, 0));
                        //insert foreign key constraints
                        foreignKeyScriptList.add(insertForeignKeyConstraints(foreignKeyConstraintsPerTable, 0));
                    }
                    selectedDatabase = 0;
                }else if (driver.contains("mysql")){
                    //create table scripts
                    statementCreateAndInsertToTable.executeUpdate(createTableSqlScript(columnsPerTables, tableName, 1));
                    if(isExisting == 0) {
                        //insert primary key constraints
                        primaryKeyScriptList.add(insertPrimaryKeyConstraints(primaryKeyConstraintsPerTable, 1));
                        //insert foreign key constraints
                        foreignKeyScriptList.add(insertForeignKeyConstraints(foreignKeyConstraintsPerTable, 1));
                    }
                    selectedDatabase = 1;
                }else if(driver.contains("derby")){
                    //create table scripts
                    createTableScriptList.add(createTableSqlScript(columnsPerTables, tableName, 2));
                    //insert primary key constraints,insert foreign key constraints
                    // it is not implemented for Derby
                    // primaryKeyScriptList.add(insertPrimaryKeyConstraints(primaryKeyConstraintsPerTable, 2));
                    //foreignKeyScriptList.add(insertForeignKeyConstraints(foreignKeyConstraintsPerTable, 2));
                    selectedDatabase = 2;
                }
                
                //add primary key constraints
                for(int i=0;i<primaryKeyScriptList.size();i++){
                    if (!primaryKeyScriptList.get(i).equals("")){
                        statementCreateAndInsertToTable.executeUpdate(primaryKeyScriptList.get(i));
                    }
                }
                primaryKeyScriptList.clear();
                ResultSet tableRows = statement.executeQuery("SELECT * FROM " + SpicyEngineConstants.TARGET_SCHEMA_NAME+String.valueOf(scenarioNo) 
                        + "." + tableName + ";");
                
                ResultSetMetaData rsmd = tableRows.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                String insertIntoScript = "";
                
                    while(tableRows.next()){
                        insertIntoScript = "INSERT INTO " + tableName + " VALUES \n";
                        insertIntoScript += "(";
                        for(int i=1;i<=columnsNumber;i++){
                            //if the column came from text field and the value is not null
                            if(isTextColumn(rsmd.getColumnTypeName(i)) && tableRows.getObject(i) != null){
                                insertIntoScript +=  "'" + tableRows.getObject(i) + "'";
                            } else {
                                insertIntoScript +=  tableRows.getObject(i);
                            }
                            if(i!=columnsNumber){
                                insertIntoScript += ",";
                            }
                        }
                        insertIntoScript += ");";
                        try{
                            statementCreateAndInsertToTable.executeUpdate(insertIntoScript);
                        } catch (Exception e){
                            //System.out.println(e);
                            //System.out.println(insertIntoScript);
                        }
                    }  
            }

//            try{
//                Statement statementCreateAndInsertToTable = connectionCreateTable.createStatement();
//                //execute create table scripts
////                for(int i=0;i<createTableScriptList.size();i++){
////                    statementCreateAndInsertToTable.executeUpdate(createTableScriptList.get(i));
////                }
//                
//                //add primary key constraints
//                for(int i=0;i<primaryKeyScriptList.size();i++){
//                    if (!primaryKeyScriptList.get(i).equals("")){
//                        statementCreateAndInsertToTable.executeUpdate(primaryKeyScriptList.get(i));
//                    }
//                }
//                
//                //add foreign key constraints
//                for(int i=0;i<foreignKeyScriptList.size();i++){
//                    if (!foreignKeyScriptList.get(i).equals("")){
//                        statementCreateAndInsertToTable.executeUpdate(foreignKeyScriptList.get(i));
//                    }
//                }
//                //export to csv
////                try{
////                        ExportCSVInstances exporter = new ExportCSVInstances();        
////                        exporter.exportCSVInstances(mappingTask, rootPath, "-temp", scenarioNo);
////                } catch (Throwable ex) {
////                        System.out.println("edww");
////                        throw new DAOException(ex.getMessage());
////                }
//                // export to db
////                insertIntoDb(mappingTask, statementCreateAndInsertToTable, rootPath, tableNames, selectedDatabase);
//            }finally{
////                try{
////                    // remove temporary csv
////                    deleteDir(new File(rootPath + mappingTask.getTargetProxy().getIntermediateSchema().getLabel() + "-temp0/"));
////                } catch(Exception ex){
////                    throw new DAOException(ex.getMessage());
////                }
//                //close connection
//                if(connectionFactoryCreateTable != null)
//                  connectionFactoryCreateTable.close(connectionCreateTable); 
//            }

        }finally{   
            if(connectionFactoryCreateTable != null)
                connectionFactoryCreateTable.close(connectionCreateTable); 
            //close connection
            if(connection != null)
              connectionFactory.close(connection); 
        }
    }
    
    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
    
    //creates the insert into scripts in order to save the values permanently in database
    private void insertIntoDb(MappingTask mappingTask, Statement statementCreateAndInsertToTable, 
            String rootPath, List<String> tableNames, int database) throws SQLException{
        for(String tableName : tableNames){
            String pathTempFile = "'" + rootPath + mappingTask.getTargetProxy().getIntermediateSchema().getLabel() + "-temp0/" + tableName +".csv'";
            //copy from csv to postgres
            if (database == 0) {
                statementCreateAndInsertToTable.executeUpdate("COPY " + tableName + " FROM " + pathTempFile + " DELIMITER ',' CSV HEADER;");
            } else if (database == 1) {
                System.out.println("LOAD DATA LOCAL INFILE " + pathTempFile
                        + " INTO TABLE " + tableName +" FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 ROWS");
                statementCreateAndInsertToTable.executeUpdate("LOAD DATA INFILE " + pathTempFile
                        + " INTO TABLE " + tableName +" FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 ROWS");
            }
            
        }
    }
    
    private String insertForeignKeyConstraints(Map<String, ArrayList<ForeignTableKeyConstraints>> foreignKeyConstraintsPerTable, int database){
        /*
        ALTER TABLE Orders
        ADD CONSTRAINT fk_PerOrders
        FOREIGN KEY (P_Id)
        REFERENCES Persons(P_Id)
        */
        String script = "";
        boolean hasPrimaryKey = false;
        for(Map.Entry<String, ArrayList<ForeignTableKeyConstraints>> entry : foreignKeyConstraintsPerTable.entrySet()) {
            hasPrimaryKey = true;
            for(int i=0;i<entry.getValue().size();i++) {
                String fkTable = entry.getValue().get(i).getForeignTable();
                String fkColumnName = entry.getValue().get(i).getForeignColumn();
                String pkTable = entry.getValue().get(i).getPrimaryKeyTable();
                String pkColumnName = entry.getValue().get(i).getPrimaryKeyColumn();
                if (database==0){
                    script += "ALTER TABLE \""+fkTable+"\" ADD FOREIGN KEY (\""+fkColumnName+"\") REFERENCES \""+pkTable+"\" (\""+pkColumnName+"\");";
                } else {
                    script += "ALTER TABLE "+fkTable+" ADD FOREIGN KEY ("+fkColumnName+") REFERENCES "+pkTable+" ("+pkColumnName+");";
                }
            }
        }
        return script;
    }
    
    private String insertPrimaryKeyConstraints(Map<String, ArrayList<String>> primaryKeyConstraintsPerTable, int database){
        /*
        ALTER TABLE table_name
        ADD CONSTRAINT MyPrimaryKey PRIMARY KEY (column1, column2...);
        */
        String script = "";
        boolean hasPrimaryKey = false;
        for(Map.Entry<String, ArrayList<String>> entry : primaryKeyConstraintsPerTable.entrySet()) {
            hasPrimaryKey = true;
            if (database==0) {
                script += "ALTER TABLE \"" + entry.getKey() + "\" ADD CONSTRAINT pk_constraint_" + entry.getKey() + " PRIMARY KEY (";
            } else {
                script += "ALTER TABLE " + entry.getKey() + " ADD CONSTRAINT pk_constraint_" + entry.getKey() + " PRIMARY KEY (";
            }
            
            for(int i=0;i<entry.getValue().size();i++) {
                if (i<entry.getValue().size()-1){
                    if(hasUpperCases(entry.getValue().get(i)) && database == 0){
                        script += "\"" + entry.getValue().get(i) + "\"" + ", ";
                    } else {
                        script += entry.getValue().get(i) + ", ";
                    }
                    
                } else {
                    if(hasUpperCases(entry.getValue().get(i)) && database == 0){
                        script += "\"" + entry.getValue().get(i) + "\"";
                    } else {
                        script += entry.getValue().get(i);
                    }
                }
            }
        }
        if (hasPrimaryKey) {
            script += ");";
        } 
        return script;
    }
    
    //checks if a column has upper case letters inside
    private boolean hasUpperCases(String text){
        return !text.equals(text.toLowerCase());
    }
    
    //checks if a column requires string format
    private boolean isTextColumn(String column){
        return column.toLowerCase().startsWith("varchar") || column.toLowerCase().startsWith("char") ||
                column.toLowerCase().startsWith("text") || column.toLowerCase().startsWith("bpchar") ||
                column.toLowerCase().startsWith("bit") || column.toLowerCase().startsWith("mediumtext") ||
                column.toLowerCase().startsWith("longtext") || column.toLowerCase().startsWith("datetime")
                || column.toLowerCase().startsWith("timestamp") || column.toLowerCase().startsWith("enum")
                || column.toLowerCase().startsWith("time") || column.toLowerCase().startsWith("date")  ;
    }
    
    // creates the proper create table sql scripts and mapps the right commands for any given database
    private String createTableSqlScript(Map<String, ArrayList<String>> columnsPerTables, String tableName, int database) throws IOException{
        String script;
        if (database==0) {
            script = "CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (\n";
        } else {
            script = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n";
        }     
        
        int row = 0;
        for(Map.Entry<String, ArrayList<String>> entry : columnsPerTables.entrySet()){
            //if the table column has at least one uppercase letter
            //and the database is Postgres(id==0)
            if(hasUpperCases(entry.getKey()) && database == 0){
                script += "\"" + entry.getKey() + "\"" + " ";
            } else {
                script += entry.getKey() + " ";
            }
            
            script += searchMappings(entry.getValue().get(0), database).toUpperCase() + " ";
            if (entry.getValue().get(1).equalsIgnoreCase("NO")){
                script += "NOT NULL" + " ";
            } 
            if(row < columnsPerTables.size()-1){
                script += ",\n";
            } else {
                script += "\n";
            }
            row++;
        }
        
        if(database == 2) {
            script += ")";
        } else {
            script += ");";
        }
        return script;
    }
    
    //loads the different database data types which will used in the table creation
    private Map<String,String> loadMappingDataTypes(int database) throws IOException{
        Map<String,String> mappings = new HashMap<>();
        String fileName = "src/misc/resources/mapDifferentDatabaseDataTypes.txt";
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = in.readLine()) != null) {
                if (!line.startsWith("--"))
                    mappings.put(line.split(",")[0].toLowerCase(), line.split(",")[database].toLowerCase());
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        return mappings;
    }
    
    //find the data type mappings for the table creation
    private String searchMappings(String value, int database) throws IOException{
        Map<String,String> mappings = loadMappingDataTypes(database);
        String result = mappings.get(value.toLowerCase());
        //if a mapping cannot be found, make the field text
        if (result == null)
            //in other than derby databases
            if(database != 2)
                return "text";
            else
                return "varchar(255)";
        return result;
    }
    
    private int createNewDatabaseIfNotExists(String driver, String uri, String userName, String password, int database) throws DAOException, SQLException{
        IConnectionFactory connectionFactoryCreateTable = new SimpleDbConnectionFactory();
        //remove database name from uri
        String extractDbName = uri.split("/")[3];
        String extractUri = uri.split(extractDbName)[0];
        Connection connection = getConnectionToDatabase(connectionFactoryCreateTable, driver, extractUri, userName, password);
        Statement statement = connection.createStatement();           
        int dbcount = 0;

        if (database == 0){
            ResultSet count = statement.executeQuery("select count(*) as dbcount from pg_catalog.pg_database where datname = '"+extractDbName+"';");
            while(count.next()){
                dbcount = count.getInt("dbcount");
            }
            count.close();
        } else if (database == 1) {
            ResultSet count = statement.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '"+extractDbName+"'");
            while(count.next()){
                dbcount += 1;
            }
            count.close();
        }

        if (dbcount==0){
            StringBuilder createDatabaseQuery = new StringBuilder(); 
            createDatabaseQuery.append("create database ").append(extractDbName).append(";\n");
            statement.executeUpdate(createDatabaseQuery.toString());
        }  
        return dbcount;
    }
    
    private boolean checkTablesIntegrityOfExistingDatabase(){
        return true;
    }
    
    private class ForeignTableKeyConstraints{
    
        private final String tableName, tableColumn, tableNamePK, tableColumnPK;
        
        public ForeignTableKeyConstraints(String tableName, String tableColumn, String tableNamePK, String tableColumnPK){
            this.tableName = tableName;
            this.tableColumn = tableColumn;
            this.tableNamePK = tableNamePK;
            this.tableColumnPK = tableColumnPK;
        }
        
        private String getForeignTable(){
            return this.tableName;
        }
        
        private String getForeignColumn(){
            return this.tableColumn;
        }
        
        private String getPrimaryKeyTable(){
            return this.tableNamePK;
        }
        
        private String getPrimaryKeyColumn(){
            return this.tableColumnPK;
        }
    }
}