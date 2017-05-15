/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

import java.util.Stack;
import org.nfunk.jep.ParseException;

/**
 *
 * @author ioannisxar
 */
public class funcGenerator extends PostfixMathCommand{
    
    public funcGenerator()
    {
         numberOfParameters = 2;
    }
    
    public void run(Stack inStack) throws ParseException 
    {
        checkStack(inStack);// check the stack
        Object param1 = inStack.pop();
        Object param2 = inStack.pop();
        System.out.println("---funcGenerator---");
        System.out.println(param1);
        System.out.println(param2);
        System.out.println("---funcGenerator---");
        inStack.push(funcGenerator(param1.toString(), param2.toString()));//push the result on the inStack
        return; 
    } 

    private Object funcGenerator(String param1, String param2) throws ParseException {        
        try  
        {  
            System.out.println(param1.replace("___", param2));
            return param1.replace("___", param2);
        }  
        catch(Exception nfe)  
        {  
            return nfe.toString();  
        }  
                 
    }
}
