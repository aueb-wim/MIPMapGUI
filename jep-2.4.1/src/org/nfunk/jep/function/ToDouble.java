/* Copyright 2015-2016 by the Athens University of Economics and Business (AUEB).

   This file is part of MIPMap.

   MIPMap is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MIPMap is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MIPMap.  If not, see <http://www.gnu.org/licenses/>.
 */

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
