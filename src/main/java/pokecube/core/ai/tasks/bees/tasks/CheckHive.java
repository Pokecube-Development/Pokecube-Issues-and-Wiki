package pokecube.core.ai.tasks.bees.tasks;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import pokecube.core.ai.tasks.bees.AbstractBeeTask;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.bees.sensors.HiveSensor;
import pokecube.core.interfaces.IPokemob;

public class CheckHive extends AbstractBeeTask
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
                final Level world = this.entity.getCommandSenderWorld();
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.dimension() != world.dimension();
                if (!clearHive)
                {
                    // Not loaded, skip this check, hive may still be there.
                    clearHive = !HiveSensor.doesHiveHaveSpace(this.entity, pos.pos());
                }
                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) this.entity.getBrain().eraseMemory(BeeTasks.HIVE_POS);
            }
        }
    }

    @Override
    public boolean doTask()
    {
        return true;
    }

}
