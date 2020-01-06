/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.function.PostfixMathCommandI;
import org.nfunk.jep.function.SpecialEvaluationI;

/**
 * This class is used for the evaluation of an expression. It uses the Visitor
 * design pattern to traverse the function tree and evaluate the expression
 * using a stack.
 * <p>
 * Function nodes are evaluated by first evaluating all the children nodes,
 * then applying the function class associated with the node. Variable and
 * constant nodes are evaluated by pushing their value onto the stack.
 * <p>
 * Some changes implemented by rjm. Nov 03.
 * Added hook to SpecialEvaluationI.
 * Clears stack before evaluation.
 * Simplifies error handeling by making visit methods throw ParseException.
 * Changed visit(ASTVarNode node) so messages not calculated every time.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class EvaluatorVisitor implements ParserVisitor
{
    /** Debug flag */
    private static final boolean debug = false;

    /** Stack used for evaluating the expression */
    protected Stack stack;

    /** The current error list */
    protected Vector errorList;

    /** The symbol table for variable lookup */
    protected SymbolTable symTab;

    /** Flag for errors during evaluation */
    protected boolean errorFlag;

    /** Constructor. Initialize the stack member */
    public EvaluatorVisitor()
    {
        this.errorList = null;
        this.symTab = null;
        this.stack = new Stack();
    }

    /**
     * Adds an error message to the list of errors
     */
    protected void addToErrorList(String errorStr)
    {
        if (this.errorList != null) this.errorList.addElement(errorStr);
    }

    /**
     * Returns the value of the expression as an object. The expression
     * tree is specified with its top node. The algorithm uses a stack
     * for evaluation.
     * <p>
     * The <code>errorList_in</code> parameter is used to
     * add error information that may occur during the evaluation. It is not
     * required, and may be set to <code>null</code> if no error information is
     * needed.
     * <p>
     * The symTab parameter can be null, if no variables are expected in the
     * expression. If a variable is found, an error is added to the error list.
     * <p>
     * An exception is thrown, if an error occurs during evaluation.
     * 
     * @return The value of the expression as an object.
     */
    public Object getValue(Node topNode, Vector errorList_in, SymbolTable symTab_in) throws Exception
    {

        // check if arguments are ok
        if (topNode == null) throw new IllegalArgumentException("topNode parameter is null");

        // set member vars
        this.errorList = errorList_in;
        this.symTab = symTab_in;
        this.errorFlag = false;
        this.stack.removeAllElements();
        // rjm addition ensure stack is correct before beginning.
        // njf changed from clear() to removeAllElements for 1.1 compatibility

        // evaluate by letting the top node accept the visitor
        try
        {
            topNode.jjtAccept(this, null);
        }
        catch (final ParseException e)
        {
            this.addToErrorList(e.getMessage());
            throw e;
        }

        // something is wrong if not exactly one item remains on the stack
        // or if the error flag has been set
        if (this.errorFlag || this.stack.size() != 1) throw new Exception(
                "EvaluatorVisitor.getValue(): Error during evaluation");

        // return the value of the expression
        return this.stack.pop();
    }

    /**
     * Visit a constant node. The value of the constant is pushed onto the
     * stack.
     */
    @Override
    public Object visit(ASTConstant node, Object data)
    {
        this.stack.push(node.getValue());
        return data;
    }

    /**
     * Visit a function node. The values of the child nodes
     * are first pushed onto the stack. Then the function class associated
     * with the node is used to evaluate the function.
     * <p>
     * If a function implements SpecialEvaluationI then the
     * evaluate method of PFMC is called.
     */
    @Override
    public Object visit(ASTFunNode node, Object data) throws ParseException
    {

        if (node == null) return null;
        final PostfixMathCommandI pfmc = node.getPFMC();

        // check if the function class is set
        if (pfmc == null) throw new ParseException("No function class associated with " + node.getName());

        // Some operators (=) need a special method for evaluation
        // as the pfmc.run method does not have enough information
        // in such cases we call the evaluate method which passes
        // all available info. Note evaluating the children is
        // the responsability of the evaluate method.
        if (pfmc instanceof SpecialEvaluationI) return ((SpecialEvaluationI) node.getPFMC()).evaluate(node, data, this,
                this.stack);

        if (EvaluatorVisitor.debug == true) System.out.println("Stack size before childrenAccept: " + this.stack
                .size());

        // evaluate all children (each leaves their result on the stack)

        data = node.childrenAccept(this, data);

        if (EvaluatorVisitor.debug == true) System.out.println("Stack size after childrenAccept: " + this.stack.size());

        if (pfmc.getNumberOfParameters() == -1) // need to tell the class how
                                                // many parameters it can take
                                                // off
            // the stack because it accepts a variable number of params
            pfmc.setCurNumberOfParameters(node.jjtGetNumChildren());

        // try to run the function

        pfmc.run(this.stack);

        if (EvaluatorVisitor.debug == true) System.out.println("Stack size after run: " + this.stack.size());

        return data;
    }

    /**
     * This method should never be called when evaluating a normal
     * expression.
     */
    @Override
    public Object visit(ASTStart node, Object data) throws ParseException
    {
        throw new ParseException("Start node encountered during evaluation");
    }

    /**
     * Visit a variable node. The value of the variable is obtained from the
     * symbol table (symTab) and pushed onto the stack.
     */
    @Override
    public Object visit(ASTVarNode node, Object data) throws ParseException
    {

        // old code
        // if (symTab == null)
        // throw new ParseException(message += "the symbol table is null");

        // optimize (table lookup is costly?)
        // Object temp = symTab.get(node.getName());

        // new code

        final Variable var = node.getVar();
        if (var == null)
        {
            final String message = "Could not evaluate " + node.getName() + ": ";
            throw new ParseException(message + " variable not set");
        }

        final Object temp = var.getValue();

        if (temp == null)
        {
            final String message = "Could not evaluate " + node.getName() + ": ";
            throw new ParseException(message + "the variable was not found in the symbol table");
        }
        else // all is fine
            // push the value on the stack
            this.stack.push(temp);

        return data;
    }

    /**
     * This method should never be called when evaluation a normal
     * expression.
     */
    @Override
    public Object visit(SimpleNode node, Object data) throws ParseException
    {
        throw new ParseException("No visit method for " + node.getClass().toString());
    }
}
