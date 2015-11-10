package it.unibas.spicy.model.algebra.query.operators.sql;

import it.unibas.spicy.model.expressions.Expression;
import it.unibas.spicy.model.paths.VariablePathExpression;
import it.unibas.spicy.utility.SpicyEngineConstants;
import java.util.Arrays;
import java.util.List;

public class JepToPostgresConverter {
    private static final List<Character> math_sign = Arrays.asList('+', '-', '*', '/', '<', '>');
    
    public String convertToPostgres(Expression transformationFunctionExpression, boolean withRel){
        
        String functionExpression = transformationFunctionExpression.getJepExpression().toStringForSql();
        //first fix attribute names
        for (VariablePathExpression vpe: transformationFunctionExpression.getAttributePaths()){                    
            if (functionExpression.contains(vpe.toString())){
                String expressionToCheckForSigns = transformationFunctionExpression.getJepExpression().toString();
                int startIndex = expressionToCheckForSigns.indexOf(vpe.toString());
                int endIndex = startIndex+vpe.toString().length();
                boolean castToFloat = false;
                //check so that the String index will not get out of bounds
                if (startIndex-2>=0){
                    if(math_sign.contains(expressionToCheckForSigns.charAt(startIndex-2))){                  
                        castToFloat = true;
                    }
                    //to catch the '>=' or '<=' case
                    else if(expressionToCheckForSigns.charAt(startIndex-2)=='=' && startIndex-3>=0){
                        if (expressionToCheckForSigns.charAt(startIndex-3)=='<' || expressionToCheckForSigns.charAt(startIndex-3)=='>'){
                            castToFloat = true;
                        }
                    }
                }
                if (endIndex+2<=expressionToCheckForSigns.length()){
                   if(math_sign.contains(expressionToCheckForSigns.charAt(endIndex+1)))  {
                        castToFloat = true;
                   }
                }
                String newAttrName;
                //replace them with the corresponding attribute name
                if (withRel){
                    newAttrName = GenerateSQL.attributeNameWithVariable(vpe);
                }
                else{
                    newAttrName = GenerateSQL.attributeNameInVariable(vpe);       
                 }
                //if previous or next character is one of {+,-,*,/,<,>} cast it to float
                if(castToFloat){
                    functionExpression = functionExpression.replaceAll(vpe.toString(), "cast(" + newAttrName + " as float)");
                }
                else{
                    functionExpression = functionExpression.replaceAll(vpe.toString(), newAttrName);
                }              
            }
        }
        //giannisk
        //in order to avoid considering postgres values separated by commas as different parameters
        //a COMMA_REPLACEMENT pattern is used instead of commas and it is replaced after the final statement has been completed
        //current COMMA_REPLACEMENT string is "#COMMA_CHAR#", a string that is unlikely to be used as user input
        return replaceExpression(functionExpression).replaceAll(SpicyEngineConstants.COMMA_REPLACEMENT, ",");
    }
    
