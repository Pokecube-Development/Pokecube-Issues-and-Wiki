/*****************************************************************************
 * JEP
 * - Java Math Expression Parser 2.3.1 January 26 2006 (c) Copyright 2004,
 * Nathan Funk and Richard Morris See LICENSE.txt for license
 * information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Ceil extends PostfixMathCommand
{
    public Ceil()
    {
        this.numberOfParameters = 1;
    }

    public Object abs(Object param) throws ParseException
    {
        if (param instanceof Complex) return Math.ceil(new Double(((Complex) param).abs()));
        else if (param instanceof Number) return Math.ceil(new Double(Math.abs(((Number) param).doubleValue())));

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.abs(param));// push the result on the inStack
        return;
    }

}
