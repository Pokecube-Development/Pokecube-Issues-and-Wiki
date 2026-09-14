package pokecube.api.entity.pokemob.ai;

import java.util.Locale;

public enum LogicStates
{
    /** Is the pokemob currently sitting */
    SITTING(1 << 0),
    /** is the pokemob in water */
    INWATER(1 << 1),
    /** Prevented from flying or floating. */
    GROUNDED(1 << 2),
    // /** is the pokemob currently pathing somewhere */
//    PATHING(1 << 3, false),
    /** is the pokemob jumping */
    JUMPING(1 << 4),
    /** is the pokemob in lava */
    INLAVA(1 << 5),
    /** is the pokemob prevented from moving (ie from ingrain), etc) */
    CANNOTMOVE(1 << 6),
    /** A sleeping pokemon will try to sit at its home location */
    SLEEPING(1 << 7),
    /** This pokemob wants to sleep, but not here */
    TIRED(1 << 8);

    final int mask;
    final boolean persist;
    final String name;

    private LogicStates(final int mask)
    {
        this(mask, true);
    }

    private LogicStates(final int mask, final boolean persist)
    {
        this.mask = mask;
        this.persist = persist;
        this.name = this.name().toLowerCase(Locale.ROOT);
    }

    public String getName()
    {
        return this.name;
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
