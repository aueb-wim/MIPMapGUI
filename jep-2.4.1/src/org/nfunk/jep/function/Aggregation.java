/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

import java.util.Stack;
import org.nfunk.jep.ParseException;

/**
 *
 * @author ioannisxar
 */
public class Aggregation extends PostfixMathCommand {
    
    public Aggregation(){
        numberOfParameters = 4;
    }
    
    public void run(Stack inStack) throws ParseException 
    {
        checkStack(inStack);// check the stack
        return; 
    } 
}
