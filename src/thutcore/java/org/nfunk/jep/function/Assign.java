/*
 * @author rich
 * Created on 18-Nov-2003
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.ParserVisitor;
import org.nfunk.jep.Variable;

/**
 * An assignment operator so we can do
 * x=3+4.
 * This function implements the SpecialEvaluationI interface
 * so that it handles seting the value of a variable.
 * 
 * @author Rich Morris
 *         Created on 18-Nov-2003
 */
@SuppressWarnings({ "rawtypes" })
public class Assign extends PostfixMathCommand implements SpecialEvaluationI
{

    public Assign()
    {
        super();
        this.numberOfParameters = 2;
    }

    /**
     * For assignment set the value of the variable on the lhs to value returned
     * by evaluating the righthand side.
     */
    @Override
    public Object evaluate(Node node, Object data, ParserVisitor pv, Stack inStack/*
                                                                                   * ,SymbolTable
                                                                                   * symTab
                                                                                   */) throws ParseException
    {
        if (node.jjtGetNumChildren() != 2) throw new ParseException("Assignment opperator must have 2 operators.");

        // evaluate the value of the righthand side. Left on top of stack
        node.jjtGetChild(1).jjtAccept(pv, data);
        this.checkStack(inStack); // check the stack
        final Object rhsVal = inStack.peek();

        // Set the value of the variable on the lhs.
        final Node lhsNode = node.jjtGetChild(0);
        if (lhsNode instanceof ASTVarNode)
        {
            final ASTVarNode vn = (ASTVarNode) lhsNode;
            final Variable var = vn.getVar();
            var.setValue(rhsVal);
            return rhsVal;
        }
        throw new ParseException("Assignment should have a variable for the lhs.");
    }
}
