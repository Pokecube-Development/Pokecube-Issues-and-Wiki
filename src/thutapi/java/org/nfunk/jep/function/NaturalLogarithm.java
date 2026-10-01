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
 * Natural logarithm.
 * RJM Change: fixed so ln(positive Double) is Double.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NaturalLogarithm extends PostfixMathCommand
{
    public NaturalLogarithm()
    {
        this.numberOfParameters = 1;

    }

    public Object ln(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).log();
        else if (param instanceof Number)
        {
            // Now returns Complex if param is <0
            final double num = ((Number) param).doubleValue();
            if (num > 0) return Double.valueOf(Math.log(num));
            else
            {
                final Complex temp = new Complex(num);
                return temp.log();
            }
        }

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.ln(param));// push the result on the inStack
        return;
    }
}
