package pokecube.compat.wearables;

import net.minecraftforge.common.MinecraftForge;
import pokecube.compat.wearables.layers.WearableWrapper;

public class Impl
{
    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(WearableWrapper.class);
    }
}