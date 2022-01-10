package pokecube.compat.concrete;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.events.CompatEvent;
import thut.api.block.flowing.IFlowingBlock;

@Mod.EventBusSubscriber
public class Compat
{
    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (ModList.get().isLoaded("concrete") && ModList.get().isLoaded("pokecube_legends"))
        {
            Block dust = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("concrete:dust_layer"));
            Block lava = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("concrete:molten_layer"));

            pokecube.legends.handlers.ForgeEventHandlers.DUST = () -> dust.defaultBlockState()
                    .setValue(IFlowingBlock.LAYERS, 4);
            pokecube.legends.handlers.ForgeEventHandlers.MOLTEN = () -> lava.defaultBlockState()
                    .setValue(IFlowingBlock.LAYERS, 8);
        }
    }
}
