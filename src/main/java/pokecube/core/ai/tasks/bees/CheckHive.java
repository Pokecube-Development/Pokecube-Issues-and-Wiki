package pokecube.core.ai.tasks.bees;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import pokecube.core.ai.tasks.bees.sensors.HiveSensor;
import pokecube.core.interfaces.IPokemob;

public class CheckHive extends BeeTask
{
    protected int new_hive_cooldown = 0;

    public CheckHive(final IPokemob pokemob)
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
        final Optional<Integer> hiveTimer = brain.getMemory(BeeTasks.OUT_OF_HIVE_TIMER);
        final int time = hiveTimer.orElseGet(() -> 0) - 1;
        brain.setMemory(BeeTasks.OUT_OF_HIVE_TIMER, time);
        if (this.new_hive_cooldown++ > 600)
        {
            this.new_hive_cooldown = 0;
            final Optional<GlobalPos> pos_opt = brain.getMemory(BeeTasks.HIVE_POS);
            if (pos_opt.isPresent())
            {
                final World world = this.entity.getEntityWorld();
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.getDimension() != world.getDimensionKey();
                if (!clearHive)
                {
                    // Not loaded, skip this check, hive may still be there.
                    if (!world.isAreaLoaded(pos.getPos(), 0)) return;
                    clearHive = !HiveSensor.doesHiveHaveSpace(this.entity, pos.getPos());
                }
                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) this.entity.getBrain().removeMemory(BeeTasks.HIVE_POS);
            }
        }
    }

    @Override
    boolean doTask()
    {
        return true;
    }

}
