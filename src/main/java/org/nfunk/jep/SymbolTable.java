/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/
package org.nfunk.jep;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A Hashtable which holds a list of all variables.
 * Hevily changed from Jep-2.24 which was just a Hashtable which stored
 * the values of each variable. Here the Hashtable contains
 * elements of type {@link Variable Variable} which contain
 * information about that variable.
 * Rather than using {@link #get get} the methods
 * {@link #getValue getValue(String)}, {@link #getVar getVar(String)}
 * should be used to return the value or variable.
 * The {@link #put put} method is deprecated and should be replace by one of
 * <ul>
 * <li>{@link #addVariable addVariable(String,Object)} adds a variable with a
 * given name and value, returns null if variable already exists.
 * <li>{@link #addConstant addConstant(String,Object)} adds a 'constant'
 * variable whos value cannot be changed.
 * <li>{@link #setVarValue setVarValue(String,Object)} sets the value of an
 * existing variable. Returns false if variable does not exist.
 * <li>{@link #makeVarIfNeeded(String,Object)} if necessary creates a variable
 * and set its value.
 * <li>{@link #makeVarIfNeeded(String)} if necessary creates a variable. Does
 * not change the value.
 * </ul>
 * <p>
 * Variables which do not have a value set are deamed to be invalid.
 * When Variables need to be constructed then methods in the
 * {@link VariableFactory}
 * should be called, which allows different types of variables to be used.
 *
 * @author Rich Morris
 *         Created on 28-Feb-2004
 */
@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class SymbolTable extends Hashtable
{
    protected VariableFactory vf;

    /**
     * SymbolTable should always be constructed an associated variable factory.
     */
    public SymbolTable(VariableFactory varFac)
    {
        this.vf = varFac;
    }

    /**
     * Create a constant variable with the given name and value.
     * Returns null if variable already exists.
     */
    public Variable addConstant(String name, Object val)
    {
        final Variable var = this.addVariable(name, val);
        if (var != null) var.setIsConstant(true);
        return var;
    }

    /**
     * Creates a variable with given value.
     * Returns null if variable already exists.
     */
    public Variable addVariable(String name, Object val)
    {
        Variable var = (Variable) super.get(name);
        if (var != null) return null;
        else
        {
            var = this.vf.createVariable(name, val);
            super.put(name, var);
        }
        var.setValidValue(true);
        return var;
    }

    /**
     * Clears the values of all variables.
     * Finer control is available through the
     * {@link Variable#setValidValue Variable.setValidValue} method.
     */
    public void clearValues()
    {
        for (final Enumeration e = this.elements(); e.hasMoreElements();)
        {
            final Variable var = (Variable) e.nextElement();
            if (!var.isConstant()) var.setValidValue(false);
        }
    }

    /**
     * @deprecated The getValue or getVar methods should be used instead.
     */
    @Deprecated
    @Override
    public Object get(Object key)
    {
        return this.getValue(key);
    }

    /**
     * Finds the value of the variable with the given name.
     * Returns null if variable does not exist.
     */
    public Object getValue(Object key)
    {
        final Variable var = (Variable) super.get(key);
        if (var == null) return null;
        return var.getValue();
    }

    /**
     * Finds the variable with given name.
     * Returns null if variable does not exist.
     */
    public Variable getVar(String name)
    {
        return (Variable) super.get(name);
    }

    /**
     * Returns the variable factory of this instance.
     */
    public VariableFactory getVariableFactory()
    {
        return this.vf;
    }

    /**
     * If necessary create a variable with the given name.
     * If the variable exists its value will not be changed.
     * 
     * @return the Variable.
     */
    public Variable makeVarIfNeeded(String name)
    {
        Variable var = (Variable) super.get(name);
        if (var != null) return var;

        var = this.vf.createVariable(name, null);
        super.put(name, var);
        return var;
    }

    /**
     * Create a variable with the given name and value.
     * It siliently does nothing if the value cannot be set.
     * 
     * @return the Variable.
     */
    public Variable makeVarIfNeeded(String name, Object val)
    {
        Variable var = (Variable) super.get(name);
        if (var != null)
        {
            var.setValue(val);
            return var;
        }
        else
        {
            var = this.vf.createVariable(name, val);
            super.put(name, var);
            return var;
        }
    }

    /**
     * @deprecated The setVarValue or makeVar methods should be used instead.
     */
    @Deprecated
    @Override
    public Object put(Object key, Object val)
    {
        return this.makeVarIfNeeded((String) key, val);
    }

    /**
     * Sets the value of variable with the given name.
     * Returns false if variable does not exist or if its value cannot be set.
     */
    public boolean setVarValue(String name, Object val)
    {
        final Variable var = (Variable) super.get(name);
        if (var != null) return var.setValue(val);
        else return false;
    }

    /**
     * Returns a list of variables, one per line.
     */
    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        for (final Enumeration e = this.elements(); e.hasMoreElements();)
        {
            final Variable var = (Variable) e.nextElement();
            sb.append(var.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
