package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.init.Config;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HerdTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static Map<MemoryModuleType<?>, MemoryStatus> _getMems()
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
    private final Vector3 herdMid = new Vector3();
    private final double herdDistSq;

    public HerdTask(double herdDist)
    {
        super(_getMems());
        herdDistSq = herdDist * herdDist;
    }

    @Override
    public void reset(Mob entityIn)
    {
        herdMid.clear();
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    protected void doFloatingIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(herdMid);
        final Vector3 temp = Vector3.getNextSurfacePoint(level, this.v, Vector3.secondAxisNeg, this.v.y);
        if (temp == null || !pokemob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        herdMid.y = temp.y + pokemob.getFloatHeight();
    }

    /**
     * Flying things will path to air, so long as not airborne, somethimes they will decide to path downwards, the
     * height they path to will be centered around players, to prevent them from all flying way up, or way down
     */
    protected void doFlyingIdle(ServerLevel level, IPokemob pokemob)
    {
        final boolean grounded = !pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        final boolean tamed =
                pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed) pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
        else if (!tamed) this.doGroundIdle(level, pokemob);
        final Player player = level.getNearestPlayer(pokemob.getEntity(), Config.Rules.despawnDistance(level));
        if (player != null)
        {
            final double diff = Math.abs(player.getY() - herdMid.y);
            if (diff > 5) herdMid.y = player.getY() + 5 * (1 - Math.random());
        }
    }

    /** Grounded things will path to surface points. */
    protected void doGroundIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(herdMid);
        this.v.set(Vector3.getNextSurfacePoint(level, this.v, Vector3.secondAxisNeg, this.v.y));
        if (this.v != null) herdMid.y = this.v.y;
    }

    /** Stationary things will not idle path at all */
    protected void doStationaryIdle(ServerLevel level, IPokemob pokemob)
    {
        this.herdMid.set(pokemob.getEntity());
    }

    /** Water things will not idle path out of water. */
    protected void doWaterIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(this.herdMid);
        if (level.getFluidState(this.v.getPos()).is(FluidTags.WATER))
        {
            this.herdMid.set(pokemob.getEntity());
        }
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob.getPokedexEntry().flys()) this.doFlyingIdle(level, pokemob);
        else if (pokemob.getPokedexEntry().floats()) this.doFloatingIdle(level, pokemob);
        else if (pokemob.getPokedexEntry().swims() && entity.isInWater()) this.doWaterIdle(level, pokemob);
        else if (pokemob.getPokedexEntry().isStationary) this.doStationaryIdle(level, pokemob);
        else this.doGroundIdle(level, pokemob);
        this.v1.set(entity);
        this.v.set(this.herdMid);
        if (this.v1.distToSq(this.v) <= 1) return;
        this.setWalkTo(entity, this.v, 1, 3);
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Not currently able to move.
        if (!TaskBase.canMove(pokemob)) return false;

        // Wander disabled, so don't run.
        if (!pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        Optional<List<LivingEntity>> herdOpt = entity.getBrain().getMemory(MemoryModules.HERD_MEMBERS.get());
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
                mid.set(entity).subtractFrom(herdMid);
                double distSq = mid.magSq();
                return distSq > herdDistSq;
            }
        }
        return false;
    }

}
