package thut.lib;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TComponent
{
    public static MutableComponent literal(String key)
    {
        return new TextComponent(key);
    }

    public static MutableComponent translatable(String key)
    {
        return new TranslatableComponent(key);
    }

    public static MutableComponent translatable(String key, Object... args)
    {
        return new TranslatableComponent(key, args);
    }
}
