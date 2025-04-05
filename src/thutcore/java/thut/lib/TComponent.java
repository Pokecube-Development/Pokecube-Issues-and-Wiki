package thut.lib;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

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
        var _args = new Object[args.length];
        for(int i = 0; i<args.length; i++) {
            var o = args[i];
            if(!(o instanceof Component || TranslatableContents.isAllowedPrimitiveArgument(o))) o = o+"";
            _args[i] = o;
        }
        return Component.translatable(key, _args);
    }
}
