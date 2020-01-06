package thut.core.client.render.x3d;

public class ModelFormatException extends RuntimeException
{

    private static final long serialVersionUID = 2023547503969671835L;

    public ModelFormatException()
    {
        super();
    }

    public ModelFormatException(String message)
    {
        super(message);
    }

    public ModelFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModelFormatException(Throwable cause)
    {
        super(cause);
    }

}
