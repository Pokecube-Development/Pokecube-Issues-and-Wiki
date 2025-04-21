package pokecube.api.events.pokemobs.combat;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;

/**
 * This event is called to apply the effects of the status. It will by default be handled by Pokecube, with priority
 * listener of LOWEST. Cancel this event to prevent pokecube dealing with it<br>
 * <br>
 * These events are fired on the {@link pokecube.api.PokecubeAPI#MOVE_BUS}
 */
public abstract class StatusEvent extends EntityEvent
{
    final Holder<MobEffect> status;
    final IPokemob pokemob;
    final Entity source;
    final int amplifier;

    public StatusEvent(Entity entity, Entity source, Holder<MobEffect> status, int amplifier)
    {
        super(entity);
        this.status = status;
        this.source = source;
        this.pokemob = PokemobCaps.getPokemobFor(entity);
        this.amplifier = amplifier;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    public Holder<MobEffect> getStatus()
    {
        return this.status;
    }

    public int getAmplifier()
    {
        return amplifier;
    }

    public Entity getSource()
    {
        return source;
    }

    public static class OnAdded extends StatusEvent
    {

        public OnAdded(Entity entity, Entity source, Holder<MobEffect> status, int amplifier)
        {
            super(entity, source, status, amplifier);
        }
    }

    public static class OnApplyTick extends StatusEvent implements ICancellableEvent
    {

        public OnApplyTick(Entity entity, Entity source, Holder<MobEffect> status, int amplifier)
        {
            super(entity, source, status, amplifier);
        }
    }

    public static class PreAdd extends StatusEvent
    {
        private TriState result = TriState.DEFAULT;

        public PreAdd(Entity entity, Entity source, Holder<MobEffect> status, int amplifier)
        {
            super(entity, source, status, amplifier);
        }

        public TriState getResult()
        {
            return result;
        }

        public void setResult(TriState result)
        {
            this.result = result;
        }
    }

}
