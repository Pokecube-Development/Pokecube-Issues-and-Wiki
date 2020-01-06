/*
 * @author rich
 * Created on 18-Nov-2003
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.ParserVisitor;

/**
 * Functions which require greater control over their evaluation should
 * implement this interface.
 *
 * @author Rich Morris
 *         Created on 18-Nov-2003
 */
@SuppressWarnings({ "rawtypes" })
public interface SpecialEvaluationI
{

    /**
     * Performs some special evaluation on the node.
     * This method has the responsability for evaluating the children of the
     * node
     * and it should generally call
     * 
     * <pre>
     * node.jjtGetChild(i).jjtAccept(pv, data);
     * </pre>
     * 
     * for each child.
     *
     * @param node
     *            The current node
     * @param data
     *            The data passed to visitor, typically not used
     * @param pv
     *            The visitor, can be used evaluate the children
     * @param stack
     *            The stack of the evaluator
     * @param symTab
     *            The symbol table
     * @return the value after evaluation
     * @throws ParseException
     */
    public Object evaluate(Node node, Object data, ParserVisitor pv, Stack stack/*
                                                                                 * ,SymbolTable
                                                                                 * symTab
                                                                                 */) throws ParseException;
}
