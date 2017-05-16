/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.model.algebra.query.operators.sql;

import it.unibas.spicy.utility.SpicyEngineConstants;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ioannisxar
 */
public class GenerateAggregation {
    
    private String[] params;
    private Map<String, String> oldToNewRelation;
    private int scenarioNo;
    
    public GenerateAggregation(String []params, Map<String, String> oldToNewRelation, int scenarioNo){
        this.params = params;
        this.oldToNewRelation = oldToNewRelation;
        this.scenarioNo = scenarioNo;
    }
    
    public String getQuery(){
        System.out.println("---- aggregation ----");
        String query = "(SELECT sum(";
        String function = params[0];
        String value = params[1];
        String[] groupby;
        groupby = params[2].replace("'", "").split(",");
        String[] where;
        where = params[3].replace("'", "").split(",");

        
        //it hasnt been used yet
        System.out.println("Function: "+ function);
        
        
        query += "#DOUBLE_QUOTES#" + extractAttributeFromNewRelation(value)+"#DOUBLE_QUOTES#) FROM \"";
        
        String []relation = groupby[0].split("\\.");
        String relationName = "";
        for(int i=1;i<relation.length-2;i++){
            relationName += relation[i]+".";
        }
        query += SpicyEngineConstants.SOURCE_SCHEMA_NAME + String.valueOf(scenarioNo) + "\"." + relationName.substring(0,relationName.length()-1) + " WHERE ";
        
        String groupByValues = "";
        for (String gb : groupby) {
            String []oldRelationParts = gb.split("\\.");
            String attribute = oldRelationParts[oldRelationParts.length-1];
            query += transformToNewRelation(gb, oldToNewRelation) + " = #DOUBLE_QUOTES#" + attribute + "#DOUBLE_QUOTES# AND ";
            groupByValues += "#DOUBLE_QUOTES#" + attribute + "#DOUBLE_QUOTES#,";
        }
        query = query.substring(0, query.length()-5);
        
        
        
        for (String w : where) {
            System.out.println(w);
            if(!w.equals("")){
                System.out.println("mpika");
                query += " AND (";
                for(String clause: getOrClauses(w)){
                    System.out.println(clause);
                    query += clause + " OR ";
                }
                query = query.substring(0, query.length()-4);
                query += ")";
            }
        }

        query += " GROUP BY "+ groupByValues.substring(0, groupByValues.length()-1) +")";
        System.out.println("FINAL QUERY ---> " + query);
        System.out.println("POSTGRESQL QUERY ---> " + query.replace("#DOUBLE_QUOTES#", ""));
        return query;
    }
    
    private String transformToNewRelation(String oldRelation, Map<String, String> oldToNewRelation){
        String []oldRelationParts = oldRelation.split("\\.");
        String attribute = oldRelationParts[oldRelationParts.length-1];
        String relation = "";
        for (String part: oldRelationParts){
            if(oldToNewRelation.containsKey(part)){
                relation = this.oldToNewRelation.get(part);
                break;
            }
        }
        return "rel_"+relation+".#DOUBLE_QUOTES#"+ attribute +"#DOUBLE_QUOTES#";
    }
    
    private ArrayList<String> getOrClauses(String w){
        ArrayList<String> orClauses = new ArrayList<>();
        String[] whereClause = w.split("\\|");
        String []relationNameParts = whereClause[0].split("\\.");
        String tableName = relationNameParts[relationNameParts.length-1];
        for(int i=1; i<whereClause.length;i++){
            orClauses.add(tableName + whereClause[i]);
        }        
        return orClauses;
    }
    
    private String extractAttributeFromNewRelation(String newAttribute){
        return newAttribute.replace("#DOUBLE_QUOTES#", "").split("\\.")[1];
    }
}