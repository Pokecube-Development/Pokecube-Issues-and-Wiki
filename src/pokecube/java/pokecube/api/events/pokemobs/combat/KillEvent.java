package pokecube.api.events.pokemobs.combat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This event is called when a pokemob kills another pokemob, it is internally used for things like lucky egg exp and
 * exp share. Fired on the PokecubeAPI.POKEMOB_BUS
 */
public class KillEvent extends Event implements ICancellableEvent
{
    public final IPokemob killer;
    public final IPokemob killed;
    public final LivingEntity killedEntity;
    public boolean giveExp;

    public KillEvent(IPokemob killer, IPokemob killed, LivingEntity killedEntity, boolean exp)
    {
        this.killed = killed;
        this.killer = killer;
        this.giveExp = exp;
        this.killedEntity = killedEntity;
    }

}