    private String replaceExpression(String functionExpression){
        while (functionExpression.contains("<fn=")){
            int startIndex = functionExpression.indexOf("<fn=");
            int endIndex = findEndIndex(functionExpression, startIndex+5);
            String textToReplace = functionExpression.substring(startIndex, endIndex + 5);

            String functionName = textToReplace.substring(textToReplace.indexOf("<fn=") + 4, textToReplace.indexOf('>'));
            
            startIndex = textToReplace.indexOf('>') + 1;
            endIndex = findEndIndex(textToReplace, startIndex);
            String parametersString = textToReplace.substring(startIndex, endIndex); 
            parametersString = replaceExpression(parametersString);
            //split into parameters - if there is a comma character inside double quotes ignore it
            //(split on the comma only if that comma has zero, or an even number of quotes ahead of it)
            String[] parameters = parametersString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); 
            if (parameters!=null){
                for (int i=0; i<parameters.length;i++){
                    //trim first
                    parameters[i] = parameters[i].trim();
                    //replace double quotes with quotes
                    parameters[i] = parameters[i].replaceAll("\"", "\'");
                    //replace the UMinus JEP class name with "-"
                    parameters[i] = parameters[i].replaceAll("UMinus", "-");                  
                }
            }
            functionExpression = functionExpression.replace(textToReplace, replaceFunctionText(functionName, parameters));            
        }
        return functionExpression;
    }
    
    public int findEndIndex(String str, int startingPos) {
        char[] charArray = str.toCharArray();
        int pos = startingPos;
        int counter = 1;
        while (counter > 0) {
            char c = charArray[pos];
            if (c == '<') {
                if (charArray[pos+1] == '/' && charArray[pos+2] == 'f' && charArray[pos+3] == 'n'){
                    counter--;
                }
                else if (charArray[pos+1] == 'f' && charArray[pos+2] == 'n' && charArray[pos+3] == '='){
                    counter++;
                }
            }
            pos++;
        }
        return --pos;
    }
    
    private String replaceFunctionText(String functionName, String[] parameters){
        String output = "";
        switch(functionName){
            case "abs":
                output = "abs(cast(" + parameters[0] + " as float))";
                break;
            case "append":
                output = parameters[0] + "||" + parameters[1];
                break;
            case "acos":
                output = "acos(cast(" + parameters[0] + " as float))";
                break;
            case "asin":
                output = "asin(cast(" + parameters[0] + " as float))";
                break;
            case "atan":
                output = "atan(cast(" + parameters[0] + " as float))";
                break;
            case "atan2":
                output = "atan2(cast(" + parameters[0] + " as float)"+SpicyEngineConstants.COMMA_REPLACEMENT+" cast(" + parameters[1] + " as float))";
                break;
            case "arg":
                //TODO
                break;
            case "binom":
                //TODO
                break;
            case "ceil":
                output = "ceil(cast(" + parameters[0] + " as float))";
                break;
            case "complex":
                //TODO
                break;
            case "conj":
                //TODO
                break;
            case "contains":
                output = parameters[0]+" like '%"+parameters[1].replaceAll("\'", "")+"%'";
                break;
            case "containCount":
                output = "(length("+parameters[0]+")-length(regexp_replace("+parameters[0]+SpicyEngineConstants.COMMA_REPLACEMENT+
                        parameters[1]+SpicyEngineConstants.COMMA_REPLACEMENT+"''"+SpicyEngineConstants.COMMA_REPLACEMENT+"'g'))) / length("+parameters[1]+")";
                break; 
            case "cos":
                output = "cos(cast(" + parameters[0] + " as float))";
                break;
            case "cosh":
                output = "(exp(cast(" + parameters[0] + " as float))+exp(-cast(" + parameters[0] + " as float)))/2";
                break;
            case "date":
                output = SpicyEngineConstants.POSTGRES_DATE_FUNCTION;
                break;
            case "datetime":
                output = "date_trunc('second'" + SpicyEngineConstants.COMMA_REPLACEMENT + "localtimestamp)";
                break;
            case "exp":
                output = "exp(cast(" + parameters[0] + " as float))";
                break;
            case "floor":
                output = "floor(cast(" + parameters[0] + " as float))";
                break;
            case "if":
                if (parameters[0].contains("==")){
                    parameters[0] = parameters[0].replaceAll("==", "=");
                }
                output = "case when " + parameters[0] + " then " + parameters[1] + " else " + parameters[2] + " end";                
                break;
            case "im":
                //TODO
                break;
            case "indexof":
                output = "position(" + parameters[1] + " in " + parameters[0] + ")";
                break;
            case "isNull":
                output = parameters[0] + " is null";
                break;
            case "isNotNull":
                output = parameters[0] + " is not null";
                break;
            case "len":
                output = "length(" + parameters[0] + ")";
                break;
            case "log":
                output = "log(cast(" + parameters[0] + " as float))";
                break;
            case "ln":
                output = "ln(cast(" + parameters[0] + " as float))";
                break;
            case "mod":
                output = "mod(round(cast(" + parameters[0] +" as numeric))"+SpicyEngineConstants.COMMA_REPLACEMENT+" round(cast(" + parameters[1] + " as numeric)))";
                break;
            case "newId":
                if(GenerateSQL.newSequence){
                    output = SpicyEngineConstants.POSTGRES_NEWID_FUNCTION;
                    GenerateSQL.newSequence = false;
                }
                else{
                    output = SpicyEngineConstants.POSTGRES_CURRENTID_FUNCTION;
                }
                break;
            case "null":
                output = "null";
                break;
            case "polar":
                //TODO
                break;
            case "pow":
                output = "power(cast(" + parameters[0] +" as float)"+SpicyEngineConstants.COMMA_REPLACEMENT+" cast(" + parameters[1] + " as float))";
                break;
            case "re":
                //TODO
                break;
            case "replace":
                output = "replace("+parameters[0]+SpicyEngineConstants.COMMA_REPLACEMENT+" "+parameters[1]+SpicyEngineConstants.COMMA_REPLACEMENT+" "+parameters[2]+")";
                break;                    
            case "round":
                //optional second parameter
                if (parameters.length==2){
                    output = "round(cast(" + parameters[0] +" as numeric)"+SpicyEngineConstants.COMMA_REPLACEMENT+" cast(cast(" + parameters[1] + " as numeric) as integer))";
                }
                else{
                    output = "round(cast(" + parameters[0] + " as float))";
                }
                break;
            case "sin":
                output = "sin(cast(" + parameters[0] + " as float))";
                break;
            case "sinh":
                output = "(exp(cast(" + parameters[0] + " as float))-exp(-cast(" + parameters[0] + " as float)))/2";
                break;
            case "sqrt":
                output = "sqrt(cast(" + parameters[0] + " as float))";
                break;
            case "substring":
                //optional third parameter
                if (parameters.length==3){
                   output = "substring(" + parameters[0] +" from "+ (Integer.parseInt(parameters[1])+1) 
                           +" for " + (Integer.parseInt(parameters[2])-Integer.parseInt(parameters[1])) + ")";
                }
                else{
                   output = "substring(" + parameters[0] +" from "+ (Integer.parseInt(parameters[1])+1) + ")"; 
                }
                break;
            case "tan":
                output = "tan(cast(" + parameters[0] + " as float))";
                break;
            case "tanh":
                output = "(exp(cast(" + parameters[0] + " as float))-exp(-cast(" + parameters[0] +
                        " as float)))/(exp(cast(" + parameters[0] + " as float))+exp(-cast(" + parameters[0] + " as float)))";
                break;
            case "todate":
                output = "to_date(" + parameters[0] + SpicyEngineConstants.COMMA_REPLACEMENT + parameters[1] + ")";
                break;
            case "todouble":
                output = "cast(" + parameters[0] + " as float)";
                break;
            case "toint":
                output = "round(cast(" + parameters[0] + " as numeric))";
                break;
            case "tolower":
                output = "lower(" + parameters[0] + ")";
                break;
            case "tostring":
                output = "cast(" + parameters[0] + " as text)";
                break;
            case "toupper":
                output = "upper(" + parameters[0] + ")";
                break;
            default:
                break;
        }        
        return output;
    }
}
