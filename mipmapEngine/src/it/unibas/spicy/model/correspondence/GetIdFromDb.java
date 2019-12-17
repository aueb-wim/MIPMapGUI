/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.model.correspondence;

/**
 *
 * @author ioannisxar
 */
public class GetIdFromDb {
    
    private String driver, uri, schema, login, password, table, column, function;

    public GetIdFromDb(String driver, String uri, String schema, String login, String password, String table, String column, String function) {
        this.driver = driver;
        this.uri = uri;
        this.schema = schema;
        this.login = login;
        this.password = password;
        this.table = table;
        this.column = column;
        this.function = function;
    }

    public String getDriver() {
        return driver;
    }

    public String getUri() {
        return uri;
    }

    public String getSchema() {
        return schema;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public String getFunction() {
        return function;
    }
    
    
    
}
