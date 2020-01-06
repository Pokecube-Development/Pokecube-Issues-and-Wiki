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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Dot extends PostfixMathCommand
{
    static Add      add = new Add();
    static Multiply mul = new Multiply();

    public Dot()
    {
        this.numberOfParameters = 2;
    }

    public Object dot(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Vector && param2 instanceof Vector) return this.dot((Vector) param1, (Vector) param2);
        throw new ParseException("Dot: Invalid parameter type, both arguments must be vectors");
    }

    public Object dot(Vector v1, Vector v2) throws ParseException
    {
        if (v1.size() != v2.size()) throw new ParseException("Dot: both sides of dot must be same length");
        final int len = v1.size();
        if (len < 1) throw new ParseException("Dot: empty vectors parsed");

        Object res = Dot.mul.mul(v1.elementAt(0), v2.elementAt(0));
        for (int i = 1; i < len; ++i)
            res = Dot.add.add(res, Dot.mul.mul(v1.elementAt(i), v2.elementAt(i)));
        return res;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack); // check the stack

        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();

        inStack.push(this.dot(param1, param2));

        return;
    }
}
