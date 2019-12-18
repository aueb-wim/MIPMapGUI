package it.unibas.spicy.persistence;

import com.ibatis.common.jdbc.ScriptRunner;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DAOHandleDB {
    
    public void createNewDatabase() throws DAOException{
        IConnectionFactory connectionFactory = null;
        Connection connection = null;         
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        try  {
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            Statement statement = connection.createStatement();            
            
            
            int dbcount =0;
            ResultSet count = statement.executeQuery("select count(*) as dbcount from pg_catalog.pg_database where datname = '"+
                    SpicyEngineConstants.MAPPING_TASK_DB_NAME+"';");
            while(count.next()){
                dbcount = count.getInt("dbcount");
            } 
            count.close();
            if (dbcount>0){
                AccessConfiguration accessConfiguration2 = new AccessConfiguration();
                accessConfiguration2.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
                accessConfiguration2.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
                accessConfiguration2.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
                accessConfiguration2.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
                Connection connection2 = connectionFactory.getConnection(accessConfiguration2);
                Statement statement2 = connection2.createStatement();
                StringBuilder dropSchemataQuery = new StringBuilder(); 
                ResultSet res = statement2.executeQuery("select nspname as schemaName from pg_catalog.pg_namespace where nspname like '"+
                        SpicyEngineConstants.WORK_SCHEMA_NAME+"%' or nspname like '"+
                        SpicyEngineConstants.SOURCE_SCHEMA_NAME+"%' or nspname like '"+
                        SpicyEngineConstants.TARGET_SCHEMA_NAME+"%';");
                while (res.next()){
                    dropSchemataQuery.append("drop schema if exists ").append(res.getString("schemaName")).append(" cascade;\n");
                }                
                statement2.executeUpdate(dropSchemataQuery.toString());
//                System.out.println("DROP SCHEMATA QUERY IN TEMP DB\n------------------------------\n" + dropSchemataQuery.toString());
            }
            else{
                StringBuilder createDatabaseQuery = new StringBuilder(); 
                createDatabaseQuery.append("create database ").append(SpicyEngineConstants.MAPPING_TASK_DB_NAME).append(";\n");
                statement.executeUpdate(createDatabaseQuery.toString());
//                System.out.println("CREATE DB QUERY IN TEMP DB\n------------------------------\n" + createDatabaseQuery.toString());
            }            
        }
        catch (Exception ex) {
            throw new DAOException(ex);
        }
        finally
        {   
            connectionFactory.close(connection);
        }
    }
    
    public void createSchema(int scenarioNo) throws DAOException{
        handleSchema(true, scenarioNo);
    }
    
    public void dropSchema(int scenarioNo) throws DAOException{
        handleSchema(false, scenarioNo);
    }
    
    public void handleSchema(boolean create, int scenarioNo) throws DAOException{
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
            StringBuilder createSchemasQuery = new StringBuilder(); 
            createSchemasQuery.append("drop schema if exists ").append(SpicyEngineConstants.SOURCE_SCHEMA_NAME).append(scenarioNo).append(" cascade;\n");
            createSchemasQuery.append("drop schema if exists ").append(SpicyEngineConstants.TARGET_SCHEMA_NAME).append(scenarioNo).append(" cascade;\n");
            if (create){
                createSchemasQuery.append("create schema ").append(SpicyEngineConstants.SOURCE_SCHEMA_NAME).append(scenarioNo).append(";\n");
                createSchemasQuery.append("create schema ").append(SpicyEngineConstants.TARGET_SCHEMA_NAME).append(scenarioNo).append(";\n");
            }
            statement.executeUpdate(createSchemasQuery.toString());
        }
        catch (Exception ex) {
            throw new DAOException(ex);
        }
        finally
        {   
            connectionFactory.close(connection);
        }
    }
    
}
