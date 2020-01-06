package pokecube.nbtedit.nbt;

public class ParseHelper
{

    public static byte parseByte(String s) throws NumberFormatException
    {
        try
        {
            return Byte.parseByte(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid byte");
        }
    }

    public static byte[] parseByteArray(String s) throws NumberFormatException
    {
        try
        {
            final String[] input = s.split(" ");
            final byte[] arr = new byte[input.length];
            for (int i = 0; i < input.length; ++i)
                arr[i] = ParseHelper.parseByte(input[i]);
            return arr;
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid byte array");
        }
    }

    public static double parseDouble(String s) throws NumberFormatException
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid double");
        }
    }

    public static float parseFloat(String s) throws NumberFormatException
    {
        try
        {
            return Float.parseFloat(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid float");
        }
    }

    public static int parseInt(String s) throws NumberFormatException
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid int");
        }
    }

    public static int[] parseIntArray(String s) throws NumberFormatException
    {
        try
        {
            final String[] input = s.split(" ");
            final int[] arr = new int[input.length];
            for (int i = 0; i < input.length; ++i)
                arr[i] = ParseHelper.parseInt(input[i]);
            return arr;
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid int array");
        }
    }

    public static long parseLong(String s) throws NumberFormatException
    {
        try
        {
            return Long.parseLong(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid long");
        }
    }

    public static short parseShort(String s) throws NumberFormatException
    {
        try
        {
            return Short.parseShort(s);
        }
        catch (final NumberFormatException e)
        {
            throw new NumberFormatException("Not a valid short");
        }
    }

}
