/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Not extends PostfixMathCommand
{
    public Not()
    {
        this.numberOfParameters = 1;

    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        if (param instanceof Number)
        {
            final int r = ((Number) param).doubleValue() == 0 ? 1 : 0;
            inStack.push(Double.valueOf(r));// push the result on the inStack
        }
        else throw new ParseException("Invalid parameter type");
        return;
    }

}
