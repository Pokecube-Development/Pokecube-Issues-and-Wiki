package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;

public abstract class BaseIdleTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseIdleTask.MEMS.put(MemoryModules.ATTACKTARGET, MemoryStatus.VALUE_ABSENT);
    }

    public BaseIdleTask(final IPokemob pokemob)
    {
        super(pokemob, BaseIdleTask.MEMS);
    }

    public BaseIdleTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(pokemob, RootTask.merge(BaseIdleTask.MEMS, mems));
    }

    @Override
    public boolean loadThrottle()
    {
        return true;
    }

}
