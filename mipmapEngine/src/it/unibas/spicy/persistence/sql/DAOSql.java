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

package it.unibas.spicy.persistence.sql;

//giannisk, ioannisxar

import it.unibas.spicy.model.algebra.query.operators.sql.GenerateSQL;
import it.unibas.spicy.model.datasource.DataSource;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.datasource.KeyConstraint;
import it.unibas.spicy.model.datasource.nodes.AttributeNode;
import it.unibas.spicy.model.datasource.nodes.LeafNode;
import it.unibas.spicy.model.datasource.nodes.SetNode;
import it.unibas.spicy.model.datasource.nodes.TupleNode;
import it.unibas.spicy.model.datasource.values.IOIDGeneratorStrategy;
import it.unibas.spicy.model.datasource.values.IntegerOIDGenerator;
import it.unibas.spicy.model.datasource.values.OID;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.mapping.proxies.ConstantDataSourceProxy;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.Types;
import it.unibas.spicy.persistence.json.ExportJsonInstances;
import it.unibas.spicy.persistence.relational.DAORelationalUtility;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DAOSql {
    
    private static int NUMBER_OF_SAMPLE = 100;
    private static Log logger = LogFactory.getLog(DAOSql.class);
    private static IOIDGeneratorStrategy oidGenerator = new IntegerOIDGenerator();
    private static final String TUPLE_SUFFIX = "Tuple";
    private Map<String, String> changedColumnNames = new HashMap<String, String>();
    
    public void exportTranslatedSQLInstances(MappingTask mappingTask, int scenarioNo, String driver, 
            String uri, String userName, String password) throws DAOException {
        try{
            ExportSQLInstances exporter = new ExportSQLInstances();        
            exporter.exportSQLInstances(mappingTask, scenarioNo, driver, uri, userName, password);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
    
    public IDataSourceProxy loadSchema (int scenarioNo, String catalog, String filePath, boolean source) throws DAOException {
//        System.out.println("Starting loading schema!!!");
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
            String sqlScript = readFile(filePath, StandardCharsets.UTF_8).trim();            
            //table
            Statements stmts = CCJSqlParserUtil.parseStatements(sqlScript);
            List<net.sf.jsqlparser.statement.Statement> stmtss = stmts.getStatements();
            for (net.sf.jsqlparser.statement.Statement stmt: stmtss){
                if (stmt instanceof CreateTable){
                    CreateTable createStmt = (CreateTable) stmt;
                    String tableName = createStmt.getTable().getName(); 
                    List<ColumnDefinition> columns = createStmt.getColumnDefinitions();  
                    INode setTable = this.createSetNode (tableName, root, columns, statement, source, scenarioNo);
                }
            }
            
            dataSource = new ConstantDataSourceProxy(new DataSource(SpicyEngineConstants.TYPE_SQL, root));
            
            dataSource.addAnnotation(SpicyEngineConstants.SQL_DB_NAME, catalog);
            dataSource.addAnnotation(SpicyEngineConstants.SQL_FILE_PATH, filePath);
            dataSource.addAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG, false);
            
            for (Map.Entry<String, String> entry : changedColumnNames.entrySet()) {
                dataSource.putChangedValue(entry.getKey(), entry.getValue());
            }            
            loadPrimaryAndForeignKeys(stmtss, dataSource, statement, source, scenarioNo, false);                            
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
        System.out.println("Load Schema Completed");
        return dataSource;
    }
    
    
    
    public IDataSourceProxy loadSchemaForWeb (int scenarioNo, String catalog, String filePath, boolean source) throws DAOException {
        INode root = null;
        IDataSourceProxy dataSource = null;
        try {            
            root = this.createRootNode(catalog);
            String sqlScript = readFile(filePath, StandardCharsets.UTF_8).trim();            
            //table
            Statements stmts = CCJSqlParserUtil.parseStatements(sqlScript);
            List<net.sf.jsqlparser.statement.Statement> stmtss = stmts.getStatements();
            for (net.sf.jsqlparser.statement.Statement stmt: stmtss){
                if (stmt instanceof CreateTable){
                    CreateTable createStmt = (CreateTable) stmt;
                    String tableName = createStmt.getTable().getName(); 

                    List<ColumnDefinition> columns = createStmt.getColumnDefinitions();  
                    INode setTable = this.createSetNode (tableName, root, columns);
                }
            }
            
            dataSource = new ConstantDataSourceProxy(new DataSource(SpicyEngineConstants.TYPE_SQL, root));
            
            dataSource.addAnnotation(SpicyEngineConstants.SQL_DB_NAME, catalog);
            dataSource.addAnnotation(SpicyEngineConstants.SQL_FILE_PATH, filePath);
            
            for (Map.Entry<String, String> entry : changedColumnNames.entrySet()) {
                dataSource.putChangedValue(entry.getKey(), entry.getValue());
            }            
            loadPrimaryAndForeignKeys(stmtss, dataSource, null, source, scenarioNo, true);                 
        } catch (Exception ex) {
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
    
    private INode createSetNode (String tableName, INode root, List<ColumnDefinition> columns, Statement statement, boolean source, int scenarioNo) throws IOException, SQLException{
        //trim and remove quotes                
        tableName = tableName.trim().replace("\"","");
        INode setTable = new SetNode(tableName);        
        //find columns               
        setTable.addChild(getTuple(tableName, columns, statement, source, scenarioNo));       
        setTable.setRequired(false);
        setTable.setNotNull(true);    
        root.addChild(setTable);
        addNode(tableName, setTable);
        return setTable;
    }
    
    //used for web
    private INode createSetNode (String tableName, INode root, List<ColumnDefinition> columns) throws IOException, SQLException{
        //trim and remove quotes                
        tableName = tableName.trim().replace("\"","");
        INode setTable = new SetNode(tableName);        
        //find columns               
        setTable.addChild(getTuple(tableName, columns));       
        setTable.setRequired(false);
        setTable.setNotNull(true);    
        root.addChild(setTable);
        addNode(tableName, setTable);
        return setTable;
    }
    
    private TupleNode getTuple(String tableName, List<ColumnDefinition> columnList, Statement statement, boolean source, int scenarioNo) throws SQLException {
        TupleNode tupleNode = new TupleNode(tableName + TUPLE_SUFFIX);
        tupleNode.setRequired(false);
        tupleNode.setNotNull(true);
        tupleNode.setVirtual(true);
        addNode(tableName + TUPLE_SUFFIX, tupleNode);  
        String columns="";
	for (ColumnDefinition column: columnList){
            String columnName = column.getColumnName();
            //trim and remove quotes                
            columnName = columnName.trim().replace("\"","");
            //the "-" character is replaced since it cannot be accepted by JEP and MIMap
            if (columnName.contains("-")){
                String oldColumnName = columnName;
                columnName = oldColumnName.replace("-","_");
                changedColumnNames.put(tableName+"."+columnName, oldColumnName);
            }
            columns += "\""+columnName+"\"";
            String keyColumn = tableName + "." + columnName;
            String columnType = column.getColDataType().getDataType();            
            columns += " " + columnType + ","; 
            boolean isNull = true;
            List<String> isNullableList = column.getColumnSpecStrings();  
            if (isNullableList!=null && !isNullableList.isEmpty()){
                for(int i=0; i<isNullableList.size(); i++){
                    if (isNullableList.get(i).equalsIgnoreCase("not") && isNullableList.get(i+1).equalsIgnoreCase("null")){
                        isNull = false;
                        //take out the last ',' character
                        columns = columns.substring(0, columns.length()-1);
                        columns += " NOT NULL,";  
                    }
                }
            }
            /*if (!this.dataDescription.checkLoadAttribute(tableName, columnName)) {
                continue;
            }*/            
            INode columnNode = new AttributeNode(columnName);
            addNode(keyColumn, columnNode);
            columnNode.setNotNull(!isNull);
            String typeOfColumn = DAORelationalUtility.convertDBTypeToDataSourceType(columnType);
            columnNode.addChild(new LeafNode(typeOfColumn));
            tupleNode.addChild(columnNode);
            if (logger.isDebugEnabled()) logger.debug("\n\tColumn Name: " + columnName + "(" + columnType + ") " + " type of column= " + typeOfColumn + "[IS_Nullable: " + isNull + "]");
	}
        //take out the last ',' character
        columns = columns.substring(0, columns.length()-1);
        //postgres create table
        String table;
        if (source){
            table = SpicyEngineConstants.SOURCE_SCHEMA_NAME+ scenarioNo+".\""+tableName+"\"";
        }
        else{
            table = SpicyEngineConstants.TARGET_SCHEMA_NAME+ scenarioNo+".\""+tableName+"\"";    
        }
        statement.executeUpdate("drop table if exists "+ table);
        statement.executeUpdate("create table "+ table +" ("+ columns+ ")");
        return tupleNode;
    }
    
    //used for web only
    private TupleNode getTuple(String tableName, List<ColumnDefinition> columns) throws SQLException {
        TupleNode tupleNode = new TupleNode(tableName + TUPLE_SUFFIX);
        tupleNode.setRequired(false);
        tupleNode.setNotNull(true);
        tupleNode.setVirtual(true);
        addNode(tableName + TUPLE_SUFFIX, tupleNode);      
	for (ColumnDefinition column: columns){
            String columnName = column.getColumnName();
            //trim and remove quotes                
            columnName = columnName.trim().replace("\"","");
            //the "-" character is replaced since it cannot be accepted by JEP and MIMap
            if (columnName.contains("-")){
                String oldColumnName = columnName;
                columnName = oldColumnName.replace("-","_");
                changedColumnNames.put(tableName+"."+columnName, oldColumnName);
            }
            String keyColumn = tableName + "." + columnName;
            String columnType = column.getColDataType().getDataType();            
            
            boolean isNull = true;
            List<String> isNullableList = column.getColumnSpecStrings();  
            if (isNullableList!=null && !isNullableList.isEmpty()){
                for(int i=0; i<isNullableList.size(); i++){
                    if (isNullableList.get(i).equalsIgnoreCase("not") && isNullableList.get(i+1).equalsIgnoreCase("null"))
                        isNull = false;
                }
            }
            /*if (!this.dataDescription.checkLoadAttribute(tableName, columnName)) {
                continue;
            }*/            
            INode columnNode = new AttributeNode(columnName);
            addNode(keyColumn, columnNode);
            columnNode.setNotNull(!isNull);
            String typeOfColumn = DAORelationalUtility.convertDBTypeToDataSourceType(columnType);
            columnNode.addChild(new LeafNode(typeOfColumn));
            tupleNode.addChild(columnNode);
            if (logger.isDebugEnabled()) logger.debug("\n\tColumn Name: " + columnName + "(" + columnType + ") " + " type of column= " + typeOfColumn + "[IS_Nullable: " + isNull + "]");

	}
        return tupleNode;
    }
    
    private void loadPrimaryAndForeignKeys(List<net.sf.jsqlparser.statement.Statement> stmtss, IDataSourceProxy dataSource, 
            Statement statement, boolean source, int scenarioNo, boolean web) throws SQLException {
        for (net.sf.jsqlparser.statement.Statement stmt: stmtss){
            if (stmt instanceof CreateTable){
                CreateTable createStmt = (CreateTable) stmt;
                List<String> PKcolumnNames = new ArrayList<String>();
                String tableName = createStmt.getTable().getName();
                //trim and remove quotes
                tableName = tableName.trim().replace("\"","");
                //there are two ways that Primary Keys and Foreign can be declared
                //1)at the end of the column definitions as indexes
                List<Index> indexes = createStmt.getIndexes();  
                if (indexes!=null && !indexes.isEmpty()){
                    for (Index index : indexes){
                        String indexType = index.getType();
                        //PRIMARY KEY
                        if (indexType.equalsIgnoreCase("PRIMARY KEY")){                       
                            for (String columnName : index.getColumnsNames()){
                                //trim and remove quotes                
                                columnName = columnName.trim().replace("\"","");                                
                                String keyPrimary = tableName + "." + columnName;
                                getNode(keyPrimary).setNotNull(true);
                                KeyConstraint keyConstraint = new KeyConstraint(DAORelationalUtility.generatePath(keyPrimary), true);
                                dataSource.addKeyConstraint(keyConstraint);
                                PKcolumnNames.add("\""+columnName+"\"");
                            }
                        }
                        //FOREIGN KEY
                        //Auto-detection of foreign keys disabled
                        else if (indexType.equalsIgnoreCase("FOREIGN KEY")){
                            ForeignKeyIndex fkIndex = (ForeignKeyIndex) index;
                            List<String> fkColumns = fkIndex.getColumnsNames();
                            List<String> refColumns = fkIndex.getReferencedColumnNames();
                            for(int i = 0; i < fkColumns.size(); i++){
                                String columnName = fkColumns.get(i).trim().replace("\"",""); 
                                String keyForeign = tableName + "." + columnName;
                                String referencedColumn = refColumns.get(i).trim().replace("\"",""); 
                                String referencedTable = fkIndex.getTable().getName().trim().replace("\"","");
                                String keyReferenced = referencedTable + "." + referencedColumn;
                                DAORelationalUtility.generateConstraint(keyForeign, keyReferenced, dataSource);
                            }                           
                        } 
                    }
                }
                //2)in the column definitions themselves
                List<ColumnDefinition> columns = createStmt.getColumnDefinitions();
                for(ColumnDefinition column : columns){
                    List<String> columnSpecs = column.getColumnSpecStrings();
                    if (columnSpecs!=null && !columnSpecs.isEmpty()){
                        for(int i=0; i<columnSpecs.size(); i++){
                            //PRIMARY KEY
                            if (columnSpecs.get(i).equalsIgnoreCase("PRIMARY") && columnSpecs.get(i+1).equalsIgnoreCase("KEY")){
                                String columnName = column.getColumnName();
                                //trim and remove quotes                
                                columnName = columnName.trim().replace("\"","");
                                String keyPrimary = tableName + "." + columnName;
                                getNode(keyPrimary).setNotNull(true);
                                KeyConstraint keyConstraint = new KeyConstraint(DAORelationalUtility.generatePath(keyPrimary), true);
                                dataSource.addKeyConstraint(keyConstraint);
                                PKcolumnNames.add("\""+columnName+"\"");
                            }
                            //FOREIGN KEY
                            //Auto-detection of foreign keys disabled
                            if (columnSpecs.get(i).equalsIgnoreCase("REFERENCES")){
                                //trim and remove quotes 
                                String columnName = column.getColumnName().trim().replace("\"","");                                               
                                String referencedTable = columnSpecs.get(i+1).trim().replace("\"","");
                                //remove quotes and parenthesis from referenced column
                                String referencedColumn = columnSpecs.get(i+2).trim().replace("\"","").replace("(", "").replace(")", "");
                                String keyForeign = tableName + "." + columnName;
                                String keyReferenced = referencedTable + "." + referencedColumn;                       
                                DAORelationalUtility.generateConstraint(keyForeign, keyReferenced, dataSource);
                            }
                        }      
                    }
                }
                //un-comment the following if Primary Key Constraints are to be considered
                /////
                if (!web && !PKcolumnNames.isEmpty()){
                    String table;
                    if (source){
                        table = SpicyEngineConstants.SOURCE_SCHEMA_NAME+ scenarioNo+".\""+tableName+"\"";
                    }
                    else{
                        String newSchemaName = SpicyEngineConstants.TARGET_SCHEMA_NAME+ scenarioNo;
                        table = newSchemaName+".\""+tableName+"\"";
                        statement.execute(GenerateSQL.createTriggerFunction(table, newSchemaName, tableName, PKcolumnNames));
                        statement.execute(GenerateSQL.createTriggerBeforeInsert(table, newSchemaName, tableName));
                    }
                    String primaryKeys = String.join(",", PKcolumnNames);
                    statement.executeUpdate("ALTER TABLE "+table+" ADD PRIMARY KEY ("+primaryKeys+");");
                }
                /////
            }
            /********************ALTER Statement**********************/
            else if (stmt instanceof Alter){
                Alter alterStmt = (Alter) stmt;
                String tableName = alterStmt.getTable().getName();
                //trim and remove quotes
                tableName = tableName.trim().replace("\"","");
            }
        }
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
    
    static String readFile(String path, Charset encoding) throws IOException 
    {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }
    
    /////////////////////////////////////////////////////////////
    //////////////////////// INSTANCE
    /////////////////////////////////////////////////////////////    
    public void loadInstance(int scenarioNo, IDataSourceProxy dataSource, boolean source) throws DAOException, SQLException {
        IConnectionFactory connectionFactory = null;
        Connection connection = null;
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);             
        try{
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            Statement statement = connection.createStatement();
            
            String filePath = (String) dataSource.getAnnotation(SpicyEngineConstants.SQL_FILE_PATH);
            String sqlScript = readFile(filePath, StandardCharsets.UTF_8).trim();                    
            Statements stmts = CCJSqlParserUtil.parseStatements(sqlScript);
            List<net.sf.jsqlparser.statement.Statement> stmtss = stmts.getStatements();
            
            for (net.sf.jsqlparser.statement.Statement stmt: stmtss){
                if (stmt instanceof Insert){
                    Insert insertStmt = (Insert) stmt;
                    //only "INSERT into ... VALUES" support
                    //not INSERT with "SELECT" or "RETURNING" yet
                    if (insertStmt.isUseValues()){
                        String tableName = insertStmt.getTable().getName(); 
                        if (source){
                            tableName =  SpicyEngineConstants.SOURCE_SCHEMA_NAME + scenarioNo + ".\"" + tableName + "\"";
                        }
                        else{
                            tableName =  SpicyEngineConstants.TARGET_SCHEMA_NAME + scenarioNo + ".\"" + tableName + "\"";
                        }
                        String values = insertStmt.getItemsList().toString();
                        //postgres string values must be enclosed in single quotes instead of double quotes
                        //so I replace them if they are found in places like: VALUES ("a","b")
                        values = values.replace("(\"","('").replace(", \"", ", '").replace("\",","',").replace("\")", "')");
                        if ( (values!=null) && (!values.equals("")) )
                            statement.executeUpdate("insert into " + tableName + " values " + values + ";");  
                    }
                }
            }
        dataSource.addAnnotation(SpicyEngineConstants.LOADED_INSTANCES_FLAG, true);  
        } catch (DAOException | SQLException | IOException | JSQLParserException ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        } 
        finally{
            if(connection != null)
            connection.close();
        }
    }
    
    public void loadInstanceSample(IDataSourceProxy dataSource, String catalog, String filePath) throws DAOException {
        INode root = null;
        try {
            root = new TupleNode(getNode(catalog).getLabel(), getOID());            
            root.setRoot(true);
            String sqlScript = readFile(filePath, StandardCharsets.UTF_8).trim();            
            //table
            Statements stmts = CCJSqlParserUtil.parseStatements(sqlScript);
            List<net.sf.jsqlparser.statement.Statement> stmtss = stmts.getStatements();
            HashSet<String> tableNames = new HashSet<>();
            for (net.sf.jsqlparser.statement.Statement stmt: stmtss){
                if (stmt instanceof Insert){
                    Insert insertStmt = (Insert) stmt;
                    //only "INSERT into ... VALUES" support
                    //not INSERT with "SELECT" or "RETURNING" yet
                    if (insertStmt.isUseValues()){
                        String tableName = insertStmt.getTable().getName();   
                        SetNode setTable = null;
                        if (!tableNames.contains(tableName)){
                            tableNames.add(tableName);
                            setTable = new SetNode(getNode(tableName).getLabel(), getOID());
                            getInstanceByTable(tableName, setTable, insertStmt, setTable.getFullSize());
                            root.addChild(setTable);
                        }
                        else{
                            setTable = (SetNode)root.getChild(tableName);
                            getInstanceByTable(tableName, setTable, insertStmt, setTable.getFullSize());
                        }                                             
                    }
                }
            }
            dataSource.addInstanceWithCheck(root);  
         } catch (IOException | JSQLParserException | DAOException ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }   
    }
    
    private void getInstanceByTable(String tableName, SetNode setTable, Insert insertStmt, int size) throws DAOException {      
        ItemsList items = insertStmt.getItemsList();   
        List<ExpressionList> values = ((MultiExpressionList)items).getExprList();
        int counter = 0;
        int sampleCounter = size;
        while (counter < values.size() && sampleCounter < NUMBER_OF_SAMPLE){         
            ExpressionList valueSet = values.get(counter);            
            TupleNode tupleNode = new TupleNode(getNode(tableName + TUPLE_SUFFIX).getLabel(), getOID());
            setTable.addChild(tupleNode);
            List<Expression> valueSetExpressions = valueSet.getExpressions();       
            int i=0;
            for (INode attributeNodeSchema : getNode(tableName + TUPLE_SUFFIX).getChildren()){
                AttributeNode attributeNode = new AttributeNode(attributeNodeSchema.getLabel(), getOID());
                String columnValue = valueSetExpressions.get(i).toString();
                if ( (columnValue.startsWith("\"") && columnValue.endsWith("\"")) || 
                     (columnValue.startsWith("'") && columnValue.endsWith("'")) )
                    columnValue = columnValue.substring(1, columnValue.length()-1);
                i++;
                LeafNode leafNode = createLeafNode(attributeNodeSchema, columnValue);
                attributeNode.addChild(leafNode);
                tupleNode.addChild(attributeNode);
            }
            counter++;
            sampleCounter++;
        }        
        setTable.setFullSize(sampleCounter+values.size()-counter);
    }
    
    private LeafNode createLeafNode(INode attributeNode, Object untypedValue) throws DAOException {
        LeafNode leafNodeInSchema = (LeafNode) attributeNode.getChild(0);
        String type = leafNodeInSchema.getLabel();
        Object typedValue = Types.getTypedValue(type, untypedValue);
        return new LeafNode(type, typedValue);
    }
    
    
    
}
