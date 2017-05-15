/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.model.algebra.query.operators.sql;

/**
 *
 * @author ioannisxar
 */
public class GeneratePLSQLfunctions {
    
    public String generateNumericFunctionEvaluation(){
        String function = "";
        function += "create or replace function functionevaluation(text) returns text as '\n";
        function += "declare result text;\n";
        function += "begin\n";
        function += "execute ''select '' || $1 || '' as result'' into result;\n";
        function += "return result;\n";
        function += "end' language plpgsql;\n";
        return function;
    }
}
