package pokecube.core.ai.routes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability.GuardState;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;
import pokecube.core.utils.CapHolders;
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
        boolean shouldRun();
    }

    public final IGuardAICapability capability;

    private final Mob entity;

    public int cooldownTicks;

    public ShouldRun shouldRun = () -> true;

    public GuardAI(final Mob entity, final IGuardAICapability capability)
    {
        this.entity = entity;
        this.capability = capability;
    }

    @Override
    public void stop()
    {
        super.stop();
        this.capability.setState(GuardState.IDLE);
        if (this.capability.getActiveTask() != null) this.capability.getActiveTask().endTask(this.entity);
    }

    public void setPos(final BlockPos pos)
    {
        if (this.capability.hasActiveTask(this.entity.getLevel().getDayTime(), 24000)) this.capability
                .getActiveTask().setPos(pos);
        else this.capability.getPrimaryTask().setPos(pos);
    }

    public void setTimePeriod(final TimePeriod time)
    {
        if (this.capability.hasActiveTask(this.entity.getLevel().getDayTime(), 24000)) this.capability
                .getActiveTask().setActiveTime(time);
        else this.capability.getPrimaryTask().setActiveTime(time);
    }

    @Override
    public boolean canContinueToUse()
    {
        if (!this.shouldRun.shouldRun()) return false;
        if (!this.capability.hasActiveTask(this.entity.getLevel().getDayTime(), 24000)) return false;
        this.capability.getActiveTask().continueTask(this.entity);
        switch (this.capability.getState())
        {
        case RUNNING:
            if (this.capability.getActiveTask().getPos() == null || this.entity.getNavigation().isDone() && this.entity
                    .blockPosition().distSqr(this.capability.getActiveTask().getPos()) < this.capability
                            .getActiveTask().getRoamDistance() * this.capability.getActiveTask().getRoamDistance() / 2)
                this.capability.setState(GuardState.COOLDOWN);
        case COOLDOWN:
            if (this.cooldownTicks < 20 * 15)
            {
                ++this.cooldownTicks;
                return true;
            }
            this.cooldownTicks = 0;
            this.capability.setState(GuardState.IDLE);
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public boolean canUse()
    {
        if (this.capability == null)
        {
            PokecubeCore.LOGGER.error(this.entity.getCapability(CapHolders.GUARDAI_CAP, null));
            return false;
        }
        // TODO find some way to determine actual length of day
        // for things like AR support.
        if (null == this.entity || !this.entity.isAlive() || !this.capability.hasActiveTask(this.entity.getLevel()
                .getDayTime(), 24000)) return false;
        final IGuardTask task = this.capability.getActiveTask();
        final BlockPos pos = task.getPos();
        if (pos == null || pos.equals(BlockPos.ZERO)) return false;
        return !pos.closerThan(this.entity.blockPosition(), task.getRoamDistance());
    }

    @Override
    public void start()
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
            double maxDist = this.capability.getActiveTask().getRoamDistance() * this.capability.getActiveTask()
                    .getRoamDistance();
            maxDist = Math.max(maxDist, 0.75);
            maxDist = Math.max(maxDist, this.entity.getBbWidth());
            this.capability.getActiveTask().continueTask(this.entity);
            if (this.entity.blockPosition().distSqr(this.capability.getActiveTask().getPos()) < maxDist)
                this.capability.setState(GuardState.COOLDOWN);
        }
    }
}
