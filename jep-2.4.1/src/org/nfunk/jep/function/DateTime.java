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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

//giannisk
public class DateTime extends PostfixMathCommand {
        
    public DateTime() {
        numberOfParameters = 0;
    }

    public void run(Stack inStack) {
        try {
            inStack.push(datetime().toString());
            return;
        } catch (ParseException ex) {
            inStack.push(null);
        }
    }

    public Object datetime() throws ParseException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(new java.util.Date());
    }
    
}
