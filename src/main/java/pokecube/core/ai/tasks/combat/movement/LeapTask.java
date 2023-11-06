package pokecube.core.ai.tasks.combat.movement;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/**
 * This one extends TaskBase, rather than FightTask, as it can apply when just a
 * move target, ie attacking blocks, so it doesn't need to actually have a
 * living target to apply.
 */
public class LeapTask extends TaskBase implements IAICombat
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        LeapTask.MEMS.put(MemoryModules.LEAP_TARGET.get(), MemoryStatus.VALUE_PRESENT);
        LeapTask.MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
    }

    int leapTick = -1;

    double leapSpeed = 1;

    PositionTracker pos = null;

    Vector3 leapTarget = new Vector3();
    Vector3 leapOrigin = new Vector3();

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
        return PokecubeCore.getConfig().leaps[ThutCore.newRandom().nextInt(PokecubeCore.getConfig().leaps.length)];
    }

    @Override
    public void reset()
    {
        // Set the timer so we don't leap again rapidly
        this.leapTick = -1;
    }

    @Override
    public void run()
    {
        final LivingEntity target = BrainUtils.getAttackTarget(this.entity);
        this.pokemob.setCombatState(CombatStates.LEAPING, true);

        // Target loc could just be a position
        this.leapTarget.set(this.pos.currentPosition());

        final Vector3 location = new Vector3().set(this.entity);
        final Vector3 diff = this.leapTarget.subtract(location);

        /* Don't leap up if too far. */
        if (diff.y > 5)
        {
            // Instead path to target quickly
            setWalkTo(leapTarget, 1.8, 0);
            return;
        }

        final double dist = diff.magSq();
        double dh = Math.fma(diff.x, diff.x, diff.x * diff.z);

        // Wait till it is a bit closer than this...
        if (dist >= 16.0D)
        {
            // Instead path to target quickly
            setWalkTo(leapTarget, 1.8, 0);
            return;
        }
        // Not close enough horizontally for a leap
        else if (dh > 1)
        {
            // Instead path to target quickly
            setWalkTo(leapTarget, 1.8, 0);
            return;
        }

        this.leapSpeed = 1.0;

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
        final Vector3 v_a = new Vector3().setToVelocity(this.entity);
        final Vector3 v_t = new Vector3();
        if (target != null) v_t.setToVelocity(target);
        // Compute velocity differential.
        final Vector3 dv = v_a.subtractFrom(v_t);
        // Adjust for existing velocity differential.
        dir.subtractFrom(dv);

        final boolean airborne = this.pokemob.floats() || this.pokemob.flys();
        // Increase leap speed for airborne things, they have a bit more
        // friction while in the air.
        if (airborne) dir.scalarMultBy(1.1);
        // Otherwise, if it is on the ground, it should jump a bit if leaping
        // but not downwards
        else if (dir.y >= 0) dir.y = Math.max(dir.y, 0.25);

        if (!airborne && !pokemob.isOnGround()) return;

        dh = Math.fma(dir.x, dir.x, dir.x * dir.z);
        // If too close, then put a minimum horizontal distance for the leap.
        if (dh > 0 && dh < 0.01)
        {
            dh = Math.sqrt(dh);
            dir.x *= 0.1 / dh;
            dir.z *= 0.1 / dh;
        }
        // Now apply the actual leap
        dir.addVelocities(this.entity);
        // Then play leap sound
        new PlaySound(this.entity.getLevel().dimension(), new Vector3().set(this.entity), this.getLeapSound(),
                SoundSource.HOSTILE, 1, 1).run(this.world);

        // Then reset things so we don't immediately re-leap.
        BrainUtils.setLeapTarget(this.entity, null);
        this.reset();
    }

    @Override
    public boolean shouldRun()
    {
        // Can't move, no leap
        if (!TaskBase.canMove(this.pokemob)) return false;

        if (leapTick++ > 10) BrainUtils.setLeapTarget(this.entity, null);
        // Update the leap target here.
        this.pos = BrainUtils.getLeapTarget(this.entity);
        // Leap may have been interupted, so clear this state if so.
        if (this.pos == null) pokemob.setCombatState(CombatStates.LEAPING, false);
        // Executing the leap, so return true.
        if (pokemob.getCombatState(CombatStates.LEAPING)) return true;
        // Leap if we have a target pos
        return pos != null;
    }

}
