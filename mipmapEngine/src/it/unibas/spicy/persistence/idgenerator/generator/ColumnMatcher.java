/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.generator;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author ioannisxar
 */
public class ColumnMatcher {
    
    private final String targetColumn, function;
    
    public ColumnMatcher(String targetColumn, String function){
        this.targetColumn = targetColumn;
        this.function = function;
    }
    
    public ArrayList<ColumnMatcherModel> getMatching(){
        ArrayList<ColumnMatcherModel> cmmList = new ArrayList<>();
        if(targetColumn.charAt(0) == '[' && targetColumn.charAt(targetColumn.length()-1) == ']'
                && function.charAt(0) == '[' && function.charAt(function.length()-1) == ']'){
            
            //this type of split ignores commas inside the paranthesis
            String[] targetCols = targetColumn.substring(1, targetColumn.length()-1).split(",(?![^(]*\\))");
            String[] functionCols = function.substring(1, function.length()-1).split(",(?![^(]*\\))");
            checkListParamCombatibility(targetCols, functionCols);
            for(int i=0; i< functionCols.length; i++){
                ColumnMatcherModel cmm;
                if(functionCols[i].contains("(") && functionCols[i].contains(")")){
                    String functionName = functionCols[i].split("\\(")[0];
                    String[] functionProperties = functionCols[i].split("\\(")[1].split("\\)")[0].split(",");
                    cmm = new ColumnMatcherModel(functionProperties[0], targetCols[i].trim(), functionName.trim(), Arrays.asList(functionProperties));
                } else if(functionCols[i].charAt(0) == '\"' && functionCols[i].charAt(functionCols[i].length()-1) == '\"'){
                    ArrayList<String> constant = new ArrayList<>();
                    constant.add(functionCols[i].trim());
                    cmm = new ColumnMatcherModel("CONSTANT_VALUE_SOURCE", targetCols[i].trim(), "CONSTANT_VALUE", constant);
                } else {
                    cmm = new ColumnMatcherModel(functionCols[i].trim(), targetCols[i].trim(), functionCols[i].trim(), new ArrayList());
                }
                cmmList.add(cmm);
            }
        } else {
            System.err.println("Wrong list form!");
            System.exit(-1);
        }
        return cmmList;
    }
    
    private void checkListParamCombatibility(String[] targetCols, String[] functionCols){
        if (functionCols.length != targetCols.length-1){
            System.err.println("Missmatching input and output list size");
            System.exit(-1);
        }    
    }
}
