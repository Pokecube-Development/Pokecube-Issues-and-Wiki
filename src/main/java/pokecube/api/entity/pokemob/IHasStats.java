package pokecube.api.entity.pokemob;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;

public interface IHasStats extends IHasEntry
{
    /**
     * At the end of a fight as a XP. {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evsToAdd the Effort Values to add
     */
    default void addEVs(final byte[] evsToAdd)
    {
        final byte[] evs = this.getEVs().clone();

        // Assign the values, cap the EVs at Byte.MAX_VALUE
        for (int i = 0; i < 6; i++) if (evs[i] + 128 + evsToAdd[i] <= 255 && evs[i] + 128 + evsToAdd[i] >= 0)
            evs[i] = (byte) (evs[i] + evsToAdd[i]);
        else evs[i] = Byte.MAX_VALUE;

        int sum = 0;

        // Cap to 510 EVs
        for (final byte ev : evs) sum += ev + 128;

        if (sum < 510) this.setEVs(evs);
    }

    /** adds to how happy is the pokemob, see {@link HappinessType} */
    void addHappiness(int toAdd);

    /** @return The actual ability object for this pokemob. */
    Ability getAbility();

    /**
     * @return the registered name for the ability, empty for no ability
     */
    String getAbilityName();

    /**
     * @return Index of ability, 0 and 1 are "normal" abilities, above 1 are
     *         "hidden" abilities.
     */
    int getAbilityIndex();

    /**
     * Computes an attack strength from stats. Only used against non-poke-mobs.
     *
     * @return the attack strength
     */
    default float getAttackStrength()
    {
        final int ATT = this.getStat(Stats.ATTACK, true);
        final int ATTSPE = this.getStat(Stats.SPATTACK, true);
        final float mult = this.getPokedexEntry().isShadowForme ? 2 : 1;
        return mult * ((ATT + ATTSPE) / 6f);
    }

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stats
     */
    default int getBaseStat(final Stats stat)
    {
        if (stat.ordinal() > 5) return 1;
        return this.getPokedexEntry().getStats()[stat.ordinal()];
    }

    /**
     * To compute exp at the end of a fight.
     *
     * @return in base XP
     */
    default int getBaseXP()
    {
        return this.getPokedexEntry().getBaseXP();
    }

    /**
     * Pokecube catch rate.
     *
     * @return the catch rate
     */
    default int getCatchRate()
    {
        return this.getPokedexEntry().isShadowForme ? 0
                : this.getGeneralState(GeneralStates.DENYCAPTURE) ? 0 : this.getPokedexEntry().getCatchRate();
    }

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Effort Values
     */
    byte[] getEVs();

    /** @return all the experience */
    int getExp();

    /**
     * 0, 1, 2, or 3 {@link Tools#xpToLevel(int, int)}
     *
     * @return in evolution mode
     */
    default int getExperienceMode()
    {
        return this.getPokedexEntry().getEvolutionMode();
    }

    /**
     * Gets the stat as a float, this is used for things like evasion/accuracy
     * which are not integer values.
     *
     * @param stat
     * @param modified
     * @return the stat
     */
    default float getFloatStat(final Stats stat, final boolean modified)
    {
        return this.getModifiers().getStat(this, stat, modified);
    }

    default float getHealth()
    {
        return this.getEntity().getHealth();
    }

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Individual Values
     */
    byte[] getIVs();

    /** @return the level 1-100 */
    default int getLevel()
    {
        return Tools.xpToLevel(this.getExperienceMode(), this.getExp());
    }

    /**
     * @return maximum health for our mob.
     */
    default float getMaxHealth()
    {
        return this.getEntity().getMaxHealth();
    }

    /** @return the Modifiers on stats */
    StatModifiers getModifiers();

    /**
     * {@link IMoveConstants#HARDY} for an example of a nature byte
     *
     * @return the nature
     */
    Nature getNature();

    /**
     * @return Scale factor for this mob, this is applied linearly to each
     *         dimension of the mob.
     */
    float getSize();

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stat
     */
    default int getStat(final Stats stat, final boolean modified)
    {
        return Math.max(1, (int) this.getModifiers().getStat(this, stat, modified));
    }

    /**
     * @return type 1 before any combat modifications
     */
    default PokeType originalType1()
    {
        return this.getPokedexEntry().getType1();
    }

    /**
     * @return type 2 before any combat modifications
     */
    default PokeType originalType2()
    {
        return this.getPokedexEntry().getType2();
    }

    /**
     * Returns 1st type.
     *
     * @see PokeType
     * @return the first type
     */
    default PokeType getType1()
    {
        if (this.getModifiers().type1 == null)
        {
            this.getModifiers().type1 = originalType1();
        }
        return this.getModifiers().type1;
    }

    /**
     * Returns 2nd type.
     *
     * @see PokeType
     * @return the second type
     */
    default PokeType getType2()
    {
        if (this.getModifiers().type2 == null)
        {
            this.getModifiers().type2 = originalType2();
        }
        return this.getModifiers().type2;
    }

    /**
     * Gets the weight of the pokemob, this scaled by the value from
     * {@link IHasStats#getSize()}
     *
     * @return
     */
    default double getWeight()
    {
        double mass = this.getPokedexEntry().mass;
        return this.getSize() * this.getSize() * this.getSize() * mass;
    }

    /**
     * @param typeIn
     * @return Are we typeIn
     */
    default boolean isType(final PokeType typeIn)
    {
        return this.getType1() == typeIn || this.getType2() == typeIn;
    }

    /**
     * Sets the ability object for the pokemob, this is for use in general/in
     * combat, if used in combat, this is temporary
     *
     * @param ability
     */
    void setAbility(Ability ability);

    /**
     * Sets the ability object for the pokemob, This is for use if the
     * underlying ability needs to be force changed, such as during evolution
     *
     * @param ability
     */
    void setAbilityRaw(Ability ability);

    /**
     * Sets the ability index for the pokemob, see
     * {@link IHasStats#getAbilityIndex()}
     *
     * @param index
     */
    void setAbilityIndex(int index);

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs the Effort Values
     */
    void setEVs(byte[] evs);

    /**
     * Sets the experience.
     *
     * @param exp
     * @param notifyLevelUp should be false in an initialize step and true in a
     *                      true exp earning
     */
    IPokemob setExp(int exp, boolean notifyLevelUp);

    /**
     * Sets current health for our mob.
     * 
     * @param health - value to set for health, should be at most
     *               {@link #getMaxHealth()}
     */
    default void setHealth(final float health)
    {
        this.getEntity().setHealth(health);
    }

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs the Individual Values
     */
    void setIVs(byte[] ivs);

    /** Bulk setting of all moves. This array must have length of 4. */
    void setMoves(String[] moves);

    /**
     * Sets the pokemobs's nature {@link IMoveConstants#HARDY} for an example of
     * a nature byte
     *
     * @param nature
     */
    void setNature(Nature nature);

    /**
     * Sets the size for this mob, see {@link IHasStats#getSize()}
     *
     * @param size
     */
    void setSize(float size);

    /** Sets ability index to 2. */
    default void setToHiddenAbility()
    {
        this.setAbilityIndex(2);
        this.setAbilityRaw(this.getPokedexEntry().getHiddenAbility(PokemobCaps.getPokemobFor(this.getEntity())));
    }

    /**
     * Sets first type
     *
     * @param type1
     */
    default void setType1(final PokeType type1)
    {
        this.getModifiers().type1 = type1;
    }

    /**
     * Sets second type
     *
     * @param type2
     */
    default void setType2(final PokeType type2)
    {
        this.getModifiers().type2 = type2;
    }
}
