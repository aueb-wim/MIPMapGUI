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
        function += "create or replace function functionevaluation(text) returns int as '\n";
        function += "declare res record;\n";
        function += "begin\n";
        function += "for res in execute ''select '' || $1 || '' as result'' loop\n";
        function += "return res.result;\n";
        function += "end loop;\n";
        function += "end' language plpgsql;\n";
        return function;
        
    }
}
