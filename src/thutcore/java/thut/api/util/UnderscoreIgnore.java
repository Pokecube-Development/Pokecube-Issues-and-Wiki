package thut.api.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class UnderscoreIgnore implements ExclusionStrategy
{
    public static UnderscoreIgnore INSTANCE = new UnderscoreIgnore();

    @Override
    public boolean shouldSkipClass(final Class<?> clazz)
    {
        return false;
    }

    @Override
    public boolean shouldSkipField(final FieldAttributes f)
    {
        return f.getName().startsWith("_");
    }
}