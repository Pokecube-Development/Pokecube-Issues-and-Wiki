package pokecube.core.ai.tasks.utility;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;

public abstract class UtilTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        UtilTask.MEMS.put(MemoryModules.ATTACKTARGET, MemoryModuleStatus.VALUE_ABSENT);
    }

    public UtilTask(final IPokemob pokemob)
    {
        super(pokemob, UtilTask.MEMS);
    }

    public UtilTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, TaskBase.merge(UtilTask.MEMS, mems));
    }

}
