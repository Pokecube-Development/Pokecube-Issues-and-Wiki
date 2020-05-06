package pokecube.legends.proxy;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.tileentity.RaidSpawn;
import thut.core.client.gui.ConfigGui;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        OBJLoader.INSTANCE.addDomain(Reference.ID);

        // Renderer for raid spawn
        ClientRegistry.bindTileEntitySpecialRenderer(RaidSpawn.class, new Raid());

        // Register config gui
        ModList.get().getModContainerById(Reference.ID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeLegends.config, parent)));
    }
}
