package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Optional;

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
        final Vector3 spot = Vector3.getNewVector();
        final Optional<GlobalPos> pos_opt = this.entity.getBrain().getMemory(AntTasks.WORK_POS);
        if (pos_opt.isPresent())
        {
            spot.set(pos_opt.get().getPos());
            this.setWalkTo(spot, 1, 1);
        }
        this.reset();
    }

    @Override
    protected boolean shouldWork()
    {
        return true;
    }

}
