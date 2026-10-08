package pokecube.gimmicks.nests.tasks.bees.tasks;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import pokecube.gimmicks.nests.tasks.bees.AbstractBeeTask;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.bees.sensors.HiveSensor;

import java.util.Optional;

public class CheckHive extends AbstractBeeTask
{
    protected int new_hive_cooldown = 0;

    public CheckHive()
    {
        super();
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.new_hive_cooldown = 0;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<Integer> hiveTimer = brain.getMemory(BeeTasks.OUT_OF_HIVE_TIMER.get());
        final int time = hiveTimer.orElseGet(() -> 0) - 1;
        brain.setMemory(BeeTasks.OUT_OF_HIVE_TIMER.get(), time);
        if (this.new_hive_cooldown++ > 600)
        {
            this.new_hive_cooldown = 0;
            final Optional<GlobalPos> pos_opt = brain.getMemory(BeeTasks.HIVE_POS.get());
            if (pos_opt.isPresent())
            {
                final Level world = entity.level();
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.dimension() != world.dimension();
                if (!clearHive)
                {
                    // Not loaded, skip this check, hive may still be there.
                    clearHive = !HiveSensor.doesHiveHaveSpace(entity, pos.pos());
                }
                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) entity.getBrain().eraseMemory(BeeTasks.HIVE_POS.get());
            }
        }
    }

    @Override
    public boolean doTask(Mob entity)
    {
        return true;
    }

}
