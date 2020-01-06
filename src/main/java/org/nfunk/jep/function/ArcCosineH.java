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

/**
 * Implements the arcCosH function.
 *
 * @author Nathan Funk
 * @since 2.3.0 beta 2 - Now returns Double result rather than Complex for x >=
 *        1
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArcCosineH extends PostfixMathCommand
{
    public ArcCosineH()
    {
        this.numberOfParameters = 1;
    }

    public Object acosh(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).acosh();
        else if (param instanceof Number)
        {
            final double val = ((Number) param).doubleValue();
            if (val >= 1.0)
            {
                final double res = Math.log(val + Math.sqrt(val * val - 1));
                return new Double(res);
            }
            else
            {
                final Complex temp = new Complex(((Number) param).doubleValue(), 0.0);
                return temp.acosh();
            }
        }

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.acosh(param));// push the result on the inStack
        return;
    }
}
