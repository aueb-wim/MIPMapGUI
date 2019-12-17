/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

//giannisk

import java.util.Stack;
import org.nfunk.jep.ParseException;

public class Null extends PostfixMathCommand{
    public Null() {
        numberOfParameters = 0;
    }

    public void run(Stack inStack) {
        inStack.push(null);
        return;
    }


}
