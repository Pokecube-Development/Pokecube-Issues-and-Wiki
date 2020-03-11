package pokecube.legends.proxy;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.init.BlockInit;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        RenderTypeLookup.setRenderLayer(BlockInit.BLOCK_PORTALWARP, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.ULTRASPACE_PORTAL, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.XERNEAS_CORE, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.TIMESPACE_CORE, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.LEGENDARY_SPAWN, RenderType.translucent());
    }
}
