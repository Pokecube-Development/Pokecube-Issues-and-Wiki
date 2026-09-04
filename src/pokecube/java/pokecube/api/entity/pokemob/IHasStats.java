package pokecube.api.entity.pokemob;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.api.events.pokemobs.combat.ComputeStatEvent;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

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
        for (int i = 0; i < 6; i++)
            if (evs[i] + 128 + evsToAdd[i] <= 255 && evs[i] + 128 + evsToAdd[i] >= 0)
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
     * @return Index of ability, 0 and 1 are "normal" abilities, above 1 are "hidden" abilities.
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
        var entry = this.getPokedexEntry();
        return switch (stat)
        {
            case HP -> entry.getStatHP();
            case ATTACK -> entry.getStatATT();
            case DEFENSE -> entry.getStatDEF();
            case SPATTACK -> entry.getStatATTSPE();
            case SPDEFENSE -> entry.getStatDEFSPE();
            case VIT -> entry.getStatVIT();
            case ACCURACY, EVASION -> 1;
        };
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
        return this.getPokedexEntry().isShadowForme
                ? 0
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
     * Gets the stat as a float, this is used for things like evasion/accuracy which are not integer values.
     *
     * @return the stat
     */
    default double getFloatStat(final Stats stat)
    {
        return PokecubeAttributes.getStatValue(this.getEntity(), stat);
    }

    /**
     * Context sensitive variant of getFloatStat, context in combat is the other party, and can result in adjustments
     * <p>
     * An example being increased evasion to attacks by members on your same side in a battle.
     */
    default double getFloatStat(Stats stat, MoveApplication context)
    {
        double statAmt = getFloatStat(stat);
        if (this instanceof IPokemob pokemob)
        {
            var event = new ComputeStatEvent(pokemob, context, stat, statAmt);
            PokecubeAPI.MOVE_BUS.post(event);
            if (!event.isCanceled()) statAmt = event.newValue;
        }
        return statAmt;
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

    /**
     * @return PokemobMoveStats object that contains all of our info about combat for moves, tracks things like toxic
     * counters, etc
     */
    PokemobMoveStats getMoveStats();

    /**
     * @return the nature
     */
    Nature getNature();

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stat
     */
    default int getStat(final Stats stat, final boolean modified)
    {
        double value = modified
                ? PokecubeAttributes.getStatValue(this.getEntity(), stat)
                : PokecubeAttributes.getBaseValue(this.getEntity(), stat);
        return Math.max(1, (int) value);
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
     * @return the first type
     * @see PokeType
     */
    default PokeType getType1()
    {
        if (this.getMoveStats().type1 == null)
        {
            this.getMoveStats().type1 = originalType1();
        }
        return this.getMoveStats().type1;
    }

    /**
     * Returns 2nd type.
     *
     * @return the second type
     * @see PokeType
     */
    default PokeType getType2()
    {
        if (this.getMoveStats().type2 == null)
        {
            this.getMoveStats().type2 = originalType2();
        }
        return this.getMoveStats().type2;
    }

    /**
     * Gets the weight of the pokemob, this scaled by the value from
     * {@link net.minecraft.world.entity.LivingEntity#getScale()}
     */
    default double getWeight()
    {
        double mass = this.getPokedexEntry().getMass();
        float scale = this.getEntity().getScale();
        return scale * scale * scale * mass;
    }

    /**
     * @return Are we typeIn
     */
    default boolean isType(final PokeType typeIn)
    {
        return this.getType1() == typeIn || this.getType2() == typeIn;
    }

    /**
     * Sets the ability object for the pokemob, this is for use in general/in combat, if used in combat, this is
     * temporary
     */
    void setAbility(Ability ability);

    /**
     * Sets the ability object for the pokemob, This is for use if the underlying ability needs to be force changed,
     * such as during evolution
     */
    void setAbilityRaw(Ability ability);

    /**
     * Sets the ability index for the pokemob, see {@link IHasStats#getAbilityIndex()}
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
     * @param notifyLevelUp should be false in an initialize step and true in a true exp earning
     */
    void setExp(int exp, boolean notifyLevelUp);

    /**
     * Sets current health for our mob.
     *
     * @param health - value to set for health, should be at most {@link #getMaxHealth()}
     */
    default void setHealth(final float health)
    {
        this.getTrackedEntity().setHealth(health);
    }

    /**
     * {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param ivs the Individual Values
     */
    void setIVs(byte[] ivs);

    /** Bulk setting of all moves. This array must have length of 4. */
    void setMoves(String[] moves);

    /**
     *
     */
    void setNature(Nature nature);

    /**
     * Sets first type
     */
    default void setType1(final PokeType type1)
    {
        this.getMoveStats().type1 = type1;
    }

    /**
     * Sets second type
     */
    default void setType2(final PokeType type2)
    {
        this.getMoveStats().type2 = type2;
    }

    /**
     * Computed the expected maxHP for the pokemob
     */
    default int getMaxHPStat()
    {
        int IV = this.getIVs()[IPokemob.Stats.HP.ordinal()];
        int EV = this.getEVs()[IPokemob.Stats.HP.ordinal()] / 4;
        int baseStat = this.getBaseStat(IPokemob.Stats.HP);
        int level = this.getLevel();
        int actualStat = 1;
        if (baseStat != 1) actualStat = level + 10 + (2 * baseStat + IV + EV) * level / 100;
        return actualStat;
    }
}
