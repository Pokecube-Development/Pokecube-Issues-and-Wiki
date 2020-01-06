/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TanH extends PostfixMathCommand
{
    public TanH()
    {
        this.numberOfParameters = 1;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.tanh(param));// push the result on the inStack
        return;
    }

    public Object tanh(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).tanh();
        else if (param instanceof Number)
        {
            final double value = ((Number) param).doubleValue();
            return new Double((Math.exp(value) - Math.exp(-value)) / (Math.pow(Math.E, value) + Math.pow(Math.E,
                    -value)));
        }
        throw new ParseException("Invalid parameter type");
    }

}
