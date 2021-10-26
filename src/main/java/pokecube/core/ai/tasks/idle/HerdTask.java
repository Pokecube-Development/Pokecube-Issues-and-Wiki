package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IPokemob;

public class HerdTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        HerdTask.MEMS.put(MemoryModules.HERD_MEMBERS, MemoryModuleStatus.VALUE_PRESENT);
    }

    public HerdTask(final IPokemob pokemob)
    {
        super(pokemob, HerdTask.MEMS);
    }

    @Override
    public void reset()
    {

    }

    @Override
    public void run()
    {

    }

    @Override
    public boolean shouldRun()
    {
        return false;
    }

}
