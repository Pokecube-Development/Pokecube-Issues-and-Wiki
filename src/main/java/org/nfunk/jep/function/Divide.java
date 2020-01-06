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
public class Divide extends PostfixMathCommand
{
    public Divide()
    {
        this.numberOfParameters = 2;
    }

    public Complex div(Complex c1, Complex c2)
    {
        return c1.div(c2);
    }

    public Complex div(Complex c, Number d)
    {
        return new Complex(c.re() / d.doubleValue(), c.im() / d.doubleValue());
    }

    public Vector div(Complex c, Vector v)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.div(c, (Number) v.elementAt(i)));

        return result;
    }

    public Complex div(Number d, Complex c)
    {
        final Complex c1 = new Complex(d.doubleValue(), 0);

        return c1.div(c);
    }

    public Double div(Number d1, Number d2)
    {
        return new Double(d1.doubleValue() / d2.doubleValue());
    }

    public Vector div(Number d, Vector v)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.div(d, (Number) v.elementAt(i)));

        return result;
    }

    public Object div(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex)
        {
            if (param2 instanceof Complex) return this.div((Complex) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.div((Complex) param1, (Number) param2);
            else if (param2 instanceof Vector) return this.div((Complex) param1, (Vector) param2);
        }
        else if (param1 instanceof Number)
        {
            if (param2 instanceof Complex) return this.div((Number) param1, (Complex) param2);
            else if (param2 instanceof Number) return this.div((Number) param1, (Number) param2);
            else if (param2 instanceof Vector) return this.div((Number) param1, (Vector) param2);
        }
        else if (param1 instanceof Vector) if (param2 instanceof Complex) return this.div((Vector) param1,
                (Complex) param2);
        else if (param2 instanceof Number) return this.div((Vector) param1, (Number) param2);

        throw new ParseException("Invalid parameter type");
    }

    public Vector div(Vector v, Complex c)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.div((Number) v.elementAt(i), c));

        return result;
    }

    public Vector div(Vector v, Number d)
    {
        final Vector result = new Vector();

        for (int i = 0; i < v.size(); i++)
            result.addElement(this.div((Number) v.elementAt(i), d));

        return result;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack); // check the stack
        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();
        inStack.push(this.div(param1, param2)); // push the result on the
                                                // inStack
        return;
    }
}
