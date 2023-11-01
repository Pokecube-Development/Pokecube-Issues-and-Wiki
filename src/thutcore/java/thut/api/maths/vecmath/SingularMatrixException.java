package thut.api.maths.vecmath;

/**
 * Indicates that inverse of a matrix can not be computed.
 */
public class SingularMatrixException extends RuntimeException
{

    /**
     *
     */
    private static final long serialVersionUID = 4437102327188497765L;

    /**
     * Create the exception object with default values.
     */
    public SingularMatrixException()
    {
    }

    /**
     * Create the exception object that outputs message.
     *
     * @param str
     *            the message string to be output.
     */
    public SingularMatrixException(final String str)
    {

        super(str);
    }

}
