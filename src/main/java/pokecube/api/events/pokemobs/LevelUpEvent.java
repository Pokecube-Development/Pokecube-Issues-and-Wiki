package pokecube.api.events.pokemobs;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;

@Cancelable
/** Fired on the PokecubeAPI.POKEMOB_BUS */
public class LevelUpEvent extends Event
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
