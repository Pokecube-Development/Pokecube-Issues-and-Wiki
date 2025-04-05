/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.type;

/**
 * This interface can be implemented to create numbers of any object type.
 * By implementing this interface and calling the setNumberFactory() method of
 * the JEP class, the constants in an expression will be created with that
 * class.
 */
public interface NumberFactory
{

    /**
     * Creates a number object and initializes its value.
     * 
     * @param value
     *            The initial value of the number as a string.
     */
    public Object createNumber(String value);
    /// ** Creates a number with given double value. */
    // public Object createNumber(double value);
    // public Object createNumber(Double value);
}
