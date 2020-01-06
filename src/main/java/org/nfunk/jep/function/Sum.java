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
 * This class serves mainly as an example of a function that accepts any number
 * of parameters. Note that the numberOfParameters is initialized to -1.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Sum extends PostfixMathCommand
{
    private final Add addFun = new Add();

    /**
     * Constructor.
     */
    public Sum()
    {
        // Use a variable number of arguments
        this.numberOfParameters = -1;
    }

    /**
     * Calculates the result of summing up all parameters, which are assumed to
     * be of the Double type.
     */
    @Override
    public void run(Stack stack) throws ParseException
    {
        this.checkStack(stack);// check the stack

        if (this.curNumberOfParameters < 1) throw new ParseException("No arguments for Sum");

        // initialize the result to the first argument
        Object sum = stack.pop();
        Object param;
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < this.curNumberOfParameters)
        {
            // get the parameter from the stack
            param = stack.pop();

            // add it to the sum (order is important for String arguments)
            sum = this.addFun.add(param, sum);

            i++;
        }

        // push the result on the inStack
        stack.push(sum);
    }
}