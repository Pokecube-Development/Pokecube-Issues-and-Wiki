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
 * Encapsulates the Math.random() function.
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class Random extends PostfixMathCommand
{
    public Random()
    {
        this.numberOfParameters = -1;

    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack

        var rand = Math.random();
        switch (this.curNumberOfParameters)
        {
        case 0:
            inStack.push(Double.valueOf(rand));
            return;
        case 1:
            var r = inStack.pop();
            if (r instanceof Number n) inStack.push(Double.valueOf(rand * n.doubleValue()));
            return;
        case 2:
            var max = inStack.pop();
            var min = inStack.pop();
            if (max instanceof Number max2 && min instanceof Number min2)
            {
                var min_ = min2.doubleValue();
                var range = max2.doubleValue() - min_;
                inStack.push(Double.valueOf(rand * range + min_));
            }
            return;
        }
    }
}