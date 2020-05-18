package pokecube.core.ai.tasks.combat;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.moves.MovesUtils;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

/**
 * This is the IAIRunnable for managing which attack is used when. It
 * determines whether the pokemob is in range, manages pathing to account for
 * range issues, and also manage auto selection of moves for wild or hunting
 * pokemobs.<br>
 * <br>
 * It also manages the message to notify the player that a wild pokemob has
 * decided to battle, as well as dealing with combat between rivals over a mate.
 * It is the one to queue the attack for the pokemob to perform.
 */
public class AIAttack extends FightTask implements IAICombat
{
    /** The target being attacked. */
    LivingEntity entityTarget;
    /** IPokemob version of entityTarget. */
    IPokemob     pokemobTarget;
    /**
     * Used to check whether we need to try swapping target, only check this
     * once per second or so.
     */
    int          targetTestTime;
    /** Where the target is/was for attack. */
    Vector3      targetLoc   = Vector3.getNewVector();
    /** Move we are using */
    Move_Base    attack;
    Matrix3      targetBox   = new Matrix3();
    Matrix3      attackerBox = new Matrix3();

    /** Temp vectors for checking things. */
    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();
    Vector3 v2 = Vector3.getNewVector();
    /** Speed for pathing. */
    double  movementSpeed;

    /** Used to determine when to give up attacking. */
    protected int     chaseTime;
    /** Also used to determine when to give up attacking. */
    protected boolean canSee    = false;
    /** Used for when to execute attacks. */
    protected int     delayTime = -1;
    boolean           running   = false;

    int battleTime = 0;

    public AIAttack(final IPokemob mob)
    {
        super(mob);
        this.movementSpeed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 1.8;
        this.setMutex(3);
    }

    private void checkMateFight(final IPokemob pokemob)
    {
        if (pokemob.getCombatState(CombatStates.MATEFIGHT)) if (this.pokemobTarget != null)
        {
            if (this.pokemobTarget.getHealth() < this.pokemobTarget.getMaxHealth() / 1.5f)
            {
                this.setCombatState(this.pokemob, CombatStates.MATEFIGHT, false);
                this.setCombatState(this.pokemobTarget, CombatStates.MATEFIGHT, false);
                AIFindTarget.deagro(this.pokemobTarget.getEntity());
            }
        }
        else this.setCombatState(this.pokemob, CombatStates.MATEFIGHT, false);
    }

    public boolean continueExecuting()
    {
        this.entityTarget = BrainUtils.getAttackTarget(this.entity);

        final IPokemob mobA = this.pokemob;
        final IPokemob mobB = this.pokemobTarget;

        if (mobB != null)
        {
            final boolean weTame = mobA.getOwnerId() == null;
            final boolean theyTame = mobB.getOwnerId() == null;
            final boolean weHunt = mobA.getCombatState(CombatStates.HUNTING);
            final boolean theyHunt = mobB.getCombatState(CombatStates.HUNTING);
            if (weTame == theyTame && !weTame && weHunt == theyHunt && !theyHunt)
            {
                final float weHealth = mobA.getEntity().getHealth() / mobA.getEntity().getMaxHealth();
                final float theyHealth = mobB.getEntity().getHealth() / mobB.getEntity().getMaxHealth();
                // Wild mobs shouldn't fight to the death unless hunting.
                if (weHealth < 0.25 || theyHealth < 0.25) return false;
            }
        }
        return this.entityTarget != null && this.entityTarget.isAlive() || !this.pokemob.getCombatState(
                CombatStates.ANGRY);
    }

    @Override
    public void reset()
    {
        this.battleTime = 0;
        if (this.running)
        {
            this.running = false;
            this.addEntityPath(this.entity, null, this.movementSpeed);
        }
        AIFindTarget.deagro(this.entity);
    }

