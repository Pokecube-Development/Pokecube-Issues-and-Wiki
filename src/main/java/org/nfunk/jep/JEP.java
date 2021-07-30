/*****************************************************************************
 * JEP
 * - Java Math Expression Parser 2.3.1 January 26 2006 (c) Copyright 2004,
 * Nathan Funk and Richard Morris See LICENSE.txt for license
 * information.
 *****************************************************************************/

package org.nfunk.jep;

import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import org.nfunk.jep.function.Abs;
import org.nfunk.jep.function.ArcCosine;
import org.nfunk.jep.function.ArcCosineH;
import org.nfunk.jep.function.ArcSine;
import org.nfunk.jep.function.ArcSineH;
import org.nfunk.jep.function.ArcTanH;
import org.nfunk.jep.function.ArcTangent;
import org.nfunk.jep.function.ArcTangent2;
import org.nfunk.jep.function.Arg;
import org.nfunk.jep.function.Ceil;
import org.nfunk.jep.function.ComplexPFMC;
import org.nfunk.jep.function.Cosine;
import org.nfunk.jep.function.CosineH;
import org.nfunk.jep.function.Exp;
import org.nfunk.jep.function.Floor;
import org.nfunk.jep.function.If;
import org.nfunk.jep.function.Imaginary;
import org.nfunk.jep.function.Logarithm;
import org.nfunk.jep.function.Modulus;
import org.nfunk.jep.function.NaturalLogarithm;
import org.nfunk.jep.function.Polar;
import org.nfunk.jep.function.PostfixMathCommandI;
import org.nfunk.jep.function.Real;
import org.nfunk.jep.function.Sine;
import org.nfunk.jep.function.SineH;
import org.nfunk.jep.function.SquareRoot;
import org.nfunk.jep.function.Str;
import org.nfunk.jep.function.Sum;
import org.nfunk.jep.function.TanH;
import org.nfunk.jep.function.Tangent;
import org.nfunk.jep.type.Complex;
import org.nfunk.jep.type.DoubleNumberFactory;
import org.nfunk.jep.type.NumberFactory;

