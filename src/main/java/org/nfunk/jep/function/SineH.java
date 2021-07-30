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
public class SineH extends PostfixMathCommand
{
    public SineH()
    {
        this.numberOfParameters = 1;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.sinh(param));// push the result on the inStack
        return;
    }

    public Object sinh(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).sinh();
        else if (param instanceof Number)
        {
            final double value = ((Number) param).doubleValue();
            return Double.valueOf((Math.exp(value) - Math.exp(-value)) / 2);
        }

        throw new ParseException("Invalid parameter type");
    }

}
