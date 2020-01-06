package thut.core.common;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.terrain.TerrainManager;

public class CommonProxy implements Proxy
{
    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        Proxy.super.setup(event);

        // Setup terrain manager
        TerrainManager.getInstance();

    }
}
