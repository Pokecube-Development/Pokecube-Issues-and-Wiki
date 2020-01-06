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
public class Sine extends PostfixMathCommand
{
    public Sine()
    {
        this.numberOfParameters = 1;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.sin(param));// push the result on the inStack
        return;
    }

    public Object sin(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).sin();
        else if (param instanceof Number) return new Double(Math.sin(((Number) param).doubleValue()));

        throw new ParseException("Invalid parameter type");
    }
}
