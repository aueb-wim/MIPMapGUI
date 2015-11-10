/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

//giannisk

import java.util.Stack;
import org.nfunk.jep.ParseException;

public class ContainCount  extends PostfixMathCommand{
    public ContainCount()
     {
         numberOfParameters = 2;
     }
    
    public void run(Stack inStack) throws ParseException 
	{
            checkStack(inStack);// check the stack
            Object param2 = inStack.pop();
            Object param1 = inStack.pop();
            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                    lastIndex = param1.toString().indexOf(param2.toString(), lastIndex);

                    if (lastIndex != -1) {
                            count++;
                            lastIndex += param2.toString().length();
                    }
            }
            inStack.push(count);//push the result on the inStack
            return; 
	} 
}
