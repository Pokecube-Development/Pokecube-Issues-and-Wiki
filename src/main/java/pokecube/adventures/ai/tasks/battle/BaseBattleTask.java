package pokecube.adventures.ai.tasks.battle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.core.ai.tasks.TaskBase;

public abstract class BaseBattleTask extends BaseTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseBattleTask.MEMS.put(MemoryTypes.BATTLETARGET, MemoryModuleStatus.VALUE_PRESENT);
    }

    protected LivingEntity target;

    public BaseBattleTask(final LivingEntity trainer)
    {
        super(trainer, BaseBattleTask.MEMS);
    }

    public BaseBattleTask(final LivingEntity trainer, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(trainer, TaskBase.merge(BaseBattleTask.MEMS, mems));
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerWorld worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET)) return false;
        this.target = brain.getMemory(MemoryTypes.BATTLETARGET).get();
        return true;
    }

}
