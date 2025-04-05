/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.evaluation;

import java.util.Enumeration;
import java.util.Vector;

import org.nfunk.jep.ASTConstant;
import org.nfunk.jep.ASTFunNode;
import org.nfunk.jep.ASTStart;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.ParserVisitor;
import org.nfunk.jep.SimpleNode;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ExpressionCompiler implements ParserVisitor
{
    /** Commands */
    private final Vector commands;

    public ExpressionCompiler()
    {
        this.commands = new Vector();
    }

    public CommandElement[] compile(Node node) throws ParseException
    {
        this.commands.removeAllElements();
        node.jjtAccept(this, null);
        final CommandElement[] temp = new CommandElement[this.commands.size()];
        final Enumeration enu = this.commands.elements();
        int i = 0;
        while (enu.hasMoreElements())
            temp[i++] = (CommandElement) enu.nextElement();
        return temp;
    }

    @Override
    public Object visit(ASTConstant node, Object data)
    {
        final CommandElement c = new CommandElement();
        c.setType(CommandElement.CONST);
        c.setValue(node.getValue());
        this.commands.addElement(c);

        return data;
    }

    @Override
    public Object visit(ASTFunNode node, Object data) throws ParseException
    {
        node.childrenAccept(this, data);

        final CommandElement c = new CommandElement();
        c.setType(CommandElement.FUNC);
        c.setPFMC(node.getPFMC());
        c.setNumParam(node.jjtGetNumChildren());
        this.commands.addElement(c);

        return data;
    }

    @Override
    public Object visit(ASTStart node, Object data)
    {
        return data;
    }

    @Override
    public Object visit(ASTVarNode node, Object data)
    {
        final CommandElement c = new CommandElement();
        c.setType(CommandElement.VAR);
        c.setVarName(node.getName());
        this.commands.addElement(c);

        return data;
    }

    @Override
    public Object visit(SimpleNode node, Object data)
    {
        return data;
    }
}
