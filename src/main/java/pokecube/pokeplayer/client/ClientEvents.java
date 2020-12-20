package pokecube.pokeplayer.client;

import java.awt.event.MouseEvent;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.pokeplayer.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;

public class ClientEvents
{
	public IPokemob getPokemob(PlayerEntity player)
    {
        IPokemob ret = PokeInfo.getPokemob(player);
        if (ret != null && player.getEntityWorld().isRemote)
        {
            PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            info.setPlayer(player);
        }
        return ret;
    }

    @SubscribeEvent
    public void onPlayerTick(final PlayerTickEvent event)
    {
        IPokemob pokemob;
        if (event.side == LogicalSide.SERVER || event.player != PokecubeCore.proxy.getPlayer((event.player.getUniqueID()))
                || (pokemob = getPokemob(event.player)) == null)
            return;
        if (Minecraft.getInstance().currentScreen instanceof GuiPokedex)
        {
            ((GuiPokedex) Minecraft.getInstance().currentScreen).pokemob = pokemob;
            GuiPokedex.pokedexEntry = pokemob.getPokedexEntry();
        }
    }

    @SuppressWarnings("unlikely-arg-type")
	@SubscribeEvent
    public void mouseClickEvent(MouseEvent event)
    {
        IPokemob pokemob = null;
        PlayerEntity player = null;
        int button = event.getButton();
        if (event.isAltDown() && button >= 0
                && (pokemob = getPokemob(player = PokecubeCore.proxy.getPlayer((UUID) null))) != null)
        {
            if (button == 0 && event.isControlDown())
            {
                GuiDisplayPokecubeInfo.instance().pokemobAttack();
                event.equals(true);
            }
            if (button == 1 && event.isControlDown())
            {
                // Our custom StanceHandler will do interaction code on -2
                PacketCommand.sendCommand(pokemob, Command.STANCE, new StanceHandler(true, (byte) -2));

//                EntityInteractSpecific evt = new EntityInteractSpecific(player, Hand.MAIN_HAND, pokemob.getEntity(),
//                        new Vector3d(0, 0, 0));
                // Apply interaction, also do not allow saddle.
                ItemStack saddle = pokemob.getInventory().getStackInSlot(0);
                if (!saddle.isEmpty()) pokemob.getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                //PokecubeCore.RegistryEvents.registerTileEntities(evt);
                if (!saddle.isEmpty()) pokemob.getInventory().setInventorySlotContents(0, saddle);
                event.equals(true);
            }
        }
    }

}