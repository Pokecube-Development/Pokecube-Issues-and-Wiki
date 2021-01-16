package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAIRunnable;

public class Gather extends AntTask
{
    int gather_timer = 0;

    StoreTask storage = null;

    public Gather(final IPokemob pokemob)
    {
        super(pokemob);
    }

    public Gather(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
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
        this.pokemob.setRoutineState(AIRoutine.STORE, true);
        this.storage.storageLoc = this.nest.nest.getPos();
        this.storage.berryLoc = this.nest.nest.getPos();
    }

    @Override
    boolean doTask()
    {
        if (this.nest == null) return false;
        if (this.job != AntJob.GATHER) return false;
        if (this.storage == null) for (final IAIRunnable run : this.pokemob.getTasks())
            if (run instanceof StoreTask)
            {
                this.storage = (StoreTask) run;
                break;
            }
        return this.storage != null;
    }

}
