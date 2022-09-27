package pokecube.mixin.invokers;

public class InvokeHelper
{
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o)
    {
        return (T) o;
    }
}
