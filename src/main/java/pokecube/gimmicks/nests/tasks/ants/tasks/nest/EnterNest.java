package pokecube.gimmicks.nests.tasks.ants.tasks.nest;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.pathfinder.Path;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntRoom;
import pokecube.gimmicks.nests.tasks.ants.nest.Node;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractAntTask;
import thut.api.maths.Vector3;

public class EnterNest extends AbstractAntTask
{
    final Vector3 homePos = new Vector3();

    int enterTimer = 0;

    public EnterNest(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.homePos.clear();
        this.entity.getNavigation().resetMaxVisitedNodesMultiplier();
        this.enterTimer = 0;
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        this.homePos.set(this.nest.nest.getBlockPos());
        this.entity.getNavigation().setMaxVisitedNodesMultiplier(10);
        if (PokecubeCore.getConfig().debug_ai) this.pokemob.setPokemonNickname(this.job + " GO HOME");

        // Ensures no jobs for 5s after this is decided
        brain.setMemory(MemoryModules.NO_WORK_TIMER.get(), -100);
        brain.eraseMemory(MemoryModules.WORK_POS.get());
        brain.eraseMemory(MemoryModules.JOB_INFO.get());

        // If we take more than 30s to progress, just tp there
        if (this.enterTimer++ > 6000) this.entity.setPos(this.homePos.x + 0.5, this.homePos.y + 1, this.homePos.z
                + 0.5);

        final List<Node> entrances = this.nest.hab.getRooms(AntRoom.ENTRANCE);
        if (entrances.isEmpty()) return;

        final Node room = entrances.get(0);
        final BlockPos pos = this.entity.blockPosition();
        // If too far, lets path over.
        if (!room.isInside(pos))
        {
            final Path p = this.entity.getNavigation().getPath();
            final boolean targ = p != null && p.canReach();
            if (!targ) this.setWalkTo(room.getCenter().above(), 1, 1);
        }
        else
        {
            final IInhabitable habitat = this.nest.hab;

            // if (habitat instanceof HabitatProvider) ((HabitatProvider)
            // habitat).wrapped = new AntHabitat();

            if (habitat.canEnterHabitat(this.entity))
            {
                brain.setMemory(MemoryModules.OUT_OF_NEST_TIMER.get(), 0);
                brain.eraseMemory(MemoryModules.GOING_HOME.get());
                habitat.onEnterHabitat(this.entity);
            }
            // Set the out of hive timer, so we don't try to re-enter
            // immediately!
            else brain.setMemory(MemoryModules.OUT_OF_NEST_TIMER.get(), 100);
        }
    }

    @Override
    protected boolean doTask()
    {
        // We were already heading home, so keep doing that.
        if (!this.homePos.isEmpty()) return true;
        final Brain<?> brain = this.entity.getBrain();
        if (brain.hasMemoryValue(MemoryModules.GOING_HOME.get())) return true;
        final Optional<Integer> hiveTimer = brain.getMemory(MemoryModules.OUT_OF_NEST_TIMER.get());
        final int timer = hiveTimer.orElseGet(() -> 0);
        // This is our counter for if something angered us, and made is leave
        // the hive, if so, we don't return to hive.
        if (timer > 0) return false;
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getBlockPos()))
        {
            brain.setMemory(MemoryModules.GOING_HOME.get(), true);
            return true;
        }
        // Been out too long, we want to return!
        if (timer < -12000 || timer < -2500 && this.entity.getRandom().nextInt(200) == 0) brain.setMemory(
                MemoryModules.GOING_HOME.get(), true);
        return brain.hasMemoryValue(MemoryModules.GOING_HOME.get());
    }

}
