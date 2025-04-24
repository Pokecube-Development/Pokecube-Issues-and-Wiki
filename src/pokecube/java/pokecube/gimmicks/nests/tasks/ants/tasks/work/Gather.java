package pokecube.gimmicks.nests.tasks.ants.tasks.work;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractWorkTask;
import thut.core.common.ThutCore;

public class Gather extends AbstractWorkTask
{
    int gather_timer = 0;

    public Gather()
    {
        super(j -> j == AntJob.GATHER);
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.gather_timer = 0;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var brain = entity.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.GATHER_DETAILS.get())) return;
        var details = brain.getMemory(MemoryModules.GATHER_DETAILS.get()).get();
        if (details.targetItem == null && this.gather_timer++ % 20 == 0)
        {
            if (!this.nest.hab.items.isEmpty())
                details.targetItem = this.nest.hab.items.get(ThutCore.newRandom().nextInt(this.nest.hab.items.size()));
        }
    }
}
