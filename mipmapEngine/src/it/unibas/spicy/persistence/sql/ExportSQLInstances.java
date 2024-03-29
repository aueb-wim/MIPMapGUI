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
    
    private IConnectionFactory connectionFactory, connectionFactoryCreateTable;
    
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
        connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory);
        int isExisting = 0;
        
        if(driver.contains("postgresql")){
            //establish a connection with the selected database
            isExisting = createNewDatabaseIfNotExists(driver, uri, userName, password, 0);
        } else if (driver.contains("mysql")){
            isExisting = createNewDatabaseIfNotExists(driver, uri, userName, password, 1);
        }
        connectionFactoryCreateTable = new SimpleDbConnectionFactory();
        Connection connectionCreateTable = getConnectionToDatabase(connectionFactoryCreateTable, driver, uri, userName, password);
        boolean isCompatible = true;
        if(driver.contains("postgresql") && isExisting != 0){
            isCompatible = checkTablesIntegrityOfExistingDatabase(connectionCreateTable, uri, connection, scenarioNo, 0);
        } else if (driver.contains("mysql") && isExisting != 0){
            isCompatible = checkTablesIntegrityOfExistingDatabase(connectionCreateTable, uri, connection, scenarioNo, 1);
        }
        
        // The above commands are used for batch insert
        //int selectedDatabase = 0;
        //String rootPath = "/tmp/";
        if (isCompatible){
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
                        // The above command are used for batch insert
                        //selectedDatabase = 0;
                    }else if (driver.contains("mysql")){
                        //create table scripts
                        statementCreateAndInsertToTable.executeUpdate(createTableSqlScript(columnsPerTables, tableName, 1));
                        if(isExisting == 0) {
                            //insert primary key constraints
                            primaryKeyScriptList.add(insertPrimaryKeyConstraints(primaryKeyConstraintsPerTable, 1));
                            //insert foreign key constraints
                            foreignKeyScriptList.add(insertForeignKeyConstraints(foreignKeyConstraintsPerTable, 1));
                        }
                        // The above command are used for batch insert
                        //selectedDatabase = 1;
                    }
                    // it is not used at this time
                    else if(driver.contains("derby")){
                        //insert primary key constraints,insert foreign key constraints
                        // it is not implemented for Derby
                        // primaryKeyScriptList.add(insertPrimaryKeyConstraints(primaryKeyConstraintsPerTable, 2));
                        //foreignKeyScriptList.add(insertForeignKeyConstraints(foreignKeyConstraintsPerTable, 2));
                        //selectedDatabase = 2;
                    }

                    //add primary key constraints
                    for(int i=0;i<primaryKeyScriptList.size();i++){
                        if (!primaryKeyScriptList.get(i).equals("")){
                            statementCreateAndInsertToTable.executeUpdate(primaryKeyScriptList.get(i));
                        }
                    }
                    primaryKeyScriptList.clear();

                    //add foreign key constraints
                    //it is not used at this time
    //                for(int i=0;i<foreignKeyScriptList.size();i++){
    //                    if (!foreignKeyScriptList.get(i).equals("")){
    //                        statementCreateAndInsertToTable.executeUpdate(foreignKeyScriptList.get(i));
    //                    }
    //                }
    //                foreignKeyScriptList.clear();

                    ResultSet tableRows = statement.executeQuery("SELECT * FROM " + SpicyEngineConstants.TARGET_SCHEMA_NAME+String.valueOf(scenarioNo) 
                            + "." + tableName + ";");
                    insertIntoDbPerRow(tableRows, tableName, statementCreateAndInsertToTable);
                }

                // batch insert - cannot manage duplicate values and stops the whole insert procedure
    //            try{
    //                Statement statementCreateAndInsertToTable = connectionCreateTable.createStatement();        
    //                // export to db
    //                batchInsertIntoDb(mappingTask, statementCreateAndInsertToTable, rootPath, tableNames, selectedDatabase, scenarioNo);
    //            }finally{
    //                //close connection
    //                if(connectionFactoryCreateTable != null)
    //                  connectionFactoryCreateTable.close(connectionCreateTable); 
    //            }
            } catch (Exception e) {
                throw new DAOException(e.getMessage());
            }finally{   
                if(connectionCreateTable != null)
                    connectionFactoryCreateTable.close(connectionCreateTable); 
                //close connection
                if(connection != null)
                    connectionFactory.close(connection); 
            }
        } else {
            if(connectionCreateTable != null)
                    connectionFactoryCreateTable.close(connectionCreateTable); 
                //close connection
                if(connection != null)
                    connectionFactory.close(connection); 
            throw new DAOException("Non compatible target and export tables!");
        }
    }
    
    
    private boolean checkTablesIntegrityOfExistingDatabase(Connection connectionCreateTable, 
            String uri, Connection connection, int scenarioNo, int database) throws SQLException {
        try{
            DatabaseMetaData exportDatabaseMetaData = connectionCreateTable.getMetaData();
            String[] tableTypes = new String[]{"TABLE"};
            ResultSet exportTableResultSet = null;
            if(database == 0) {
                exportTableResultSet = exportDatabaseMetaData.getTables(uri, "public", null, tableTypes);
            } else if (database == 1) {
                exportTableResultSet = exportDatabaseMetaData.getTables(null, null, "%", tableTypes);
            } 
            Statement statement = connectionCreateTable.createStatement();
            ArrayList<TableSchema> exportDatabaseTables = new ArrayList<>();
            while (exportTableResultSet.next()) {
                String tableName = exportTableResultSet.getString("TABLE_NAME");
                ResultSet tableColumns = null;
                if(database == 0) {
                    tableColumns = statement.executeQuery("SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE " 
                        + " table_schema = 'public' AND table_name = '"+ tableName  + "' ORDER BY ordinal_position;");
                } else if (database == 1) {
                    String extractDbName = uri.split("/")[3];
                    tableColumns = statement.executeQuery("SELECT distinct column_name, data_type, is_nullable FROM information_schema.columns WHERE " 
                        + " table_schema = '"+ extractDbName + "' AND table_name = '"+ tableName  + "' ORDER BY ordinal_position;");
                }
                
                TableSchema t = new TableSchema(tableName);
                while(tableColumns.next()){
                    t.addDataType(tableColumns.getString("data_type"));
                    t.addIsNull(tableColumns.getString("is_nullable"));
                    t.addColumn(tableColumns.getString("column_name"));
                }
                exportDatabaseTables.add(t);
            }

            ArrayList<TableSchema> targetDatabaseTables = new ArrayList<>();
            statement = connection.createStatement();   
            //get table names from target database
            DatabaseMetaData databaseMetaData = connection.getMetaData();          
            ResultSet tableResultSet = databaseMetaData.getTables(SpicyEngineConstants.MAPPING_TASK_DB_NAME, 
                    SpicyEngineConstants.TARGET_SCHEMA_NAME+scenarioNo, null, tableTypes);
            while (tableResultSet.next()) { 
                String tableName = tableResultSet.getString("TABLE_NAME");
                ResultSet tableColumns = statement.executeQuery("SELECT column_name, data_type, is_nullable "+
                " FROM information_schema.columns WHERE " + " table_schema = '" + SpicyEngineConstants.TARGET_SCHEMA_NAME+String.valueOf(scenarioNo) 
                        + "' AND table_name = '"+ tableName  + "' ORDER BY ordinal_position;");
                
                TableSchema t = new TableSchema(tableName);
                while(tableColumns.next()){
                    if(database == 0) {
                        t.addDataType(tableColumns.getString("data_type"));
                    } else if (database == 1) {
                        t.addDataType(searchMappings(tableColumns.getString("data_type"), 1));
                    }
                    t.addIsNull(tableColumns.getString("is_nullable"));
                    t.addColumn(tableColumns.getString("column_name"));
                }
                targetDatabaseTables.add(t);
            }
            
            for(int i=0; i<exportDatabaseTables.size();i++){
                String exportTableName = exportDatabaseTables.get(i).getName();
                TableSchema searched = searchTableName(exportTableName, targetDatabaseTables);
                if (searched == null){
                    return false;
                } else {     
                    for(int j=0;j<exportDatabaseTables.get(i).getColumns().size();j++){
                        String exportTableColumn = exportDatabaseTables.get(i).getColumns().get(j);
                        String exportTableDataType = exportDatabaseTables.get(i).getDataType().get(j);
                        String exportTableNullable = exportDatabaseTables.get(i).getIsNull().get(j);
                        boolean columnCompatibility = searchColumnCompatibility(exportTableColumn, exportTableDataType, exportTableNullable, searched);
                        if (!columnCompatibility) {
                            return false;
                        }
                    }
                } 
            }
            
            for(int i=0; i<targetDatabaseTables.size();i++){
                String targetTableName = targetDatabaseTables.get(i).getName();
                TableSchema searched = searchTableName(targetTableName, exportDatabaseTables);
                if (searched == null){
                    return false;
                } else {     
                    for(int j=0;j<targetDatabaseTables.get(i).getColumns().size();j++){
                        String targetTableColumn = targetDatabaseTables.get(i).getColumns().get(j);
                        String targetTableDataType = targetDatabaseTables.get(i).getDataType().get(j);
                        String targetTableNullable = targetDatabaseTables.get(i).getIsNull().get(j);
                        boolean columnCompatibility = searchColumnCompatibility(targetTableColumn, targetTableDataType, targetTableNullable, searched);
                        if (!columnCompatibility) {
                            return false;
                        }
                    }
                } 
            } 
            
        } catch(Exception e) {
            System.out.println(e);
            if(connectionCreateTable != null)
                connectionFactoryCreateTable.close(connectionCreateTable); 
            //close connection
            if(connection != null)
                connectionFactory.close(connection); 
        }
        return true;
    }
    
    private boolean searchColumnCompatibility(String column, String dataType, String nullable, TableSchema t){
        boolean found = false;
        for(int i=0;i<t.getColumns().size();i++){
            if (column.equals(t.getColumns().get(i))
                    && dataType.split("\\(")[0].toLowerCase().equals(t.getDataType().get(i).split("\\(")[0].toLowerCase())
                    && nullable.equals(t.getIsNull().get(i))){
                found = true;
                break;
            }
        }
        return found;
    }
    
    private TableSchema searchTableName(String tableName, ArrayList<TableSchema> t){
        for (int i=0;i<t.size();i++){
            if (tableName.equals(t.get(i).getName())){
                return t.get(i);
            }
        }
        return null;
    }
    
    private void insertIntoDbPerRow(ResultSet tableRows, String tableName, Statement statementCreateAndInsertToTable) throws SQLException{
        ResultSetMetaData rsmd = tableRows.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int rowsnumber = 0;
        while(tableRows.next()){
            rowsnumber++;
            String insertIntoScript = "INSERT INTO " + tableName + " VALUES \n";
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
            }
        }
    }
    
    //batch insert to database
    //it is not used at this time
    private void batchInsertIntoDb(MappingTask mappingTask, Statement statementCreateAndInsertToTable, 
            String rootPath, List<String> tableNames, int database, int scenarioNo) throws SQLException, DAOException{
        //export to csv
        try{
                ExportCSVInstances exporter = new ExportCSVInstances();        
                exporter.exportCSVInstances(mappingTask, rootPath, "-temp", scenarioNo);
        } catch (Throwable ex) {
                throw new DAOException(ex.getMessage());
        }
        for(String tableName : tableNames){
            String pathTempFile = "'" + rootPath + mappingTask.getTargetProxy().getIntermediateSchema().getLabel() + "-temp0/" + tableName +".csv'";
            //copy from csv to postgres
            if (database == 0) {
                statementCreateAndInsertToTable.executeUpdate("COPY " + tableName + " FROM " + pathTempFile + " DELIMITER ',' CSV HEADER;");
            } else if (database == 1) {
                statementCreateAndInsertToTable.executeUpdate("LOAD DATA INFILE " + pathTempFile
                        + " INTO TABLE " + tableName +" FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 ROWS");
            }
            
        }
        try{
            // remove temporary csv
            deleteDir(new File(rootPath + mappingTask.getTargetProxy().getIntermediateSchema().getLabel() + "-temp0/"));
        } catch(Exception ex){
            throw new DAOException(ex.getMessage());
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
        for(Map.Entry<String, ArrayList<ForeignTableKeyConstraints>> entry : foreignKeyConstraintsPerTable.entrySet()) {
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
        if(database == 1){
            mappings.put("SMALLINT".toLowerCase(),"TINYINT".toLowerCase());
            mappings.put("INTEGER".toLowerCase(),"INT".toLowerCase());
            mappings.put("INT".toLowerCase(),"INT".toLowerCase());
            mappings.put("REAL".toLowerCase(),"FLOAT".toLowerCase());
            mappings.put("DOUBLE PRECISION".toLowerCase(),"DOUBLE".toLowerCase());
            mappings.put("BYTEA".toLowerCase(),"BLOB".toLowerCase());
            mappings.put("not available".toLowerCase(),"ZEROFILL".toLowerCase());
            mappings.put("TIME WITHOUT TIME ZONE".toLowerCase(),"TIME".toLowerCase());
            mappings.put("TIMESTAMP".toLowerCase(),"DATETIME".toLowerCase());
            mappings.put("TIMESTAMP WITHOUT TIME ZONE".toLowerCase(),"DATETIME".toLowerCase());
            mappings.put("CHARACTER VARYING".toLowerCase(),"VARCHAR(255)".toLowerCase());
            mappings.put("CHAR".toLowerCase(),"VARCHAR(255)".toLowerCase());
            mappings.put("SERIAL".toLowerCase(),"INT AUTO_INCREMENT".toLowerCase());
            mappings.put("TEXT".toLowerCase(),"TEXT".toLowerCase());
            mappings.put("NUMERIC".toLowerCase(),"DOUBLE".toLowerCase());
        } else if (database == 0) {
        mappings.put("SMALLINT".toLowerCase(),"SMALLINT".toLowerCase());
            mappings.put("INTEGER".toLowerCase(),"INTEGER".toLowerCase());
            mappings.put("INT".toLowerCase(),"INT".toLowerCase());
            mappings.put("REAL".toLowerCase(),"REAL".toLowerCase());
            mappings.put("DOUBLE PRECISION".toLowerCase(),"DOUBLE PRECISION".toLowerCase());
            mappings.put("BYTEA".toLowerCase(),"BYTEA".toLowerCase());
            mappings.put("not available".toLowerCase(),"not available".toLowerCase());
            mappings.put("TIME WITHOUT TIME ZONE".toLowerCase(),"TIME WITHOUT TIME ZONE".toLowerCase());
            mappings.put("TIMESTAMP".toLowerCase(),"TIMESTAMP".toLowerCase());
            mappings.put("TIMESTAMP WITHOUT TIME ZONE".toLowerCase(),"TIMESTAMP WITHOUT TIME ZONE".toLowerCase());
            mappings.put("CHARACTER VARYING".toLowerCase(),"CHARACTER VARYING".toLowerCase());
            mappings.put("CHAR".toLowerCase(),"CHAR".toLowerCase());
            mappings.put("SERIAL".toLowerCase(),"SERIAL".toLowerCase());
            mappings.put("TEXT".toLowerCase(),"TEXT".toLowerCase());
            mappings.put("NUMERIC".toLowerCase(),"NUMERIC".toLowerCase());
        }

        return mappings;
    }
    
    //find the data type mappings for the table creation
    private String searchMappings(String value, int database) throws IOException{
        Map<String,String> mappings = loadMappingDataTypes(database);
        String result = mappings.get(value.toLowerCase());
        //if a mapping cannot be found, make the field text
        if (result == null) {
            //other than derby databases
            if(database != 2)
                return "text";
            else
                return "varchar(255)";
        }
        return result;
    }
    
    private int createNewDatabaseIfNotExists(String driver, String uri, String userName, String password, int database) throws DAOException, SQLException{
        IConnectionFactory connectionFactoryCreateTable = new SimpleDbConnectionFactory();
        //remove database name from uri
        String extractDbName = uri.split("/")[3];
//        String extractUri = uri.split(extractDbName)[0];
        String extractUri = uri.substring(0, uri.lastIndexOf(extractDbName));
        Connection connection = getConnectionToDatabase(connectionFactoryCreateTable, driver, extractUri, userName, password);
//        System.out.println("connection created");
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
            createDatabaseQuery.append("create database \"").append(extractDbName).append("\" ;\n");
            statement.executeUpdate(createDatabaseQuery.toString());
        }  
        return dbcount;
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
    
    private class TableSchema {
    
        private final String tableName;
        private ArrayList<String> columns, dataType, isNull;
        
        public TableSchema(String tableName){
            this.tableName = tableName;
            columns = new ArrayList<String>();
            dataType = new ArrayList<String>();
            isNull = new ArrayList<String>();
        }
        
        private String getName(){
            return this.tableName;
        }
        
        private void addColumn(String col){
            columns.add(col);
        }
        
        private void addDataType(String dt){
            dataType.add(dt);
        }
        
        private void addIsNull(String in){
            isNull.add(in);
        }
        
        private ArrayList<String> getColumns(){
            return columns;
        }
        
        private ArrayList<String> getDataType(){
            return dataType;
        }
        
        private ArrayList<String> getIsNull(){
            return isNull;
        }
    }
    
}