package thut.api.entity.ai;

import net.minecraft.world.entity.Mob;

public interface IAIRunnable
{
    /**
     * @return an identifier for use with saving this if it is supposed to be saved to capability data.
     */
    default String getIdentifier()
    {
        return "";
    }

    /** @return the priority of this AIRunnable. Lower numbers run first. */
    int getPriority();

    /** Resets the task. */
    void reset(Mob entityIn);

    /**
     * Sets the priority.
     */
    IAIRunnable setPriority(int prior);

    /**
     * Should the task start running. if true, will call run next.
     */
    boolean shouldRun(Mob entityIn);
}
