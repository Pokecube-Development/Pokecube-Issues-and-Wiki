package pokecube.core.ai.routes;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability.GuardState;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.TimePeriod;

/**
 * Guards a given point. Constructor parameters:
 * <ul>
 * <li>BlockPos position - the position to guard; the entity's current position
 * if <i>null</i>
 * <li>float roamingDistance - how far the AI should be able to stray from the
 * guard point; at least 1.0f
 * <li>float pathSearchDistance - how far from the guard post should the AI
 * still try to find its way back; at least the same as roamingDistance + 1.0f
 * <li>TimePeriod guardingPeriod - which part of the day should this entity
 * guard its post; the full day if <i>null</i>
 * </ul>
 */
public class GuardAI extends Goal
{
    public static interface ShouldRun
    {
        default boolean shouldRun()
        {
            return true;
        }
    }

    public final IGuardAICapability capability;
    private final MobEntity         entity;
    public int                      cooldownTicks;
    public ShouldRun                shouldRun = new ShouldRun()
                                              {
                                              };

    public GuardAI(MobEntity entity, IGuardAICapability capability)
    {
        this.entity = entity;
        this.capability = capability;
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        this.capability.setState(GuardState.IDLE);
        if (this.capability.getActiveTask() != null) this.capability.getActiveTask().endTask(this.entity);
    }

    public void setPos(BlockPos pos)
    {
        if (this.capability.hasActiveTask(this.entity.getEntityWorld().getDayTime(), 24000)) this.capability
                .getActiveTask().setPos(pos);
        else this.capability.getPrimaryTask().setPos(pos);
    }

    public void setTimePeriod(TimePeriod time)
    {
        if (this.capability.hasActiveTask(this.entity.getEntityWorld().getDayTime(), 24000)) this.capability
                .getActiveTask().setActiveTime(time);
        else this.capability.getPrimaryTask().setActiveTime(time);
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (!this.shouldRun.shouldRun()) return false;
        if (!this.capability.hasActiveTask(this.entity.getEntityWorld().getDayTime(), 24000)) return false;
        this.capability.getActiveTask().continueTask(this.entity);
        switch (this.capability.getState())
        {
        case RUNNING:
            if (this.capability.getActiveTask().getPos() == null || this.entity.getNavigator().noPath() && this.entity
                    .getPosition().distanceSq(this.capability.getActiveTask().getPos()) < this.capability
                            .getActiveTask().getRoamDistance() * this.capability.getActiveTask().getRoamDistance() / 2)
            {
                this.capability.setState(GuardState.COOLDOWN);
                return true;
            }
        case COOLDOWN:
            if (this.cooldownTicks < 20 * 15)
            {
                ++this.cooldownTicks;
                return true;
            }
            this.cooldownTicks = 0;
            this.capability.setState(GuardState.IDLE);
            return false;
        default:
            return false;
        }
    }

    @Override
    public boolean shouldExecute()
    {
        if (this.capability == null)
        {
            PokecubeCore.LOGGER.error(this.entity.getCapability(EventsHandler.GUARDAI_CAP, null));
            return false;
        }
        // TODO find some way to determine actual length of day
        // for things like AR support.
        if (null == this.entity || !this.entity.isAlive() || !this.capability.hasActiveTask(this.entity.getEntityWorld()
                .getDayTime(), 24000)) return false;
        final BlockPos pos = this.capability.getActiveTask().getPos();
        if (pos == null || pos.equals(BlockPos.ZERO)) return false;
        final double distanceToGuardPointSq = this.entity.getPosition().distanceSq(this.capability.getActiveTask()
                .getPos());
        double maxDist = this.capability.getActiveTask().getRoamDistance() * this.capability.getActiveTask()
                .getRoamDistance();
        maxDist = Math.max(maxDist, this.entity.getWidth());
        return distanceToGuardPointSq > maxDist;
    }

    @Override
    public void startExecuting()
    {
        this.capability.setState(GuardState.RUNNING);
        this.capability.getActiveTask().startTask(this.entity);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.capability.getState() == GuardState.RUNNING)
        {
            final double maxDist = this.capability.getActiveTask().getRoamDistance() * this.capability.getActiveTask()
                    .getRoamDistance();
            this.capability.getActiveTask().continueTask(this.entity);
            if (this.entity.getPosition().distanceSq(this.capability.getActiveTask().getPos()) < maxDist)
                this.capability.setState(GuardState.COOLDOWN);
        }
    }
}
