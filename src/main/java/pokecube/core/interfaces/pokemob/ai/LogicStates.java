package pokecube.core.interfaces.pokemob.ai;

public enum LogicStates
{
    /** Is the pokemob currently sitting */
    SITTING(1 << 0),
    /** is the pokemob in water */
    INWATER(1 << 1, false),
    /** Prevented from flying or floating. */
    GROUNDED(1 << 2, false),
    // /** is the pokemob currently pathing somewhere */
    PATHING(1 << 3, false),
    /** is the pokemob jumping */
    JUMPING(1 << 4, false),
    /** is the pokemob in lava */
    INLAVA(1 << 5, false),
    /** is the pokemob prevented from moving (ie from ingrain), etc) */
    NOPATHING(1 << 6, false),
    /** A sleeping pokemon will try to sit at its home location */
    SLEEPING(1 << 7, false),
    /** This pokemob wants to sleep, but not here */
    TIRED(1 << 8),
    /** This pokemob has fainted and cannot battle */
    FAINTED(1 << 9);

    final int     mask;
    final boolean persist;

    private LogicStates(final int mask)
    {
        this.mask = mask;
        this.persist = true;
    }

    private LogicStates(final int mask, final boolean persist)
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
