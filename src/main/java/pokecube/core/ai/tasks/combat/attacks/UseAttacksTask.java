package pokecube.core.ai.tasks.combat.attacks;

import org.apache.logging.log4j.Level;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.IMoveUseAI;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.moves.MovesUtils;
import thut.api.Tracker;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

/**
 * This is the IAIRunnable for managing which attack is used when. It determines
 * whether the pokemob is in range, manages pathing to account for range issues,
 * and also manage auto selection of moves for wild or hunting pokemobs.<br>
 * <br>
 * It also manages the message to notify the player that a wild pokemob has
 * decided to battle, as well as dealing with combat between rivals over a mate.
 * It is the one to queue the attack for the pokemob to perform.
 */
public class UseAttacksTask extends CombatTask implements IAICombat, IMoveUseAI
{
    /** IPokemob version of entityTarget. */
    IPokemob pokemobTarget;

    /** Where the target is/was for attack. */
    Vector3 targetLoc = new Vector3();
    /** Move we are using */
    MoveEntry attack;

    /** Temp vectors for checking things. */
    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();
    Vector3 v2 = new Vector3();

    /** Used for when to execute attacks. */
    protected int delayTime = -1;
    protected int leapDelay = -1;

    boolean waitingToStart = false;

    public UseAttacksTask(final IPokemob mob)
    {
        super(mob);
    }

    public boolean continueExecuting()
    {
        return this.pokemob.getCombatState(CombatStates.BATTLING);
    }

    @Override
    public void reset()
    {
        this.clearUseMove(this.pokemob);
        this.waitingToStart = false;
        leapDelay = -1;
    }

    @Override
    public void run()
    {
        // Check if the pokemob has an active move being used, if so return
        if (this.pokemob.getMoveStats().isExecutingMoves()) return;

        this.attack = this.pokemob.getSelectedMove();
        final boolean self = "user".equals(attack.root_entry._target_type);

        if (!this.waitingToStart)
        {
            this.targetLoc.set(this.target);
            this.waitingToStart = true;
            /**
             * Don't want to notify if the pokemob just broke out of a pokecube.
             */
            final boolean previousCaptureAttempt = !EntityPokecubeBase.canCaptureBasedOnConfigs(this.pokemob);

            /**
             * Check if it should notify the player of agression, and do so if
             * it should.
             */
            if (!previousCaptureAttempt && PokecubeCore.getConfig().pokemobagresswarning
                    && this.target instanceof ServerPlayer player && !(this.target instanceof FakePlayer)
                    && !this.pokemob.getGeneralState(GeneralStates.TAMED) && player.getLastHurtByMob() != this.entity
                    && player.getLastHurtMob() != this.entity)
            {
                final Component message = TComponent.translatable("pokemob.agress",
                        this.pokemob.getDisplayName().getString());
                try
                {
                    // Only send this once.
                    if (this.pokemob.getAttackCooldown() == 0) thut.lib.ChatHelper.sendSystemMessage(player, message);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.log(Level.WARN, "Error with message for " + this.target, e);
                }
                this.pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            }
            return;
        }

        // Look at the target
        BehaviorUtils.lookAtEntity(this.entity, this.target);

        // No executing move state with no target location.
        if (this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE) && this.targetLoc.isEmpty())
            this.clearUseMove(this.pokemob);

        double var1 = (this.entity.getBbWidth() + 0.75) * (this.entity.getBbWidth() + 0.75);
        boolean distanced = false;
        final double dist = this.entity.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());

        distanced = this.attack.isRanged(this.pokemob);
        // Check to see if the move is ranged, contact or self.
        if (distanced)
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
        else if (PokecubeCore.getConfig().contactAttackDistance > 0)
        {
            var1 = PokecubeCore.getConfig().contactAttackDistance * PokecubeCore.getConfig().contactAttackDistance;
            distanced = true;
        }

        this.delayTime = this.pokemob.getAttackCooldown();
        final boolean canUseMove = MovesUtils.canUseMove(this.pokemob);
        if (!canUseMove) return;
        boolean inRange = false;

        // Checks to see if the target is in range.
        if (distanced) inRange = dist < var1;
        else inRange = MovesUtils.contactAttack(this.pokemob, this.target);

        if (self)
        {
            inRange = true;
            this.targetLoc.set(this.entity);
        }

        final boolean canSee = BrainUtils.canSee(this.entity, this.target);

        // If we have not set a move executing, we update target location. If we
        // have a move executing, we leave the old location to give the target
        // time to dodge needed.
        if (!this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            this.targetLoc.set(this.target).addTo(0, this.target.getBbHeight() / 2, 0);

        final boolean isTargetDodging = this.pokemobTarget != null
                && this.pokemobTarget.getCombatState(CombatStates.DODGING);

        // If the target is not trying to dodge, and the move allows it,
        // then set target location to where the target is now. This is so that
        // it can use the older postion set above, lowering the accuracy of move
        // use, allowing easier dodging.
        if (!isTargetDodging) this.targetLoc.set(this.target).addTo(0, this.target.getBbHeight() / 2, 0);

        boolean delay = false;
        // Check if the attack should, applying a new delay if this is the
        // case..
        if (inRange && canSee || self)
        {
            if (this.delayTime <= 0 && this.entity.isAddedToWorld())
            {
                this.delayTime = this.pokemob.getAttackCooldown();
                delay = true;
            }
            if (!self) this.setUseMove(this.pokemob, this.targetLoc);
            else this.clearUseMove(this.pokemob);
        }

        if (!self && (!inRange || !distanced))
        {
            this.setUseMove(this.pokemob, this.targetLoc);
            if (BrainUtils.getLeapTarget(this.entity) == null)
            {
                BrainUtils.setLeapTarget(this.entity, new EntityTracker(this.target, false));
            }
        }

        // If all the conditions match, queue up an attack.
        if (!this.targetLoc.isEmpty() && delay && inRange)
        {
            // Tell the target no need to try to dodge anymore, move is fired.
            if (this.pokemobTarget != null) this.pokemobTarget.setCombatState(CombatStates.DODGING, false);
            // Swing arm for effect.
            if (this.entity.getMainHandItem() != null) this.entity.swing(InteractionHand.MAIN_HAND);
            // Apply the move.
            final float f = (float) this.targetLoc.distToEntity(this.entity);
            if (this.entity.isAddedToWorld())
            {
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("{} using attack on {} at {}",
                        this.entity.getDisplayName(), this.target.getDisplayName(), this.targetLoc);

                this.pokemob.executeMove(this.target, this.targetLoc.copy(), f);
                // Reset executing move and no item use status now that we have
                // used a move.
                this.clearUseMove(this.pokemob);
                this.pokemob.setCombatState(CombatStates.NOITEMUSE, false);
                this.targetLoc.clear();
                this.delayTime = this.pokemob.getAttackCooldown();
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        // If we do have the target, but are not angry, return false.
        if (!this.pokemob.getCombatState(CombatStates.BATTLING)) return false;

        final LivingEntity target = this.getAttackTarget();
        // No target, we can't do anything, so return false
        if (target == null) return false;
        // If either us, or target is dead, or about to be so (0 health) return
        // false
        if (!target.isAlive() || target.getHealth() <= 0 || this.pokemob.getHealth() <= 0 || !this.entity.isAlive())
            return false;

        if (target != this.target) this.pokemobTarget = PokemobCaps.getPokemobFor(target);
        this.target = target;

        return true;
    }

    @Override
    public void tick()
    {
        this.entity.getPersistentData().putLong("lastAttackTick", Tracker.instance().getTick());
    }
}
