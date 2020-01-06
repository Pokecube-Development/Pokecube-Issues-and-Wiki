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
public class Imaginary extends PostfixMathCommand
{
    public Imaginary()
    {
        this.numberOfParameters = 1;
    }

    public Number im(Object param) throws ParseException
    {

        if (param instanceof Complex) return new Double(((Complex) param).im());
        else if (param instanceof Number) return new Double(0);

        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {

        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.im(param));// push the result on the inStack
        return;
    }

}
