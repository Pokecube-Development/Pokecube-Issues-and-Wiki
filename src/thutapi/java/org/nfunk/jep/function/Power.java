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
public class Power extends PostfixMathCommand
{
    public Power()
    {
        this.numberOfParameters = 2;
    }

    public Object power(Complex c1, Complex c2)
    {
        final Complex temp = c1.power(c2);

        if (temp.im() == 0) return Double.valueOf(temp.re());
        else return temp;
    }

    public Object power(Complex c, Number d)
    {
        final Complex temp = c.power(d.doubleValue());

        if (temp.im() == 0) return Double.valueOf(temp.re());
        else return temp;
    }

    public Object power(Number d, Complex c)
    {
        final Complex base = new Complex(d.doubleValue(), 0.0);
        final Complex temp = base.power(c);

        if (temp.im() == 0) return Double.valueOf(temp.re());
        else return temp;
    }

    public Object power(Number d1, Number d2)
    {
        if (d1.doubleValue() < 0 && d2.doubleValue() != d2.intValue())
        {
            final Complex c = new Complex(d1.doubleValue(), 0.0);
            return c.power(d2.doubleValue());
        }
        else return Double.valueOf(Math.pow(d1.doubleValue(), d2.doubleValue()));
    }

    public Object power(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex)
        {
            if (param2 instanceof Complex) return this.power((Complex) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.power((Complex) param1, (Number) param2);
        }
        else if (param1 instanceof Number) if (param2 instanceof Complex) return this.power((Number) param1,
                (Complex) param2);
        else if (param2 instanceof Number) return this.power((Number) param1, (Number) param2);

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack); // check the stack

        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();

        inStack.push(this.power(param1, param2));
    }

}
