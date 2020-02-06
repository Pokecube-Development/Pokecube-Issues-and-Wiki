package pokecube.core.interfaces;

import pokecube.core.items.berries.BerryManager;
import thut.core.common.ThutCore;

public enum Nature
{
    //@formatter:off
    HARDY   (new byte[]{0,0,0,0,0,0}),
    LONELY  (new byte[]{0,1,-1,0,0,0}),
    BRAVE   (new byte[]{0,1,0,0,0,-1}),
    ADAMANT (new byte[]{0,1,0,-1,0,0}),
    NAUGHTY (new byte[]{0,1,0,0,-1,0}),
    BOLD    (new byte[]{0,-1,1,0,0,0}),
    DOCILE  (new byte[]{0,0,0,0,0,0}),
    RELAXED (new byte[]{0,0,1,0,0,-1}),
    IMPISH  (new byte[]{0,0,1,-1,0,0}),
    LAX     (new byte[]{0,0,1,0,-1,0}),
    TIMID   (new byte[]{0,-1,0,0,0,1}),
    HASTY   (new byte[]{0,0,-1,0,0,1}),
    SERIOUS (new byte[]{0,0,0,0,0,0}),
    JOLLY   (new byte[]{0,0,0,-1,0,1}),
    NAIVE   (new byte[]{0,0,0,0,-1,1}),
    MODEST  (new byte[]{0,-1,0,1,0,0}),
    MILD    (new byte[]{0,0,-1,1,0,0}),
    QUIET   (new byte[]{0,0,0,1,0,-1}),
    BASHFUL (new byte[]{0,0,0,0,0,0}),
    RASH    (new byte[]{0,0,0,1,-1,0}),
    CALM    (new byte[]{0,-1,0,0,1,0}),
    GENTLE  (new byte[]{0,0,-1,0,1,0}),
    SASSY   (new byte[]{0,0,0,0,1,-1}),
    CAREFUL (new byte[]{0,0,0,-1,1,0}),
    QUIRKY  (new byte[]{0,0,0,0,0,0});
    // @formatter:on

    public static int getBerryWeight(final int berryIndex, final Nature type)
    {
        int ret = 0;
        final int[] flavours = BerryManager.berryItems.get(berryIndex).type.flavours;
        if (type.goodFlavour == type.badFlavour || flavours == null) return ret;
        ret = flavours[type.goodFlavour] - flavours[type.badFlavour];
        return ret;
    }

    /**
     * Returns the prefered berry for this nature, if it returns -1, it likes
     * all berries equally.
     *
     * @param type
     * @return
     */
    public static int getFavouriteBerryIndex(final Nature type)
    {
        int ret = -1;
        final byte good = type.goodFlavour;
        final byte bad = type.badFlavour;
        if (good == bad) return ret;
        if (type.favourteBerry != -1) return type.favourteBerry;

        int max = 0;
        int current;
        for (final Integer i : BerryManager.berryItems.keySet())
        {
            current = Nature.getBerryWeight(i, type);
            if (current > max)
            {
                ret = i;
                max = current;
            }
        }
        type.favourteBerry = ret;
        return ret;
    }

    public final byte[] stats;

    final byte badFlavour;

    final byte goodFlavour;

    int favourteBerry = -1;

    private Nature(final byte[] stats)
    {
        this.stats = stats;
        byte good = -1;
        byte bad = -1;
        for (int i = 1; i < 6; i++)
        {
            if (stats[i] == 1) good = (byte) (i - 1);
            if (stats[i] == -1) bad = (byte) (i - 1);
        }
        this.goodFlavour = good;
        this.badFlavour = bad;
    }

    public byte[] getStatsMod()
    {
        return this.stats;
    }

    public static String getLocalizationKey(final Nature type)
    {
        return "pokecube.nature." + ThutCore.trim(type.toString());
    }
}
