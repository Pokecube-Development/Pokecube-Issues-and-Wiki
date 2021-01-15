package pokecube.core.ai.routes;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{

    public static class GuardTask implements IGuardTask
    {
        private static final UUID UID = UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0");

        private AttributeModifier executingGuardTask = null;

        private Vector3d lastPos;

        int path_fails = 0;

        private int lastPosCounter = -1;

        private BlockPos pos;

        private float roamDistance = 2;

        private TimePeriod activeTime = new TimePeriod(0, 0);

        public GuardTask()
        {
            this.executingGuardTask = new AttributeModifier(GuardTask.UID, "pokecube:guard_task", 1, Operation.ADDITION);
        }

        @Override
        public void continueTask(final MobEntity entity)
        {
            final Vector3d newPos = entity.getPositionVec();
            if (this.getPos().withinDistance(newPos, this.getRoamDistance())) return;

            final double speed = entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            this.path(entity, speed);

            final double ds2 = this.lastPos == null ? 1 : newPos.squareDistanceTo(this.lastPos);

            final boolean samePos = ds2 < 0.01;
            if (samePos)
            {
                if (this.lastPosCounter-- >= 0) this.pathFail(entity);
                else this.lastPosCounter = 10;
            }
            else
            {
                this.lastPosCounter = 10;
                this.path_fails = 0;
                this.lastPos = newPos;
            }
        }

        @Override
        public void endTask(final MobEntity entity)
        {
            entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            this.path_fails = 0;
        }

        @Override
        public TimePeriod getActiveTime()
        {
            return this.activeTime;
        }

        @Override
        public BlockPos getPos()
        {
            return this.pos;
        }

        @Override
        public float getRoamDistance()
        {
            return this.roamDistance;
        }

        @Override
        public void setActiveTime(final TimePeriod active)
        {
            this.activeTime = active;
            if (active == null) this.activeTime = new TimePeriod(0, 0);
        }

        @Override
        public void setPos(final BlockPos pos)
        {
            this.pos = pos;
        }

        @Override
        public void setRoamDistance(final float roam)
        {
            this.roamDistance = roam;
        }

        @Override
        public void startTask(final MobEntity entity)
        {
            entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            entity.getAttribute(Attributes.FOLLOW_RANGE).applyNonPersistentModifier(this.executingGuardTask);
            final double speed = entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            if (!this.path(entity, speed)) this.pathFail(entity);
        }

        private void pathFail(final MobEntity entity)
        {
            if (this.path_fails++ > 100)
            {
                final ServerWorld world = (ServerWorld) entity.getEntityWorld();

                final BlockPos old = entity.getPosition();
                // Only path fail if we actually are nearby.
                if (old.distanceSq(this.getPos()) > 128 * 128) return;
                // Ensure chunk exists
                world.getChunk(this.getPos());
                final BlockState state = world.getBlockState(this.getPos());
                final VoxelShape shape = state.getCollisionShape(world, this.getPos());
                if (shape.isEmpty() || !state.isSolid()) entity.moveToBlockPosAndAngles(this.getPos(), 0, 0);
                else entity.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + shape.getEnd(Axis.Y),
                        this.pos.getZ() + 0.5D, 0, 0);
                this.path_fails = 0;
            }
        }

        private boolean path(final MobEntity entity, final double speed)
        {
            final Vector3d pos = new Vector3d(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ()
                    + 0.5);
            this.setWalkTo(entity, pos, speed, 0);
            return true;
        }

        protected void setWalkTo(final MobEntity entity, final Vector3d pos, final double speed, final int dist)
        {
            entity.getBrain().setMemory(MemoryModules.WALK_TARGET, new WalkTarget(pos, (float) speed, dist));
        }
    }

    private final List<IGuardTask> tasks = Lists.newArrayList(new GuardTask());

    private GuardState state = GuardState.IDLE;

    private IGuardTask activeTask;

    private int activeIndex = 0;

    @Override
    public IGuardTask getActiveTask()
    {
        return this.activeTask;
    }

    @Override
    public IGuardTask getPrimaryTask()
    {
        if (this.tasks.isEmpty()) this.tasks.add(new GuardTask());
        return this.tasks.get(0);
    }

    @Override
    public void setTask(final int index, final IGuardTask task)
    {
        if (index == this.activeIndex) this.activeTask = task;
        IGuardAICapability.super.setTask(index, task);
    }

    @Override
    public GuardState getState()
    {
        return this.state;
    }

    @Override
    public List<IGuardTask> getTasks()
    {
        return this.tasks;
    }

    @Override
    public boolean hasActiveTask(final long time, final long daylength)
    {
        if (this.activeTask != null && this.activeTask.getActiveTime().contains(time, daylength)) return true;
        for (int i = 0; i < this.getTasks().size(); i++)
        {
            final IGuardTask task = this.getTasks().get(i);
            if (task.getActiveTime().contains(time, daylength))
            {
                this.activeIndex = i;
                this.activeTask = task;
                return true;
            }
        }
        return false;
    }

    @Override
    public void loadTasks(final ListNBT list)
    {
        this.tasks.clear();
        for (final INBT element : list)
        {
            final GuardTask task = new GuardTask();
            task.load(element);
            this.tasks.add(task);
        }
        if (this.tasks.isEmpty()) this.tasks.add(new GuardTask());
    }

    @Override
    public ListNBT serializeTasks()
    {
        final ListNBT list = new ListNBT();
        for (final IGuardTask task : this.tasks)
            list.add(task.serialze());
        return list;
    }

    @Override
    public void setState(final GuardState state)
    {
        this.state = state;
    }
}
