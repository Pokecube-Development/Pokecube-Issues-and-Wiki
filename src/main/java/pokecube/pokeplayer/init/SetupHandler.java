package pokecube.pokeplayer.init;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.client.GuiAsPokemob;
//import pokecube.pokeplayer.client.gui.PokeTransformGUI;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
public class SetupHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
    	 GuiDisplayPokecubeInfo.instance = new GuiAsPokemob();
    	// ScreenManager.registerFactory(ContainerInit.POKEPLAYER_CONTAINER.get(), PokeTransformGUI::new);
    }
}
