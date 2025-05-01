package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.Map;

public class IdleRestTask extends BaseIdleTask
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
        }
        return _MEMS;
    }

    private int restTimer;
    private BlockPos restPos;

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public IdleRestTask()
    {
        super(_getMems());
        restTimer = ThutCore.newRandom().nextInt(IdleWalkTask.IDLETIMER);
    }

    @Override
    public void reset(Mob entityIn)
    {}

    /** Grounded things will path to surface points. */
    protected BlockPos doGroundIdle(ServerLevel level)
    {
        Vector3 v2 = Vector3.getNextSurfacePoint(level, this.v, Vector3.secondAxisNeg,
                this.v.y - level.getMinBuildHeight());
        if (v2 == null) return null;
        if (!level.getFluidState(v2.getPos()).isEmpty()) return null;
        return v2.getPos();
    }

    /** Water things will not idle path out of water. */
    protected BlockPos doWaterIdle(ServerLevel level)
    {
        if (!level.getFluidState(this.v.getPos()).is(FluidTags.WATER)) return null;
        return v.getPos();
    }

    protected BlockPos getLocation(ServerLevel level, IPokemob pokemob)
    {
        final boolean tameFactor =
                pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING);
        var entry = pokemob.getPokedexEntry();
        int distance = tameFactor ? PokecubeCore.getConfig().idleMaxPathTame : PokecubeCore.getConfig().idleMaxPathWild;
        boolean goHome = false;
        var entity = pokemob.getEntity();
        if (!tameFactor)
        {
            if (pokemob.getHome() == null)
            {
                this.v1.set(entity);
                pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
            }
            distance = (int) Math.min(distance, pokemob.getHomeDistance());
            this.v.set(pokemob.getHome());
            if (entity.blockPosition().distSqr(pokemob.getHome())
                    > pokemob.getHomeDistance() * pokemob.getHomeDistance() * 0.75 || pokemob.onGround()) goHome = true;
        }
        else
        {
            LivingEntity setTo = entity;
            if (pokemob.getOwner() != null) setTo = pokemob.getOwner();
            this.v.set(setTo);
        }
        if (!goHome)
        {
            double minDy = 0;
            double maxDy = 2;

            boolean verticalMotion = entry.flys() || entry.floats() || (entry.swims() && entity.isInWater());
            if (verticalMotion) minDy = -2;
            final Vector3 v = IdleWalkTask.getRandomPointNear(level, pokemob, this.v, distance, minDy, maxDy);
            if (v == null) return null;
            double diff = Math.max(entry.length * pokemob.getEntity().getScale(),
                    entry.width * pokemob.getEntity().getScale());
            diff = Math.max(2, diff);
            if (this.v1.distToSq(v) < diff) return null;
        }
        BlockPos pos;
        if (entry.swims() && entity.isInWater()) pos = this.doWaterIdle(level);
        else pos = this.doGroundIdle(level);

        return pos;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        restTimer--;
        if (restTimer > 0) return;
        boolean sitting = pokemob.getLogicState(LogicStates.SITTING);
        if (sitting)
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            reset(entity);
            restTimer = 20 + entity.getRandom().nextInt(IdleWalkTask.IDLETIMER) + entity.getRandom().nextInt(100);
            restTimer *= 10;
        }
        else
        {
            if (restTimer < -400)
            {
                restPos = null;
                restTimer = 20 + entity.getRandom().nextInt(IdleWalkTask.IDLETIMER) + entity.getRandom().nextInt(100);
            }
            if (restPos == null) restPos = getLocation(level, pokemob);
            else
            {
                v1.set(entity);
                v.set(restPos);
                if (v.distToSq(v1) > 3)
                {
                    var path = entity.getNavigation().getPath();
                    if (path == null || path.isDone())
                    {
                        this.setWalkTo(entity, restPos, 1, 0);
                    }
                }
                else
                {
                    v.set(entity);
                    BlockPos pos;
                    if (pokemob.getPokedexEntry().swims() && entity.isInWater()) pos = this.doWaterIdle(level);
                    else pos = this.doGroundIdle(level);
                    if (pos != null)
                    {
                        pokemob.setLogicState(LogicStates.SITTING, true);
                        restTimer = 20 + entity.getRandom().nextInt(IdleWalkTask.IDLETIMER) + entity.getRandom()
                                .nextInt(100);
                    }
                    restPos = null;
                }
            }
        }
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Wander disabled, so don't run.
        if (!pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Don't run in combat
        if (pokemob.getCombatState(CombatStates.BATTLING)) return false;

        // Tamed mobs will only wander when set to STAYING mode
        if (pokemob.getGeneralState(GeneralStates.TAMED)) return pokemob.getGeneralState(GeneralStates.STAYING);
        return true;
    }

}
