package pokecube.pokeplayer.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.EntityProvider;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.EntityTools;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.network.EntityProviderPokeplayer;
import pokecube.pokeplayer.network.handlers.AttackEntityHandler;
import pokecube.pokeplayer.network.handlers.AttackLocationHandler;
import pokecube.pokeplayer.network.handlers.StanceHandler;
import thut.core.common.handlers.PlayerDataHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
	public static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
		PokecubeCore.provider = new EntityProviderPokeplayer((EntityProvider) PokecubeCore.provider);
	
		PacketCommand.init();
		
		IHasCommands.COMMANDHANDLERS.put(Command.ATTACKENTITY, AttackEntityHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.ATTACKLOCATION, AttackLocationHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.ATTACKNOTHING, AttackNothingHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.CHANGEFORM, ChangeFormHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.CHANGEMOVEINDEX, MoveIndexHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.STANCE, StanceHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.SWAPMOVES, SwapMovesHandler.class);
		IHasCommands.COMMANDHANDLERS.put(Command.TELEPORT, TeleportHandler.class);
	}
	
    public void setPokemob(PlayerEntity player, IPokemob pokemob)
    {
        setMapping(player, pokemob);
    }

    public void savePokemob(PlayerEntity player)
    {
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        if (info != null) info.save(player);
    }

    private void setMapping(PlayerEntity player, IPokemob pokemob)
    {
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        info.set(pokemob, player);
        if (pokemob != null)
        {
            info.setPlayer(player);
            EntityTools.copyEntityTransforms(info.getPokemob(player.world).getEntity(), player);
            info.save(player);
        }
    }

    public IPokemob getPokemob(PlayerEntity player)
    {
        if (player == null || player.getUniqueID() == null) return null;
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        return info.getPokemob(player.world);
    }

    public void updateInfo(PlayerEntity player, World world)
    {
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        try
        {
            info.onUpdate(player, world);
        }
        catch (Exception e)
        {
        	PokecubeCore.LOGGER.debug("ERRO!"+ e);
        }
    }
}
