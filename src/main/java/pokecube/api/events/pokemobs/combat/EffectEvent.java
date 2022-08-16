package pokecube.api.events.pokemobs.combat;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect.Effect;

/**
 * This event is called to apply the effects of the status. It will by default
 * be handled by Pokecube, with priority listener of LOWEST. Cancel this event
 * to prevent pokecube dealing with it<br>
 * <br>
 * These events are fired on the
 * {@link pokecube.core.PokecubeCore#MOVE_BUS}
 */
@Cancelable
public class EffectEvent extends EntityEvent
{
    final Effect   status;
    final IPokemob pokemob;

    public EffectEvent(Entity entity, Effect status)
    {
        super(entity);
        this.status = status;
        this.pokemob = PokemobCaps.getPokemobFor(entity);
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    public Effect getStatus()
    {
        return this.status;
    }

}
