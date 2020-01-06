package thut.api.entity.ai;

public interface IAIRunnable
{
    /** Last stage of tick, called after tick() */
    void finish();

    /**
     * Should the task start running. if true, will call run next.
     *
     * @return
     */
    default void firstRun()
    {

    }

    /**
     * @return an identifier for use with saving this if it is supposed to be
     *         saved to capability data.
     */
    default String getIdentifier()
    {
        return "";
    }

    /**
     * Will only run an AI if it is higher priority (ie lower number) or a
     * bitwise AND of the two mutex is 0.
     *
     * @return
     */
    int getMutex();

    /** @return the priority of this AIRunnable. Lower numbers run first. */
    int getPriority();

    /** Resets the task. */
    void reset();

    /** runs the task */
    void run();

    /**
     * Sets the mutex.
     *
     * @param mutex
     * @return
     */
    IAIRunnable setMutex(int mutex);

    /**
     * Sets the priority.
     *
     * @param prior
     * @return
     */
    IAIRunnable setPriority(int prior);

    /**
     * Should the task start running. if true, will call run next.
     *
     * @return
     */
    boolean shouldRun();

    /**
     * If this is saveable, should tag be synced to clients.
     *
     * @return
     */
    default boolean sync()
    {
        return false;
    }

    /** second stage of tick code, called after run(). */
    default void tick()
    {

    }
}
