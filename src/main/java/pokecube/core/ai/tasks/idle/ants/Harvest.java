package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.interfaces.IPokemob;

public class Harvest extends AntTask
{
    int gather_timer = 0;

    public Harvest(final IPokemob pokemob)
    {
        super(pokemob);
    }

    public Harvest(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, mems);
    }

    @Override
    public void reset()
    {
        this.gather_timer = 0;
    }

    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }

    @Override
    boolean doTask()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
