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
 * All function classes must implement this interface to ensure that the run()
 * method is implemented.
 */
@SuppressWarnings({ "rawtypes" })
public interface PostfixMathCommandI
{
    /**
     * Returns the number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    public int getNumberOfParameters();

    /**
     * Run the function on the stack. Pops the arguments from the stack, and
     * pushes the result on the top of the stack.
     */
    public void run(Stack aStack) throws ParseException;

    /**
     * Sets the number of current number of parameters used in the next call
     * of run(). This method is only called when the reqNumberOfParameters is
     * -1.
     */
    public void setCurNumberOfParameters(int n);
}
