package pokecube.adventures.ai.tasks.battle;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.TaskBase;

import java.util.Map;

public abstract class BaseBattleTask extends BaseTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    public static record BattleTarget(BlockPos battlePos, LivingEntity target){}

    static
    {
        BaseBattleTask.MEMS.put(MemoryTypes.BATTLETARGET.get(), MemoryStatus.VALUE_PRESENT);
    }

    public BaseBattleTask()
    {
        this(BaseBattleTask.MEMS);
    }

    public BaseBattleTask(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(TaskBase.merge(BaseBattleTask.MEMS, mems));
    }

    protected LivingEntity getTarget(LivingEntity owner)
    {
        var brain = owner.getBrain();
        var target = brain.getMemory(MemoryTypes.BATTLETARGET.get()).get().target();
        var target2 = BrainUtils.getAttackTarget(owner);
        target_checking:
        {
            if (target2 != null && target2 != target)
            {
                var battleA = Battle.getBattle(target2);
                var battleB = Battle.getBattle(target);
                if (battleA == battleB)
                {
                    target = target2;
                    break target_checking;
                }

            }
            final IHasPokemobs other = TrainerCaps.getHasPokemobs(target);
            if (other != null)
            {
                other.onSetTarget(owner, true);
                var mob = other.getOutMob();
                if (mob != null && mob.getEntity().isAddedToLevel()) target = mob.getEntity();
            }
        }
        return target;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        return brain.hasMemoryValue(MemoryTypes.BATTLETARGET.get());
    }

}
