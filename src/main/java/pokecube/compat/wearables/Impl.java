package pokecube.compat.wearables;

import pokecube.compat.wearables.layers.WearableWrapper;
import thut.core.common.ThutCore;

public class Impl
{
    public static void register()
    {
        ThutCore.FORGE_BUS.register(WearableWrapper.class);
    }
}