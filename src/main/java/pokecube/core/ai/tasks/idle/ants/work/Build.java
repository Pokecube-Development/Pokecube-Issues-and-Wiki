package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Build extends AbstractWorkTask
{
    int gather_timer = 0;

    StoreTask storage = null;

    public Build(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.BUILD);
    }

    @Override
    public void reset()
    {
        this.gather_timer = 0;
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> room = brain.getMemory(AntTasks.WORK_POS);
        if (room.isPresent())
        {
            final BlockPos pos = room.get().getPos();
            if (pos.distanceSq(this.entity.getPosition()) > 9)
            {
                this.setWalkTo(pos, 1, 0);
                return;
            }
            brain.removeMemory(AntTasks.WORK_POS);
            final Vector3 v = Vector3.getNewVector();
            v.set(pos);
        }
    }

    @Override
    protected boolean shouldWork()
    {
        return true;
    }

}
