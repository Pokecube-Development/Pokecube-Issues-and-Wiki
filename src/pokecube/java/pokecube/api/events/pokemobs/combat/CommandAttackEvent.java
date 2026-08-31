package pokecube.api.events.pokemobs.combat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;

/**
 * This event is called before the pokemob receives orders to execute an
 * attack. Cancelling this event will prevent the pokemob from actually
 * recieving said order. Fired on the PokecubeAPI.POKEMOB_BUS
 */
public class CommandAttackEvent extends EntityEvent implements ICancellableEvent
{
    private Entity target;

    public CommandAttackEvent(@Nonnull Entity entity, @Nullable Entity target)
    {
        super(entity);
        this.target = target;
    }

    public IPokemob getPokemob()
    {
        return PokemobCaps.getPokemobFor(this.getEntity());
    }

    /**
     * Target of this attack command.
     */
    public Entity getTarget()
    {
        return this.target;
    }

    /**
     * Sets the target of the attack command.
     */
    public void setTarget(@Nullable Entity target)
    {
        this.target = target;
    }
}
