package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.tasks.AbstractWorkTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.Tracker;

public class CarryEgg extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only run this if we have an egg to carry
        CarryEgg.mems.put(AntTasks.EGG, MemoryModuleStatus.VALUE_PRESENT);
        CarryEgg.mems.put(AntTasks.GOING_HOME, MemoryModuleStatus.VALUE_ABSENT);
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
        this.entity.getNavigation().resetMaxVisitedNodesMultiplier();
    }

    @Override
    public void run()
    {
        this.egg.getPersistentData().putLong("__carried__", Tracker.instance().getTick() + 100);
        AntTasks.setJob(this.entity, AntJob.NONE);
        final Brain<?> brain = this.entity.getBrain();
        final GlobalPos dropOff = brain.getMemory(AntTasks.WORK_POS).get();
        this.entity.getNavigation().setMaxVisitedNodesMultiplier(10);
        if (!this.entity.hasPassenger(this.egg))
        {
            final double d = this.entity.distanceToSqr(this.egg);
            if (d > 2)
            {
                this.setWalkTo(this.egg, 1, 0);
                return;
            }
            else this.egg.startRiding(this.entity, true);
        }
        else
        {
            if (!this.nest.hab.eggs.contains(this.egg.getUUID())) this.nest.hab.eggs.add(this.egg.getUUID());
            final BlockPos p = dropOff.pos();
            final double d = p.distSqr(this.entity.blockPosition());
            if (d > 3) this.setWalkTo(p, 1, 0);
            else
            {
                this.egg.stopRiding();
                brain.eraseMemory(AntTasks.EGG);
                brain.eraseMemory(AntTasks.WORK_POS);
                brain.setMemory(AntTasks.GOING_HOME, true);
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
