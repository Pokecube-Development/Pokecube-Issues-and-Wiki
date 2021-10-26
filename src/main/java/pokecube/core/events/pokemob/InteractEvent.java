package pokecube.core.events.pokemob;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import pokecube.core.interfaces.IPokemob;

@HasResult
/**
 * This is called before any other interaction code is run. <br>
 * <br>
 * This event has effects based on the set result:<br>
 * <br>
 * Result.DEFAULT: interaction proceeds as normal.<br>
 * Otherwise: interaction will be cancelled, nothing further will happen.<br>
 * <br>
 */
public class InteractEvent extends Event
{
    public final IPokemob            pokemob;
    public final PlayerInteractEvent event;
    public final PlayerEntity        player;

    public InteractEvent(IPokemob pokemob, PlayerEntity player, PlayerInteractEvent event)
    {
        this.pokemob = pokemob;
        this.player = player;
        this.event = event;
    }
}
