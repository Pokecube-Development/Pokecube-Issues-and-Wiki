package pokecube.core.ai.brain;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.vector.Vector3d;
import pokecube.core.ai.pathing.PosWrapWrap;
import thut.api.maths.Vector3;

public class RootTask<E extends LivingEntity> extends Task<E>
{
    public static Map<MemoryModuleType<?>, MemoryModuleStatus> merge(
            final Map<MemoryModuleType<?>, MemoryModuleStatus> mems2,
            final Map<MemoryModuleType<?>, MemoryModuleStatus> mems3)
    {
        final Map<MemoryModuleType<?>, MemoryModuleStatus> ret = Maps.newHashMap();
        ret.putAll(mems2);
        ret.putAll(mems3);
        return ImmutableMap.copyOf(ret);
    }

    public static boolean doLoadThrottling = false;

    public static int runRate = 10;

    protected final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems;

    private final MemoryModuleType<?>[] neededModules;
    private final MemoryModuleStatus[]  neededStatus;

    protected E entity;

    protected boolean runWhileDead = false;

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems, final int duration,
            final int maxDuration)
    {
        super(neededMems, duration, maxDuration);
        this.entity = entity;
        this.neededMems = neededMems;
        final List<MemoryModuleType<?>> neededModules = Lists.newArrayList();
        final List<MemoryModuleStatus> neededStatus = Lists.newArrayList();
        neededModules.addAll(neededMems.keySet());
        for (final MemoryModuleType<?> mod : neededModules)
            neededStatus.add(neededMems.get(mod));
        this.neededModules = neededModules.toArray(new MemoryModuleType<?>[neededModules.size()]);
        this.neededStatus = neededStatus.toArray(new MemoryModuleStatus[neededModules.size()]);
    }

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems, final int duration)
    {
        this(entity, neededMems, duration, duration);
    }

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        this(entity, neededMems, 60);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems, final int duration)
    {
        this(null, neededMems, duration);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems, final int duration,
            final int maxDuration)
    {
        this(null, neededMems, duration, maxDuration);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        this(null, neededMems, 60);
    }

    protected boolean canTimeOut()
    {
        return false;
    }

    protected void setWalkTo(final Vector3 pos, final double speed, final int dist)
    {
        this.setWalkTo(pos.toVec3d(), speed, dist);
    }

    protected void setWalkTo(final Vector3d pos, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final BlockPos pos, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final Entity mobIn, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(new EntityPosWrapper(mobIn, false), (float) speed, dist));
    }

    protected void setWalkTo(WalkTarget target)
    {
        if (!(target.getTarget() instanceof EntityPosWrapper) && target != null)
        {
            final boolean inRange = target.getTarget().currentPosition().closerThan(this.entity.position(), target
                    .getCloseEnoughDist());
            if (inRange) return;
        }
        // In this case, we want to wrap it to include throttling information.
        if (target != null)
        {
            final PosWrapWrap wrapped = new PosWrapWrap(target.getTarget(), this.loadThrottle());
            target = new WalkTarget(wrapped, target.getSpeedModifier(), target.getCloseEnoughDist());
        }
        this.entity.getBrain().setMemory(MemoryModules.WALK_TARGET, target);
    }

    protected final boolean isPaused(final E mobIn)
    {
        if (!this.loadThrottle() || !RootTask.doLoadThrottling) return false;
        final Random rng = new Random(mobIn.getUUID().hashCode());
        final int tick = rng.nextInt(RootTask.runRate);
        return mobIn.tickCount % RootTask.runRate != tick;
    }

    public boolean loadThrottle()
    {
        return false;
    }

    @Override
    protected boolean timedOut(final long gameTime)
    {
        if (!this.canTimeOut()) return false;
        return super.timedOut(gameTime);
    }

    @Override
    public boolean hasRequiredMemories(final E mobIn)
    {
        this.entity = mobIn;
        // If we are paused, return early here.
        if (this.isPaused(mobIn)) return false;
        final Brain<?> brain = mobIn.getBrain();
        for (int i = 0; i < this.neededStatus.length; i++)
            if (!brain.checkMemory(this.neededModules[i], this.neededStatus[i])) return false;
        // Dead mobs don't have AI!
        if (!this.runWhileDead && !mobIn.isAlive()) return false;
        // Otherwise continue;
        return true;
    }
}
