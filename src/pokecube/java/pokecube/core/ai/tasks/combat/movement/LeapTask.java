package pokecube.core.ai.tasks.combat.movement;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.PokemobBehaviour;
import pokecube.core.ai.tasks.TaskBase;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.Map;

/**
 * This one extends TaskBase, rather than FightTask, as it can apply when just a move target, ie attacking blocks, so it
 * doesn't need to actually have a living target to apply.
 */
public class LeapTask extends PokemobBehaviour implements IAICombat
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        LeapTask.MEMS.put(MemoryModules.TIMER_LEAP.get(), MemoryStatus.REGISTERED);
        LeapTask.MEMS.put(MemoryModules.LEAP_TARGET.get(), MemoryStatus.VALUE_PRESENT);
        LeapTask.MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
    }

    public LeapTask()
    {
        super(LeapTask.MEMS);
    }

    /**
     * Gets a random sound to play on leaping, selects from the options in config.
     */
    private SoundEvent getLeapSound()
    {
        if (PokecubeCore.getConfig().leaps.length == 1) return PokecubeCore.getConfig().leaps[0];
        return PokecubeCore.getConfig().leaps[ThutCore.newRandom().nextInt(PokecubeCore.getConfig().leaps.length)];
    }

    @Override
    public void reset(Mob entity)
    {
        var brain = entity.getBrain();
        // Set the timer so we don't leap again rapidly
        brain.eraseMemory(MemoryModules.TIMER_LEAP.get());
        brain.eraseMemory(MemoryModules.LEAP_TARGET.get());
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var brain = entity.getBrain();
        int leapTick = brain.getMemory(MemoryModules.TIMER_LEAP.get()).orElse(0);
        if (leapTick > 0)
        {
            leapTick++;
            if (leapTick > PokecubeCore.getConfig().attackCooldown) reset(entity);
            else brain.setMemory(MemoryModules.TIMER_LEAP.get(), leapTick);
            return;
        }

        var pokemob = PokemobCaps.getPokemobFor(entity);
        LivingEntity target = BrainUtils.getAttackTarget(entity);
        pokemob.setCombatState(CombatStates.LEAPING, true);
        var pos = brain.getMemory(MemoryModules.LEAP_TARGET.get()).get();
        // Target loc could just be a position
        Vector3 leapTarget = new Vector3(pos.currentPosition());

        Vector3 location = new Vector3().set(entity);
        Vector3 diff = leapTarget.subtract(location);

        /* Don't leap up if too far. */
        if (diff.y > 5)
        {
            // Instead path to target quickly
            setWalkTo(entity, leapTarget, 1.8, 0);
            return;
        }

        double dist = diff.magSq();
        double dh = Math.fma(diff.x, diff.x, diff.x * diff.z);

        // Wait till it is a bit closer than this...
        if (dist >= 16.0D)
        {
            // Instead path to target quickly
            setWalkTo(entity, leapTarget, 1.8, 0);
            return;
        }
        // Not close enough horizontally for a leap
        else if (dh > 1)
        {
            // Instead path to target quickly
            setWalkTo(entity, leapTarget, 1.8, 0);
            return;
        }

        double leapSpeed = PokecubeCore.getConfig().leapSpeedFactor;

        Vector3 dir = diff.normalize();
        if (dir.isNaN())
        {
            PokecubeAPI.LOGGER.error("Leap direction was NaN", new IllegalStateException());
            dir.clear();
        }
        if (dist < 9) leapSpeed *= dist/9;

        // Compute differences in velocities, and then account for that during
        // the leap.
        Vector3 v_a = new Vector3().setToVelocity(entity);
        Vector3 v_t = new Vector3();
        if (target != null) v_t.setToVelocity(target);
        // Compute velocity differential.
        Vector3 dv = v_a.subtractFrom(v_t);
        dir.scalarMultBy(leapSpeed);
        // Adjust for existing velocity differential.
        dir.subtractFrom(dv);

        double g = entity.getGravity() * 20;

        boolean airborne = pokemob.floats() || pokemob.flys();
        // Increase leap speed for airborne things, they have a bit more
        // friction while in the air.
        if (airborne) dir.scalarMultBy(1.1);
            // Otherwise, if it is on the ground, it should jump a bit if leaping
            // but not downwards
        else if (dir.y >= 0)
        {
            dir.y = Math.max(dir.y, 0.05);
            dir.y = Math.sqrt(2 * dir.y * g);
        }

        if (!airborne && !pokemob.onGround()) return;

        dh = Math.fma(dir.x, dir.x, dir.x * dir.z);
        // If too close, then put a minimum horizontal distance for the leap.
        if (dh > 0 && dh < 0.5)
        {
            dh = Math.sqrt(dh);
            dh = Math.max(dh, 0.1);
            dir.x *= 0.5 / dh;
            dir.z *= 0.5 / dh;
        }
        // Limit velocity to 1.3m/t, which is ~26m/s
        if(dir.magSq()>1.69)
        {
            dir.norm().scalarMultBy(1.3);
        }
        // Now apply the actual leap
        dir.addVelocities(entity);
        brain.setMemory(MemoryModules.TIMER_LEAP.get(), 1);
        // Then play leap sound
        new TaskBase.PlaySound(entity.level().dimension(), new Vector3().set(entity), this.getLeapSound(),
                SoundSource.HOSTILE, 1, 1).run(level);
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Can't move, no leap
        if (!TaskBase.canMove(pokemob)) return false;
        // Update the leap target here.
        var pos = BrainUtils.getLeapTarget(entity);
        // Leap may have been interupted, so clear this state if so.
        if (pos == null) pokemob.setCombatState(CombatStates.LEAPING, false);
        // Executing the leap, so return true.
        if (pokemob.getCombatState(CombatStates.LEAPING)) return true;
        // Leap if we have a target pos
        return pos != null;
    }

}
