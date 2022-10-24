package pokecube.api.moves;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public abstract class Move_Base
{
    public final String name;
    public boolean aoe = false;
    public final MoveEntry move;

    /**
     * Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name           the English name of the attack, used as identifier
     *                       and translation key
     * @param attackCategory can be either {@link MovesUtils#CATEGORY_CONTACT}
     *                       or {@link MovesUtils#CATEGORY_DISTANCE}
     */
    public Move_Base(final String name)
    {
        this.move = MoveEntry.get(name);
        this.name = this.move.name;
    }

    /**
     * This is called when the move is registered
     */
    public void init()
    {

    }

    /**
     * This is called if the move is being replaced by another one for
     * registration
     */
    public void destroy()
    {

    }

    /**
     * This method actually applies the move use from the pokemob.
     *
     * @param user
     * @param target
     * @param start
     * @param end
     */
    @Deprecated
    public void ActualMoveUse(@Nonnull final LivingEntity user, @Nullable final LivingEntity target,
            @Nonnull final Vector3 start, @Nonnull final Vector3 end)
    {}

    /**
     * First stage of attack use, this is called when the attack is being
     * initiated.<br>
     * This version is called for an attack at a specific entity, should only be
     * called for not-interceptable attacks.
     *
     * @param attacker
     * @param attacked
     */
    public abstract void attack(IPokemob attacker, LivingEntity attacked);

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     *
     * @return the attack category
     */
    public byte getAttackCategory()
    {
        return (byte) this.move.attackCategory;
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor. <br>
     * <br>
     * This version is user aware
     *
     * @return the attack category
     */
    public byte getAttackCategory(final IPokemob user)
    {
        return (byte) this.move.attackCategory;
    }

    public abstract Move_Base getMove(String name);

    /**
     * Name getter
     *
     * @return the name of this move
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * This is a factor for how long of a cooldown occurs after the attack is
     * done.
     *
     * @param attacker
     * @return
     */
    public float getPostDelayFactor(final IPokemob attacker)
    {
        return this.move.cooldown_scale;
    }

    /**
     * PP getter PP is not used normally, so this mostly just scaled hunger cost
     * or cooldowns
     *
     * @return the number of Power points of this move
     */
    public int getPP()
    {
        return this.move.pp;
    }

    /**
     * PRE getter
     *
     * @return the precision of this move
     */
    public int getPRE()
    {
        return this.move.accuracy;
    }

    /**
     * PRE getter
     *
     * @return the precision of this move
     */
    public int getPRE(final IPokemob user, final LivingEntity target)
    {
        return this.move.accuracy;
    }

    /**
     * Gets the self heal ratio for this attack and the given user.
     *
     * @param user
     * @return
     */
    public float getSelfHealRatio(final IPokemob user)
    {
        return this.move.selfHealRatio;
    }

    public float getTargetHealRatio(final IPokemob user)
    {
        return this.move.targetHealRatio;
    }

    /**
     * Called after the attack is done but before postAttack is called.
     *
     * @param packet
     */
    public abstract void handleStatsChanges(MovePacket packet);

    /** @return Does this move targer the user. */
    public boolean isSelfMove()
    {
        return (this.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0;
    }

    /** @return Does this move targer the user. */
    public boolean isSelfMove(IPokemob user)
    {
        return (this.getAttackCategory(user) & IMoveConstants.CATEGORY_SELF) > 0;
    }

    public boolean isRanged(IPokemob user)
    {
        return (this.getAttackCategory(user) & IMoveConstants.CATEGORY_DISTANCE) > 0;
    }

    /**
     * This is where the move's damage should be applied to the mob.
     *
     * @param packet
     * @return
     */
    public abstract void onAttack(MovePacket packet);

    /**
     * Plays any sounds needed for this move
     *
     * @param attacker
     * @param attacked
     * @param targetPos
     */
    public void playSounds(final LivingEntity attacker, @Nullable final LivingEntity attacked,
            @Nullable final Vector3 targetPos)
    {
        this.move.playSounds(new MoveApplication(move, PokemobCaps.getPokemobFor(attacker), attacked));
    }

    /**
     * Called after the attack is done for any additional effects needed Both
     * involved mobs should be notified of the packet here.
     *
     * @param packet
     */
    public abstract void postAttack(MovePacket packet);

    /**
     * Called before the attack is applied. Both involved mobs should be
     * notified of the packet here.
     *
     * @param packet
     */
    public abstract void preAttack(MovePacket packet);

    /**
     * Sets the move animation
     *
     * @param anim
     * @return
     */
    public Move_Base setAnimation(final IMoveAnimation anim)
    {
        this.move.setAnimation(anim);
        return this;
    }

    /**
     * Sets if the attack hits all targets in the area, this area is default 4x4
     * around the mob, but should be specified via Overriding the doFinalAttack
     * method, see Earthquake for an example.
     *
     * @return
     */
    public Move_Base setAOE()
    {
        this.aoe = true;
        return this;
    }

    /**
     * Sets if the move can not be intercepted. this should be used for moves
     * like psychic, which should not be intercepted.
     *
     * @param bool
     * @return
     */
    public Move_Base setNotInterceptable()
    {
        this.move.setCanHitNonTarget(false);
        return this;
    }
}
