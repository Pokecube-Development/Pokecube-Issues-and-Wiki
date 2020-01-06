/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

/**
 * Implements the comparative operations <, >, <=, >=, != and ==.
 * Caverts should work where arguments are Double, Complex or String
 * for the last two only != and == work.
 * For other types care might be needed.
 *
 * @author N Funk and R Morris
 * @since 2.3.0 beta 1 a bit of a rewrite to make sub classing easier, now
 *        allows Complex to be compared to Double i.e. 1+0 i == 1.
 * @since 2.3.0 beta 2 changed the internal lt,gt,le,ge,ne and eq method to
 *        return boolean.
 *        If this breaks anything use
 *        if(lt(obj1,obj2)) inStack.push(new Double(1));
 *        else inStack.push(new Double(0));
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Comparative extends PostfixMathCommand
{
    public static final int LT = 0;
    public static final int GT = 1;
    public static final int LE = 2;
    public static final int GE = 3;
    public static final int NE = 4;
    public static final int EQ = 5;
    int                     id;
    double                  tolerance;

    public Comparative(int id_in)
    {
        this.id = id_in;
        this.numberOfParameters = 2;
        this.tolerance = 1e-6;
    }

    public boolean eq(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex && param2 instanceof Complex) return ((Complex) param1).equals((Complex) param2,
                this.tolerance);
        if (param1 instanceof Complex && param2 instanceof Double) return ((Complex) param1).equals(new Complex(
                (Number) param2), this.tolerance);
        if (param2 instanceof Complex && param1 instanceof Double) return ((Complex) param2).equals(new Complex(
                (Number) param1), this.tolerance);
        return param1.equals(param2);
    }

    public boolean ge(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex) throw new ParseException(
                ">= not defined for complex numbers");
        if (param1 instanceof Number && param2 instanceof Number)
        {
            final double x = ((Number) param1).doubleValue();
            final double y = ((Number) param2).doubleValue();
            return x >= y;
        }
        throw new ParseException(">= not defined for object of type " + param1.getClass().getName() + " and " + param1
                .getClass().getName());
    }

    /*
     * old code
     * if ((param1 instanceof Complex) || (param2 instanceof Complex))
     * throw new ParseException(">= not defined for complex numbers");
     * if ((param1 instanceof Number) && (param2 instanceof Number))
     * {
     * double x = ((Number)param1).doubleValue();
     * double y = ((Number)param2).doubleValue();
     * int r = (x>=y) ? 1 : 0;
     * return new Double(r);
     * }
     * throw new
     * ParseException(">= not defined for object of type "+param1.getClass().
     * getName()+" and "+param1.getClass().getName());
     * }
     * {
     * int r;
     * switch (id)
     * {
     * case NE:
     * r = ((Complex)param1).equals((Complex)param2,tolerance) ? 0 : 1;
     * break;
     * case EQ:
     * r = ((Complex)param1).equals((Complex)param2,tolerance) ? 1 : 0;
     * break;
     * default:
     * throw new ParseException("Relational operator type error");
     * }
     * inStack.push(new Double(r));//push the result on the inStack
     * }
     * else if ((param1 instanceof Number) && (param2 instanceof Number))
     * {
     * double x = ((Number)param1).doubleValue();
     * double y = ((Number)param2).doubleValue();
     * int r;
     * switch (id)
     * {
     * case LT:
     * r = (x<y) ? 1 : 0;
     * break;
     * case GT:
     * r = (x>y) ? 1 : 0;
     * break;
     * case LE:
     * r = (x<=y) ? 1 : 0;
     * break;
     * case GE:
     * r = (x>=y) ? 1 : 0;
     * break;
     * case NE:
     * r = (x!=y) ? 1 : 0;
     * break;
     * case EQ:
     * r = (x==y) ? 1 : 0;
     * break;
     * default:
     * throw new ParseException("Unknown relational operator");
     * }
     * inStack.push(new Double(r));//push the result on the inStack
     * }
     * else if ((param1 instanceof String) && (param2 instanceof String))
     * {
     * int r;
     * switch (id)
     * {
     * case NE:
     * r = ((String)param1).equals((String)param2) ? 0 : 1;
     * break;
     * case EQ:
     * r = ((String)param1).equals((String)param2) ? 1 : 0;
     * break;
     * default:
     * throw new ParseException("Relational operator type error");
     * }
     * inStack.push(new Double(r));//push the result on the inStack
     * } else
     * {
     * throw new ParseException("Invalid parameter type");
     * }
     * return;
     * }
     */
    /**
     * Returns the tolerance used for comparing complex numbers
     */
    public double getTolerance()
    {
        return this.tolerance;
    }

    public boolean gt(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex) throw new ParseException(
                "> not defined for complex numbers");
        if (param1 instanceof Number && param2 instanceof Number)
        {
            final double x = ((Number) param1).doubleValue();
            final double y = ((Number) param2).doubleValue();
            return x > y;
        }
        throw new ParseException("> not defined for object of type " + param1.getClass().getName() + " and " + param1
                .getClass().getName());
    }

    public boolean le(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex) throw new ParseException(
                "<= not defined for complex numbers");
        if (param1 instanceof Number && param2 instanceof Number)
        {
            final double x = ((Number) param1).doubleValue();
            final double y = ((Number) param2).doubleValue();
            return x <= y;
        }
        throw new ParseException("<= not defined for object of type " + param1.getClass().getName() + " and " + param1
                .getClass().getName());
    }

    public boolean lt(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex) throw new ParseException(
                "< not defined for complex numbers");
        if (param1 instanceof Number && param2 instanceof Number)
        {
            final double x = ((Number) param1).doubleValue();
            final double y = ((Number) param2).doubleValue();
            return x < y;
        }
        throw new ParseException("< not defined for object of type " + param1.getClass().getName() + " and " + param1
                .getClass().getName());
    }

    public boolean ne(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex && param2 instanceof Complex) return !((Complex) param1).equals((Complex) param2,
                this.tolerance);
        if (param1 instanceof Complex && param2 instanceof Double) return !((Complex) param1).equals(new Complex(
                (Number) param2), this.tolerance);
        if (param2 instanceof Complex && param1 instanceof Double) return !((Complex) param2).equals(new Complex(
                (Number) param1), this.tolerance);
        return !param1.equals(param2);
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack

        final Object param2 = inStack.pop();
        final Object param1 = inStack.pop();
        boolean res = false;
        switch (this.id)
        {
        case LT:
            res = this.lt(param1, param2);
            break;
        case GT:
            res = this.gt(param1, param2);
            break;
        case LE:
            res = this.le(param1, param2);
            break;
        case GE:
            res = this.ge(param1, param2);
            break;
        case NE:
            res = this.ne(param1, param2);
            break;
        case EQ:
            res = this.eq(param1, param2);
            break;
        }
        if (res) inStack.push(new Double(1));
        else inStack.push(new Double(0));
    }

    /**
     * Sets the tolerance used for comparing complex numbers
     * 
     * @param d
     */
    public void setTolerance(double d)
    {
        this.tolerance = d;
    }

}
