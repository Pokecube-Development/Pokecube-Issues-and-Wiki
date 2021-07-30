/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Multiply extends PostfixMathCommand
{

    public Multiply()
    {
        this.numberOfParameters = -1;
    }

    public Complex mul(Complex c1, Complex c2)
    {
        return c1.mul(c2);
    }

    public Complex mul(Complex c, Number d)
    {
        return c.mul(d.doubleValue());
    }

    public Double mul(Number d1, Number d2)
    {
        return Double.valueOf(d1.doubleValue() * d2.doubleValue());
    }

    public Object mul(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex)
        {
            if (param2 instanceof Complex) return this.mul((Complex) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.mul((Complex) param1, (Number) param2);
            else if (param2 instanceof Vector) return this.mul((Vector) param2, (Complex) param1);
        }
        else if (param1 instanceof Number)
        {
            if (param2 instanceof Complex) return this.mul((Complex) param2, (Number) param1);
            else if (param2 instanceof Number) return this.mul((Number) param1, (Number) param2);
            else if (param2 instanceof Vector) return this.mul((Vector) param2, (Number) param1);
        }
        else if (param1 instanceof Vector) if (param2 instanceof Complex) return this.mul((Vector) param1,
                (Complex) param2);
        else if (param2 instanceof Number) return this.mul((Vector) param1, (Number) param2);

        throw new ParseException("Invalid parameter type");
    }

    public Vector mul(Vector v, Complex c)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.mul(c, (Number) v.elementAt(i)));

        return result;
    }

    public Vector mul(Vector v, Number d)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.mul((Number) v.elementAt(i), d));

        return result;
    }

    @Override
    public void run(Stack stack) throws ParseException
    {
        this.checkStack(stack); // check the stack

        Object product = stack.pop();
        Object param;
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < this.curNumberOfParameters)
        {
            // get the parameter from the stack
            param = stack.pop();

            // multiply it with the product, order is important
            // if matricies are used
            product = this.mul(param, product);

            i++;
        }

        stack.push(product);

        return;
    }
}
