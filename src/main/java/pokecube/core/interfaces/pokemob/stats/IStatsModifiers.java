package pokecube.core.interfaces.pokemob.stats;

import pokecube.core.interfaces.IPokemob.Stats;

public interface IStatsModifiers
{

    /**
     * Returns the effective value of the modifier, either a percantage, or a
     * flat amount, based on isFlat
     *
     * @param stat
     * @return
     */
    float getModifier(Stats stat);

    /**
     * Returns the raw value for the modifier, this should match whatever is
     * set in setModifier.
     *
     * @param stat
     * @return
     */
    float getModifierRaw(Stats stat);

    /**
     * Priority of application of these stats modifiers, higher numbers go
     * later, the default modifiers (such as from growl) will be given priority
     * of 100, so set yours accordingly.
     *
     * @return
     */
    int getPriority();

    /**
     * Applies the modifier for the given stat to the value.
     *
     * @param stat
     * @param valueIn
     * @return Modified valueIn for the stat
     */
    float apply(Stats stat, float valueIn);

    /**
     * Is this modifier saved with the pokemob, and persists outside of battle
     *
     * @return
     */
    boolean persistant();

    default void reset()
    {
        for (final Stats stat : Stats.values())
            this.setModifier(stat, 0);
    }

    void setModifier(Stats stat, float value);
}
