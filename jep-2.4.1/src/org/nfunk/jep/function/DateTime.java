/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
