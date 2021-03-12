package pokecube.core.ai.tasks.misc;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.pathing.PosWrapWrap;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.world.IPathHelper;
import pokecube.core.world.WorldTickManager;

public class WalkToTask extends RootTask<MobEntity>
{

    @Nullable
    private Path     currentPath;
    @Nullable
    private BlockPos current_target;
    private float    speed;
    private int      time_till_next_check;

    public WalkToTask(final int duration)
    {
        super(ImmutableMap.of(MemoryModuleType.PATH, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET,
                MemoryModuleStatus.VALUE_PRESENT), duration);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    private boolean tryPause(final MobEntity owner)
    {
        final Random rng = new Random(owner.getUUID().hashCode());
        final int tick = rng.nextInt(RootTask.runRate);
        return owner.tickCount % RootTask.runRate != tick;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerWorld worldIn, final MobEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        final WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(owner);
        if (pokemob != null && !TaskBase.canMove(pokemob)) return false;

        if (RootTask.doLoadThrottling)
        {
            final boolean pauseable = walktarget.getTarget() instanceof PosWrapWrap;
            final boolean pauseTick = this.tryPause(owner);
            if (pauseable && pauseTick)
            {
                final PosWrapWrap wrapped = (PosWrapWrap) walktarget.getTarget();
                if (wrapped.canThrottle) return false;
            }
        }

        if (!this.hasReachedTarget(owner, walktarget) && this.isPathValid(owner, walktarget, worldIn.getGameTime()))
        {
            this.current_target = walktarget.getTarget().currentBlockPosition();
            return true;
        }
        else
        {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            return false;
        }
    }

    @Override
    protected boolean canStillUse(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        if (this.currentPath != null && this.current_target != null)
        {
            final Optional<WalkTarget> optional = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            final PathNavigator pathnavigator = entityIn.getNavigation();
            return !pathnavigator.isDone() && optional.isPresent() && !this.hasReachedTarget(entityIn, optional.get());
        }
        else return false;
    }

    @Override
    protected void stop(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getNavigation().stop();
        entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entityIn.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.currentPath = null;
    }

    @Override
    protected void start(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.PATH, this.currentPath);
        entityIn.getNavigation().moveTo(this.currentPath, this.speed);
        this.time_till_next_check = worldIn.getRandom().nextInt(10);
    }

    @Override
    protected void tick(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        --this.time_till_next_check;
        if (this.time_till_next_check <= 0)
        {
            final Path path = owner.getNavigation().getPath();
            final Brain<?> brain = owner.getBrain();
            if (this.currentPath != path)
            {
                this.currentPath = path;
                brain.setMemory(MemoryModuleType.PATH, path);
            }

            if (path != null && this.current_target != null)
            {
                final WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
                if (walktarget.getTarget().currentBlockPosition().distSqr(this.current_target) > 4.0D && this.isPathValid(
                        owner, walktarget, worldIn.getGameTime()))
                {
                    this.current_target = walktarget.getTarget().currentBlockPosition();
                    this.start(worldIn, owner, gameTime);
                }

            }
        }
    }

    private boolean isPathValid(final MobEntity mob, final WalkTarget target, final long gametime)
    {
        final BlockPos blockpos = target.getTarget().currentBlockPosition();

        final World world = mob.getCommandSenderWorld();

        if (!world.isAreaLoaded(blockpos, 8)) return false;
        if (!world.isAreaLoaded(mob.blockPosition(), 8)) return false;

        final Brain<?> brain = mob.getBrain();
        pathing:
        if (this.currentPath == null)
        {
            final PathNavigator navi = mob.getNavigation();
            if (navi.isInProgress())
            {
                this.currentPath = navi.getPath();
                brain.setMemory(MemoryModuleType.PATH, this.currentPath);
                break pathing;
            }

            final List<IPathHelper> pathers = WorldTickManager.pathHelpers.get(mob.getCommandSenderWorld().dimension());
            for (final IPathHelper h : pathers)
            {
                final boolean valid = h.shouldHelpPath(mob, target);
                if (valid)
                {
                    this.currentPath = h.getPath(mob, target);
                    if (this.currentPath != null) break pathing;
                }
            }
            this.currentPath = mob.getNavigation().createPath(ImmutableSet.of(blockpos), 16, false, 0);
            final double dist = target.getTarget().currentPosition().distanceTo(mob.position());
            if (dist < 3 && (this.currentPath == null || !this.currentPath.canReach()))
            {
                mob.getNavigation().stop();
                mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                mob.getBrain().eraseMemory(MemoryModuleType.PATH);
                this.currentPath = null;
                return false;
            }
        }
        this.speed = target.getSpeedModifier();
        final boolean atTarget = this.hasReachedTarget(mob, target);

        if (!atTarget)
        {

            if (this.currentPath != null && !this.currentPath.canReach() && this.currentPath
                    .getNodeCount() == 1)
            {
                brain.eraseMemory(MemoryModuleType.PATH);
                mob.getNavigation().stop();
                this.currentPath = null;
            }

            final boolean flag = this.currentPath != null && this.currentPath.canReach();
            if (flag) brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, Optional.empty());
            else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) brain.setMemory(
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, gametime);

            if (this.currentPath != null) return true;

            final int xz = 16;
            final int y = 10;
            final double scale = 2 * Math.PI / 2F;
            final Vector3d pos = new Vector3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            final Vector3d vec3d = RandomPositionGenerator.getPosTowards((CreatureEntity) mob, xz, y,
                    pos, scale);
            if (vec3d != null)
            {
                this.currentPath = mob.getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, 0);
                return this.currentPath != null;
            }
        }
        return false;
    }

    private boolean hasReachedTarget(final MobEntity mob, final WalkTarget target)
    {
        return target.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= target.getCloseEnoughDist();
    }
}
