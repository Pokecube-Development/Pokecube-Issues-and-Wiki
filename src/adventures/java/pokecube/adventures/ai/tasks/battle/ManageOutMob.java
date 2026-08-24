package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.Config;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.MessageState;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.PCEventsHandler;
import pokecube.core.items.pokecubes.PokecubeManager;

import java.util.List;

public class ManageOutMob extends BaseBattleTask
{
    public ManageOutMob()
    {
        super();
    }

    void doAggression(LivingEntity living, ServerLevel level)
    {
        var trainer = this.getTrainer(living);
        // Check if maybe mob was sent out, but just not seen
        final List<Entity> mobs = PCEventsHandler.getOutMobs(living, false);
        if (!mobs.isEmpty())
        {
            boolean found = false;
            for (final Entity mob : mobs)
                // Ones not added to chunk are in pokecubes, so wait for them to
                // exit.
                if (mob.isAddedToLevel())
                {
                    final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
                    if (pokemob != null && !found)
                    {
                        trainer.setOutMob(pokemob);
                        found = true;
                    }
                }
            if (!found) trainer.setOutMob(null);
            return;
        }
        if (this.getAIStates(living).getAIState(AIState.THROWING)) return;

        final int cooldown = trainer.getTarget() instanceof Player ? trainer.getAttackCooldown() : 0;

        // If no mob was found, then it means trainer was not throwing cubes, as
        // those are counted along with active pokemobs.
        this.getAIStates(living).setAIState(AIState.THROWING, false);
        // If the trainer is on attack cooldown, then check if to send message
        // about next pokemob, or to return early.
        if (cooldown > 0)
        {
            // If no next pokemob, reset trainer and return early.
            if (trainer.getNextPokemob().isEmpty())
            {
                this.getAIStates(living).setAIState(AIState.INBATTLE, false);
                trainer.onLose(trainer.getTarget());
                return;
            }
            // If cooldown is at specific number, send the message for sending
            // out next pokemob.
            if (cooldown == Config.instance.trainerSendOutDelay / 2)
            {
                final ItemStack nextStack = trainer.getNextPokemob();
                if (!nextStack.isEmpty())
                {
                    IPokemob next = PokecubeManager.itemToPokemob(nextStack, level);
                    if (next != null)
                    {
                        // check if our mob should evolve, if so, do so
                        while (next.canEvolve(next.getHeldItem()))
                        {
                            boolean evolved = next.evolve(false);
                            PokemobCaps.updatePokecube(nextStack,
                                    PokemobCaps.getPokemobIn(nextStack).withPokemob(next));
                            if (!evolved) break;
                        }
                        this.getMessages(living)
                                .sendMessage(MessageState.ABOUTSEND, trainer.getTarget(), living.getDisplayName(),
                                        next.getDisplayName(), trainer.getTarget().getDisplayName());
                        this.getMessages(living).doAction(MessageState.ABOUTSEND,
                                trainer.setLatestContext(new ActionContext(trainer.getTarget(), living)));
                        if (living.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                            living.setItemInHand(InteractionHand.MAIN_HAND, nextStack);
                    }
                }
            }
            return;
        }
        // Send next cube at the target.
        trainer.throwCubeAt(trainer.getTarget());
    }

    private boolean considerSwapPokemob(LivingEntity living)
    {
        // TODO check if the target pokemob is bad matchup, consider swapping to
        // better choice.

        // check if can mega evolve
        final IPokemob out = this.getTrainer(living).getOutMob();
        if (this.getTrainer(living).canMegaEvolve() && out != null)
        {
            final List<PokedexEntry> formes = Database.getFormes(out.getPokedexEntry());
            if (!formes.isEmpty())
            {
                final int start = living.getRandom().nextInt(formes.size());
                for (int i = 0; i < formes.size(); i++)
                {
                    final PokedexEntry mega = formes.get((i + start) % formes.size());
                    if (mega.isMega())
                    {
                        out.changeForm(mega);
                        break;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        final boolean hasMob = this.getTrainer(owner).getOutMob() != null;

        owner.getBrain().getMemory(MemoryTypes.BATTLETARGET.get())
                .ifPresent(target -> BehaviorUtils.lookAtEntity(owner, target));

        if (hasMob) this.considerSwapPokemob(owner);
        else this.doAggression(owner, worldIn);
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        return super.checkExtraStartConditions(worldIn, entityIn);
    }
}
