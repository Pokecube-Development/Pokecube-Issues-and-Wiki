package pokecube.pokeplayer.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.network.EntityProvider;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.data.PokeInfo;
import pokecube.pokeplayer.util.EntityProviderPokeplayer;
import pokecube.pokeplayer.util.handlers.AttackEntity;
import pokecube.pokeplayer.util.handlers.AttackLocation;
import pokecube.pokeplayer.util.handlers.Stance;
import thut.core.client.gui.ConfigGui;
import thut.core.common.handlers.PlayerDataHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
	@SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        PlayerDataHandler.register(PokeInfo.class);
        PokecubeCore.provider = new EntityProviderPokeplayer((EntityProvider) PokecubeCore.provider);

        PacketCommand.init();

        IHasCommands.COMMANDHANDLERS.put(Command.ATTACKENTITY, AttackEntity.class);
        IHasCommands.COMMANDHANDLERS.put(Command.ATTACKLOCATION, AttackLocation.class);
        IHasCommands.COMMANDHANDLERS.put(Command.STANCE, Stance.class);
        
        // Register config gui
        ModList.get().getModContainerById(Reference.ID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(Pokeplayer.config, parent)));
    }
}
