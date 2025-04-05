package pokecube.api.events.pokemobs;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;

/** Fired on the PokecubeAPI.POKEMOB_BUS */
public class LevelUpEvent extends Event implements ICancellableEvent
{
    public final IPokemob mob;
    public final int      newLevel;
    public final int      oldLevel;

    public LevelUpEvent(IPokemob mob, int newLevel, int oldLevel)
    {
        this.mob = mob;
        this.newLevel = newLevel;
        this.oldLevel = oldLevel;
    }

}
