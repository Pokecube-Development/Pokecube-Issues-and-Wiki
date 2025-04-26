package pokecube.core.ai.tasks.combat.attacks;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.apache.logging.log4j.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.IMoveUseAI;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

import java.util.Map;

/**
 * This is the IAIRunnable for managing which attack is used when. It determines whether the pokemob is in range,
 * manages pathing to account for range issues, and also manage auto selection of moves for wild or hunting
 * pokemobs.<br>
 * <br>
 * It also manages the message to notify the player that a wild pokemob has decided to battle, as well as dealing with
 * combat between rivals over a mate. It is the one to queue the attack for the pokemob to perform.
 */
public class UseAttacksTask extends CombatTask implements IMoveUseAI
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.ATTACKDELAY.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.TIMER_LEAP.get(), MemoryStatus.REGISTERED);
    }

    public UseAttacksTask()
    {
        super(MEMS);
    }

    @Override
    public void reset(Mob entityIn)
    {
        var pokemob = PokemobCaps.getPokemobFor(entityIn);
        this.clearUseMove(pokemob);
        entityIn.getBrain().eraseMemory(MemoryModules.MOVE_TARGET.get());
        entityIn.getBrain().eraseMemory(MemoryModules.ATTACKDELAY.get());
        entityIn.getBrain().eraseMemory(MemoryModules.TIMER_LEAP.get());
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var brain = entity.getBrain();
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Check if the pokemob has an active move being used, if so return
        if (pokemob.getMoveStats().isExecutingMoves()) return;
        // If no move is selected, don't bother running the below code.
        if (pokemob.getMoveIndex() > 3) return;

        var attack = pokemob.getSelectedMove();
        final boolean self = "user".equals(attack.root_entry._target_type);

        var target = this.getAttackTarget(entity);
        var pokemobTarget = PokemobCaps.getPokemobFor(target);

        var onDelay = brain.getMemory(MemoryModules.ATTACKDELAY.get());
        var moveTarget = brain.getMemory(MemoryModules.MOVE_TARGET.get());

        if (onDelay.isEmpty())
        {
            brain.setMemory(MemoryModules.ATTACKDELAY.get(), true);
            /*
             * Don't want to notify if the pokemob just broke out of a pokecube.
             */
            final boolean previousCaptureAttempt = !EntityPokecubeBase.canCaptureBasedOnConfigs(pokemob);

            /*
             * Check if it should notify the player of agression, and do so if
             * it should.
             */
            if (!previousCaptureAttempt && PokecubeCore.getConfig().pokemobagresswarning
                    && target instanceof ServerPlayer player && !(target instanceof FakePlayer)
                    && !pokemob.getGeneralState(GeneralStates.TAMED) && player.getLastHurtByMob() != entity
                    && player.getLastHurtMob() != entity)
            {
                final Component message = TComponent.translatable("pokemob.agress",
                        pokemob.getDisplayName().getString());
                try
                {
                    // Only send this once.
                    if (pokemob.getAttackCooldown() == 0) thut.lib.ChatHelper.sendSystemMessage(player, message);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.log(Level.WARN, "Error with message for " + target, e);
                }
                pokemob.setAttackCooldown(PokecubeCore.getConfig().pokemobagressticks);
            }
            return;
        }

        // Look at the target
        BehaviorUtils.lookAtEntity(entity, target);

        // No executing move state with no target location.
        if (pokemob.getCombatState(CombatStates.EXECUTINGMOVE) && moveTarget.isEmpty()) this.clearUseMove(pokemob);

        double var1 = (entity.getBbWidth() + 0.75) * (entity.getBbWidth() + 0.75);
        boolean distanced = false;
        final double dist = entity.distanceToSqr(target.getX(), target.getY(), target.getZ());

        distanced = attack.isRanged(pokemob);
        // Check to see if the move is ranged, contact or self.
        if (distanced)
            var1 = PokecubeCore.getConfig().rangedAttackDistance * PokecubeCore.getConfig().rangedAttackDistance;
        else if (PokecubeCore.getConfig().contactAttackDistance > 0)
        {
            var1 = PokecubeCore.getConfig().contactAttackDistance * PokecubeCore.getConfig().contactAttackDistance;
            distanced = true;
        }

        final boolean canUseMove = MovesUtils.canUseMove(pokemob);
        if (!canUseMove) return;
        boolean inRange = false;

        // Checks to see if the target is in range.
        if (distanced) inRange = dist < var1;
        else inRange = MovesUtils.contactAttack(pokemob, target);

        if (self)
        {
            inRange = true;
            BrainUtils.setMoveUseTarget(entity, entity);
        }

        final boolean canSee = BrainUtils.canSee(entity, target);
        Vector3 targetLoc = new Vector3(target).addTo(0, target.getBbHeight() / 2, 0);

        // If we have not set a move executing, we update target location. If we
        // have a move executing, we leave the old location to give the target
        // time to dodge needed.
        if (!pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) BrainUtils.setMoveUseTarget(entity, targetLoc);

        final boolean isTargetDodging = pokemobTarget != null && pokemobTarget.getCombatState(CombatStates.DODGING);

        // If the target is not trying to dodge, and the move allows it,
        // then set target location to where the target is now. This is so that
        // it can use the older postion set above, lowering the accuracy of move
        // use, allowing easier dodging.
        if (!isTargetDodging) BrainUtils.setMoveUseTarget(entity, targetLoc);

        boolean offCooldown = pokemob.getAttackCooldown() <= 0 && entity.isAddedToLevel();
        // Check if the attack should, applying a new delay if this is the
        // case..
        if (inRange && canSee || self)
        {
            if (!self) this.setUseMove(pokemob, targetLoc);
            else this.clearUseMove(pokemob);
        }

        if (!self && (!inRange || !distanced))
        {
            this.setUseMove(pokemob, targetLoc);
            if (BrainUtils.getLeapTarget(entity) == null)
            {
                BrainUtils.setLeapTarget(entity, new EntityTracker(target, false));
            }
        }

        // If all the conditions match, queue up an attack.
        if (!targetLoc.isEmpty() && offCooldown && inRange)
        {
            // Tell the target no need to try to dodge anymore, move is fired.
            if (pokemobTarget != null) pokemobTarget.setCombatState(CombatStates.DODGING, false);
            // Swing arm for effect.
            if (entity.getMainHandItem() != null) entity.swing(InteractionHand.MAIN_HAND);
            // Apply the move.
            final float f = (float) targetLoc.distToEntity(entity);
            if (entity.isAddedToLevel())
            {
                if (PokecubeCore.getConfig().debug_ai)
                    PokecubeAPI.logInfo("{} using attack on {} at {}", entity.getDisplayName(), target.getDisplayName(),
                            targetLoc);

                pokemob.executeMove(target, targetLoc.copy(), f);
                // Reset executing move and no item use status now that we have
                // used a move.
                this.clearUseMove(pokemob);
                pokemob.setCombatState(CombatStates.NOITEMUSE, false);
            }
        }
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // If we do have the target, but are not angry, return false.
        if (!pokemob.getCombatState(CombatStates.BATTLING)) return false;

        final LivingEntity target = this.getAttackTarget(entity);
        // No target, we can't do anything, so return false
        if (target == null) return false;
        // If either us, or target is dead, or about to be so (0 health) return
        // false
        return target.isAlive() && !(target.getHealth() <= 0) && !(pokemob.getHealth() <= 0) && entity.isAlive();
    }
}
