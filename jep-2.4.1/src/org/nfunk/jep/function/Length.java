/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

//giannisk

import java.util.Stack;
import org.nfunk.jep.ParseException;

public class Length  extends PostfixMathCommand{
     public Length()
	{
            numberOfParameters = 1;
	}
     
        public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(param.toString().length());//push the result on the inStack
		return;
	}
}
