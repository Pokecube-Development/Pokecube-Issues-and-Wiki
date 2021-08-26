package pokecube.core.ai.tasks.ants.tasks.work;

import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.tasks.AbstractWorkTask;
import pokecube.core.ai.tasks.utility.GatherTask;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.ThutCore;

public class Gather extends AbstractWorkTask
{
    int gather_timer = 0;

    GatherTask task = null;

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
        if (this.task == null)
        {
            for (final IAIRunnable run : this.pokemob.getTasks())
                if (run instanceof GatherTask)
                {
                    this.task = (GatherTask) run;
                    break;
                }
        }
        else if (this.gather_timer++ > 100 && this.task.targetItem == null)
        {
            this.gather_timer = 0;
            if (!this.nest.hab.items.isEmpty()) this.task.targetItem = this.nest.hab.items.get(ThutCore.newRandom().nextInt(
                    this.nest.hab.items.size()));
        }
    }
}
