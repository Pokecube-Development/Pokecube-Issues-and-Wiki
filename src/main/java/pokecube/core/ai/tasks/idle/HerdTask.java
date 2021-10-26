package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IPokemob;

public class HerdTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        HerdTask.MEMS.put(MemoryModules.HERD_MEMBERS, MemoryStatus.VALUE_PRESENT);
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
