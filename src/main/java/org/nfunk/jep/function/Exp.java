/*****************************************************************************
 * Exp function
 * created by R. Morris
 * JEP - Java Math Expression Parser 2.24
 * December 30 2002
 * (c) Copyright 2002, Nathan Funk
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

/**
 * The exp function.
 * Defines a method exp(Object param)
 * which calculates
 * 
 * @author Rich Morris
 *         Created on 20-Jun-2003
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Exp extends PostfixMathCommand
{
    public Exp()
    {
        this.numberOfParameters = 1;
    }

    public Object exp(Object param) throws ParseException
    {
        if (param instanceof Complex)
        {
            final Complex z = (Complex) param;
            final double x = z.re();
            final double y = z.im();
            final double mod = Math.exp(x);
            return new Complex(mod * Math.cos(y), mod * Math.sin(y));
        }
        else if (param instanceof Number) return Double.valueOf(Math.exp(((Number) param).doubleValue()));

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.exp(param));// push the result on the inStack
        return;
    }
}
