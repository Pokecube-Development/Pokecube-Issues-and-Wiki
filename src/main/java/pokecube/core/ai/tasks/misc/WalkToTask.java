package pokecube.core.ai.tasks.misc;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.ai.PosWrapWrap;
import thut.api.entity.ai.RootTask;
import thut.api.world.IPathHelper;
import thut.api.world.WorldTickManager;

public class WalkToTask extends RootTask<Mob>
{

    @Nullable
    private Path     currentPath;
    @Nullable
    private BlockPos current_target;
    private float    speed;
    private int      time_till_next_check;

    public WalkToTask(final int duration)
    {
        super(ImmutableMap.of(MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_PRESENT), duration);
    }

    @Override
    protected boolean canTimeOut()
    {
        return true;
    }

    private boolean tryPause(final Mob owner)
    {
        final Random rng = new Random(owner.getUUID().hashCode());
        final int tick = rng.nextInt(RootTask.runRate);
        return owner.tickCount % RootTask.runRate != tick;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final Mob owner)
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
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        if (this.currentPath != null && this.current_target != null)
        {
            final Optional<WalkTarget> optional = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            final PathNavigation pathnavigator = entityIn.getNavigation();
            return !pathnavigator.isDone() && optional.isPresent() && !this.hasReachedTarget(entityIn, optional.get());
        }
        else return false;
    }

    @Override
    protected void stop(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        entityIn.getNavigation().stop();
        entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entityIn.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.currentPath = null;
    }

    @Override
    protected void start(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.PATH, this.currentPath);
        entityIn.getNavigation().moveTo(this.currentPath, this.speed);
        this.time_till_next_check = worldIn.getRandom().nextInt(10);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final Mob owner, final long gameTime)
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
                if (walktarget.getTarget().currentBlockPosition().distSqr(this.current_target) > 4.0D && this
                        .isPathValid(owner, walktarget, worldIn.getGameTime()))
                {
                    this.current_target = walktarget.getTarget().currentBlockPosition();
                    this.start(worldIn, owner, gameTime);
                }

            }
        }
    }

    private boolean isPathValid(final Mob mob, final WalkTarget target, final long gametime)
    {
        final BlockPos blockpos = target.getTarget().currentBlockPosition();

        final Brain<?> brain = mob.getBrain();
        pathing:
        if (this.currentPath == null)
        {
            final PathNavigation navi = mob.getNavigation();
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

            if (this.currentPath != null && !this.currentPath.canReach() && this.currentPath.getNodeCount() == 1)
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
//            final double scale = 2 * Math.PI / 2F;
            final Vec3 pos = new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            final Vec3 vec3d = LandRandomPos.getPosTowards((PathfinderMob) mob, xz, y, pos);
            if (vec3d != null)
            {
                this.currentPath = mob.getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, 0);
                return this.currentPath != null;
            }
        }
        return false;
    }

    private boolean hasReachedTarget(final Mob mob, final WalkTarget target)
    {
        return target.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= target
                .getCloseEnoughDist();
    }
}
