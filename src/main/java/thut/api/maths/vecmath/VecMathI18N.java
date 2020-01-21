package thut.api.maths.vecmath;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

class VecMathI18N
{
    static String getString(final String key)
    {
        String s;
        try
        {
            s = ResourceBundle.getBundle("thut.api.maths.vecmath.ExceptionStrings").getString(key);
        }
        catch (final MissingResourceException e)
        {
            System.err.println("VecMathI18N: Error looking up: " + key);
            s = key;
        }
        return s;
    }
}
