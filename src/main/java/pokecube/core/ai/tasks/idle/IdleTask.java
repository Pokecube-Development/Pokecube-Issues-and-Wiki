package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;

public abstract class IdleTask extends TaskBase<MobEntity>
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        IdleTask.MEMS.put(MemoryModuleType.HURT_BY_ENTITY, MemoryModuleStatus.VALUE_ABSENT);
    }

    public IdleTask(final IPokemob pokemob)
    {
        super(pokemob, IdleTask.MEMS);
    }

    public IdleTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, TaskBase.merge(IdleTask.MEMS, mems));
    }

}
