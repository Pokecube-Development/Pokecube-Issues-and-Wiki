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
public class Add extends PostfixMathCommand
{

    public Add()
    {
        this.numberOfParameters = -1;
    }

    public Complex add(Complex c1, Complex c2)
    {
        return new Complex(c1.re() + c2.re(), c1.im() + c2.im());
    }

    public Complex add(Complex c, Number d)
    {
        return new Complex(c.re() + d.doubleValue(), c.im());
    }

    public Double add(Number d1, Number d2)
    {
        return new Double(d1.doubleValue() + d2.doubleValue());
    }

    public Object add(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex)
        {
            if (param2 instanceof Complex) return this.add((Complex) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.add((Complex) param1, (Number) param2);
        }
        else if (param1 instanceof Number)
        {
            if (param2 instanceof Complex) return this.add((Complex) param2, (Number) param1);
            else if (param2 instanceof Number) return this.add((Number) param1, (Number) param2);
        }
        else if (param1 instanceof String && param2 instanceof String) return (String) param1 + (String) param2;

        throw new ParseException("Invalid parameter type");
    }

    /**
     * Calculates the result of applying the "+" operator to the arguments from
     * the stack and pushes it back on the stack.
     */
    @Override
    public void run(Stack stack) throws ParseException
    {
        this.checkStack(stack);// check the stack

        Object sum = stack.pop();
        Object param;
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < this.curNumberOfParameters)
        {
            // get the parameter from the stack
            param = stack.pop();

            // add it to the sum (order is important for String arguments)
            sum = this.add(param, sum);

            i++;
        }

        stack.push(sum);

        return;
    }
}
