/*
Copyright (C) 2007-2011  Database Group - Universita' della Basilicata
Giansalvatore Mecca - giansalvatore.mecca@unibas.it
Salvatore Raunich - salrau@gmail.com

This file is part of ++Spicy - a Schema Mapping and Data Exchange Tool

++Spicy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

++Spicy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ++Spicy.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicy.model.algebra.query.operators.sql;

import com.ibatis.common.jdbc.ScriptRunner;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.mapping.proxies.ChainingDataSourceProxy;
import it.unibas.spicy.model.paths.SetAlias;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.relational.DAORelational;
import it.unibas.spicy.persistence.relational.DBFragmentDescription;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExecuteSQL {

    private static Log logger = LogFactory.getLog(ExecuteSQL.class);
    private HashSet<String> pkConstraintsTableNames = new HashSet<String>();
    
    public void executeScript(MappingTask mappingTask, AccessConfiguration accessConfiguration, AccessConfiguration tempAccessConfiguration, String sqlScript, 
            Reader sourceSQLScriptReader, Reader sourceInstanceSQLScriptReader, Reader targetSQLScriptReader, Reader intermediateSQLScriptReader, int scenarioNo
        ) throws DAOException {
        IConnectionFactory connectionFactory = null;
        Connection connection = null;
        try {
            String databaseName = extractDatabaseName(accessConfiguration);
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(tempAccessConfiguration);
            ScriptRunner scriptRunner = new ScriptRunner(connection, true, false);
            StringBuilder dropQuery = new StringBuilder();
            dropQuery.append("drop database ").append(databaseName).append(";");
            scriptRunner.runScript(new StringReader(dropQuery.toString()));
            StringBuilder createQuery = new StringBuilder();
            createQuery.append("create database ").append(databaseName).append(";");
            scriptRunner.runScript(new StringReader(createQuery.toString()));
        } catch (Exception ex) {
            logger.error(ex);
            throw new DAOException(ex);
        } finally {
            connectionFactory.close(connection);
        }
        executeScript(mappingTask, accessConfiguration, sqlScript, sourceSQLScriptReader, sourceInstanceSQLScriptReader, targetSQLScriptReader, intermediateSQLScriptReader, scenarioNo);
    }

    public void executeScript(MappingTask mappingTask, AccessConfiguration accessConfiguration, String sqlScript, Reader sourceSQLScriptReader,
            Reader sourceInstanceSQLScriptReader, Reader targetSQLScriptReader, Reader intermediateSQLScriptReader, int scenarioNo
        ) throws DAOException {
        boolean isChainingScenario = (mappingTask.getSourceProxy() instanceof ChainingDataSourceProxy);
        IConnectionFactory connectionFactory = null;
        Connection connection = null;
        try {
            connectionFactory = new SimpleDbConnectionFactory();
            connection = connectionFactory.getConnection(accessConfiguration);
            ScriptRunner scriptRunner = new ScriptRunner(connection, true, true);
            scriptRunner.setLogWriter(null);

            //giannisk
            if (sourceSQLScriptReader!=null && sourceInstanceSQLScriptReader!=null && targetSQLScriptReader!=null){
                StringBuilder createSchemasQuery = new StringBuilder();
                createSchemasQuery.append("create schema " + SpicyEngineConstants.SOURCE_SCHEMA_NAME + scenarioNo + ";\n");
                createSchemasQuery.append("create schema " + SpicyEngineConstants.TARGET_SCHEMA_NAME + scenarioNo + ";\n");
                //createSchemasQuery.append("create schema " + GenerateSQL.WORK_SCHEMA_NAME + ";\n");

                scriptRunner.runScript(new StringReader(createSchemasQuery.toString()));

                Reader sourceSchemaScript = getSourceSchemaReader();
                Reader targetSchemaScript = getTargetSchemaReader();
                scriptRunner.runScript(sourceSchemaScript);
                scriptRunner.runScript(sourceSQLScriptReader);
                scriptRunner.runScript(sourceInstanceSQLScriptReader);
                scriptRunner.runScript(targetSchemaScript);
                scriptRunner.runScript(targetSQLScriptReader);
            }
            if (isChainingScenario) {
                scriptRunner.runScript(new StringReader("create schema " + GenerateSQL.INTERMEDIATE_SCHEMA_NAME + ";\n"));
                Reader intermediateSchemaScript = getIntermediateSchemaReader();
                scriptRunner.runScript(intermediateSchemaScript);
                scriptRunner.runScript(intermediateSQLScriptReader);
            } 
            
            Statement statement = connection.createStatement();
            
            System.out.println("Starting Data Translation" + new java.util.Date());
            statement.execute(sqlScript);
            System.out.println("Data Translation Ended with " + statement.getUpdateCount() + "\t insertions\t" + new java.util.Date());
            SQLWarning warning = statement.getWarnings();
            String notice = SpicyEngineConstants.PRIMARY_KEY_CONSTR_NOTICE;
            while (warning != null){ 
                if (warning.getMessage().startsWith(notice)){
                    String tempTableName = warning.getMessage().substring(warning.getMessage().lastIndexOf(notice)+notice.length()).trim();
                    pkConstraintsTableNames.add(tempTableName);
                }
                warning = warning.getNextWarning();
            }
            
            ////Reader sqlReader = new StringReader(sqlScript);
            /////scriptRunner.runScript(sqlReader);
        } catch (Exception ex) {
            logger.error(ex);
            throw new DAOException(ex);
        } finally {
            connectionFactory.close(connection);
            try {
                if (sourceSQLScriptReader!=null && sourceInstanceSQLScriptReader!=null && targetSQLScriptReader!=null){
                    sourceSQLScriptReader.close();
                    sourceInstanceSQLScriptReader.close();
                    targetSQLScriptReader.close();
                }
            } catch (IOException ex) {
                logger.error("Unable to close readers..." + ex);
            }
        }
        //return loadInstance(mappingTask, accessConfiguration, scenarioNo);
    }
    
    //giannisk
    public void executeScript(MappingTask mappingTask, String sqlScript, Reader sourceSQLScriptReader,
            Reader sourceInstanceSQLScriptReader, Reader targetSQLScriptReader, Reader intermediateSQLScriptReader, int scenarioNo
        ) throws DAOException {
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.ACCESS_CONFIGURATION_DRIVER);
        accessConfiguration.setUri(SpicyEngineConstants.ACCESS_CONFIGURATION_URI+SpicyEngineConstants.MAPPING_TASK_DB_NAME);
        accessConfiguration.setLogin(SpicyEngineConstants.ACCESS_CONFIGURATION_LOGIN);
        accessConfiguration.setPassword(SpicyEngineConstants.ACCESS_CONFIGURATION_PASS);
        
        executeScript(mappingTask, accessConfiguration, sqlScript, sourceSQLScriptReader, sourceInstanceSQLScriptReader, targetSQLScriptReader, intermediateSQLScriptReader, scenarioNo);
    }

    public static String extractDatabaseName(AccessConfiguration accessConfiguration) {
        String uri = accessConfiguration.getUri();
        if (uri.lastIndexOf("/") != -1) {
            return uri.substring(uri.lastIndexOf("/") + 1);
        }
        return uri.substring(uri.lastIndexOf(":") + 1);
    }

    private Reader getSourceSchemaReader() {
        return getSchemaReader(SpicyEngineConstants.SOURCE_SCHEMA_NAME);
    }

    private Reader getTargetSchemaReader() {
        return getSchemaReader(SpicyEngineConstants.TARGET_SCHEMA_NAME);
    }

    private Reader getIntermediateSchemaReader() {
        return getSchemaReader(GenerateSQL.INTERMEDIATE_SCHEMA_NAME);
    }

    private Reader getSchemaReader(String schemaName) {
        String sqlScript = "SET search_path TO " + schemaName + ";";
        Reader sqlReader = new StringReader(sqlScript);
        return sqlReader;
    }
    
    //returns the table names that have been created in the "work" schema
    //for the tables that violated any primary key constraints
    public HashSet<String> getPKConstraintsTables(){
        return this.pkConstraintsTableNames;
    }
/*
    private INode loadInstance(MappingTask mappingTask, AccessConfiguration accessConfiguration, int scenarioNo) {
        IConnectionFactory connectionFactory = null;
        try {
            connectionFactory = new SimpleDbConnectionFactory();
            DBFragmentDescription dataDescription = generateDBFragmentDescription(mappingTask);
            DAORelational daoRelational = new DAORelational();
            //needed to initialize the node map for DAORelational
            daoRelational.loadSchema(scenarioNo, accessConfiguration, dataDescription, connectionFactory, false);
            INode instance = daoRelational.loadInstanceSample(accessConfiguration, dataDescription, connectionFactory);
            return instance;
        } catch (Exception ex) {
            logger.error("Error loading instance: " + ex);
            return null;
        } finally {
        }
    }*/

    private DBFragmentDescription generateDBFragmentDescription(MappingTask mappingTask) {
        DBFragmentDescription dataDescription = new DBFragmentDescription();
        List<SetAlias> variables = mappingTask.getTargetProxy().getMappingData().getVariables();
        for (SetAlias variable : variables) {
            String setName = variable.getBindingPathExpression().getLastStep();
            String inclusionPath = setName + ".*";
            dataDescription.addInclusionPath(inclusionPath.toLowerCase());
        }
        return dataDescription;
    }
}
