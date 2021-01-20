package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Guard extends AbstractWorkTask
{
    int patrolTimer = 0;

    public Guard(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.GUARD);
    }

    @Override
    public void reset()
    {
        this.patrolTimer = 0;
    }

    @Override
    public void run()
    {
        if(this.entity.getNavigator().hasPath()) return;
        final Vector3 spot = Vector3.getNewVector();
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> pos_opt = brain.getMemory(AntTasks.WORK_POS);
        if (pos_opt.isPresent())
        {
            spot.set(pos_opt.get().getPos());
            this.setWalkTo(spot, 1, 1);
            if (spot.distToEntity(this.entity) < 2) brain.removeMemory(AntTasks.WORK_POS);
        }
        this.reset();
    }
}
