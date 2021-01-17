package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class CarryEgg extends AntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only run this if we have an egg to carry
        CarryEgg.mems.put(AntTasks.EGG, MemoryModuleStatus.VALUE_PRESENT);
        CarryEgg.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_PRESENT);
    }

    EntityPokemobEgg egg;

    public CarryEgg(final IPokemob pokemob)
    {
        super(pokemob, CarryEgg.mems);
    }

    @Override
    public void reset()
    {
        this.egg = null;
    }

    @Override
    public void run()
    {
        this.egg.getPersistentData().putLong("__carried__", this.world.getGameTime() + 100);
        AntTasks.setJob(this.entity, AntJob.NONE);
        final GlobalPos dropOff = this.entity.getBrain().getMemory(AntTasks.WORK_POS).get();
        if (!this.entity.isRidingOrBeingRiddenBy(this.egg))
        {
            final double d = this.entity.getDistanceSq(this.egg);
            if (d > 2)
            {
                this.setWalkTo(this.egg, 1, 0);
                return;
            }
            else this.egg.startRiding(this.entity, true);
        }
        else
        {
            final BlockPos p = dropOff.getPos();
            final double d = p.distanceSq(this.entity.getPosition());
            if (d > 3) this.setWalkTo(p, 1, 0);
            else
            {
                this.egg.stopRiding();
                this.entity.getBrain().removeMemory(AntTasks.EGG);
                this.entity.getBrain().removeMemory(AntTasks.WORK_POS);
            }

        }
    }

    @Override
    boolean doTask()
    {
        // Don't do this if guard, our job will be set elsewhere if we need to
        // cancel guard to carry eggs
        if (this.job == AntJob.GUARD) return false;
        this.egg = this.entity.getBrain().getMemory(AntTasks.EGG).orElse(null);
        return this.egg != null;
    }

}
