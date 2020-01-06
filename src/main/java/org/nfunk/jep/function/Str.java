/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;

/**
 * Converts an object into its string representation.
 * Calls the toString method of the object.
 *
 * @author Rich Morris
 *         Created on 27-Mar-2004
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Str extends PostfixMathCommand
{
    public Str()
    {
        this.numberOfParameters = 1;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(param.toString());// push the result on the inStack
        return;
    }
}
