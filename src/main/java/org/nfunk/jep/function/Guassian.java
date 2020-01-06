package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Guassian extends PostfixMathCommand
{
    private final java.util.Random rand;

    public Guassian()
    {
        this.numberOfParameters = 0;
        this.rand = new java.util.Random();
    }

    @Override
    public void run(Stack inStack) throws ParseException
    {
        this.checkStack(inStack);// check the stack
        inStack.push(new Double(this.rand.nextGaussian()));
        return;
    }
}