package pokecube.core.database.stats;

import java.util.HashMap;

import com.google.common.collect.Maps;

import pokecube.core.database.PokedexEntry;
import pokecube.core.events.pokemob.SpawnEvent.SpawnContext;
import pokecube.core.interfaces.IPokemob;

public interface ISpecialSpawnCondition
{
    public static enum CanSpawn
    {
        YES, NOTHERE, ALREADYHERE, ALREADYHAVE, KILLEDTOOMANY, NO;

        public boolean test()
        {
            return this == YES;
        }
    }

    public static final HashMap<PokedexEntry, ISpecialSpawnCondition> spawnMap = Maps.newHashMap();

    /**
     * Whether or not the pokemon can spawn, given the trainer is nearby, or is
     * causing the spawn to occur
     *
     * @param trainer
     * @return
     */
    public CanSpawn canSpawn(SpawnContext context);

    /**
     * Location specfic canSpawn
     *
     * @param trainer
     * @param location
     * @return
     */
    public CanSpawn canSpawn(SpawnContext context, boolean message);

    /**
     * Called right before the mob is actually spawned into the world
     *
     * @param mob
     */
    public void onSpawn(IPokemob mob);
}
