/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.utils;

import it.unibas.spicy.persistence.idgenerator.generator.InputDataModel;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ioannisxar
 */
public class ExportDB {
    
    private String path, table;
    private ArrayList<InputDataModel> targetValues;
    
    public ExportDB(String path, String table, ArrayList<InputDataModel> targetValues){
        this.path = path;
        this.table = table;
        this.targetValues = targetValues;
    }
    
    public void performAction() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        try{
            ArrayList<String> configurationProperties = getExportDatabaseConfig();
            connection = getConnectionToDatabase(configurationProperties.get(0), configurationProperties.get(1)+configurationProperties.get(4), 
                    configurationProperties.get(2), configurationProperties.get(3));
            Statement statement = connection.createStatement();
            for(InputDataModel output: targetValues){
                String row = "";
                for(String s: output.getValue()){
                    row += s + ",";
                }
                row = row.substring(0, row.length()-1);
                System.out.println("insert into " + table + " values(" + row + ");");
//                statement.executeUpdate("insert into " + table + " values(" + row + ");");
            }            
        } catch (IOException ex){
            System.err.println(ex.getMessage());
            System.exit(-1);
        } finally {
            if (connection != null)
                connection.close();
        }
    }
    
    private Connection getConnectionToDatabase(String driver, String uri, String login, String pass) throws ClassNotFoundException, SQLException{
        Connection connection = null;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(uri, login, pass);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return connection;
    }
    
    private ArrayList<String> getExportDatabaseConfig() throws FileNotFoundException, IOException{
        ArrayList<String> config = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(FilenameUtils.separatorsToSystem(this.path)));
        try{
            String line;
            while((line = in.readLine()) != null) {
                config.add(line.split("=")[1].trim());
            }
        } catch(IOException ex){
            System.err.print(ex);
            System.exit(-1);
        } finally {
            in.close();
        }
        return config;
    }
}
