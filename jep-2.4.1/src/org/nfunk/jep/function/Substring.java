/*****************************************************************************
JEP 2.4.1, Extensions 1.1.1
April 30 2007
(coffee) Copyright 2007, Nathan Funk and Richard Morris
See LICENSE-*.txt for license information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.*;
import org.nfunk.jep.*;
import org.nfunk.jep.type.*;

public class Substring extends PostfixMathCommand {

    public Substring() {
        //avenet
        numberOfParameters = -1;
        //numberOfParameters = 2;
    }
    
    	/**
	 * Checks the number of parameters of the call.
	 * 
	 */
	public boolean checkNumberOfParameters(int n) {
		return (n == 2 || n == 3);
	}

    public void run(Stack stack) throws ParseException {
        checkStack(stack);// check the stack 
	
        if( !checkNumberOfParameters(curNumberOfParameters))
			throw new ParseException("Substring operator must have 2 or 3 arguments.");

        int pos2 = 0;
        if ( curNumberOfParameters == 3 )
        {
            pos2 = Utility.convertInteger(stack.pop().toString());
        }
        
        int position = Utility.convertInteger(stack.pop().toString());
        
        String superString = stack.pop().toString();

        String result = "";
        if ( curNumberOfParameters == 2 )
             result = superString.substring(position);
        else if ( curNumberOfParameters == 3 )
            result = superString.substring(position, pos2);

        stack.push(result);

        return;
    }

    public Object substring(Object param1, Object param2, Object param3) throws ParseException {
        return param1.toString().substring(Integer.parseInt(param2.toString()), Integer.parseInt(param3.toString()));
    }

    public Object substring(Object param1, Object param2) throws ParseException {
        return param1.toString().substring(Integer.parseInt(param2.toString()));
    }

}