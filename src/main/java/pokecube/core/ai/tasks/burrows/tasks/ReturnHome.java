package pokecube.core.ai.tasks.burrows.tasks;

import net.minecraft.entity.ai.brain.Brain;
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
        // call "enter" for it as well?
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
