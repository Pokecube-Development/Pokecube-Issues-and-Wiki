package pokecube.pokeplayer.proxy;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;
import thut.core.common.handlers.PlayerDataHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
public class SetupHandler
{
	@SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
		GuiDisplayPokecubeInfo.instance = new GuiAsPokemob();
    }
}
