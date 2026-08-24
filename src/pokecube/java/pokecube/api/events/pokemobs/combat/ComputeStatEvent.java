package pokecube.api.events.pokemobs.combat;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.utils.MoveApplication;

/**
 * This is fired on the PokecubeAPI.MOVE_BUS
 * <p>
 * This is used during combat while computing evasion and accuracy modifiers,
 * ideally for use when the pokemobs are in battle, and members of the same
 * side of the battle are avoiding each other's attacks.
 * <p>
 * Cancelling this event will prevent the default implementation from occuring.
 */
public class ComputeStatEvent extends Event implements ICancellableEvent
{
    public final IPokemob affected;
    public final MoveApplication context;
    public final Stats stat;
    public final double originalValue;
    public double newValue;

    public ComputeStatEvent(IPokemob affected, MoveApplication context, Stats stat, double originalValue)
    {
        this.affected = affected;
        this.context = context;
        this.stat = stat;
        this.originalValue = originalValue;
        this.newValue = originalValue;
    }
}
