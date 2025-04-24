package pokecube.gimmicks.nests.tasks.bees.tasks;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import pokecube.gimmicks.nests.tasks.bees.AbstractBeeTask;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.bees.sensors.HiveSensor;
import thut.api.maths.Vector3;

import java.util.Optional;

public class EnterHive extends AbstractBeeTask
{
    final Vector3 homePos = new Vector3();

    public EnterHive()
    {
        super();
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.homePos.clear();
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<GlobalPos> pos_opt = brain.getMemory(BeeTasks.HIVE_POS.get());
        if (pos_opt.isPresent())
        {
            final Level world = entity.level();
            final GlobalPos pos = pos_opt.get();
            final boolean clearHive = pos.dimension() != world.dimension();
            // This will be cleared by CheckHive, so lets just exit here.
            if (clearHive) return;
            this.homePos.set(pos.pos());
            // If too far, lets path over.
            if (this.homePos.distToEntity(entity) > 2) this.setWalkTo(entity, this.homePos, 1, 0);
                // If we can't get into the hive, forget it as a hive.
            else if (!HiveSensor.tryAddToBeeHive(entity, pos.pos()))
                entity.getBrain().eraseMemory(BeeTasks.HIVE_POS.get());
        }
    }

    @Override
    public boolean doTask(Mob entity)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR.get());
        // We have nectar to return to the hive with.
        if (hasNectar.isPresent() && hasNectar.get()) return true;
        final Optional<Integer> hiveTimer = brain.getMemory(BeeTasks.OUT_OF_HIVE_TIMER.get());
        // This is our counter for if something angered us, and made is leave
        // the hive, if so, we don't return to hive.
        if (hiveTimer.isPresent() && hiveTimer.get() > 0) return false;
        // Return home if it is raining
        if (entity.level().isRaining()) return true;
        // Return home if it is night time
        return entity.level().isNight();
        // Otherwise don't return home
    }

}
