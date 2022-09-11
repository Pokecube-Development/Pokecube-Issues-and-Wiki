package thut.api.terrain;

public class BiomeDatabase
{

    public static boolean isBiomeTag(final String name)
    {
        return name.startsWith("#");
    }
}