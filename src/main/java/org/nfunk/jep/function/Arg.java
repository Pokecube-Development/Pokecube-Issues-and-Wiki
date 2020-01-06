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
 * Argument of a complex number
 * 
 * @author Rich Morris
 *         Created on 20-Nov-2003
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Arg extends PostfixMathCommand
{
    private static final Double ONE = new Double(1.0);

    public Arg()
    {
        this.numberOfParameters = 1;
    }

    public Number arg(Object param) throws ParseException
    {
        if (param instanceof Complex) return new Double(((Complex) param).arg());
        else if (param instanceof Number) return Arg.ONE;
        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.arg(param));// push the result on the inStack
        return;
    }

}
