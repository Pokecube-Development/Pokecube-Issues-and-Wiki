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
 * atan2(y, x) Returns the angle whose tangent is y/x.
 * 
 * @author nathan
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArcTangent2 extends PostfixMathCommand
{
    public ArcTangent2()
    {
        this.numberOfParameters = 2;
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();

        if (param1 instanceof Number && param2 instanceof Number)
        {
            final double y = ((Number) param1).doubleValue();
            final double x = ((Number) param2).doubleValue();
            inStack.push(Double.valueOf(Math.atan2(y, x)));// push the result on the
                                                       // inStack
        }
        else throw new ParseException("Invalid parameter type");
        return;
    }
}
