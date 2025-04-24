package thut.api.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

public interface IAIRunnable
{
    /** Last stage of tick, called after tick() */
    void finish(ServerLevel level, Mob owner);

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

    /** runs the task */
    void run(ServerLevel level, Mob owner);

    /**
     * Sets the priority.
     */
    IAIRunnable setPriority(int prior);

    default boolean sync() {return false;}

    /**
     * Should the task start running. if true, will call run next.
     */
    boolean shouldRun(Mob entityIn);

    /** second stage of tick code, called after run(). */
    default void runTick(ServerLevel level, Mob owner)
    {

    }
}
