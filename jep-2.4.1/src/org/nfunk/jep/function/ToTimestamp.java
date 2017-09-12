package org.nfunk.jep.function;

import java.text.SimpleDateFormat;
import java.util.Stack;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

public class ToTimestamp extends PostfixMathCommand{
     public ToTimestamp()
	{
            numberOfParameters = 2;
	}
     
     public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object param2 = inStack.pop();
                Object param = inStack.pop();
                try{
                    SimpleDateFormat dateFormat = new SimpleDateFormat((String) param2);
                    inStack.push(dateFormat.parse((String) param));//push the result on the inStack
                    return;
                } catch (java.text.ParseException ex) {
                    throw new ParseException(""+ex);
                }
	}
}
