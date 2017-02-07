package org.nfunk.jep.function;


import java.lang.Math;
import java.util.*;
import org.nfunk.jep.*;

//giannisk
public class ToDouble extends PostfixMathCommand
{
        public ToDouble()
	{
            numberOfParameters = 1;
	}
        
        public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
                param = Utility.convertParamToNumber(param);
		inStack.push(toDouble(param));//push the result on the inStack
		return;
	}
    
    public Object toDouble(Object param)
		throws ParseException
	{
		if (param instanceof Number)
		{
			return new Double( ((Number) param).doubleValue());
		}

		throw new ParseException("Invalid parameter type");
	}
    
}
