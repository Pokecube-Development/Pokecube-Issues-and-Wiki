package pokecube.legends.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        for (final Block b : BlockInit.BLOCKS)
            RenderTypeLookup.setRenderLayer(b, RenderType.translucent());
        for (final Block b : PlantsInit.BLOCKFLOWERS)
            RenderTypeLookup.setRenderLayer(b, RenderType.cutoutMipped());
    }
}
