/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.generator;

import java.util.List;

/**
 *
 * @author ioannisxar
 */
public class ColumnMatcherModel {
    
    private final String sourceColumn, targetColumn, function;
    private final List<String> functionProperties;
    public ColumnMatcherModel(String sourceColumn, String targetColumn, String function, List<String> functionProperties){
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
        this.function = function;
        this.functionProperties = functionProperties;
    }
    
    public String getSourceColumn(){
        return this.sourceColumn;
    }
    
    public String getTargetColumn(){
        return this.targetColumn;
    }
    
    public String getFunction(){
        return this.function;
    }
    
    public List<String> getFunctionProperties(){
        return this.functionProperties;
    }
    
}
