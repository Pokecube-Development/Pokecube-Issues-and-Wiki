package pokecube.core.events.pokemob.combat;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.interfaces.IPokemob;

@Cancelable
/**
 * This event is called when a pokemob kills another pokemob, it is internally
 * used for things like lucky egg exp and exp share. Fired on the
 * PokecubeCore.POKEMOB_BUS
 */
public class KillEvent extends Event
{
    public final IPokemob killer;
    public final IPokemob killed;
    public boolean        giveExp;

    public KillEvent(IPokemob killer, IPokemob killed, boolean exp)
    {
        this.killed = killed;
        this.killer = killer;
        this.giveExp = exp;
    }

}
