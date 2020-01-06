/*****************************************************************************
 * JEP - Java Math Expression Parser 2.3.1
 * January 26 2006
 * (c) Copyright 2004, Nathan Funk and Richard Morris
 * See LICENSE.txt for license information.
 *****************************************************************************/

package org.nfunk.jep.evaluation;

import java.util.Stack;

import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.function.PostfixMathCommandI;

/**
 * @author nathan
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommandEvaluator
{
    private CommandElement      command;
    private final Stack         stack;
    private PostfixMathCommandI pfmc;
    private int                 i;

    public CommandEvaluator()
    {
        this.stack = new Stack();
    }

    public Object evaluate(final CommandElement[] commands, final SymbolTable symTab) throws Exception
    {

        final int nCommands = commands.length;

        this.stack.removeAllElements();

        // for each command
        this.i = 0;
        while (this.i < nCommands)
        {
            this.command = commands[this.i];

            switch (this.command.getType())
            {
            case CommandElement.FUNC:
            {
                // Function
                this.pfmc = this.command.getPFMC();

                // set the number of current parameters
                // (it is no faster to first check getNumberOfParameters()==-1)
                this.pfmc.setCurNumberOfParameters(this.command.getNumParam());

                this.pfmc.run(this.stack);
                break;
            }
            case CommandElement.VAR:
            {
                // Variable
                this.stack.push(symTab.getValue(this.command.getVarName()));
                break;
            }
            default:
            {
                // Constant
                this.stack.push(this.command.getValue());
            }
            }
            /*
             * if (command instanceof ASTVarNode) {
             * // Variable
             * stack.push(symTab.get(((ASTVarNode)command).getName()));
             * } else if (command instanceof PostfixMathCommandI) {
             * // Function
             * pfmc = (PostfixMathCommandI)command;
             * // set the number of current parameters
             * // (it is no faster to first check getNumberOfParameters()==-1)
             * nParam = ((Integer)commands.elementAt(++i)).intValue();
             * pfmc.setCurNumberOfParameters(nParam);
             * pfmc.run(stack);
             * } else {
             * }
             */

            this.i++;
        }
        if (this.stack.size() != 1) throw new Exception("CommandEvaluator.evaluate(): Stack size is not 1");
        return this.stack.pop();
    }
}
