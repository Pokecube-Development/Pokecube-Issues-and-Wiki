package pokecube.api.events.pokemobs;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This is called before any other interaction code is run. <br>
 * <br>
 * This event has effects based on the set result:<br>
 * <br>
 * Result.DEFAULT: interaction proceeds as normal.<br>
 * Otherwise: interaction will be cancelled, nothing further will happen.<br>
 * <br>
 */
public class InteractEvent extends Event implements ICancellableEvent
{
    public final IPokemob            pokemob;
    public final PlayerInteractEvent event;
    public final Player        player;

    public InteractEvent(IPokemob pokemob, Player player, PlayerInteractEvent event)
    {
        this.pokemob = pokemob;
        this.player = player;
        this.event = event;
    }

    private TriState result = TriState.DEFAULT;

    public void setResult(TriState result)
    {
        this.result = result;
    }

    public TriState getResult()
    {
        return this.result;
    }
}
