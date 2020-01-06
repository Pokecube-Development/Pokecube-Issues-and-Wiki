package pokecube.core.interfaces.pokemob.ai;

public enum GeneralStates
{
    /** A Staying pokemon will act like a wild pokemon. */
    STAYING(1 << 0),
    /** Does the pokemob have an owner */
    TAMED(1 << 1),
    /** is the pokemob's movement being controlled. */
    CONTROLLED(1 << 2, false),
    /** Is the pokemob currently trying to mate */
    MATING(1 << 3, false),
    /** is the pokemob evolving */
    EVOLVING(1 << 4, false),
    /** is the pokemob sheared */
    SHEARED(1 << 5),
    /** should capture be denied for this pokemob. */
    DENYCAPTURE(1 << 6),
    /** Has the Pokemon been traded */
    TRADED(1 << 7),
    /** Pokemon is executing idle pathfinding. */
    IDLE(1 << 8, false),
    /** Pokemob is exiting pokecube */
    EXITINGCUBE(1 << 9);

    final int     mask;
    final boolean persist;

    private GeneralStates(int mask)
    {
        this.mask = mask;
        this.persist = true;
    }

    private GeneralStates(int mask, boolean persist)
    {
        this.mask = mask;
        this.persist = persist;
    }

    public int getMask()
    {
        return this.mask;
    }

    /**
     * if this is false, then the value will be cleared whenever the pokemob is
     * loaded from nbt.
     */
    public boolean persists()
    {
        return this.persist;
    }
}
