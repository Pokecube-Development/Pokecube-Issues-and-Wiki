package pokecube.adventures.ai.tasks.battle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.core.ai.tasks.TaskBase;

public abstract class BaseBattleTask extends BaseTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseBattleTask.MEMS.put(MemoryTypes.BATTLETARGET, MemoryStatus.VALUE_PRESENT);
    }

    protected LivingEntity target;

    public BaseBattleTask(final LivingEntity trainer)
    {
        super(trainer, BaseBattleTask.MEMS);
    }

    public BaseBattleTask(final LivingEntity trainer, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(trainer, TaskBase.merge(BaseBattleTask.MEMS, mems));
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET)) return false;
        this.target = brain.getMemory(MemoryTypes.BATTLETARGET).get();
        return true;
    }

}
