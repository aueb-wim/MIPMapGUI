package it.unibas.spicy.persistence.json;

//giannisk

import au.com.bytecode.opencsv.CSVWriter;
import it.unibas.spicy.model.algebra.query.operators.sql.GenerateSQL;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.ExportCSVInstances;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExportJsonInstances {
    private static Log logger = LogFactory.getLog(ExportCSVInstances.class);
    
    private Connection getConnectionToPostgres(IConnectionFactory connectionFactory, int scenarioNo) throws DAOException{
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME+scenarioNo);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        
        return connectionFactory.getConnection(accessConfiguration);
    }
    
    public void exportJsonInstances(MappingTask mappingTask, String directoryPath, String suffix, int scenarioNo) throws DAOException, SQLException, IOException{          
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
                createJsonDocument(tableName, GenerateSQL.TARGET_SCHEMA_NAME+".", mappingTask.getTargetProxy().getIntermediateSchema().getChild(tableName), folderPath, statement, null);           
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
    
    public void createJsonDocument(String tableName, String schema, INode tableNode, String folderPath, Statement statement, String[] columnNames) throws SQLException, IOException{
        BufferedWriter bw = null;
        ResultSet instancesSet = null;        
        try{
            File file = new File(folderPath+File.separator+tableName+".json");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            if (!file.exists()) {
                file.createNewFile();
            }            
            instancesSet =  statement.executeQuery("SELECT row_to_json(\""+tableName+"\") FROM "+schema+"\""+tableName+"\";");
            //check to see if the result set is empty
            if (instancesSet.isBeforeFirst() ){
                bw.write("[");
                bw.newLine();
                while (instancesSet.next()){                 
                    bw.write(instancesSet.getString(1));
                    //if it is not the last result
                    if (!instancesSet.isLast()){
                        bw.write(",");
                    }
                    bw.newLine();                    
                }
                bw.write("]");
            }            
            //alternative for one-row json 
            /*instancesSet = statement.executeQuery("SELECT array_to_json(array_agg("+"\""+tableName+"\")) FROM "+schema+"\""+tableName+"\";");
                while (instancesSet.next() && instancesSet.getString(1)!=null){ 
                    bw.write(instancesSet.getString(1));
                }
            }*/            
        }finally{
            bw.close();
            instancesSet.close();
        }
    }
    
    public void exportPKConstraintJsonInstances(MappingTask mappingTask, String directoryPath, HashSet<String> tableNames, String suffix, int scenarioNo) throws DAOException, SQLException, IOException{
        String folderPath = generateFolderPath(mappingTask.getTargetProxy().getIntermediateSchema(), directoryPath, suffix, 0);     
        //create Folder
        new File(folderPath).mkdir();    
        //connection to Postgres
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = getConnectionToPostgres(connectionFactory, scenarioNo);
        
        try{
            Statement statement = connection.createStatement();
            for (String tableName : tableNames) {
                createJsonDocument(tableName, GenerateSQL.WORK_SCHEMA_NAME+".", mappingTask.getTargetProxy().getIntermediateSchema().getChild(tableName), folderPath, statement, null);           
            }  
        }finally{        
            //close connection
            if(connection != null)
              connectionFactory.close(connection); 
        }
    }
}
