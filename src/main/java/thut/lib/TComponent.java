package thut.lib;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TComponent
{
    public static MutableComponent literal(String key)
    {
        return Component.literal(key);
    }

    public static MutableComponent translatable(String key)
    {
        return Component.translatable(key);
    }

    public static MutableComponent translatable(String key, Object... args)
    {
        return Component.translatable(key, args);
    }
}
