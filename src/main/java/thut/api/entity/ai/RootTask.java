package thut.api.entity.ai;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import thut.api.maths.Vector3;

public class RootTask<E extends LivingEntity> extends Behavior<E>
{
    public static Map<MemoryModuleType<?>, MemoryStatus> merge(final Map<MemoryModuleType<?>, MemoryStatus> mems2,
            final Map<MemoryModuleType<?>, MemoryStatus> mems3)
    {
        final Map<MemoryModuleType<?>, MemoryStatus> ret = Maps.newHashMap();
        ret.putAll(mems2);
        ret.putAll(mems3);
        return ImmutableMap.copyOf(ret);
    }

    public static record MemoryRequriment(MemoryModuleType<?> memory, MemoryStatus status)
    {
    }

    public static boolean doLoadThrottling = false;

    public static int runRate = 10;

    public static Comparator<MemoryModuleType<?>> _NO_ORDER = (a, b) -> 0;

    protected final Map<MemoryModuleType<?>, MemoryStatus> neededMems;

    private final MemoryRequriment[] memoryRequirements;

    protected E entity;

    protected boolean runWhileDead = false;
    // This is a minimum run rate when load balancing is enabled, or for tasks
    // that autothrottle, 1 means every tick.
    protected int _run_rate = 10;

    protected boolean tempRun = false;
    protected boolean tempCont = false;

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryStatus> neededMems, final int duration,
            final int maxDuration)
    {
        super(neededMems, duration, maxDuration);
        this.entity = entity;
        this.neededMems = neededMems;
        memoryRequirements = new MemoryRequriment[neededMems.size()];
        initMems(memoryRequirements, neededMems);
    }

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryStatus> neededMems, final int duration)
    {
        this(entity, neededMems, duration, duration);
    }

    public RootTask(final E entity, final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        this(entity, neededMems, 60);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMems, final int duration)
    {
        this(null, neededMems, duration);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMems, final int duration, final int maxDuration)
    {
        this(null, neededMems, duration, maxDuration);
    }

    public RootTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        this(null, neededMems, 60);
    }

    protected Comparator<MemoryModuleType<?>> getCheckOrder()
    {
        return _NO_ORDER;
    }

    protected void initMems(MemoryRequriment[] reqs, final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        final List<MemoryModuleType<?>> neededModules = Lists.newArrayList();
        neededModules.addAll(neededMems.keySet());
        neededModules.sort(getCheckOrder());
        for (int i = 0; i < neededModules.size(); i++)
        {
            var mem = neededModules.get(i);
            var status = neededMems.get(mem);
            reqs[i] = new MemoryRequriment(mem, status);
        }
    }

    protected boolean runTick(final E mobIn)
    {
        int rate = Math.max(runRate, _run_rate);
        return mobIn.tickCount % rate == mobIn.id % rate;
    }

    protected void setWalkTo(final Vector3 pos, final double speed, final int dist)
    {
        this.setWalkTo(pos.toVec3d(), speed, dist);
    }

    protected void setWalkTo(final Vec3 pos, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final BlockPos pos, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(final Entity mobIn, final double speed, final int dist)
    {
        this.setWalkTo(new WalkTarget(new EntityTracker(mobIn, false), (float) speed, dist));
    }

    protected void setWalkTo(WalkTarget target)
    {
        if (!(target.getTarget() instanceof EntityTracker) && target != null)
        {
            final boolean inRange = target.getTarget().currentPosition().closerThan(this.entity.position(),
                    target.getCloseEnoughDist());
            if (inRange) return;
        }
        // In this case, we want to wrap it to include throttling information.
        if (target != null)
        {
            final PosWrapWrap wrapped = new PosWrapWrap(target.getTarget(), this.loadThrottle());
            target = new WalkTarget(wrapped, target.getSpeedModifier(), target.getCloseEnoughDist());
        }
        this.entity.getBrain().setMemory(MemoryModuleTypes.WALK_TARGET, target);
    }

    protected final boolean isPaused(final E mobIn)
    {
        if (!this.loadThrottle() || !RootTask.doLoadThrottling) return false;
        return !runTick(mobIn);
    }

    @Override
    protected boolean timedOut(final long gameTime)
    {
        if (!this.canTimeOut()) return false;
        return super.timedOut(gameTime);
    }

    public boolean loadThrottle()
    {
        return false;
    }

    protected boolean canTimeOut()
    {
        return false;
    }

    protected boolean simpleRun()
    {
        return false;
    }

    protected boolean shouldNotRun(final E mobIn)
    {
        return false;
    }

    @Override
    public boolean hasRequiredMemories(final E mobIn)
    {
        // Default to true, everything below will set false.
        boolean ret = true;
        check:
        {
            this.entity = mobIn;

            // if we are a "simple" check, we only run every so often, as we
            // will run everything immediately.
            if (this.simpleRun() && !this.runTick(mobIn))
            {
                ret = false;
                break check;
            }

            // This means it has a simpler check for if not to run, so we do
            // that instead of below.
            if (this.shouldNotRun(mobIn))
            {
                ret = false;
                break check;
            }

            // If we are paused, return early here.
            if (this.isPaused(mobIn))
            {
                ret = this.tempCont;
                break check;
            }

            final Brain<?> brain = mobIn.getBrain();
            // Check memories, this loop takes 50% of the time to run as the
            // vanilla way, so we do it like this.
            for (var mem : this.memoryRequirements)
            {
                if (!brain.checkMemory(mem.memory(), mem.status()))
                {
                    ret = false;
                    break check;
                }
            }
            // Dead mobs don't have AI!
            if (!this.runWhileDead && !mobIn.isAlive())
            {
                ret = false;
                break check;
            }
        }
        this.tempCont = ret;
        return ret;
    }
}
