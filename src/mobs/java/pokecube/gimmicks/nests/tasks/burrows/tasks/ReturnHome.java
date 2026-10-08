package pokecube.gimmicks.nests.tasks.burrows.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
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

    public ReturnHome()
    {
        super();
    }

    @Override
    public void reset(Mob entity)
    {
        entity.getBrain().eraseMemory(MemoryModules.GOING_HOME.get());
        this.homePos.clear();
        entity.getNavigation().resetMaxVisitedNodesMultiplier();
        this.enterTimer = 0;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        // This should path the mob over to the center of the home room, maybe
        // call "enter" for it as well?{
        entity.getBrain().eraseMemory(MemoryModules.JOB_INFO.get());
        this.homePos.set(this.burrow.nest.getBlockPos());
        if (this.enterTimer++ > 6000) entity.setPos(this.homePos.x + 0.5, this.homePos.y + 1, this.homePos.z + 0.5);
        final BlockPos pos = entity.blockPosition();
        this.burrow.hab.onEnterHabitat(entity);
        if (pos.distSqr(this.homePos.getPos()) > this.burrow.hab.burrow.getSize())
        {
            final Path p = entity.getNavigation().getPath();
            final boolean targ = p != null && p.canReach();
            if (!targ) this.setWalkTo(entity, this.homePos, 1, 1);
        }
        else
        {
            final Brain<?> brain = entity.getBrain();
            brain.setMemory(MemoryModules.GOING_HOME.get(), false);
        }
    }

    @Override
    protected boolean doTask(IPokemob pokemob)
    {
        // We were already heading home, so keep doing that.
        if (!this.homePos.isEmpty()) return true;
        var entity = pokemob.getEntity();
        var level = entity.level();
        final Brain<?> brain = entity.getBrain();
        if (brain.hasMemoryValue(MemoryModules.GOING_HOME.get())) return true;
        if (BurrowTasks.shouldBeInside((ServerLevel) level, this.burrow))
        {
            brain.setMemory(MemoryModules.GOING_HOME.get(), true);
            return true;
        }
        return false;
    }

}
