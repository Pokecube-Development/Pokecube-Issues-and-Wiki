/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.type;

/**
 * Default class for creating number objects. This class can be replaced by
 * other NumberFactory implementations if other number types are required. This
 * can be done using the
 */
public class DoubleNumberFactory implements NumberFactory
{

    /**
     * Creates a Double object initialized to the value of the parameter.
     *
     * @param value
     *            The initialization value for the returned object.
     */
    @Override
    public Object createNumber(String value)
    {
        return new Double(value);
    }
    // public Object createNumber(double value) { return new Double(value); }
    // public Object createNumber(Double value) { return value; }

}
