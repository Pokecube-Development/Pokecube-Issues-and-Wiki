package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class EggEvent extends Event
{
    @Cancelable
    /**
     * This is called when two pokemobs try to decide if they can breed, if
     * cancelled, they are not compatible.
     */
    public static class CanBreed extends LivingEvent
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

    @Cancelable
    /**
     * This is called when a pokemob or nest tries to lay the egg, cancelling
     * it will prevent the egg from being laid.
     */
    public static class Lay extends EggEvent
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

    @Cancelable
    /**
     * This event is fired right before the egg hatches, cancelling it will
     * prevent it from hatching.
     */
    public static class PreHatch extends EggEvent
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
