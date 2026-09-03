package pokecube.api.events.pokemobs;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;

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

    /**
     * This one is called before the level up occurs,
     * cancelling the event will reset the exp, and prevent level up
     */
    public static class Pre extends LevelUpEvent implements ICancellableEvent
    {
        public Pre(IPokemob mob, int newLevel, int oldLevel)
        {
            super(mob, newLevel, oldLevel);
        }
    }

    /**
     * This one is called after the level up has occured, and after the exp orb is added to world.
     */
    public static class Post extends LevelUpEvent
    {
        public Post(IPokemob mob, int newLevel, int oldLevel)
        {
            super(mob, newLevel, oldLevel);
        }
    }
}
