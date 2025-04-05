package pokecube.gimmicks.nests.tasks.ants.tasks.work;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractWorkTask;
import thut.api.Tracker;

public class CarryEgg extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Only run this if we have an egg to carry
        CarryEgg.mems.put(MemoryModules.EGG.get(), MemoryStatus.VALUE_PRESENT);
        CarryEgg.mems.put(MemoryModules.GOING_HOME.get(), MemoryStatus.VALUE_ABSENT);
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
        final GlobalPos dropOff = brain.getMemory(MemoryModules.WORK_POS.get()).get();
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
                brain.eraseMemory(MemoryModules.EGG.get());
                brain.eraseMemory(MemoryModules.WORK_POS.get());
                brain.setMemory(MemoryModules.GOING_HOME.get(), true);
            }
        }
    }

    @Override
    protected boolean shouldWork()
    {
        this.egg = this.entity.getBrain().getMemory(MemoryModules.EGG.get()).orElse(null);
        return this.egg != null;
    }
}
