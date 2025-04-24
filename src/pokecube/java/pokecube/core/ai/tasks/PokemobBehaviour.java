package pokecube.core.ai.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.MemoryModuleTypes;
import thut.api.entity.ai.PosWrapWrap;
import thut.api.maths.Vector3;

import java.util.Map;

public abstract class PokemobBehaviour extends Behavior<Mob> implements ITask
{
    public PokemobBehaviour(Map<MemoryModuleType<?>, MemoryStatus> entryCondition)
    {
        super(entryCondition, Integer.MAX_VALUE);
    }

    @Override
    public void run(ServerLevel level, Mob owner)
    {

    }

    @Override
    protected void tick(final ServerLevel level, final Mob owner, final long gameTime)
    {
        this.run(level, owner);
        this.runTick(level, owner);
        this.finish(level, owner);
    }

    @Override
    public void finish(ServerLevel level, Mob owner)
    {

    }

    int priority = 0;

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public void reset(Mob entityIn)
    {

    }

    @Override
    protected void stop(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        this.reset(entityIn);
    }

    @Override
    protected boolean canStillUse(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        return this.shouldRun(entityIn);
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel level, final Mob owner)
    {
        return this.shouldRun(owner);
    }

    @Override
    public IAIRunnable setPriority(final int prior)
    {
        this.priority = prior;
        return this;
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        return false;
    }

    protected void setWalkTo(Mob user, Vector3 pos, double speed, int dist)
    {
        this.setWalkTo(user, pos.toVec3d(), speed, dist);
    }

    protected void setWalkTo(Mob user, Vec3 pos, double speed, int dist)
    {
        this.setWalkTo(user, new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(Mob user, BlockPos pos, double speed, int dist)
    {
        this.setWalkTo(user, new WalkTarget(pos, (float) speed, dist));
    }

    protected void setWalkTo(Mob user, Entity mobIn, double speed, int dist)
    {
        this.setWalkTo(user, new WalkTarget(new EntityTracker(mobIn, false), (float) speed, dist));
    }

    protected void setWalkTo(Mob user, WalkTarget target)
    {
        if (!(target.getTarget() instanceof EntityTracker) && target != null)
        {
            final boolean inRange = target.getTarget().currentPosition()
                    .closerThan(user.position(), target.getCloseEnoughDist());
            if (inRange) return;
        }
        // In this case, we want to wrap it to include throttling information.
        if (target != null)
        {
            final PosWrapWrap wrapped = new PosWrapWrap(target.getTarget(), false);
            target = new WalkTarget(wrapped, target.getSpeedModifier(), target.getCloseEnoughDist());
        }
        user.getBrain().setMemory(MemoryModuleTypes.WALK_TARGET, target);
    }
}
