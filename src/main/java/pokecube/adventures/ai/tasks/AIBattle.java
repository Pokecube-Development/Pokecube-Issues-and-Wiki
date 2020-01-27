package pokecube.adventures.ai.tasks;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class AIBattle extends AITrainerBase
{
    private boolean  canPath   = true;
    private BlockPos battleLoc = null;

    public AIBattle(final LivingEntity trainer)
    {
        super(trainer);
    }

    public AIBattle(final LivingEntity trainer, final boolean canPath)
    {
        super(trainer);
        this.canPath = canPath;
    }

    private boolean checkPokemobTarget()
    {
        final Entity mobTarget = this.trainer.getOutMob().getEntity().getAttackTarget();
        final IPokemob target = CapabilityPokemob.getPokemobFor(mobTarget);
        if (!this.trainer.getOutMob().getCombatState(CombatStates.ANGRY)) this.trainer.getOutMob().setCombatState(
                CombatStates.ANGRY, true);
        // check if pokemob's target is same as trainers.
        if (mobTarget != this.trainer.getTarget() && target == null) // If not,
                                                                     // set it
                                                                     // as such.
            this.trainer.getOutMob().getEntity().setAttackTarget(this.trainer.getTarget());
        // Return if trainer's pokemob's target is also a pokemob.
        return CapabilityPokemob.getPokemobFor(this.trainer.getOutMob().getEntity().getAttackTarget()) != null;
    }

    private void considerSwapMove()
    {
        // TODO choose between damaging/stats/status moves
        this.setMostDamagingMove();
    }

    private boolean considerSwapPokemob()
    {
        // TODO check if the target pokemob is bad matchup, consider swapping to
        // better choice.

        // check if can mega evolve
        final IPokemob out = this.trainer.getOutMob();
        if (this.trainer.canMegaEvolve() && out != null && out.getPokedexEntry().hasMegaForm)
        {
            final List<PokedexEntry> formes = Database.getFormes(out.getPokedexEntry());
            if (!formes.isEmpty())
            {
                final int start = this.entity.getRNG().nextInt(formes.size());
                for (int i = 0; i < formes.size(); i++)
                {
                    final PokedexEntry mega = formes.get((i + start) % formes.size());
                    if (mega.isMega)
                    {
                        out.megaEvolve(mega);
                        break;
                    }
                }
            }

        }
        return false;
    }

    void doAggression()
    {
        // Check if we are being targetted by the enemies pokemob, if so, we
        // will make it be passive for now.
        if (this.entity instanceof MobEntity)
        {
            final MobEntity living = (MobEntity) this.entity;
            final Entity target = living.getAttackTarget();
            final IPokemob tarMob = CapabilityPokemob.getPokemobFor(target);
            if (tarMob != null)
            {
                tarMob.setCombatState(CombatStates.ANGRY, true);
                tarMob.getEntity().setAttackTarget(null);
            }
        }

        // Check if maybe mob was sent out, but just not seen
        final List<Entity> mobs = PCEventsHandler.getOutMobs(this.entity, false);
        if (!mobs.isEmpty())
        {
            for (final Entity mob : mobs)
                // Ones not added to chunk are in pokecubes, so wait for them to
                // exit.
                if (mob.addedToChunk)
                {
                    final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                    if (pokemob != null) this.trainer.setOutMob(pokemob);
                    return;
                }
            return;
        }
        // If no mob was found, then it means trainer was not throwing cubes, as
        // those are counted along with active pokemobs.
        this.aiTracker.setAIState(IHasNPCAIStates.THROWING, false);
        // If the trainer is on attack cooldown, then check if to send message
        // about next pokemob, or to return early.
        if (this.trainer.getAttackCooldown() > 0)
        {
            // If no next pokemob, reset trainer and return early.
            if (this.trainer.getNextPokemob().isEmpty())
            {
                this.aiTracker.setAIState(IHasNPCAIStates.INBATTLE, false);
                this.trainer.onDefeated(this.trainer.getTarget());
                this.trainer.resetPokemob();
                return;
            }
            // If cooldown is at specific number, send the message for sending
            // out next pokemob.
            if (this.trainer.getAttackCooldown() == Config.instance.trainerSendOutDelay / 2)
            {
                final ItemStack nextStack = this.trainer.getNextPokemob();
                if (!nextStack.isEmpty())
                {
                    final IPokemob next = PokecubeManager.itemToPokemob(nextStack, this.world);
                    if (next != null)
                    {
                        this.messages.sendMessage(MessageState.ABOUTSEND, this.trainer.getTarget(), this.entity
                                .getDisplayName(), next.getDisplayName(), this.trainer.getTarget().getDisplayName());
                        this.messages.doAction(MessageState.ABOUTSEND, this.trainer.getTarget());
                    }
                }
            }
            return;
        }
        // Send next cube at the target.
        this.trainer.throwCubeAt(this.trainer.getTarget());
    }

    /**
     * @param move
     *            - the attack to check
     * @param user
     *            - the user of the sttack
     * @param target
     *            - the target of the attack
     * @return - the damage that will be dealt by the attack (before reduction
     *         due to armour)
     */
    private int getPower(final String move, final IPokemob user, final Entity target)
    {
        final Move_Base attack = MovesUtils.getMoveFromName(move);
        int pwr = attack.getPWR(user, target);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(target);
        if (mob != null) pwr *= PokeType.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
        return pwr;
    }

    /** Resets the task */
    public void resetTask()
    {
        this.trainer.resetPokemob();
        this.battleLoc = null;
    }

    /**
     * Searches for pokemobs most damaging move against the target, and sets it
     * as current attack
     */
    private void setMostDamagingMove()
    {
        final IPokemob outMob = this.trainer.getOutMob();
        int index = outMob.getMoveIndex();
        int max = 0;
        final Entity target = outMob.getEntity().getAttackTarget();
        final String[] moves = outMob.getMoves();
        for (int i = 0; i < 4; i++)
        {
            final String s = moves[i];
            if (s != null)
            {
                final int temp = this.getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        outMob.setMoveIndex(index);
    }

    @Override
    public void tick()
    {
        super.tick();
        ItemStack cube = this.trainer.getNextPokemob();
        if (this.trainer.getCooldown() > 0 && this.trainer.getTarget() == null) cube = ItemStack.EMPTY;
        this.entity.setHeldItem(Hand.MAIN_HAND, cube);
        if (this.trainer.getTarget() != null) this.updateTask();
        else if (this.trainer.getOutID() != null) this.resetTask();
        this.trainer.lowerCooldowns();
    }

    /** Updates the task */
    public void updateTask()
    {
        if (this.trainer.getTarget() == null) return;
        // Check if trainer has any pokemobs, if not, cancel agression, no
        // reward.
        if (this.trainer.getPokemob(0).isEmpty())
        {
            this.trainer.setTarget(null);
            return;
        }

        // Stop trainer from pathing if it shouldn't do so during battle
        if (!this.canPath && this.entity instanceof MobEntity)
        {
            if (this.battleLoc == null) this.battleLoc = this.entity.getPosition();
            ((MobEntity) this.entity).getNavigator().setPath(((MobEntity) this.entity).getNavigator().getPathToPos(
                    this.battleLoc, 0), 0.75);
        }

        // If target is no longer visbile, forget about it and reset.
        if (!Vector3.isVisibleEntityFromEntity(this.entity, this.trainer.getTarget()))
        {
            if (this.noSeeTicks++ > Config.instance.trainerDeAgressTicks)
            {
                this.trainer.setTarget(null);
                this.trainer.resetPokemob();
            }
            return;
        }
        this.noSeeTicks = 0;
        // Check if in range, if too far, target has run away, so forget about
        // it.
        final double distance = this.entity.getDistanceSq(this.trainer.getTarget());
        if (distance > 1024)
        {
            this.trainer.setTarget(null);
            this.trainer.resetPokemob();
        }
        else if (this.trainer.getOutMob() != null && this.trainer.getOutMob().getEntity().isAlive() && this.trainer
                .getOutMob().getEntity().addedToChunk)
        {
            // If trainer has a living, real mob out, tell it to do stuff.
            // Check if pokemob has a valid Pokemob as a target.
            if (this.checkPokemobTarget())
            {
                // If not swapping the pokemob (not implemented), then Ensure
                // using best move for target.
                if (!this.considerSwapPokemob()) this.considerSwapMove();
            }
            // Otherwise, set to most damaging more for non pokemobs.
            else this.setMostDamagingMove();
        }
        else
        {
            // Set out mob to null if it is dead so the trainer forgets about
            // it.
            this.trainer.setOutMob(null);
            // Do agression code for sending out next pokemob.
            this.doAggression();
        }
    }
}
