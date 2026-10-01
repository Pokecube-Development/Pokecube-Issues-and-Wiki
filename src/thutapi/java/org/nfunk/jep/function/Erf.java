/*****************************************************************************
 * Exp function
 * created by R. Morris
 * JEP - Java Math Expression Parser 2.24
 * December 30 2002
 * (c) Copyright 2002, Nathan Funk
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.function;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

import java.util.Stack;

/**
 * The erf function.
 * Defines a method erf(Object param)
 * which calculates the value of the error function
 * <p>
 *  From <a href="https://introcs.cs.princeton.edu/java/21function/ErrorFunction.java">...</a>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Erf extends PostfixMathCommand
{
    // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
    public static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                t * ( 1.00002368 +
                        t * ( 0.37409196 +
                                t * ( 0.09678418 +
                                        t * (-0.18628806 +
                                                t * ( 0.27886807 +
                                                        t * (-1.13520398 +
                                                                t * ( 1.48851587 +
                                                                        t * (-0.82215223 +
                                                                                t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
    }

    // fractional error less than x.xx * 10 ^ -4.
    // Algorithm 26.2.17 in Abromowitz and Stegun, Handbook of Mathematical.
    public static double erf2(double z) {
        double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
        double poly = t * (0.3480242 + t * (-0.0958798 + t * (0.7478556)));
        double ans = 1.0 - poly * Math.exp(-z*z);
        if (z >= 0) return  ans;
        else        return -ans;
    }

    public Erf()
    {
        this.numberOfParameters = 1;
    }

    public Object exp(Object param) throws ParseException
    {
        double input = 0;
        boolean valid = false;
        if (param instanceof Complex z)
        {
            input = z.abs();
            valid = true;
        }
        else if (param instanceof Number)
        {
            input = ((Number) param).doubleValue();
            valid = true;
        }
        if(valid)
        {
            return Math.abs(input)<1e-8 ? erf2(input) : erf(input);
        }
        throw new ParseException("Invalid parameter type");
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object param = inStack.pop();
        inStack.push(this.exp(param));// push the result on the inStack
        return;
    }
}
