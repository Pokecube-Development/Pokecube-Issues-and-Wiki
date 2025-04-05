package pokecube.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class EggEvent extends Event
{
    /**
     * This is called when two pokemobs try to decide if they can breed, if
     * cancelled, they are not compatible.
     */
    public static class CanBreed extends LivingEvent implements ICancellableEvent
    {
        private final LivingEntity other;

        public CanBreed(final LivingEntity first, final LivingEntity other)
        {
            super(first);
            this.other = other;
        }

        public LivingEntity getOther()
        {
            return this.other;
        }
    }

    public static class Hatch extends EggEvent
    {
        public Hatch(final Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }

    /**
     * This is called when a pokemob or nest tries to lay the egg, cancelling it
     * will prevent the egg from being laid.
     */
    public static class Lay extends EggEvent implements ICancellableEvent
    {
        public Lay(final Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }

    /**
     * This event is fired whenever a player places an egg, it cannot be
     * cancelled.
     */
    public static class Place extends EggEvent
    {
        public Place(final Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }

    /**
     * This event is fired right before the egg hatches, cancelling it will
     * prevent it from hatching.
     */
    public static class PreHatch extends EggEvent implements ICancellableEvent
    {
        public PreHatch(final Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }

    public final Entity placer;

    public final EntityPokemobEgg egg;

    private EggEvent(final EntityPokemobEgg egg)
    {
        this.placer = egg.getEggOwner();
        this.egg = egg;
    }
}
