package pokecube.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import pokecube.core.inventory.tms.TMContainer;

import java.util.List;

/**
 * This is fired on the PokecubeAPI.MOVES_BUS. This event is used to populate the moves for the TM machine.
 * It should only be fired server side, then a PacketTM will send the result to the client.
 * <br>
 * Packet sending is done by a LOW priority event listener in PacketTMs.
 */
public class TMMachineEvent extends Event
{
    public final ServerPlayer player;
    public final TMContainer container;
    public final List<String> moves;
    public final ItemStack item;

    public TMMachineEvent(ServerPlayer player, TMContainer container, ItemStack item, List<String> moves)
    {
        this.player = player;
        this.container = container;
        this.item = item;
        this.moves = moves;
    }
}
