package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class Dig extends AbstractWorkTask
{

    public Dig(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.DIG);
    }

    @Override
    public void reset()
    {
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
            BlockPos.getAllInBox(pos.add(0, 0, 0), pos.add(1, 1, 1)).forEach(p ->
            {
                v.set(p);
                final BlockState state = this.world.getBlockState(p);
                if (PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isRock(state))
                    if (MoveEventsHandler.canAffectBlock(this.pokemob, v, "ant_dig", false, false)) this.world
                            .destroyBlock(p, true, this.entity);
            });
        }
    }

    @Override
    protected boolean shouldWork()
    {
        return true;
    }

}
