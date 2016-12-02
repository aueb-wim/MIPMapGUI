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
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
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