    @Override
    public void run()
    {
        this.battleTime++;
        if (!this.continueExecuting())
        {
            this.reset();
            return;
        }
        Path path;
        // Check if the pokemob has an active move being used, if so return
        if (this.pokemob.getActiveMove() != null) return;
        if (!this.running)
        {

            if (!(this.attack == null || (this.attack.getAttackCategory() & IMoveConstants.CATEGORY_SELF) != 0)
                    && !this.pokemob.getGeneralState(GeneralStates.CONTROLLED))
            {
                path = this.entity.getNavigator().getPathToEntity(this.entityTarget, 0);
                this.addEntityPath(this.entity, path, this.movementSpeed);
            }
            this.targetLoc.set(this.entityTarget);
            this.chaseTime = 0;
            this.running = true;
            /**
             * Don't want to notify if the pokemob just broke out of a
             * pokecube.
             */
            final boolean previousCaptureAttempt = !EntityPokecubeBase.canCaptureBasedOnConfigs(this.pokemob);

            /**
             * Check if it should notify the player of agression, and do so if
             * it should.
             */
            if (!previousCaptureAttempt && PokecubeCore.getConfig().pokemobagresswarning
                    && this.entityTarget instanceof ServerPlayerEntity && !(this.entityTarget instanceof FakePlayer)
                    && !this.pokemob.getGeneralState(GeneralStates.TAMED) && ((PlayerEntity) this.entityTarget)
                    .getRevengeTarget() != this.entity && ((PlayerEntity) this.entityTarget)
                    .getLastAttackedEntity() != this.entity)
            {
                final ITextComponent message = new TranslationTextComponent("pokemob.agress", this.pokemob
                        .getDisplayName().getFormattedText());
                try
                {
                    // Only send this once.
                    if (this.pokemob.getAttackCooldown() == 0) this.entityTarget.sendMessage(message);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
                }
                this.pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            }
            return;
        }

        // Look at the target
        this.entity.getLookController().setLookPositionWithEntity(this.entityTarget, 30.0F, 30.0F);

        // Check if it is fighting over a mate, and deal with it accordingly.
        this.checkMateFight(this.pokemob);

        // No executing move state with no target location.
        if (this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE) && this.targetLoc.isEmpty()) this.setCombatState(
                this.pokemob, CombatStates.EXECUTINGMOVE, false);

