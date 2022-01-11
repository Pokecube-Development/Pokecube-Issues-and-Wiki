package pokecube.core.ai.routes;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{

    public static class GuardTask implements IGuardTask
    {
        private static final UUID UID = UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0");

        private AttributeModifier executingGuardTask = null;

        private Vec3 lastPos;

        int path_fails = 0;

        private int lastPosCounter = -1;

        private BlockPos pos;

        private float roamDistance = 2;

        private TimePeriod activeTime = new TimePeriod(0, 0);

        public GuardTask()
        {
            this.executingGuardTask = new AttributeModifier(GuardTask.UID, "pokecube:guard_task", 1,
                    Operation.ADDITION);
        }

        @Override
        public void continueTask(final Mob entity)
        {
            final Vec3 newPos = entity.position();
            if (this.getPos().closerThan(newPos, this.getRoamDistance())) return;

            // Ensure we are not stuck riding something when trying to path
            entity.unRide();

            final double speed = entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            this.path(entity, speed);

            final double ds2 = this.lastPos == null ? 1 : newPos.distanceToSqr(this.lastPos);

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
        public void endTask(final Mob entity)
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
        public void startTask(final Mob entity)
        {
            entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            entity.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(this.executingGuardTask);
            final double speed = entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            if (!this.path(entity, speed)) this.pathFail(entity);
        }

        private void pathFail(final Mob entity)
        {
            if (this.path_fails++ > 100)
            {
                final ServerLevel world = (ServerLevel) entity.getLevel();

                final BlockPos old = entity.blockPosition();
                // Only path fail if we actually are nearby.
                if (old.distSqr(this.getPos()) > 128 * 128) return;
                // Ensure chunk exists
                world.getChunk(this.getPos());
                final BlockState state = world.getBlockState(this.getPos());
                final VoxelShape shape = state.getCollisionShape(world, this.getPos());
                if (shape.isEmpty() || !state.canOcclude()) entity.moveTo(this.getPos(), 0, 0);
                else entity.moveTo(this.pos.getX() + 0.5D, this.pos.getY() + shape.max(Axis.Y), this.pos.getZ() + 0.5D,
                        0, 0);
                this.path_fails = 0;
            }
        }

        private boolean path(final Mob entity, final double speed)
        {
            final Vec3 pos = new Vec3(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ()
                    + 0.5);
            this.setWalkTo(entity, pos, speed, 0);
            return true;
        }

        protected void setWalkTo(final Mob entity, final Vec3 pos, final double speed, final int dist)
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
    public void loadTasks(final ListTag list)
    {
        this.tasks.clear();
        for (final Tag element : list)
        {
            final GuardTask task = new GuardTask();
            task.load(element);
            this.tasks.add(task);
        }
        if (this.tasks.isEmpty()) this.tasks.add(new GuardTask());
    }

    @Override
    public ListTag serializeTasks()
    {
        final ListTag list = new ListTag();
        for (final IGuardTask task : this.tasks)
            list.add(task.serialze());
        return list;
    }

    @Override
    public void setState(final GuardState state)
    {
        this.state = state;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.setState(GuardState.values()[nbt.getInt("state")]);
        if (nbt.contains("tasks"))
        {
            final ListTag tasks = (ListTag) nbt.get("tasks");
            this.loadTasks(tasks);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag ret = new CompoundTag();
        ret.putInt("state", this.getState().ordinal());
        ret.put("tasks", this.serializeTasks());
        return ret;
    }
}
