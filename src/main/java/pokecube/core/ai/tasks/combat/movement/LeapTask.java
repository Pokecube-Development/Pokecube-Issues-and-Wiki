package pokecube.core.ai.tasks.combat.movement;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.IPosWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/**
 * This one extends TaskBase, rather than FightTask, as it can apply when just a
 * move target, ie attacking blocks, so it doesn't need to actually have a
 * living target to apply.
 */
public class LeapTask extends TaskBase implements IAICombat
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        LeapTask.MEMS.put(MemoryModules.LEAP_TARGET, MemoryModuleStatus.VALUE_PRESENT);
    }

    int leapTick = -1;

    double leapSpeed = 1;

    IPosWrapper pos = null;

    Vector3 leapTarget = Vector3.getNewVector();
    Vector3 leapOrigin = Vector3.getNewVector();

    public LeapTask(final IPokemob mob)
    {
        super(mob, LeapTask.MEMS);
    }

    /**
     * Gets a random sound to play on leaping, selects from the options in
     * config.
     */
    private SoundEvent getLeapSound()
    {
        if (PokecubeCore.getConfig().leaps.length == 1) return PokecubeCore.getConfig().leaps[0];
        return PokecubeCore.getConfig().leaps[new Random().nextInt(PokecubeCore.getConfig().leaps.length)];
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        final LivingEntity target = BrainUtils.getAttackTarget(this.entity);
        this.pokemob.setCombatState(CombatStates.LEAPING, true);

        // Target loc could just be a position
        this.leapTarget.set(this.pos.getPos());
        final Vector3 location = Vector3.getNewVector().set(this.entity);
        final Vector3 diff = this.leapTarget.subtract(location);

        /* Don't leap up if too far. */
        if (diff.y > 5) return;

        final double dist = diff.magSq();

        // Wait till it is a bit closer than this...
        if (dist >= 16.0D) return;

        final Vector3 dir = diff.normalize();
        dir.scalarMultBy(this.leapSpeed * PokecubeCore.getConfig().leapSpeedFactor);
        if (dir.isNaN())
        {
            new Exception().printStackTrace();
            dir.clear();
        }
        if (dist < 9) dir.scalarMultBy(dist / 9);

        // Compute differences in velocities, and then account for that during
        // the leap.
        final Vector3 v_a = Vector3.getNewVector().setToVelocity(this.entity);
        final Vector3 v_t = Vector3.getNewVector();
        if (target != null) v_t.setToVelocity(target);
        // Compute velocity differential.
        final Vector3 dv = v_a.subtractFrom(v_t);
        // Adjust for existing velocity differential.
        dir.subtractFrom(dv);
        /*
         * Apply the leap
         */
        dir.addVelocities(this.entity);

        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Leap: " + this.entity + " " + diff + " " + dir);

        // Set the timer so we don't leap again rapidly
        this.leapTick = this.entity.ticksExisted + PokecubeCore.getConfig().attackCooldown / 2;

        new PlaySound(this.entity.getEntityWorld().getDimensionKey(), Vector3.getNewVector().set(this.entity), this
                .getLeapSound(), SoundCategory.HOSTILE, 1, 1).run(this.world);
        BrainUtils.setLeapTarget(this.entity, null);
    }

    @Override
    public boolean shouldRun()
    {
        // Can't move, no leap
        if (!TaskBase.canMove(this.pokemob)) return false;
        // On cooldown, no leap
        if (this.leapTick > this.entity.ticksExisted) return false;
        // Leap if we have a target pos
        return (this.pos = BrainUtils.getLeapTarget(this.entity)) != null;
    }

}
