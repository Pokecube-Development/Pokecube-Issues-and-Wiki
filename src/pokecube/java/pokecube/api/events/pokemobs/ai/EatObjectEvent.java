package pokecube.api.events.pokemobs.ai;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.impl.capabilities.impl.PokemobHungry;

public class EatObjectEvent extends Event
{
    public final IPokemob eater;
    public final Object originalFood;

    protected EatObjectEvent(IPokemob eater, Object originalFood)
    {
        this.eater = eater;
        this.originalFood = originalFood;
    }

    /**
     * This is fired on the PokecubeAPI.POKEMOB_BUS
     * <p>
     * Called when a pokemob is attempting to eat something. Cancelling this
     * event will make the attempt to eat food fail. Cancelling this event
     * will prevent the Post event from being fired.
     */
    public static class Pre extends EatObjectEvent implements ICancellableEvent
    {
        public Pre(IPokemob eater, Object originalFood)
        {
            super(eater, originalFood);
        }
    }

    /**
     * This is fired on the PokecubeAPI.POKEMOB_BUS
     * <p>
     * Called when a pokemob actually eats the object.
     * This event cannot be cancelled, and results are ignored.
     * This is called before final consumption of the food, so
     * the pokemob has the pre-hunger value set.
     * <br>
     * Pokemob Usable effects from the contained item will have been applied.
     */
    public static class Post extends EatObjectEvent
    {
        public final int happinessAdjustmentOriginal;
        public final int hungerValueOriginal;
        public final float toHealOriginal;

        public int happinessAdjustment;
        public int hungerValue;
        public float toHeal;

        public <T> Post(PokemobHungry eater, T e, int happinessAdjustment, int hungerValue, float toHeal)
        {
            super(eater, e);
            happinessAdjustmentOriginal = happinessAdjustment;
            hungerValueOriginal = hungerValue;
            toHealOriginal = toHeal;
            this.happinessAdjustment = happinessAdjustment;
            this.hungerValue = hungerValue;
            this.toHeal = toHeal;
        }
    }
}
