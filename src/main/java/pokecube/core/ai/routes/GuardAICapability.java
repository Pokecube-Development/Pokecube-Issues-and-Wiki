package pokecube.core.ai.routes;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{
    public static class Factory implements Callable<IGuardAICapability>
    {
        @Override
        public IGuardAICapability call() throws Exception
        {
            return new GuardAICapability();
        }
    }

    public static class GuardTask implements IGuardTask
    {
        private AttributeModifier executingGuardTask = null;
        private BlockPos          lastPos;
        private int               lastPosCounter     = 10;
        private BlockPos          pos;
        private float             roamDistance       = 2;
        private TimePeriod        activeTime         = new TimePeriod(0, 0);

        public GuardTask()
        {
            this.executingGuardTask = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0"),
                    "pokecube:guard_task", 1, Operation.ADDITION);
        }

        @Override
        public void continueTask(MobEntity entity)
        {
            boolean hasPath = !entity.getNavigator().noPath();
            final BlockPos newPos = entity.getPosition();
            double maxDist = this.getRoamDistance() * this.getRoamDistance();
            maxDist = Math.max(maxDist, entity.getWidth());

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
                final boolean pathed = entity.getNavigator().tryMoveToXYZ(this.getPos().getX() + 0.5, this.getPos()
                        .getY(), this.getPos().getZ() + 0.5, speed);
                final IAttributeInstance attri = entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
                if (!pathed)
                {
                    if (!attri.hasModifier(this.executingGuardTask)) attri.applyModifier(this.executingGuardTask);
                }
                else if (attri.hasModifier(this.executingGuardTask)) attri.removeModifier(this.executingGuardTask);
            }
            else
            {
                final PathPoint end = entity.getNavigator().getPath().getFinalPathPoint();
                final BlockPos endPos = new BlockPos(end.x, end.y, end.z);
                if (endPos.distanceSq(this.getPos()) > maxDist)
                {
                    final double speed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
                    entity.getNavigator().tryMoveToXYZ(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos()
                            .getZ() + 0.5, speed);
                }
            }
            this.lastPos = newPos;
        }

        @Override
        public void endTask(MobEntity entity)
        {
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
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
        public void setActiveTime(TimePeriod active)
        {
            this.activeTime = active;
            if (active == null) this.activeTime = new TimePeriod(0, 0);
        }

        @Override
        public void setPos(BlockPos pos)
        {
            this.pos = pos;
        }

        @Override
        public void setRoamDistance(float roam)
        {
            this.roamDistance = roam;
        }

        @Override
        public void startTask(MobEntity entity)
        {
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.executingGuardTask);
            entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(this.executingGuardTask);
            final double speed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            entity.getNavigator().tryMoveToXYZ(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ()
                    + 0.5, speed);
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
    public boolean hasActiveTask(long time, long daylength)
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
    public void loadTasks(ListNBT list)
    {
        this.tasks.clear();
        for (int i = 0; i < list.size(); i++)
        {
            final GuardTask task = new GuardTask();
            task.load(list.get(i));
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
    public void setState(GuardState state)
    {
        this.state = state;
    }
}
