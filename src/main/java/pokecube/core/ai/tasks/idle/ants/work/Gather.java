package pokecube.core.ai.tasks.idle.ants.work;

import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAIRunnable;

public class Gather extends AbstractWorkTask
{
    int gather_timer = 0;

    StoreTask storage = null;

    public Gather(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.GATHER);
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
    protected boolean shouldWork()
    {
        if (this.storage == null) for (final IAIRunnable run : this.pokemob.getTasks())
            if (run instanceof StoreTask)
            {
                this.storage = (StoreTask) run;
                break;
            }
        return this.storage != null;
    }

}
