package org.nfunk.jep.function;


import java.lang.Math;
import java.util.*;
import org.nfunk.jep.*;

//giannisk
public class ToInt extends PostfixMathCommand
 {
    public ToInt()
	{
            numberOfParameters = 1;
	}
    
    public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
                param = Utility.convertParamToNumber(param);
		inStack.push(toInt(param));//push the result on the inStack
		return;
	}
    
    public Object toInt(Object param)
		throws ParseException
	{
		if (param instanceof Number)
		{
			return new Integer( ((Number) param).intValue() );
		}

		throw new ParseException("Invalid parameter type");
	}
}
