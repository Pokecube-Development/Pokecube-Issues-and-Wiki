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
 * Converts a pair of real numbers to a complex number Complex(x,y)=x+i y.
 *
 * @author Rich Morris
 *         Created on 24-Mar-2004
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Polar extends PostfixMathCommand
{
    public Polar()
    {
        this.numberOfParameters = 2;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();

        if (param1 instanceof Number && param2 instanceof Number) inStack.push(Complex.polarValueOf((Number) param1,
                (Number) param2));
        else throw new ParseException("Complex: Invalid parameter types " + param1.getClass().getName() + " " + param1
                .getClass().getName());
        return;
    }
}
