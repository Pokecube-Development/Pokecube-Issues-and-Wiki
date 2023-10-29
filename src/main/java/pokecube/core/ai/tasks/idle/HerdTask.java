package pokecube.core.ai.tasks.idle;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.init.Config;
import thut.api.maths.Vector3;

public class HerdTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static final Map<MemoryModuleType<?>, MemoryStatus> _getMems()
    {
        if (_MEMS.isEmpty())
        {
            // Dont run if have a walk target
            _MEMS.put(MemoryModules.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
            // Don't run if have a target location for moves
            _MEMS.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.VALUE_ABSENT);
            // Don't run if we have a path
            _MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
            // Only run if we have a herd
            _MEMS.put(MemoryModules.HERD_MEMBERS.get(), MemoryStatus.VALUE_PRESENT);
            // Don't run if guarding an egg
            _MEMS.put(MemoryModules.EGG.get(), MemoryStatus.VALUE_ABSENT);
        }
        return _MEMS;
    }

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();
    private Vector3 herdMid = new Vector3();
    private double herdDistSq;

    public HerdTask(final IPokemob pokemob, double herdDist)
    {
        super(pokemob, _getMems());
        herdDistSq = herdDist * herdDist;
    }

    @Override
    public void reset()
    {
        herdMid.clear();
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    protected void doFloatingIdle()
    {
        this.v.set(herdMid);
        final Vector3 temp = Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y);
        if (temp == null || !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        herdMid.y = temp.y + this.pokemob.getFloatHeight();
    }

    /**
     * Flying things will path to air, so long as not airborne, somethimes they
     * will decide to path downwards, the height they path to will be centered
     * around players, to prevent them from all flying way up, or way down
     */
    protected void doFlyingIdle()
    {
        final boolean grounded = !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        final boolean tamed = this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed) this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
        else if (!tamed) this.doGroundIdle();
        final Player player = this.world.getNearestPlayer(this.entity, Config.Rules.despawnDistance(world));
        if (player != null)
        {
            final double diff = Math.abs(player.getY() - herdMid.y);
            if (diff > 5) herdMid.y = player.getY() + 5 * (1 - Math.random());
        }
    }

    /** Grounded things will path to surface points. */
    protected void doGroundIdle()
    {
        this.v.set(herdMid);
        this.v.set(Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y));
        if (this.v != null) herdMid.y = this.v.y;
    }

    /** Stationary things will not idle path at all */
    protected void doStationaryIdle()
    {
        this.herdMid.set(this.entity);
    }

    /** Water things will not idle path out of water. */
    protected void doWaterIdle()
    {
        this.v.set(this.herdMid);
        if (this.world.getFluidState(this.v.getPos()).is(FluidTags.WATER))
        {
            this.herdMid.set(this.entity);
        }
    }

    @Override
    public void run()
    {
        if (this.pokemob.getPokedexEntry().flys()) this.doFlyingIdle();
        else if (this.pokemob.getPokedexEntry().floats()) this.doFloatingIdle();
        else if (this.pokemob.getPokedexEntry().swims() && this.entity.isInWater()) this.doWaterIdle();
        else if (this.pokemob.getPokedexEntry().isStationary) this.doStationaryIdle();
        else this.doGroundIdle();
        this.v1.set(this.entity);
        this.v.set(this.herdMid);
        if (this.v1.distToSq(this.v) <= 1) return;
        this.setWalkTo(this.v, 1, 3);
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        // Not currently able to move.
        if (!TaskBase.canMove(this.pokemob)) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        Optional<List<LivingEntity>> herdOpt = this.entity.getBrain().getMemory(MemoryModules.HERD_MEMBERS.get());
        if (herdOpt.isPresent())
        {
            var herd = herdOpt.get();
            if (!herd.isEmpty())
            {
                Vector3 mid = new Vector3();
                for (var e : herd)
                {
                    mid.addTo(e.getX(), e.getY(), e.getZ());
                }
                mid.scalarMultBy(1.0 / herd.size());
                herdMid.set(mid);
                mid.set(this.entity).subtractFrom(herdMid);
                double distSq = mid.magSq();
                return distSq > herdDistSq;
            }
        }
        return false;
    }

}