/**
 * The JEP class is the main interface with which the user should interact. It
 * contains all neccessary methods to parse and evaluate expressions.
 * <p>
 * The most important methods are parseExpression(String), for parsing the
 * mathematical expression, and getValue() for obtaining the value of the
 * expression.
 * <p>
 * Visit
 * <a href="http://www.singularsys.com/jep">http://www.singularsys.com/jep</a>
 * for the newest version of JEP, and complete documentation.
 *
 * @author Nathan Funk
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JEP
{

    /** Debug flag for extra command line output */
    private static final boolean debug = false;

    /** Traverse option */
    private boolean traverse;

    /** Allow undeclared variables option */
    protected boolean allowUndeclared;

    /** Allow undeclared variables option */
    protected boolean allowAssignment;

    /** Implicit multiplication option */
    protected boolean implicitMul;

    /** Symbol Table */
    protected SymbolTable symTab;

    /** Function Table */
    protected FunctionTable funTab;

    /** Error List */
    protected Vector errorList;

    /** The parser object */
    protected Parser parser;

    /** Node at the top of the parse tree */
    private Node topNode;

    /** Evaluator */
    protected EvaluatorVisitor ev;

    /** Number factory */
    protected NumberFactory numberFactory;

    /** OperatorSet */
    protected OperatorSet opSet;

    /**
     * Creates a new JEP instance with the default settings.
     * <p>
     * Traverse = false<br>
     * Allow undeclared variables = false<br>
     * Implicit multiplication = false<br>
     * Number Factory = DoubleNumberFactory
     */
    public JEP()
    {
        this.topNode = null;
        this.traverse = false;
        this.allowUndeclared = false;
        this.allowAssignment = false;
        this.implicitMul = false;
        this.numberFactory = new DoubleNumberFactory();
        this.opSet = new OperatorSet();
        this.initSymTab();
        this.initFunTab();
        this.errorList = new Vector();
        this.ev = new EvaluatorVisitor();
        this.parser = new Parser(new StringReader(""));

        // Ensure errors are reported for the initial expression
        // e.g. No expression entered
        // parseExpression("");
    }

    /**
     * Creates a new JEP instance with custom settings. If the numberFactory_in
     * is null, the default number factory is used.
     *
     * @param traverse_in
     *            The traverse option.
     * @param allowUndeclared_in
     *            The "allow undeclared variables" option.
     * @param implicitMul_in
     *            The implicit multiplication option.
     * @param numberFactory_in
     *            The number factory to be used.
     */
    public JEP(boolean traverse_in, boolean allowUndeclared_in, boolean implicitMul_in, NumberFactory numberFactory_in)
    {
        this.topNode = null;
        this.traverse = traverse_in;
        this.allowUndeclared = allowUndeclared_in;
        this.implicitMul = implicitMul_in;
        if (numberFactory_in == null) this.numberFactory = new DoubleNumberFactory();
        else this.numberFactory = numberFactory_in;
        this.initSymTab();
        this.initFunTab();
        this.errorList = new Vector();
        this.ev = new EvaluatorVisitor();
        this.parser = new Parser(new StringReader(""));

        // Ensure errors are reported for the initial expression
        // e.g. No expression entered
        this.parseExpression("");
    }

    /**
     * This constructor copies the SymbolTable and other components of the
     * arguments to the new instance. Subclasses can call this protected
     * constructor and set the individual components themselves.
     *
     * @since 2.3.0 alpha
     */
    protected JEP(JEP j)
    {
        this.topNode = null;
        this.traverse = j.traverse;
        this.allowUndeclared = j.allowUndeclared;
        this.allowAssignment = j.allowAssignment;
        this.implicitMul = j.implicitMul;
        this.ev = j.ev;
        this.funTab = j.funTab;
        this.numberFactory = j.numberFactory;
        this.parser = j.parser;
        this.symTab = j.symTab;
        this.errorList = j.errorList;
    }

    /**
     * Call this function if you want to parse expressions which involve
     * complex numbers. This method specifies "i" as the imaginary unit (0,1).
     * Two functions re() and im() are also added for extracting the real or
     * imaginary components of a complex number respectively.
     * <p>
     *
     * @since 2.3.0 alpha The functions cmod and arg are added to get the
     *        modulus and argument.
     * @since 2.3.0 beta 1 The functions complex and polar to convert x,y and
     *        r,theta to Complex.
     */
    public void addComplex()
    {
        // add constants to Symbol Table
        this.symTab.addConstant("i", new Complex(0, 1));
        this.funTab.put("re", new Real());
        this.funTab.put("im", new Imaginary());
        this.funTab.put("arg", new Arg());
        this.funTab.put("cmod", new Abs());
        this.funTab.put("complex", new ComplexPFMC());
        this.funTab.put("polar", new Polar());
    }

    /**
     * Adds a constant. This is a variable whos value cannot be changed.
     *
     * @since 2.3.0 beta 1
     */
    public void addConstant(String name, Object value)
    {
        this.symTab.addConstant(name, value);
    }

    /**
     * Adds a new function to the parser. This must be done before parsing an
     * expression so the parser is aware that the new function may be contained
     * in the expression.
     *
     * @param functionName
     *            The name of the function
     * @param function
     *            The function object that is used for evaluating the
     *            function
     */
    public void addFunction(String functionName, PostfixMathCommandI function)
    {
        this.funTab.put(functionName, function);
    }

    /**
     * Adds the constants pi and e to the parser. As addStandardFunctions(),
     * this method should be called immediatly after the JEP object is
     * created.
     */
    public void addStandardConstants()
    {
        // add constants to Symbol Table
        this.symTab.addConstant("pi", Double.valueOf(Math.PI));
        this.symTab.addConstant("e", Double.valueOf(Math.E));
    }

    /**
     * Adds the standard functions to the parser. If this function is not
     * called before parsing an expression, functions such as sin() or cos()
     * would produce an "Unrecognized function..." error. In most cases, this
     * method should be called immediately after the JEP object is created.
     *
     * @since 2.3.0 alpha added if and exp functions
     * @since 2.3.0 beta 1 added str function
     */
    public void addStandardFunctions()
    {
        // add functions to Function Table
        this.funTab.put("sin", new Sine());
        this.funTab.put("cos", new Cosine());
        this.funTab.put("tan", new Tangent());
        this.funTab.put("asin", new ArcSine());
        this.funTab.put("acos", new ArcCosine());
        this.funTab.put("atan", new ArcTangent());
        this.funTab.put("atan2", new ArcTangent2());

        this.funTab.put("sinh", new SineH());
        this.funTab.put("cosh", new CosineH());
        this.funTab.put("tanh", new TanH());
        this.funTab.put("asinh", new ArcSineH());
        this.funTab.put("acosh", new ArcCosineH());
        this.funTab.put("atanh", new ArcTanH());

        this.funTab.put("log", new Logarithm());
        this.funTab.put("ln", new NaturalLogarithm());
        this.funTab.put("exp", new Exp());

        this.funTab.put("sqrt", new SquareRoot());
        this.funTab.put("abs", new Abs());
        this.funTab.put("mod", new Modulus());
        this.funTab.put("sum", new Sum());

        this.funTab.put("ceil", new Ceil());
        this.funTab.put("floor", new Floor());

        this.funTab.put("rand", new org.nfunk.jep.function.Random());
        this.funTab.put("guassian", new org.nfunk.jep.function.Guassian());

        // rjm additions
        this.funTab.put("if", new If());
        this.funTab.put("str", new Str());
    }

    /**
     * Adds a new variable to the parser, or updates the value of an existing
     * variable. This must be done before parsing an expression so the parser is
     * aware that the new variable may be contained in the expression.
     *
     * @param name
     *            Name of the variable to be added
     * @param value
     *            Initial value or new value for the variable
     * @return Double object of the variable
     */
    public Double addVariable(String name, double value)
    {
        final Double object = Double.valueOf(value);
        this.symTab.makeVarIfNeeded(name, object);
        return object;
    }

    /**
     * Adds a new complex variable to the parser, or updates the value of an
     * existing variable. This must be done before parsing an expression so the
     * parser is aware that the new variable may be contained in the expression.
     *
     * @param name
     *            Name of the variable to be added
     * @param re
     *            Initial real value or new real value for the variable
     * @param im
     *            Initial imaginary value or new imaginary value for the
     *            variable
     * @return Complex object of the variable
     */
    public Complex addVariable(String name, double re, double im)
    {
        final Complex object = new Complex(re, im);
        this.symTab.makeVarIfNeeded(name, object);
        return object;
    }

    /**
     * Adds a new variable to the parser as an object, or updates the value of
     * an existing variable. This must be done before parsing an expression so
     * the parser is aware that the new variable may be contained in the
     * expression.
     *
     * @param name
     *            Name of the variable to be added
     * @param object
     *            Initial value or new value for the variable
     */
    public void addVariable(String name, Object object)
    {
        this.symTab.makeVarIfNeeded(name, object);
    }

    /**
     * Evaluate an expression. This method evaluates the argument rather than
     * the topNode of the JEP instance. It should be used in conjunction with
     * {@link #parse parse} rather than {@link #parseExpression
     * parseExpression}.
     *
     * @param node
     *            the top node of the tree representing the expression.
     * @return The value of the expression
     * @throws Exception
     *             if for some reason the expression could not be evaluated
     * @since 2.3.0 alpha
     */
    public Object evaluate(Node node) throws Exception
    {
        return this.ev.getValue(node, new Vector(), this.symTab);
    }

    /**
     * Whether assignment equation <tt>y=x+1</tt> equations are allowed.
     *
     * @since 2.3.0 alpha
     */
    public boolean getAllowAssignment()
    {
        return this.allowAssignment;
    }

    /**
     * Returns the value of the allowUndeclared option.
     *
     * @return True if the allowUndeclared option is enabled. False
     *         otherwise.
     */
    public boolean getAllowUndeclared()
    {
        return this.allowUndeclared;
    }

    /**
     * Evaluates and returns the value of the expression as a complex number.
     *
     * @return The calculated value of the expression as a complex number if no
     *         errors occur. Returns null otherwise.
     */
    public Complex getComplexValue()
    {
        final Object value = this.getValueAsObject();

        if (value == null) return null;
        else if (value instanceof Complex) return (Complex) value;
        else if (value instanceof Number) return new Complex(((Number) value).doubleValue(), 0);
        else return null;
    }

    /**
     * Reports information on the errors that occured during the most recent
     * action.
     *
     * @return A string containing information on the errors, each separated by
     *         a newline character; null if no error has occured
     */
    public String getErrorInfo()
    {
        if (this.hasError())
        {
            String str = "";

            // iterate through all errors and add them to the return string
            for (int i = 0; i < this.errorList.size(); i++)
                str += this.errorList.elementAt(i) + "\n";

            return str;
        }
        else return null;
    }

    /**
     * Returns the function table (the list of all functions that the parser
     * recognises).
     *
     * @return The function table
     */
    public FunctionTable getFunctionTable()
    {
        return this.funTab;
    }

    /**
     * Returns the value of the implicit multiplication option.
     *
     * @return True if the implicit multiplication option is enabled. False
     *         otherwise.
     */
    public boolean getImplicitMul()
    {
        return this.implicitMul;
    }

    /**
     * Returns the number factory.
     *
     * @return the NumberFactory used by this JEP instance.
     */
    public NumberFactory getNumberFactory()
    {
        return this.numberFactory;
    }

    /**
     * Returns the operator set.
     *
     * @return the OperatorSet used by this JEP instance.
     * @since 2.3.0 alpha
     */
    public OperatorSet getOperatorSet()
    {
        return this.opSet;
    }

    /**
     * Returns the parse object.
     *
     * @return the Parse used by this JEP.
     * @since 2.3.0 beta 1
     */
    public Parser getParser()
    {
        return this.parser;
    }
    // ------------------------------------------------------------------------
    // Old code

    /**
     * Returns the symbol table (the list of all variables that the parser
     * recognises).
     *
     * @return The symbol table
     */
    public SymbolTable getSymbolTable()
    {
        return this.symTab;
    }

    /**
     * Returns the top node of the expression tree. Because all nodes are
     * pointed to either directly or indirectly, the entire expression tree can
     * be accessed through this node. It may be used to manipulate the
     * expression, and subsequently evaluate it manually.
     *
     * @return The top node of the expression tree
     */
    public Node getTopNode()
    {
        return this.topNode;
    }

    /**
     * Returns the value of the traverse option.
     *
     * @return True if the traverse option is enabled. False otherwise.
     */
    public boolean getTraverse()
    {
        return this.traverse;
    }

    /**
     * Evaluates and returns the value of the expression as a double number.
     *
     * @return The calculated value of the expression as a double number. If the
     *         type of the value does not implement the Number interface (e.g.
     *         Complex), NaN is returned. If an error occurs during evaluation,
     *         NaN is returned and hasError() will return true.
     * @see #getComplexValue()
     */
    public double getValue()
    {
        final Object value = this.getValueAsObject();

        if (value == null) return Double.NaN;

        if (value instanceof Complex)
        {
            final Complex c = (Complex) value;
            if (c.im() != 0.0) return Double.NaN;
            return c.re();
        }
        if (value != null && value instanceof Number) return ((Number) value).doubleValue();

        return Double.NaN;
    }

    /**
     * Evaluates and returns the value of the expression as an object. The
     * EvaluatorVisitor member ev is used to do the evaluation procedure. This
     * method is useful when the type of the value is unknown, or not important.
     *
     * @return The calculated value of the expression if no errors occur.
     *         Returns null otherwise.
     */
    public Object getValueAsObject()
    {
        Object result;

        if (this.topNode != null && !this.hasError())
        {
            // evaluate the expression
            try
            {
                result = this.ev.getValue(this.topNode, this.errorList, this.symTab);
            }
            catch (final Exception e)
            {
                if (JEP.debug) System.out.println(e);
                this.errorList.addElement("Error during evaluation");
                return null;
            }

            return result;
        }
        else return null;
    }

    /**
     * Gets the object representing the variable with a given name.
     *
     * @param name
     *            the name of the variable to find.
     * @return the Variable object or null if name not found.
     * @since 2.3.0 alpha
     */
    public Variable getVar(String name)
    {
        return this.symTab.getVar(name);
    }

    /**
     * Returns the value of the varible with given name.
     *
     * @param name
     *            name of the variable.
     * @return the current value of the variable.
     * @since 2.3.0 alpha
     */
    public Object getVarValue(String name)
    {
        return this.symTab.getVar(name).getValue();
    }

    /**
     * Returns true if an error occured during the most recent action (parsing
     * or evaluation).
     *
     * @return Returns <code>true</code> if an error occured during the most
     *         recent action (parsing or evaluation).
     */
    public boolean hasError()
    {
        return !this.errorList.isEmpty();
    }

    /** Creates a new FunctionTable object as funTab. */
    public void initFunTab()
    {
        // Init FunctionTable
        this.funTab = new FunctionTable();
    }

    /** Creates a new SymbolTable object as symTab. */
    public void initSymTab()
    {
        // Init SymbolTable
        this.symTab = new SymbolTable(new VariableFactory());
    }

    /**
     * Parses an expression. Returns a object of type Node, does not catch
     * errors. Does not set the topNode variable of the JEP instance. This
     * method should generally be used with the {@link #evaluate evaluate}
     * method rather than getValueAsObject.
     *
     * @param expression
     *            represented as a string.
     * @return The top node of an tree representing the parsed expression.
     * @throws ParseException
     * @since 2.3.0 alpha
     */
    public Node parse(String expression) throws ParseException
    {
        final java.io.StringReader sr = new java.io.StringReader(expression);
        final Node node = this.parser.parseStream(sr, this);
        return node;
    }

    /**
     * Parses the expression. If there are errors in the expression, they are
     * added to the <code>errorList</code> member.
     *
     * @param expression_in
     *            The input expression string
     */
    public void parseExpression(String expression_in)
    {
        final Reader reader = new StringReader(expression_in);

        try
        {
            // try parsing
            this.errorList.removeAllElements();
            this.topNode = this.parser.parseStream(reader, this);
        }
        catch (final Throwable e)
        {
            // an exception was thrown, so there is no parse tree
            this.topNode = null;

            // check the type of error
            if (e instanceof ParseException) // the ParseException object
                                             // contains additional error
                // information
                this.errorList.addElement(((ParseException) e).getMessage());
            // getErrorInfo());
            else
            {
                // if the exception was not a ParseException, it was most
                // likely a syntax error
                if (JEP.debug)
                {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                this.errorList.addElement("Syntax error");
            }
        }

        // If traversing is enabled, print a dump of the tree to
        // standard output
        if (this.traverse && !this.hasError())
        {
            final ParserVisitor v = new ParserDumpVisitor();
            try
            {
                this.topNode.jjtAccept(v, null);
            }
            catch (final ParseException e)
            {
                this.errorList.addElement(e.getMessage());
            }
        }
    }

    /**
     * Removes a function from the parser.
     *
     * @return If the function was added earlier, the function class instance is
     *         returned. If the function was not present, <code>null</code> is
     *         returned.
     */
    public Object removeFunction(String name)
    {
        return this.funTab.remove(name);
    }

    /**
     * Removes a variable from the parser. For example after calling
     * addStandardConstants(), removeVariable("e") might be called to remove the
     * euler constant from the set of variables.
     *
     * @return The value of the variable if it was added earlier. If the
     *         variable is not in the table of variables, <code>null</code> is
     *         returned.
     */
    public Object removeVariable(String name)
    {
        return this.symTab.remove(name);
    }

    /**
     * Sets wheter assignment equations like <tt>y=x+1</tt> are allowed.
     *
     * @since 2.3.0 alpha
     */
    public void setAllowAssignment(boolean value)
    {
        this.allowAssignment = value;
    }

    /**
     * Sets the value for the undeclared variables option. If this option is
     * set to true, expressions containing variables that were not previously
     * added to JEP will not produce an "Unrecognized Symbol" error. The new
     * variables will automatically be added while parsing, and initialized to
     * 0.
     * <p>
     * If this option is set to false, variables that were not previously added
     * to JEP will produce an error while parsing.
     * <p>
     * The default value is false.
     *
     * @param value
     *            The boolean option for allowing undeclared variables.
     */
    public void setAllowUndeclared(boolean value)
    {
        this.allowUndeclared = value;
    }

    /**
     * Sets the value of the implicit multiplication option. If this option is
     * set to true before parsing, implicit multiplication will be allowed. That
     * means that an expression such as
     *
     * <pre>
     * "1 2"
     * </pre>
     *
     * is valid and is interpreted as
     *
     * <pre>
     * "1*2"
     * </pre>
     *
     * .
     * <p>
     * The default value is false.
     *
     * @param value
     *            The boolean implicit multiplication option.
     */
    public void setImplicitMul(boolean value)
    {
        this.implicitMul = value;
    }

    /**
     * Sets the value of the traverse option. setTraverse is useful for
     * debugging purposes. When traverse is set to true, the parse-tree will be
     * dumped to the standard ouput device.
     * <p>
     * The default value is false.
     *
     * @param value
     *            The boolean traversal option.
     */
    public void setTraverse(boolean value)
    {
        this.traverse = value;
    }

    /**
     * Sets the value of a variable. Returns false if variable does not exist
     * or if its value cannot be changed.
     *
     * @param name
     *            name of the variable.
     * @param val
     *            the initial value of the variable.
     * @return false if variable does not exist or if its value cannot be
     *         changed.
     * @since 2.3.0 alpha
     */
    public boolean setVarValue(String name, Object val)
    {
        return this.symTab.setVarValue(name, val);
    }

    /*
     * /** Returns the position (vertical) at which the last error occured. /
     * public int getErrorColumn() { if (hasError && parseException != null)
     * return parseException.getColumn(); else return 0; } /** Returns the line
     * in which the last error occured. / public int getErrorLine() { if
     * (hasError && parseException != null) return parseException.getLine();
     * else return 0; }
     */
}
