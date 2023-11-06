package pokecube.api.events.pokemobs;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;

/** Fired on the ThutCore.FORGE_BUS */
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
