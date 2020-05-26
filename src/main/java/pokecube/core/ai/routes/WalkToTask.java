package pokecube.core.ai.routes;

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
import net.minecraft.util.math.Vec3d;
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
    private BlockPos field_220489_b;
    private float    speed;
    private int      field_220491_d;

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

        if (!this.hasReachedTarget(owner, walktarget) && this.func_220487_a(owner, walktarget, worldIn.getGameTime()))
        {
            this.field_220489_b = walktarget.getTarget().getBlockPos();
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
        if (this.currentPath != null && this.field_220489_b != null)
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
        this.field_220491_d = worldIn.getRandom().nextInt(10);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final MobEntity owner, final long gameTime)
    {
        --this.field_220491_d;
        if (this.field_220491_d <= 0)
        {
            final Path path = owner.getNavigator().getPath();
            final Brain<?> brain = owner.getBrain();
            if (this.currentPath != path)
            {
                this.currentPath = path;
                brain.setMemory(MemoryModuleType.PATH, path);
            }

            if (path != null && this.field_220489_b != null)
            {
                final WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
                if (walktarget.getTarget().getBlockPos().distanceSq(this.field_220489_b) > 4.0D && this.func_220487_a(
                        owner, walktarget, worldIn.getGameTime()))
                {
                    this.field_220489_b = walktarget.getTarget().getBlockPos();
                    this.startExecuting(worldIn, owner, gameTime);
                }

            }
        }
    }

    private boolean func_220487_a(final MobEntity mob, final WalkTarget target, final long gametime)
    {
        final BlockPos blockpos = target.getTarget().getBlockPos();
        this.currentPath = mob.getNavigator().func_225464_a(ImmutableSet.of(blockpos), 16, false, 0);
        this.speed = target.getSpeed();
        if (!this.hasReachedTarget(mob, target))
        {
            final Brain<?> brain = mob.getBrain();
            final boolean flag = this.currentPath != null && this.currentPath.reachesTarget();
            if (flag) brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, Optional.empty());
            else if (!brain.hasMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) brain.setMemory(
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, gametime);

            if (this.currentPath != null) return true;

            final Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards((CreatureEntity) mob, 10, 7,
                    new Vec3d(blockpos));
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
        return target.getTarget().getBlockPos().manhattanDistance(new BlockPos(mob)) <= target.getDistance();
    }
}
