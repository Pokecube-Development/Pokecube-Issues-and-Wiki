package pokecube.gimmicks.nests.tasks.burrows.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.pathfinder.Path;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.tasks.burrows.AbstractBurrowTask;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import thut.api.maths.Vector3;

public class ReturnHome extends AbstractBurrowTask
{
    final Vector3 homePos = new Vector3();

    int enterTimer = 0;

    public ReturnHome(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.entity.getBrain().eraseMemory(MemoryModules.GOING_HOME.get());
        this.homePos.clear();
        this.entity.getNavigation().resetMaxVisitedNodesMultiplier();
        this.enterTimer = 0;
    }

    @Override
    public void run()
    {
        // This should path the mob over to the center of the home room, maybe
        // call "enter" for it as well?{
        this.entity.getBrain().eraseMemory(MemoryModules.JOB_INFO.get());
        this.homePos.set(this.burrow.nest.getBlockPos());
        if (this.enterTimer++ > 6000) this.entity.setPos(this.homePos.x + 0.5, this.homePos.y + 1, this.homePos.z
                + 0.5);
        final BlockPos pos = this.entity.blockPosition();
        this.burrow.hab.onEnterHabitat(this.entity);
        if (pos.distSqr(this.homePos.getPos()) > this.burrow.hab.burrow.getSize())
        {
            final Path p = this.entity.getNavigation().getPath();
            final boolean targ = p != null && p.canReach();
            if (!targ) this.setWalkTo(this.homePos, 1, 1);
        }
        else
        {
            final Brain<?> brain = this.entity.getBrain();
            brain.setMemory(MemoryModules.GOING_HOME.get(), false);
        }
    }

    @Override
    protected boolean doTask()
    {
        // We were already heading home, so keep doing that.
        if (!this.homePos.isEmpty()) return true;
        final Brain<?> brain = this.entity.getBrain();
        if (brain.hasMemoryValue(MemoryModules.GOING_HOME.get())) return true;
        if (BurrowTasks.shouldBeInside(this.world, this.burrow))
        {
            brain.setMemory(MemoryModules.GOING_HOME.get(), true);
            return true;
        }
        return false;
    }

}
