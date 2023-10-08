package pokecube.adventures.ai.tasks.battle.agro;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import pokecube.adventures.Config;
import pokecube.adventures.ai.tasks.battle.BaseBattleTask;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.eventhandlers.PCEventsHandler;

public class DeAgro extends BaseBattleTask
{
    int deagroTimer = 0;
    int noSeeTicks  = 0;

    public DeAgro(final LivingEntity trainer)
    {
        super(trainer);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        boolean deagro = !this.target.isAlive() || this.target.getHealth() <= 0;

        boolean won = false;

        // Check if trainer has any pokemobs, if not, cancel agression, no
        // reward.
        if (this.trainer.getPokemob(0).isEmpty()) deagro = true;

        if (!deagro)
        {
            final double distance = this.entity.distanceToSqr(this.target);
            if (distance > PokecubeCore.getConfig().chaseDistance * PokecubeCore.getConfig().chaseDistance)
                deagro = true;
        }

        if (!deagro && !BrainUtils.canSee(this.entity, this.target))
        {
            final boolean timeout = this.noSeeTicks++ > Config.instance.trainerDeAgressTicks;
            if (timeout) deagro = true;
            else this.noSeeTicks = 0;
        }

        final IHasPokemobs other = TrainerCaps.getHasPokemobs(this.target);

        final Brain<?> brain = this.entity.getBrain();
        final LivingEntity lastHitBy = brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY) ? brain.getMemory(
                MemoryModuleType.HURT_BY_ENTITY).get() : null;
        boolean hitUs = lastHitBy == this.target;

        hitUs = hitUs && this.entity.tickCount - this.entity.getLastHurtMobTimestamp() > 20;

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
                        if (mob.isAddedToWorld() && mob.distanceToSqr(this.target) < 32 * 32)
                        {
                            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
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
            this.trainer.deAgro(TrainerCaps.getHasPokemobs(this.target));
        }
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        this.deagroTimer = 20;
        this.noSeeTicks = 0;
    }

}
