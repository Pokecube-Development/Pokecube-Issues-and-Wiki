package pokecube.gimmicks.nests.tasks.ants.tasks.work;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.utils.AITools;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractWorkTask;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Guard extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        Guard.mems.put(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT);
        Guard.mems.put(MemoryModules.GOING_HOME.get(), MemoryStatus.VALUE_ABSENT);
    }

    public static double ANTGUARDDIST = 8;

    /**
     * Checks the validTargts as well as team settings, will not allow targetting things on the same team.
     */
    final BiFunction<IPokemob, Entity, Boolean> validGuardTarget;

    int patrolTimer = 0;

    public Guard()
    {
        super(Guard.mems, j -> j == AntJob.GUARD);
        this.validGuardTarget = (pokemob, input) -> AITools.shouldBeAbleToAgro(pokemob.getEntity(), input)
                && !this.nest.hab.ants.contains(input.getUUID());
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.patrolTimer = 0;
    }

    /**
     * Check for and agress any guard targets. <br>
     */
    protected void checkGuard(IPokemob pokemob)
    {
        // Disabled via the boolean config.
        if (!PokecubeCore.getConfig().guardModeEnabled) return;
        var entity = pokemob.getEntity();
        // TODO find out why this happens, the needed memories should have dealt
        // with it...
        if (!entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = new Vector3();
        centre.set(pokemob.getOwner());

        final List<LivingEntity> ret = new ArrayList<>();
        final Iterable<LivingEntity> pokemobs = entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                .findAll(e -> this.validGuardTarget.apply(pokemob, e) && e.distanceTo(entity) <= Guard.ANTGUARDDIST);
        // Only allow valid guard targets.
        for (final LivingEntity o : pokemobs)
            ret.add(o);
        if (ret.isEmpty()) return;

        // This is already sorted by distance!
        final LivingEntity newtarget = ret.getFirst();
        // Agro the target.
        if (newtarget != null)
        {
            Battle.createOrAddToBattle(entity, newtarget);
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Ant Selecting Guard Target.");
        }
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        if (this.patrolTimer++ > PokecubeCore.getConfig().guardTickRate)
        {
            this.checkGuard(PokemobCaps.getPokemobFor(entity));
            this.patrolTimer = 0;
        }
    }
}
