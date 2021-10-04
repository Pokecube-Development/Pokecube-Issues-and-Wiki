package pokecube.core.database.stats;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.Entity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

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
    public CanSpawn canSpawn(Entity trainer);

    /**
     * Location specfic canSpawn
     *
     * @param trainer
     * @param location
     * @return
     */
    public CanSpawn canSpawn(Entity trainer, Vector3 location, boolean message);

    /**
     * Called right before the mob is actually spawned into the world
     *
     * @param mob
     */
    public void onSpawn(IPokemob mob);
}
