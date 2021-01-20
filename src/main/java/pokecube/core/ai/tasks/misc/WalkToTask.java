package pokecube.core.ai.tasks.misc;

import java.util.Optional;

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
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

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

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final MobEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        final WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(owner);
        if (pokemob != null && !TaskBase.canMove(pokemob)) return false;

        if (!this.hasReachedTarget(owner, walktarget) && this.isPathValid(owner, walktarget, worldIn.getGameTime()))
        {
            this.current_target = walktarget.getTarget().getBlockPos();
            return true;
        }
        else
        {
            brain.removeMemory(MemoryModuleType.WALK_TARGET);
            return false;
        }
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final MobEntity entityIn,
            final long gameTimeIn)
    {
        if (this.currentPath != null && this.current_target != null)
        {
            final Optional<WalkTarget> optional = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            final PathNavigator pathnavigator = entityIn.getNavigator();
            return !pathnavigator.noPath() && optional.isPresent() && !this.hasReachedTarget(entityIn, optional.get());
        }
        else return false;
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getNavigator().clearPath();
        entityIn.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        entityIn.getBrain().removeMemory(MemoryModuleType.PATH);
        this.currentPath = null;
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final MobEntity entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.PATH, this.currentPath);
        entityIn.getNavigator().setPath(this.currentPath, this.speed);
        this.time_till_next_check = worldIn.getRandom().nextInt(10);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        --this.time_till_next_check;
        if (this.time_till_next_check <= 0)
        {
            final Path path = owner.getNavigator().getPath();
            final Brain<?> brain = owner.getBrain();
            if (this.currentPath != path)
            {
                this.currentPath = path;
                brain.setMemory(MemoryModuleType.PATH, path);
            }

            if (path != null && this.current_target != null)
            {
                final WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
                if (walktarget.getTarget().getBlockPos().distanceSq(this.current_target) > 4.0D && this.isPathValid(
                        owner, walktarget, worldIn.getGameTime()))
                {
                    this.current_target = walktarget.getTarget().getBlockPos();
                    this.startExecuting(worldIn, owner, gameTime);
                }

            }
        }
    }

    private boolean isPathValid(final MobEntity mob, final WalkTarget target, final long gametime)
    {
        final BlockPos blockpos = target.getTarget().getBlockPos();
        this.currentPath = mob.getNavigator().pathfind(ImmutableSet.of(blockpos), 16, false, 0);
        this.speed = target.getSpeed();
        if (!this.hasReachedTarget(mob, target))
        {
            final Brain<?> brain = mob.getBrain();

            if (this.currentPath != null && !this.currentPath.reachesTarget() && this.currentPath
                    .getCurrentPathLength() == 1)
            {
                brain.removeMemory(MemoryModuleType.PATH);
                mob.getNavigator().clearPath();
                this.currentPath = null;
            }

            final boolean flag = this.currentPath != null && this.currentPath.reachesTarget();
            if (flag) brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, Optional.empty());
            else if (!brain.hasMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) brain.setMemory(
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, gametime);

            if (this.currentPath != null) return true;

            final int xz = 16;
            final int y = 10;
            final double scale = 2 * Math.PI / 2F;
            final Vector3d pos = new Vector3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            final Vector3d vec3d = RandomPositionGenerator.findRandomTargetTowardsScaled((CreatureEntity) mob, xz, y,
                    pos, scale);
            if (vec3d != null)
            {
                this.currentPath = mob.getNavigator().getPathToPos(vec3d.x, vec3d.y, vec3d.z, 0);
                return this.currentPath != null;
            }
        }

        return false;
    }

    private boolean hasReachedTarget(final MobEntity mob, final WalkTarget target)
    {
        return target.getTarget().getBlockPos().manhattanDistance(mob.getPosition()) <= target.getDistance();
    }
}
