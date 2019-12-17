package org.nfunk.jep.function;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Stack;

public class CurrentYear extends PostfixMathCommand {
    public CurrentYear() {
        numberOfParameters = 0;
    }

    public void run(Stack inStack) {
        try {
            inStack.push(currentYear());
            return;
        } catch (ParseException ex) {
            inStack.push(null);
        }
    }

    public Object currentYear() throws ParseException {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR);
    }
}
