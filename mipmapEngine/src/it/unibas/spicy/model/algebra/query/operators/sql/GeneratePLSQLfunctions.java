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
    
    //Not used - old version of function generateFuncGenerator()
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
    
    public String generateFuncGenerator(){
        String function = "";
        function += "create or replace function functionevaluation(text, text, text, text, text, text, text) returns setof text as ' \n";
        function += "DECLARE \n";
        function += "ii INT; \n";
        function += "r TEXT; result TEXT; function_text TEXT; i TEXT; value TEXT; groupby_text TEXT; clauses_text TEXT; query TEXT; m TEXT; \n";
        function += "function_parameters TEXT[]; clauses TEXT[]; groupby TEXT[]; clause_parameters TEXT[]; attribute_path TEXT[]; \n";
        function += "BEGIN \n";
        function += "IF $1 LIKE ''%aggregation%'' THEN \n";
        function += "\tfunction_text := SUBSTRING($1 FROM ''\\((.+)\\)''); \n";
        function += "\tfunction_parameters := REGEXP_SPLIT_TO_ARRAY(function_text, ''\\,''); \n";
        function += "\tattribute_path = REGEXP_SPLIT_TO_ARRAY($4,''\\.''); \n";
        function += "\tFOREACH i IN ARRAY REGEXP_SPLIT_TO_ARRAY(function_parameters[2], ''\\|'') \n";
        function += "\tLOOP \n";
        function += "\t\tclause_parameters := ARRAY_APPEND(clause_parameters, attribute_path[4]||i); \n";
        function += "\tEND LOOP; \n";
        function += "\tFOREACH i IN ARRAY REGEXP_SPLIT_TO_ARRAY($3, ''\\,'') \n";
        function += "\tLOOP \n";
        function += "\t\tgroupby := ARRAY_APPEND(groupby,split_part(i,''.'',4)); \n";
        function += "\tEND LOOP; \n";
        function += "\tvalue := SUBSTRING($2 FROM ''\\#DOUBLE_QUOTES#(.+)\\#DOUBLE_QUOTES#''); \n";
        function += "\tclauses_text := ''''; \n";
        function += "\tFOR ii IN 2..array_upper(clause_parameters,1) LOOP \n";
        function += "\t\tclauses_text := clauses_text || clause_parameters[ii] || '' OR ''; \n";
        function += "\tEND LOOP; \n";
        function += "\tgroupby_text := ''''; \n";
        function += "\tFOREACH i IN ARRAY groupby LOOP \n";
        function += "\t\tgroupby_text := groupby_text || i || '',''; \n";
        function += "\tEND LOOP; \n";
        function += "\tquery := ''SELECT '' || function_parameters[1] || ''('' || value || '') FROM source'' || $7 || ''.'' || attribute_path[array_upper(attribute_path,1)-2] \n";
        function += "\t|| '' WHERE id='' || $5 || '' and ('' || substring(clauses_text from 1 for char_length(clauses_text)-4) || '') GROUP BY '' || substring(groupby_text from 1 for char_length(groupby_text)-1) || \n";
        function += "\t'';''; \n";
        function += "\tEXECUTE query INTO result; \n";
        function += "\tRETURN NEXT result; \n";
        function += "\tELSE \n";
        function += "\tEXECUTE ''SELECT '' || REPLACE($1,''_mipmap_function_'',$6) INTO result; \n";
        function += "\tRETURN NEXT result; \n";
        function += "END IF; \n";
        function += "RETURN; \n";
        function += "END' language plpgsql; \n";
        
        return function;
    }
}
