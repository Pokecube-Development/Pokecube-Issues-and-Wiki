package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;

public class IdleRestTask extends BaseIdleTask
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
        }
        return _MEMS;
    }

    private int restTimer = 0;
    private BlockPos restPos;
    PokedexEntry entry;

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public IdleRestTask(IPokemob pokemob)
    {
        super(pokemob, _getMems());
        restTimer = this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER);
    }

    @Override
    public void reset()
    {}

    /** Grounded things will path to surface points. */
    protected BlockPos doGroundIdle()
    {
        Vector3 v2 = Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg,
                this.v.y - entity.level.getMinBuildHeight());
        if (v2 == null) return null;
        if (!entity.level.getFluidState(v2.getPos()).isEmpty()) return null;
        return v2.getPos();
    }

    /** Water things will not idle path out of water. */
    protected BlockPos doWaterIdle()
    {
        if (!this.world.getFluidState(this.v.getPos()).is(FluidTags.WATER)) return null;
        return v.getPos();
    }

    protected BlockPos getLocation()
    {
        final boolean tameFactor = this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        if (this.entry != pokemob.getPokedexEntry()) this.entry = pokemob.getPokedexEntry();
        int distance = tameFactor ? PokecubeCore.getConfig().idleMaxPathTame : PokecubeCore.getConfig().idleMaxPathWild;
        boolean goHome = false;
        if (!tameFactor)
        {
            if (this.pokemob.getHome() == null)
            {
                this.v1.set(this.entity);
                this.pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
            }
            distance = (int) Math.min(distance, this.pokemob.getHomeDistance());
            this.v.set(this.pokemob.getHome());
            if (this.entity.blockPosition().distSqr(this.pokemob.getHome()) > this.pokemob.getHomeDistance()
                    * this.pokemob.getHomeDistance() * 0.75 || pokemob.isOnGround())
                goHome = true;
        }
        else
        {
            LivingEntity setTo = this.entity;
            if (this.pokemob.getOwner() != null) setTo = this.pokemob.getOwner();
            this.v.set(setTo);
        }
        if (!goHome)
        {
            double minDy = 0;
            double maxDy = 2;

            boolean verticalMotion = entry.flys() || entry.floats() || (entry.swims() && entity.isInWater());
            if (verticalMotion) minDy = -2;
            final Vector3 v = IdleWalkTask.getRandomPointNear(this.world, this.pokemob, this.v, distance, minDy, maxDy);
            if (v == null) return null;
            double diff = Math.max(this.entry.length * this.pokemob.getSize(),
                    this.entry.width * this.pokemob.getSize());
            diff = Math.max(2, diff);
            if (this.v1.distToSq(v) < diff) return null;
        }
        BlockPos pos = this.v.getPos();
        if (this.entry.swims() && this.entity.isInWater()) pos = this.doWaterIdle();
        else pos = this.doGroundIdle();

        return pos;
    }

    @Override
    public void run()
    {
        restTimer--;
        if (restTimer > 0) return;
        boolean sitting = pokemob.getLogicState(LogicStates.SITTING);
        if (sitting)
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            reset();
            restTimer = 20 + this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER)
                    + this.entity.getRandom().nextInt(100);
            restTimer *= 10;
        }
        else
        {
            if (restTimer < -400)
            {
                restPos = null;
                restTimer = 20 + this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER)
                        + this.entity.getRandom().nextInt(100);
            }
            if (restPos == null) restPos = getLocation();
            else
            {
                v1.set(entity);
                v.set(restPos);
                if (v.distToSq(v1) > 3)
                {
                    var path = entity.getNavigation().getPath();
                    if (path == null || path.isDone())
                    {
                        this.setWalkTo(restPos, 1, 0);
                    }
                }
                else
                {
                    v.set(entity);
                    BlockPos pos = v.getPos();
                    if (this.entry.swims() && this.entity.isInWater()) pos = this.doWaterIdle();
                    else pos = this.doGroundIdle();
                    if (pos != null)
                    {
                        pokemob.setLogicState(LogicStates.SITTING, true);
                        restTimer = 20 + this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER)
                                + this.entity.getRandom().nextInt(100);
                    }
                    restPos = null;
                }
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Don't run in combat
        if (this.pokemob.getCombatState(CombatStates.BATTLING)) return false;

        // Tamed mobs will only wander when set to STAYING mode
        if (this.pokemob.getGeneralState(GeneralStates.TAMED))
            return this.pokemob.getGeneralState(GeneralStates.STAYING);
        return true;
    }

}
