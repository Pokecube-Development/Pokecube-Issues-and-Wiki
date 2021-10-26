package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;

public abstract class BaseIdleTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseIdleTask.MEMS.put(MemoryModules.ATTACKTARGET, MemoryModuleStatus.VALUE_ABSENT);
    }

    public BaseIdleTask(final IPokemob pokemob)
    {
        super(pokemob, BaseIdleTask.MEMS);
    }

    public BaseIdleTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, RootTask.merge(BaseIdleTask.MEMS, mems));
    }

    @Override
    public boolean loadThrottle()
    {
        return true;
    }

}
