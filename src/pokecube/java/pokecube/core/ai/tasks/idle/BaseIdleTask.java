package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class BaseIdleTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    private static Map<MemoryModuleType<?>, MemoryStatus> getMems()
    {
        if (MEMS.isEmpty()) MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_ABSENT);
        return MEMS;
    }

    public BaseIdleTask()
    {
        super(getMems());
    }

    public BaseIdleTask(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(RootTask.merge(getMems(), mems));
    }

    @Override
    public boolean loadThrottle()
    {
        return true;
    }

}
