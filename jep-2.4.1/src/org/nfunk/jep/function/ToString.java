package org.nfunk.jep.function;

import java.util.*;
import org.nfunk.jep.*;

//giannisk
public class ToString extends PostfixMathCommand
{
        public ToString()
	{
            numberOfParameters = 1;
	}
 
        public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(param.toString());//push the result on the inStack
		return;
	}
    
}
