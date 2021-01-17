package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class CarryEgg extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only run this if we have an egg to carry
        CarryEgg.mems.put(AntTasks.EGG, MemoryModuleStatus.VALUE_PRESENT);
    }

    // Any that is not a guard ant is allowed to carry eggs
    private static final Predicate<AntJob> EGG_CARRY = j -> j != AntJob.GUARD;

    EntityPokemobEgg egg;

    public CarryEgg(final IPokemob pokemob)
    {
        super(pokemob, CarryEgg.mems, CarryEgg.EGG_CARRY);
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
            if (!this.nest.hab.eggs.contains(this.egg.getUniqueID())) this.nest.hab.eggs.add(this.egg.getUniqueID());
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
    protected boolean shouldWork()
    {
        this.egg = this.entity.getBrain().getMemory(AntTasks.EGG).orElse(null);
        return this.egg != null;
    }

}
