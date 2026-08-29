package pokecube.adventures.ai.tasks.battle.agro;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.adventures.Config;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.battle.BaseBattleTask;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.eventhandlers.PCEventsHandler;

import java.util.List;
import java.util.Map;

public class DeAgro extends BaseBattleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryTypes.BATTLETARGET.get(), MemoryStatus.VALUE_PRESENT);
        MEMS.put(MemoryTypes.NO_SEEN_TARGET_TIMER.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryTypes.DE_AGRO_TIMER.get(), MemoryStatus.REGISTERED);
    }

    public DeAgro()
    {
        super(MEMS);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        var brain = owner.getBrain();
        var target = brain.getMemory(MemoryTypes.BATTLETARGET.get()).get().target();
        boolean deagro = !target.isAlive() || target.getHealth() <= 0;
        int noSeeTicks = brain.getMemory(MemoryTypes.NO_SEEN_TARGET_TIMER.get()).orElse(0);
        int deagroTimer = brain.getMemory(MemoryTypes.DE_AGRO_TIMER.get()).orElse(20);
        boolean won = false;

        // Check if trainer has any pokemobs, if not, cancel agression, no
        // reward.
        if (this.getTrainer(owner).getPokemob(0).isEmpty()) deagro = true;

        if (!deagro)
        {
            final double distance = owner.distanceToSqr(target);
            if (distance > PokecubeCore.getConfig().chaseDistance * PokecubeCore.getConfig().chaseDistance)
                deagro = true;
        }

        if (!deagro && !BrainUtils.canSee(owner, target))
        {
            final boolean timeout = noSeeTicks++ > Config.instance.trainerDeAgressTicks;
            if (timeout) deagro = true;
            else noSeeTicks = 0;
        }

        final IHasPokemobs other = TrainerCaps.getHasPokemobs(target);

        final LivingEntity lastHitBy = brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY) ? brain.getMemory(
                MemoryModuleType.HURT_BY_ENTITY).get() : null;
        boolean hitUs = lastHitBy == target;

        hitUs = hitUs && owner.tickCount - owner.getLastHurtMobTimestamp() > 20;

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
                final List<Entity> mobs = PCEventsHandler.getOutMobs(target, false);
                if (!mobs.isEmpty())
                {
                    boolean found = false;
                    for (final Entity mob : mobs)
                        if (mob.isAddedToLevel() && mob.distanceToSqr(target) < 32 * 32)
                        {
                            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
                            if (pokemob != null && !found)
                            {
                                other.setOutMob(pokemob);
                                found = true;
                                break;
                            }
                        }
                    if (found) deagroTimer = 20;
                }
                if (deagroTimer-- < 0) deagro = true;
            }
        }

        if (deagro)
        {
            if (won)
            {
                this.getTrainer(owner).onWin(target);
                if (other.getTarget() == owner) other.onLose(owner);
            }
            this.getTrainer(owner).deAgro(TrainerCaps.getHasPokemobs(target));
            brain.eraseMemory(MemoryTypes.DE_AGRO_TIMER.get());
            brain.eraseMemory(MemoryTypes.NO_SEEN_TARGET_TIMER.get());
        }
        else
        {
            brain.setMemory(MemoryTypes.NO_SEEN_TARGET_TIMER.get(), noSeeTicks);
            brain.setMemory(MemoryTypes.DE_AGRO_TIMER.get(), deagroTimer);
        }
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }
}
