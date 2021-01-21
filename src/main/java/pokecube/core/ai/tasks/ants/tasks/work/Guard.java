package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.tasks.AbstractWorkTask;
import pokecube.core.ai.tasks.combat.management.FindTargetsTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.Battle;
import pokecube.core.utils.AITools;
import thut.api.maths.Vector3;

public class Guard extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        Guard.mems.put(MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT);
        Guard.mems.put(AntTasks.GOING_HOME, MemoryModuleStatus.VALUE_ABSENT);
    }

    public static double ANTGUARDDIST = 8;

    /**
     * Checks the validTargts as well as team settings, will not allow
     * targetting things on the same team.
     */
    final Predicate<Entity> validGuardTarget;

    int patrolTimer = 0;

    public Guard(final IPokemob pokemob)
    {
        super(pokemob, Guard.mems, j -> j == AntJob.GUARD);
        Predicate<Entity> valid = input -> AITools.shouldBeAbleToAgro(this.entity, input);
        valid = valid.and(e -> !this.nest.hab.ants.contains(e.getUniqueID()));
        this.validGuardTarget = valid;
    }

    @Override
    public void reset()
    {
        this.patrolTimer = 0;
    }

    private boolean initiateBattle(final LivingEntity target)
    {
        if (!Battle.createOrAddToBattle(this.entity, target)) return false;
        return true;
    }

    /**
     * Check for and agress any guard targets. <br>
     * <br>
     * This is called from {@link FindTargetsTask#run()}
     *
     * @return a guard target was found
     */
    protected boolean checkGuard()
    {
        // Disabled via the boolean config.
        if (!PokecubeCore.getConfig().guardModeEnabled) return false;

        // TODO find out why this happens, the needed memories should have dealt with it...
        if (!this.entity.getBrain().hasMemory(MemoryModuleType.VISIBLE_MOBS)) return false;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = Vector3.getNewVector();
        centre.set(this.pokemob.getOwner());

        final List<LivingEntity> ret = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        // Only allow valid guard targets.
        for (final LivingEntity o : pokemobs)
            if (this.validGuardTarget.test(o)) ret.add(o);
        ret.removeIf(e -> e.getDistance(this.entity) > Guard.ANTGUARDDIST);
        if (ret.isEmpty()) return false;

        // This is already sorted by distance!
        final LivingEntity newtarget = ret.get(0);
        // Agro the target.
        if (newtarget != null)
        {
            this.initiateBattle(newtarget);
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Selecting Guard Target.");
            return true;
        }
        return false;
    }

    @Override
    public void run()
    {
        if (this.patrolTimer++ > PokecubeCore.getConfig().guardTickRate)
        {
            this.checkGuard();
            this.patrolTimer = 0;
        }
    }
}
