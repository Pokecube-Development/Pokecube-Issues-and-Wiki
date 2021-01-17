package pokecube.core.ai.tasks.idle.ants.nest;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import pokecube.core.ai.tasks.idle.ants.AbstractAntTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.bees.BeeTasks;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IPokemob;

public class CheckNest extends AbstractAntTask
{
    protected int new_hive_cooldown = 0;

    public CheckNest(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.new_hive_cooldown = 0;
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Integer> hiveTimer = brain.getMemory(AntTasks.OUT_OF_HIVE_TIMER);
        final int time = hiveTimer.orElseGet(() -> 0) - 1;
        brain.setMemory(BeeTasks.OUT_OF_HIVE_TIMER, time);
        if (this.new_hive_cooldown++ > 600)
        {
            this.new_hive_cooldown = 0;
            final Optional<GlobalPos> pos_opt = brain.getMemory(AntTasks.NEST_POS);
            if (pos_opt.isPresent())
            {
                final World world = this.entity.getEntityWorld();
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.getDimension() != world.getDimensionKey();
                final double dist = pos.getPos().distanceSq(this.entity.getPosition());
                clearHive = clearHive || dist > 10000;
                if (!clearHive)
                {
                    // Not loaded, skip this check, hive may still be there.
                    if (!world.isAreaLoaded(pos.getPos(), 0)) return;
                    clearHive = !(world.getTileEntity(pos.getPos()) instanceof NestTile);
                }
                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) this.entity.getBrain().removeMemory(AntTasks.NEST_POS);
            }
        }
    }

    @Override
    protected boolean doTask()
    {
        return true;
    }

}
