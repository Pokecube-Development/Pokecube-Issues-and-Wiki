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

/**
 * The list function.
 * Returns a Vector comprising all the children.
 *
 * @author Rich Morris
 *         Created on 29-Feb-2004
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class List extends PostfixMathCommand
{
    public List()
    {
        this.numberOfParameters = -1;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack); // check the stack
        if (this.curNumberOfParameters < 1) throw new ParseException("Empty list");
        final Vector res = new Vector(this.curNumberOfParameters);
        res.setSize(this.curNumberOfParameters);
        for (int i = this.curNumberOfParameters - 1; i >= 0; --i)
        {
            final Object param = inStack.pop();
            res.setElementAt(param, i);
        }
        inStack.push(res);
        return;
    }
}
