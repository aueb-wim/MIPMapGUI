package org.nfunk.jep.function;


import java.lang.Math;
import java.util.*;
import org.nfunk.jep.*;

//giannisk
public class ToLowercase extends PostfixMathCommand
{
        public ToLowercase()
	{
            numberOfParameters = 1;
	}
 
        public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(param.toString().toLowerCase());//push the result on the inStack
		return;
	}
    
}
