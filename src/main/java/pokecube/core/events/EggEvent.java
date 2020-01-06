package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class EggEvent extends Event
{
    public static class Hatch extends EggEvent
    {
        public Hatch(Entity egg)
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
        public Lay(Entity egg)
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
        public Place(Entity egg)
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
        public PreHatch(Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }

    public final Entity placer;

    public final EntityPokemobEgg egg;

    private EggEvent(EntityPokemobEgg egg)
    {
        this.placer = egg.getEggOwner();
        this.egg = egg;
    }
}
