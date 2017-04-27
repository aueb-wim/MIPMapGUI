/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

//avenet

import java.util.Stack;
import org.nfunk.jep.ParseException;

public class IsNumeric  extends PostfixMathCommand{
  
    public IsNumeric()
     {
         numberOfParameters = 1;
     }
    
    public void run(Stack inStack) throws ParseException 
	{
            checkStack(inStack);// check the stack
            Object param1 = inStack.pop();
            inStack.push(isNumeric(param1.toString()));//push the result on the inStack
            return; 
	} 

    private Object isNumeric(String toString) throws ParseException {        
        try  
        {  
            double d = Double.parseDouble(toString);  
        }  
        catch(NumberFormatException nfe)  
        {  
            return new Double(0.0);  
        }  
        return new Double(1.0);          
    }
}