        // If it has been too long since last seen the target, give up.
        if (this.chaseTime > 200)
        {
            this.setCombatState(this.pokemob, CombatStates.ANGRY, false);
            this.addEntityPath(this.entity, null, this.movementSpeed);
            this.chaseTime = 0;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.INFO, "Too Long Chase, Forgetting Target: "
                    + this.entity + " " + this.entityTarget);
            // Send deagress message and put mob on cooldown.
            final ITextComponent message = new TranslationTextComponent("pokemob.deagress.timeout", this.pokemob
                    .getDisplayName().getFormattedText());
            try
            {
                this.entityTarget.sendMessage(message);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.log(Level.WARN, "Error with message for " + this.entityTarget, e);
            }
            this.pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            return;
        }

        double var1 = (double) (this.entity.getWidth() * 2.0F) * (this.entity.getWidth() * 2.0F);
        boolean distanced = false;
        boolean self = false;
        Move_Base move = null;
        final double dist = this.entity.getDistanceSq(this.entityTarget.posX, this.entityTarget.posY,
                this.entityTarget.posZ);

        move = MovesUtils.getMoveFromName(this.pokemob.getMove(this.pokemob.getMoveIndex()));

        if (move == null) move = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
        // Check to see if the move is ranged, contact or self.
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
        {
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
            distanced = true;
        }
        else if (PokecubeCore.getConfig().contactAttackDistance > 0)
        {
            var1 = PokecubeCore.getConfig().contactAttackDistance * PokecubeCore.getConfig().contactAttackDistance;
            distanced = true;
        }
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0) self = true;

        this.delayTime = this.pokemob.getAttackCooldown();
        final boolean canUseMove = MovesUtils.canUseMove(this.pokemob);
        if (!canUseMove) return;
        boolean shouldPath = this.delayTime <= 0;
        boolean inRange = false;

        // Checks to see if the target is in range.
        if (distanced) inRange = dist < var1;
        else inRange = MovesUtils.contactAttack(this.pokemob, this.entityTarget);

        if (self)
        {
            inRange = true;
            this.targetLoc.set(this.entity);
        }

        // If can't see, increment the timer for giving up later.
        if (!this.canSee)
        {
            this.chaseTime++;
            if (!this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) this.targetLoc.set(this.entityTarget).addTo(0,
                    this.entityTarget.getHeight() / 2, 0);
            // Try to path to target if you can't see it, regardless of what
            // move you have selected.
            shouldPath = true;
        }
        else
        {
            // Otherwise set timer to 0, and if newly executing the move, set
            // the target location as a "aim". This aiming is done so that when
            // the move is fired, it is fired at the location, not the target,
            // giving option to dodge.
            this.chaseTime = 0;
            if (!this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) this.targetLoc.set(this.entityTarget).addTo(0,
                    this.entityTarget.getHeight() / 2, 0);
        }

        boolean delay = false;
        // Check if the attack should, applying a new delay if this is the
        // case..
        if (inRange || self)
        {
            if (this.canSee || self)
            {
                if (this.delayTime <= 0 && this.entity.addedToChunk)
                {
                    this.delayTime = this.pokemob.getAttackCooldown();
                    delay = canUseMove;
                }
                shouldPath = false;
                this.setCombatState(this.pokemob, CombatStates.EXECUTINGMOVE, true);
            }
        }
        else this.setCombatState(this.pokemob, CombatStates.EXECUTINGMOVE, false);

        // If all the conditions match, queue up an attack.
        if (!this.targetLoc.isEmpty() && delay && inRange)
        {
            // If the target is not trying to dodge, and the move allows it,
            // then
            // set target location to where the target is now. This is so that
            // it can use the older postion set above, lowering the accuracy of
            // move use, allowing easier dodging.
            if (this.pokemobTarget != null && !this.pokemobTarget.getCombatState(CombatStates.DODGING)
                    || !(this.pokemobTarget != null) || this.attack.move.isNotIntercepable()) this.targetLoc.set(
                            this.entityTarget).addTo(0, this.entityTarget.getHeight() / 2, 0);
            // Tell the target no need to try to dodge anymore, move is fired.
            if (this.pokemobTarget != null) this.setCombatState(this.pokemobTarget, CombatStates.DODGING, false);
            // Swing arm for effect.
            if (this.entity.getHeldItemMainhand() != null) this.entity.swingArm(Hand.MAIN_HAND);
            // Apply the move.
            final float f = (float) this.targetLoc.distToEntity(this.entity);
            if (this.entity.addedToChunk)
            {
                if (this.entityTarget.isAlive()) this.addMoveInfo(this.pokemob, this.entityTarget, this.targetLoc
                        .copy(), f);
                // Reset executing move and no item use status now that we have
                // used a move.
                this.setCombatState(this.pokemob, CombatStates.EXECUTINGMOVE, false);
                this.setCombatState(this.pokemob, CombatStates.NOITEMUSE, false);
                this.targetLoc.clear();
                shouldPath = false;
                this.delayTime = this.pokemob.getAttackCooldown();
            }
        }
        // If the conditions that failed were due to distance, try to start
        // leaping to close distance.
        else if (shouldPath && !(distanced || self) && !this.pokemob.getCombatState(CombatStates.LEAPING))
        {
            this.setCombatState(this.pokemob, CombatStates.EXECUTINGMOVE, true);
            this.setCombatState(this.pokemob, CombatStates.LEAPING, true);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.INFO, "Set To Leap: " + this.entity);
        }
        // If there is a target location, and it should path to it, queue a path
        // for the mob.
        if (!this.targetLoc.isEmpty() && shouldPath)
        {
            path = this.entity.getNavigator().getPathToPos(this.targetLoc.x, this.targetLoc.y, this.targetLoc.z, 0);
            if (path != null) this.addEntityPath(this.entity, path, this.movementSpeed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        final LivingEntity target = BrainUtils.getAttackTarget(this.entity);
        // No target, we can't do anything, so return false
        if (target == null)
        {
            if (this.entity.getNavigator().noPath() && this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) this
            .setCombatState(this.pokemob, CombatStates.EXECUTINGMOVE, false);
            return false;
        }
        // If either us, or target is dead, or about to be so (0 health) return
        // false
        if (!target.isAlive() || target.getHealth() <= 0 || this.pokemob.getHealth() <= 0 || !this.entity.isAlive())
            return false;

        // If we do have the target, but are not angry, return false.
        if (!this.pokemob.getCombatState(CombatStates.ANGRY)) return false;

        // Set target, set attack, return true
        this.attack = MovesUtils.getMoveFromName(this.pokemob.getMove(this.pokemob.getMoveIndex()));
        this.entityTarget = target;
        if (this.attack == null) this.attack = MovesUtils.getMoveFromName(IMoveConstants.DEFAULT_MOVE);
        return true;
    }

    @Override
    public void tick()
    {
        this.canSee = false;
        if (this.running)
        {
            this.entity.getPersistentData().putLong("lastAttackTick", this.entity.getEntityWorld().getGameTime());
            if (this.entityTarget != null)
            {
                final double dist = this.entity.getDistanceSq(this.entityTarget.posX, this.entityTarget.posY,
                        this.entityTarget.posZ);
                this.canSee = dist < 1 || Vector3.isVisibleEntityFromEntity(this.entity, this.entityTarget);

                if (CapabilityPokemob.getPokemobFor(this.entityTarget) == null
                        && this.entity.ticksExisted > this.targetTestTime && this.pokemob.getCombatState(
                                CombatStates.ANGRY) && this.pokemob.getTargetID() != this.entityTarget.getEntityId())
                {

                    ForgeHooks.onLivingSetAttackTarget(this.entity, this.entityTarget);
                    this.targetTestTime = this.entity.ticksExisted + 20;
                }
            }
        }
    }
}
