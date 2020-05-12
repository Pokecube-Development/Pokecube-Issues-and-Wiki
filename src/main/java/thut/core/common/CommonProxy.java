package thut.core.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.LinkableCaps;
import thut.api.TickHandler;
import thut.api.terrain.StructureManager;
import thut.api.terrain.TerrainManager;
import thut.core.common.ThutCore.MobEvents;
import thut.core.common.world.mobs.data.SyncHandler;

public class CommonProxy implements Proxy
{
    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        Proxy.super.setup(event);

        // Setup terrain manager
        TerrainManager.getInstance();

        MinecraftForge.EVENT_BUS.register(LinkableCaps.class);
        MinecraftForge.EVENT_BUS.register(TerrainManager.class);
        MinecraftForge.EVENT_BUS.register(StructureManager.class);
        MinecraftForge.EVENT_BUS.register(TickHandler.class);
        MinecraftForge.EVENT_BUS.register(MobEvents.class);
        MinecraftForge.EVENT_BUS.register(SyncHandler.class);
    }
}
