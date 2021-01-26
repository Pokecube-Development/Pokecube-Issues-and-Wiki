package pokecube.core.ai.tasks.burrows.tasks;

import java.util.Map;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.burrows.AbstractBurrowTask;
import pokecube.core.interfaces.IPokemob;

public class DigBurrow extends AbstractBurrowTask
{

    public DigBurrow(IPokemob pokemob)
    {
        super(pokemob);
        // TODO Auto-generated constructor stub
    }

    public DigBurrow(IPokemob pokemob, Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        super(pokemob, neededMems);
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
    protected boolean doTask()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
