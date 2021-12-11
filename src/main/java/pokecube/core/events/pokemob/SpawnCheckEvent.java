package pokecube.core.events.pokemob;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;

/** Fired on the MinecraftForge.EVENT_BUS */
public class SpawnCheckEvent extends Event
{
    @Cancelable
    /**
     * This should be canceled if the spawn does not match the checker, it will
     * only be called if every other condition for the spawnBiomeMatcher is
     * met.
     */
    public static class Check extends SpawnCheckEvent
    {
        public final SpawnCheck checker;

        public Check(SpawnBiomeMatcher spawnBiomeMatcher, SpawnCheck checker)
        {
            super(spawnBiomeMatcher);
            this.checker = checker;
        }
    }

    public static class Init extends SpawnCheckEvent
    {
        public Init(SpawnBiomeMatcher spawnBiomeMatcher)
        {
            super(spawnBiomeMatcher);
        }
    }

    public final SpawnBiomeMatcher matcher;

    public SpawnCheckEvent(SpawnBiomeMatcher spawnBiomeMatcher)
    {
        this.matcher = spawnBiomeMatcher;
    }
}
