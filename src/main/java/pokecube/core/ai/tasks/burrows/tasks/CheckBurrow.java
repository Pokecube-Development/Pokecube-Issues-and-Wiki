package pokecube.core.ai.tasks.burrows.tasks;

import java.util.Map;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.interfaces.IPokemob;

public class CheckBurrow extends BaseIdleTask
{

    public CheckBurrow(IPokemob pokemob)
    {
        super(pokemob);
        // TODO Auto-generated constructor stub
    }

    public CheckBurrow(IPokemob pokemob, Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, mems);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reset()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean shouldRun()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
