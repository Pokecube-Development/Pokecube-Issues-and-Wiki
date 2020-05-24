package pokecube.adventures.ai.tasks.battle;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ManageOutMob extends BaseBattleTask
{
    public ManageOutMob(final LivingEntity trainer)
    {
        super(trainer);
    }

    void doAggression()
    {
        // Check if maybe mob was sent out, but just not seen
        final List<Entity> mobs = PCEventsHandler.getOutMobs(this.entity, false);
        if (!mobs.isEmpty())
        {
            boolean found = false;
            for (final Entity mob : mobs)
                // Ones not added to chunk are in pokecubes, so wait for them to
                // exit.
                if (mob.isAddedToWorld())
                {
                    final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                    if (pokemob != null && !found)
                    {
                        this.trainer.setOutMob(pokemob);
                        found = true;
                    }
                }
            return;
        }
        if (this.aiTracker.getAIState(IHasNPCAIStates.THROWING)) return;

        final int cooldown = this.trainer.getTarget() instanceof PlayerEntity ? this.trainer.getAttackCooldown() : 0;

        // If no mob was found, then it means trainer was not throwing cubes, as
        // those are counted along with active pokemobs.
        this.aiTracker.setAIState(IHasNPCAIStates.THROWING, false);
        // If the trainer is on attack cooldown, then check if to send message
        // about next pokemob, or to return early.
        if (cooldown > 0)
        {
            // If no next pokemob, reset trainer and return early.
            if (this.trainer.getNextPokemob().isEmpty())
            {
                this.aiTracker.setAIState(IHasNPCAIStates.INBATTLE, false);
                this.trainer.onLose(this.trainer.getTarget());
                return;
            }
            // If cooldown is at specific number, send the message for sending
            // out next pokemob.
            if (cooldown == Config.instance.trainerSendOutDelay / 2)
            {
                final ItemStack nextStack = this.trainer.getNextPokemob();
                if (!nextStack.isEmpty())
                {
                    IPokemob next = PokecubeManager.itemToPokemob(nextStack, this.world);
                    if (next != null)
                    {
                        // check if our mob should evolve, if so, do so
                        while (next.canEvolve(next.getHeldItem()))
                        {
                            next = next.evolve(false, false);
                            nextStack.setTag(PokecubeManager.pokemobToItem(next).getTag());
                        }
                        this.messages.sendMessage(MessageState.ABOUTSEND, this.trainer.getTarget(), this.entity
                                .getDisplayName(), next.getDisplayName(), this.trainer.getTarget().getDisplayName());
                        this.messages.doAction(MessageState.ABOUTSEND, this.trainer.getTarget(), this.entity);
                    }
                }
            }
            return;
        }
        // Send next cube at the target.
        this.trainer.throwCubeAt(this.trainer.getTarget());
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

    @Override
    protected void updateTask(final ServerWorld worldIn, final LivingEntity owner, final long gameTime)
    {
        final boolean hasMob = this.trainer.getOutMob() != null;
        if (hasMob) this.considerSwapPokemob();
        else this.doAggression();
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return super.shouldExecute(worldIn, entityIn);
    }
}
