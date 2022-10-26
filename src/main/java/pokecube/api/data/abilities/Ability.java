package pokecube.api.data.abilities;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;

public abstract class Ability
{
    String name = "";

    public Ability setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Called when combat is started for this pokemob
     * 
     * @param mob - The pokemob with this ability
     */
    public void startCombat(final IPokemob mob)
    {}

    /**
     * Called when combat is ended for this pokemob
     * 
     * @param mob - The pokemob with this ability
     */
    public void endCombat(final IPokemob mob)
    {}

    /**
     * Called for the attacked target right before damage is dealt, after other
     * calculations are done.
     *
     * @param mob    - The pokemob with this ability
     * @param move   - the movepack being applied
     * @param damage - the damage to apply
     * @return the actual damage dealt
     */
    public int beforeDamage(final IPokemob mob, final MoveApplication move, final int damage)
    {
        return damage;
    }

    /**
     * Called when a pokemob tries to mega evolve.
     *
     * @param mob      - the pokemob with this ability
     * @param changeTo - the entry mob is trying to change to
     */
    public boolean canChange(final IPokemob mob, final PokedexEntry changeTo)
    {
        return true;
    }

    /** Ensure to call this if your entity is ever set dead. */
    public void destroy(@Nullable IPokemob mob)
    {}

    public String getName()
    {
        return "ability." + this.toString() + ".name";
    }

    /**
     * Inits the Ability, if args isn't null, it will usually have the Pokemob
     * passed in as the first argument.<br>
     * If there is a second argument, it should be and integer range for the
     * expected distance the ability affects.
     *
     * @param args - optional arguments for constructing the ability, this is
     *             empty in most initial cases!
     * @return this
     */
    public Ability init(@Nullable Object... args)
    {
        return this;
    }

    /**
     * Calls when the pokemob first agresses the target. This is called by the
     * agressor, so mob is the pokemob doing the agression. Target is the
     * agressed mob.
     *
     * @param mob    - The pokemob with this ability
     * @param target - the target of the agression
     */
    public void onAgress(final IPokemob mob, final LivingEntity target)
    {}

    /**
     * Called whenever a move is used.
     *
     * @param mob  - The pokemob with this ability
     * @param move - the move being used
     */
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {}

    /**
     * Called whenever a move is used.
     *
     * @param mob  - The pokemob with this ability
     * @param move - the move being used
     */
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {}

    /**
     * Called during the pokemob's update tick.
     *
     * @param mob - The pokemob with this ability
     */
    public void onUpdate(final IPokemob mob)
    {}

    /**
     * Called when a pokemob tries to recall, this might change the pokemob, so
     * check that the returned value is not the same as mob!
     *
     * @param mob - The pokemob with this ability
     */
    public IPokemob onRecall(final IPokemob mob)
    {
        return mob;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean areWeTarget(IPokemob mob, MoveApplication move)
    {
        return mob.getEntity() == move.getTarget();
    }

    public boolean areWeUser(IPokemob mob, MoveApplication move)
    {
        return mob == move.getUser();
    }
}
