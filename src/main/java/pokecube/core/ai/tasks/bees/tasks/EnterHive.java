package pokecube.core.ai.tasks.bees.tasks;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import pokecube.core.ai.tasks.bees.AbstractBeeTask;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.bees.sensors.HiveSensor;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class EnterHive extends AbstractBeeTask
{
    final Vector3 homePos = new Vector3();

    public EnterHive(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.homePos.clear();
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> pos_opt = brain.getMemory(BeeTasks.HIVE_POS);
        if (pos_opt.isPresent())
        {
            final Level world = this.entity.getLevel();
            final GlobalPos pos = pos_opt.get();
            final boolean clearHive = pos.dimension() != world.dimension();
            // This will be cleared by CheckHive, so lets just exit here.
            if (clearHive) return;
            this.homePos.set(pos.pos());
            // If too far, lets path over.
            if (this.homePos.distToEntity(this.entity) > 2) this.setWalkTo(this.homePos, 1, 0);
            // If we can't get into the hive, forget it as a hive.
            else if (!HiveSensor.tryAddToBeeHive(this.entity, pos.pos()))
                this.entity.getBrain().eraseMemory(BeeTasks.HIVE_POS);
        }
    }

    @Override
    public boolean doTask()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
        // We have nectar to return to the hive with.
        if (hasNectar.isPresent() && hasNectar.get()) return true;
        final Optional<Integer> hiveTimer = brain.getMemory(BeeTasks.OUT_OF_HIVE_TIMER);
        // This is our counter for if something angered us, and made is leave
        // the hive, if so, we don't return to hive.
        if (hiveTimer.isPresent() && hiveTimer.get() > 0) return false;
        // Return home if it is raining
        if (this.entity.getLevel().isRaining()) return true;
        // Return home if it is night time
        if (this.entity.getLevel().isNight()) return true;
        // Otherwise don't return home
        return false;
    }

}
