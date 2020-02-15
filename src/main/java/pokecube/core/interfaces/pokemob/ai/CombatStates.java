package pokecube.core.interfaces.pokemob.ai;

public enum CombatStates
{
    /** Is the pokemob angry at something */
    ANGRY(1 << 0),
    /** A Guarding pokemon will attack any strangers nearby */
    GUARDING(1 << 1),
    /**
     * A Hunting pokemon will look for food to eat)), Either prey or
     * berries.
     */
    HUNTING(1 << 2),
    /** has the pokemob used a zmove this "battle" */
    USEDZMOVE(1 << 3, false),
    /** is the pokemon leaping)), used for the leap AI */
    LEAPING(1 << 4, false),
    /**
     * in the process of dodging)), used to determine if to use the old attack
     * location)), or new
     */
    DODGING(1 << 5, false),
    /** Pokemon is fighting over mate)), should stop when hp hits 50%. */
    MATEFIGHT(1 << 6, false),
    /** Indicates that the pokemon is going to execute a move. */
    EXECUTINGMOVE(1 << 7, false),
    /** Indeicates that there is a new move to use. */
    NEWEXECUTEMOVE(1 << 8, false),
    /** Pokemon cannot have item used on it */
    NOITEMUSE(1 << 9),
    /** Pokemon is forbidden from swapping move */
    NOMOVESWAP(1 << 10, false),
    /** is the pokemob megaevolved */
    MEGAFORME(1 << 11),
    /** is the pokemob dynamaxed */
    DYNAMAX(1 << 12),
    /** can the pokemob gigantamax */
    GIGANTAMAX(1 << 13);

    final int     mask;
    final boolean persist;

    private CombatStates(final int mask)
    {
        this.mask = mask;
        this.persist = true;
    }

    private CombatStates(final int mask, final boolean persist)
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
