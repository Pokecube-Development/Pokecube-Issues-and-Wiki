package pokecube.legends.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;
import pokecube.legends.tileentity.RaidSpawn;
import thut.core.client.gui.ConfigGui;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        for (final Block b : BlockInit.BLOCKS)
            RenderTypeLookup.setRenderLayer(b, RenderType.getTranslucent());
        for (final Block b : PlantsInit.BLOCKFLOWERS)
            RenderTypeLookup.setRenderLayer(b, RenderType.getCutoutMipped());

        // Renderer for raid spawn
        ClientRegistry.bindTileEntityRenderer(RaidSpawn.TYPE, Raid::new);

        // Register config gui
        ModList.get().getModContainerById(Reference.ID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeLegends.config, parent)));
    }
}
