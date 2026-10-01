package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class Clamp extends PostfixMathCommand
{
    public Clamp()
    {
        this.numberOfParameters = 3;
    }

    public boolean gt(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex)
            throw new ParseException("> not defined for complex numbers");
        if (param1 instanceof Number n1 && param2 instanceof Number n2)
        {
            final double x = n1.doubleValue();
            final double y = n2.doubleValue();
            return x > y;
        }
        throw new ParseException("> not defined for object of type " + param1.getClass().getName() + " and "
                + param1.getClass().getName());
    }

    public boolean lt(Object param1, Object param2) throws ParseException
    {
        if (param1 instanceof Complex || param2 instanceof Complex)
            throw new ParseException("< not defined for complex numbers");
        if (param1 instanceof Number n1 && param2 instanceof Number n2)
        {
            final double x = n1.doubleValue();
            final double y = n2.doubleValue();
            return x < y;
        }
        throw new ParseException("< not defined for object of type " + param1.getClass().getName() + " and "
                + param1.getClass().getName());
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        final Object max = inStack.pop();
        final Object min = inStack.pop();
        final Object value = inStack.pop();

        Object result = value;

        if (gt(value, max))
        {
            result = max;
        }
        else if (lt(value, min))
        {
            result = min;
        }
        inStack.push(result);
    }
}
