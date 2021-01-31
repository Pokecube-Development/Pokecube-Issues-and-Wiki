package pokecube.core.ai.tasks.burrows.tasks;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import pokecube.core.ai.tasks.burrows.AbstractBurrowTask;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class ReturnHome extends AbstractBurrowTask
{
    final Vector3 homePos = Vector3.getNewVector();

    int enterTimer = 0;

    public ReturnHome(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.entity.getBrain().removeMemory(BurrowTasks.GOING_HOME);
        this.homePos.clear();
        this.entity.getNavigator().resetRangeMultiplier();
        this.enterTimer = 0;
    }

    @Override
    public void run()
    {
        // This should path the mob over to the center of the home room, maybe
        // call "enter" for it as well?{
        this.entity.getBrain().removeMemory(BurrowTasks.JOB_INFO);
        this.homePos.set(this.burrow.nest.getPos());
        if (this.enterTimer++ > 6000) this.entity.setPosition(this.homePos.x + 0.5, this.homePos.y + 1, this.homePos.z
                + 0.5);
        final BlockPos pos = this.entity.getPosition();
        this.burrow.hab.onEnterHabitat(this.entity);
        if (pos.distanceSq(this.homePos.getPos()) > this.burrow.hab.burrow.getSize())
        {
            final Path p = this.entity.getNavigator().getPath();
            final boolean targ = p != null && p.reachesTarget();
            if (!targ) this.setWalkTo(this.homePos, 1, 1);
        }
        else
        {
            final Brain<?> brain = this.entity.getBrain();
            brain.setMemory(BurrowTasks.GOING_HOME, false);
        }
    }

    @Override
    protected boolean doTask()
    {
        // We were already heading home, so keep doing that.
        if (!this.homePos.isEmpty()) return true;
        final Brain<?> brain = this.entity.getBrain();
        if (brain.hasMemory(BurrowTasks.GOING_HOME)) return true;
        if (BurrowTasks.shouldBeInside(this.world, this.burrow))
        {
            brain.setMemory(BurrowTasks.GOING_HOME, true);
            return true;
        }
        return false;
    }

}
