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
public class SquareRoot extends PostfixMathCommand
{
    public SquareRoot()
    {
        this.numberOfParameters = 1;
    }

    /**
     * Applies the function to the parameters on the stack.
     */
    @Override
    public void run(Stack inStack) throws ParseException
    {

        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.sqrt(param));// push the result on the inStack
        return;
    }

    /**
     * Calculates the square root of the parameter. The parameter must
     * either be of type Double or Complex.
     *
     * @return The square root of the parameter.
     */
    public Object sqrt(Object param) throws ParseException
    {
        if (param instanceof Complex) return ((Complex) param).sqrt();
        if (param instanceof Number)
        {
            final double value = ((Number) param).doubleValue();

            // a value less than 0 will produce a complex result
            if (value < 0.0) return new Complex(value).sqrt();
            else return Double.valueOf(Math.sqrt(value));
        }

        throw new ParseException("Invalid parameter type");
    }
}
