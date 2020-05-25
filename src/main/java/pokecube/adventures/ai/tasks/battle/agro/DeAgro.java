package pokecube.adventures.ai.tasks.battle.agro;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.Config;
import pokecube.adventures.ai.tasks.battle.BaseBattleTask;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class DeAgro extends BaseBattleTask
{
    int deagroTimer = 0;
    int noSeeTicks  = 0;

    public DeAgro(final LivingEntity trainer)
    {
        super(trainer);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final LivingEntity owner, final long gameTime)
    {
        boolean deagro = !this.target.isAlive() || this.target.getHealth() <= 0;

        boolean won = false;

        // Check if trainer has any pokemobs, if not, cancel agression, no
        // reward.
        if (this.trainer.getPokemob(0).isEmpty()) deagro = true;

        if (!deagro)
        {
            final double distance = this.entity.getDistanceSq(this.target);
            if (distance > PokecubeCore.getConfig().chaseDistance * PokecubeCore.getConfig().chaseDistance)
                deagro = true;
        }

        if (!deagro && !BrainUtil.canSee(this.entity.getBrain(), this.target))
        {
            final boolean timeout = this.noSeeTicks++ > Config.instance.trainerDeAgressTicks;
            if (timeout) deagro = true;
            else this.noSeeTicks = 0;
        }

        final IHasPokemobs other = TrainerCaps.getHasPokemobs(this.target);

        final Brain<?> brain = this.entity.getBrain();
        final LivingEntity lastHitBy = brain.hasMemory(MemoryModuleType.HURT_BY_ENTITY) ? brain.getMemory(
                MemoryModuleType.HURT_BY_ENTITY).get() : null;
        boolean hitUs = lastHitBy == this.target;

        hitUs = hitUs && this.entity.ticksExisted - this.entity.getLastAttackedEntityTime() > 20;

        if (!deagro && !hitUs && other != null && other.getNextPokemob().isEmpty())
        {
            won = true;
            if (other.getOutID() != null)
            {
                final IPokemob outMob = other.getOutMob();
                if (outMob != null && !outMob.getEntity().isAlive())
                {
                    other.setOutID(null);
                    other.setOutMob(null);
                }
            }
            if (other.getOutID() == null)
            {
                final List<Entity> mobs = PCEventsHandler.getOutMobs(this.target, false);
                if (!mobs.isEmpty())
                {
                    boolean found = false;
                    for (final Entity mob : mobs)
                        if (mob.addedToChunk && mob.getDistanceSq(this.target) < 32 * 32)
                        {
                            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                            if (pokemob != null && !found)
                            {
                                other.setOutMob(pokemob);
                                found = true;
                                break;
                            }
                        }
                    if (found) this.deagroTimer = 20;
                }
                if (this.deagroTimer-- < 0) deagro = true;
            }
        }

        if (deagro)
        {
            if (won)
            {
                this.trainer.onWin(this.target);
                if (other.getTarget() == this.entity) other.onLose(this.entity);
            }
            this.trainer.deAgro(this.trainer, TrainerCaps.getHasPokemobs(this.target));
        }
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return this.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        this.deagroTimer = 20;
        this.noSeeTicks = 0;
    }

}
