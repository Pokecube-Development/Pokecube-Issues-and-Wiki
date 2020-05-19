package pokecube.core.ai.routes;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{

    public static class GuardTask implements IGuardTask
    {
        private AttributeModifier executingGuardTask = null;
        private BlockPos          lastPos;
        private int               lastPosCounter     = -1;
        private int               lastPathedCounter  = -1;
        private BlockPos          pos;
        private float             roamDistance       = 2;
        private TimePeriod        activeTime         = new TimePeriod(0, 0);
        int                       path_fails         = 0;

        public GuardTask()
        {
            this.executingGuardTask = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0"),
                    "pokecube:guard_task", 1, Operation.ADDITION);
        }

        @Override
        public void continueTask(final MobEntity entity)
        {
            boolean hasPath = !entity.getNavigator().noPath();
            final BlockPos newPos = entity.getPosition();

            if (hasPath) entity.getBrain().setMemory(MemoryModuleType.PATH, entity.getNavigator().getPath());

            if (entity.getPosition().withinDistance(this.getPos(), this.getRoamDistance()))
            {
                this.path_fails = 0;
                this.lastPosCounter = -1;
                this.lastPathedCounter = -1;
                return;
            }

            this.lastPathedCounter--;
            if (this.lastPathedCounter > 0) return;

            if (hasPath) if (this.lastPos != null && this.lastPos.equals(newPos))
            {
                if (this.lastPosCounter-- >= 0)
                {

                }
                else
                {
                    this.lastPosCounter = 10;
                    hasPath = false;
                }
            }
            else this.lastPosCounter = 10;

            if (!hasPath)
            {
                final double speed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
                if (!this.path(entity, speed)) this.pathFail(entity);
            }
            else
            {
                final PathPoint end = entity.getNavigator().getPath().getFinalPathPoint();
                final BlockPos endPos = new BlockPos(end.x, end.y, end.z);
                double maxDist = this.getRoamDistance() * this.getRoamDistance();
                maxDist = Math.max(maxDist, 0.75);
                maxDist = Math.max(maxDist, entity.getWidth());
                if (endPos.distanceSq(this.getPos()) > maxDist)
                {
                    final double speed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
                    final boolean pathed = this.path(entity, speed);
                    if (!pathed) this.pathFail(entity);
                    else this.path_fails = 0;
                }
            }
            this.lastPos = newPos;
        }

        @Override
        public void endTask(final MobEntity entity)
        {
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            entity.getBrain().removeMemory(MemoryModuleType.PATH);
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
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(this.executingGuardTask);
            final double speed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            if (!this.path(entity, speed)) this.pathFail(entity);
        }

        private void pathFail(final MobEntity entity)
        {
            if (this.path_fails++ > 100)
            {
                final ServerWorld world = (ServerWorld) entity.getEntityWorld();
                // Ensure chunk exists
                world.getChunk(this.getPos());
                final BlockState state = world.getBlockState(this.getPos());
                final VoxelShape shape = state.getCollisionShape(world, this.getPos());
                if (shape.isEmpty() || !state.isSolid()) entity.moveToBlockPosAndAngles(this.getPos(), 0, 0);
                else entity.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + shape.getEnd(Axis.Y),
                        this.pos.getZ() + 0.5D, 0, 0);
                entity.getNavigator().clearPath();
                this.path_fails = 0;
            }
        }

        private boolean path(final MobEntity entity, final double speed)
        {
            if (this.lastPathedCounter < 0)
            {
                // Limit how often this can path.
                this.lastPathedCounter = 20;
                final BlockPos mobs = entity.getPosition();
                final int d = mobs.manhattanDistance(this.getPos());
                entity.getNavigator().setRangeMultiplier(Math.max(MathHelper.ceil(d / 8.0), 1));
                final Path p = entity.getNavigator().getPathToPos(this.getPos(), 1);
                final boolean pathed = entity.getNavigator().setPath(p, speed);
                entity.getBrain().setMemory(MemoryModuleType.PATH, entity.getNavigator().getPath());
                if (p != null) return pathed;
            }
            return false;
        }

    }

    private GuardState             state = GuardState.IDLE;
    private final List<IGuardTask> tasks = Lists.newArrayList(new GuardTask());
    private IGuardTask             activeTask;

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
        for (final IGuardTask task : this.getTasks())
            if (task.getActiveTime().contains(time, daylength))
            {
                this.activeTask = task;
                return true;
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
