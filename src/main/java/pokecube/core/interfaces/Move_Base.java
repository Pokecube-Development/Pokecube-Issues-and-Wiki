package pokecube.core.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public abstract class Move_Base
{
    public final int       index;
    public final String    name;
    private IMoveAnimation animation;
    public boolean         aoe              = false;
    public boolean         fixedDamage      = false;
    protected SoundEvent   soundUser;
    protected SoundEvent   soundTarget;
    public boolean         hasStatModSelf   = false;
    public boolean         hasStatModTarget = false;
    public final MoveEntry move;

    /**
     * Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name
     *            the English name of the attack, used as identifier and
     *            translation key
     * @param attackCategory
     *            can be either {@link MovesUtils#CATEGORY_CONTACT} or
     *            {@link MovesUtils#CATEGORY_DISTANCE}
     */
    public Move_Base(String name)
    {
        this.name = name;
        this.move = MoveEntry.get(name);
        this.index = this.move.index;
        this.fixedDamage = this.move.fixed;
        boolean mod = false;
        for (final int i : this.move.attackedStatModification)
            if (i != 0)
            {
                mod = true;
                break;
            }
        if (!mod) this.move.attackedStatModProb = 0;
        mod = false;
        for (final int i : this.move.attackerStatModification)
            if (i != 0)
            {
                mod = true;
                break;
            }
        if (!mod) this.move.attackerStatModProb = 0;

        if (this.move.attackedStatModProb > 0) this.hasStatModTarget = true;
        if (this.move.attackerStatModProb > 0) this.hasStatModSelf = true;
    }

    /**
     * This method actually applies the move use from the pokemob.
     *
     * @param user
     * @param target
     * @param start
     * @param end
     */
    public void ActualMoveUse(@Nonnull Entity user, @Nullable Entity target, @Nonnull Vector3 start,
            @Nonnull Vector3 end)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        if (PokecubeCore.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, this, target))) // Move
                                                                                               // Failed
                                                                                               // message
                                                                                               // here?
            return;
        final EntityMoveUse moveUse = new EntityMoveUse(EntityMoveUse.TYPE, user.getEntityWorld());
        moveUse.setUser(user).setMove(this).setTarget(target).setStart(start).setEnd(end);
        pokemob.setActiveMove(moveUse);
        MoveQueuer.queueMove(moveUse);
    }

    /**
     * Applies hunger cost to attacker when this move is used. Hunger is used
     * instead of PP in pokecube
     *
     * @param attacker
     */
    public abstract void applyHungerCost(IPokemob attacker);

    /**
     * First stage of attack use, this is called when the attack is being
     * initiated.<br>
     * This version is called for an attack at a specific entity, should only be
     * called for not-interceptable attacks.
     *
     * @param attacker
     * @param attacked
     */
    public abstract void attack(IPokemob attacker, Entity attacked);

    /**
     * First stage of attack use, this is called when the attack is being
     * initiated.<br>
     * This version is called for an attack at a location.
     *
     * @param attacker
     * @param location
     */
    public abstract void attack(IPokemob attacker, Vector3 location);

    /**
     * Applys world effects of the move
     *
     * @param attacker
     *            - mob using the move
     * @param location
     *            - locaton move hits
     */
    public abstract void doWorldAction(IPokemob attacker, Vector3 location);

    /**
     * Gets the {@link IMoveAnimation} for this move.
     *
     * @return
     */
    public IMoveAnimation getAnimation()
    {
        return this.animation;
    }

    /**
     * User sensitive version of {@link Move_Base#getAnimation()}
     *
     * @param user
     * @return
     */
    public IMoveAnimation getAnimation(IPokemob user)
    {
        return this.getAnimation();
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT}
     * or {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     *
     * @return the attack category
     */
    public byte getAttackCategory()
    {
        return (byte) this.move.attackCategory;
    }

    /** @return Move category for this move. */
    public Category getCategory()
    {
        return Category.values()[this.move.category];
    }

    /**
     * User sensitive version of {@link Move_Base#getCategory()}
     *
     * @param user
     * @return
     */
    public Category getCategory(IPokemob user)
    {
        return this.getCategory();
    }

    /**
     * Index getter.
     *
     * @return a int ID for this move
     */
    public int getIndex()
    {
        return this.index;
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
    public float getPostDelayFactor(IPokemob attacker)
    {
        return this.move.delayAfter ? 4 : 1;
    }

    /**
     * PP getter PP is not used normally, so this mostly just scaled hunger
     * cost or cooldowns
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
    public int getPRE(IPokemob user, Entity target)
    {
        return this.move.accuracy;
    }

    /**
     * PWR getter
     *
     * @return the power of this move
     */
    public int getPWR()
    {
        return this.move.power;
    }

    /**
     * PWR getter
     *
     * @return the power of this move
     */
    public int getPWR(IPokemob user, Entity target)
    {
        return this.move.power;
    }

    /**
     * Gets the self heal ratio for this attack and the given user.
     *
     * @param user
     * @return
     */
    public float getSelfHealRatio(IPokemob user)
    {
        return this.move.selfHealRatio;
    }

    /**
     * Type getter
     *
     * @return the type of this move
     */
    public PokeType getType(IPokemob user)
    {
        return this.move.type;
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
    public void playSounds(Entity attacker, @Nullable Entity attacked, @Nullable Vector3 targetPos)
    {
        if (attacker != null) if (this.soundUser != null || this.move.baseEntry.soundEffectSource != null)
        {
            if (this.move.baseEntry.soundEffectSource != null)
            {
                this.soundUser = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                        this.move.baseEntry.soundEffectSource));
                if (this.soundUser == null) PokecubeCore.LOGGER.error("No Sound found for `"
                        + this.move.baseEntry.soundEffectSource + "` for attack " + this.getName());
                this.move.baseEntry.soundEffectSource = null;
            }
            if (this.soundUser != null) attacker.playSound(this.soundUser, 1f, 1);
        }
        if (attacked != null)
        {
            if (this.soundTarget != null || this.move.baseEntry.soundEffectTarget != null) if (this.soundTarget != null
                    || this.move.baseEntry.soundEffectTarget != null)
            {
                if (this.move.baseEntry.soundEffectTarget != null)
                {
                    this.soundTarget = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                            this.move.baseEntry.soundEffectTarget));
                    if (this.soundTarget == null) PokecubeCore.LOGGER.error("No Sound found for `"
                            + this.move.baseEntry.soundEffectTarget + "` for attack " + this.getName());
                    this.move.baseEntry.soundEffectTarget = null;
                }
                if (this.soundTarget != null) attacked.playSound(this.soundTarget, 1f, 1);
            }
        }
        else if (attacker != null && targetPos != null) if (this.soundTarget != null
                || this.move.baseEntry.soundEffectTarget != null)
        {
            if (this.move.baseEntry.soundEffectTarget != null)
            {
                this.soundTarget = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
                        this.move.baseEntry.soundEffectTarget));
                if (this.soundTarget == null) PokecubeCore.LOGGER.error("No Sound found for `"
                        + this.move.baseEntry.soundEffectTarget + "` for attack " + this.getName());
                this.move.baseEntry.soundEffectTarget = null;
            }
            if (this.soundTarget != null) attacker.getEntityWorld().playSound((PlayerEntity) null, targetPos.x,
                    targetPos.y, targetPos.z, this.soundTarget, attacker.getSoundCategory(), 1f, 1);
        }
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
    public Move_Base setAnimation(IMoveAnimation anim)
    {
        this.animation = anim;
        return this;
    }

    /**
     * Sets if the attack hits all targets in the area, this area is default
     * 4x4 around the mob, but should be specified via Overriding the
     * doFinalAttack method, see Earthquake for an example.
     *
     * @return
     */
    public Move_Base setAOE()
    {
        this.aoe = true;
        return this;
    }

    /**
     * Sets if the attack hits all targets in the direction it is fired,
     * example being flamethrower, that should hit all things in front.
     *
     * @return
     */
    public Move_Base setFixedDamage()
    {
        this.fixedDamage = true;
        return this;
    }

    /**
     * Sets if the attack hits all targets in the direction it is fired,
     * example being flamethrower, that should hit all things in front.
     *
     * @return
     */
    public Move_Base setMultiTarget()
    {
        this.move.baseEntry.multiTarget = true;
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
        this.move.setNotIntercepable(true);
        return this;
    }

    /**
     * Sets if the move can not be intercepted. this should be used for moves
     * like psychic, which should not be intercepted.
     *
     * @param bool
     * @return
     */
    public Move_Base setSelf()
    {
        this.hasStatModSelf = true;
        return this;
    }
}
