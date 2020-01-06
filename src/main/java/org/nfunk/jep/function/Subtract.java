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
public class Subtract extends PostfixMathCommand
{
    public Subtract()
    {
        this.numberOfParameters = 2;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack); // check the stack

        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();

        inStack.push(this.sub(param1, param2));

        return;
    }

    public Complex sub(Complex c1, Complex c2)
    {
        return new Complex(c1.re() - c2.re(), c1.im() - c2.im());
    }

    public Complex sub(Complex c, Number d)
    {
        return new Complex(c.re() - d.doubleValue(), c.im());
    }

    public Complex sub(Number d, Complex c)
    {
        return new Complex(d.doubleValue() - c.re(), -c.im());
    }

    public Double sub(Number d1, Number d2)
    {
        return new Double(d1.doubleValue() - d2.doubleValue());
    }

    public Object sub(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex)
        {
            if (param2 instanceof Complex) return this.sub((Complex) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.sub((Complex) param1, (Number) param2);
        }
        else if (param1 instanceof Number) if (param2 instanceof Complex) return this.sub((Number) param1,
                (Complex) param2);
        else if (param2 instanceof Number) return this.sub((Number) param1, (Number) param2);
        throw new ParseException("Invalid parameter type");
    }
}
