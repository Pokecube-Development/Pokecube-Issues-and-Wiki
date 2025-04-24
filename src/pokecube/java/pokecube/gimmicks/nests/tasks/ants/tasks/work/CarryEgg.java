package pokecube.gimmicks.nests.tasks.ants.tasks.work;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractWorkTask;
import thut.api.Tracker;

import java.util.Map;
import java.util.function.Predicate;

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

    public CarryEgg()
    {
        super(CarryEgg.mems, CarryEgg.EGG_CARRY);
    }

    @Override
    public void reset(Mob entity)
    {
        this.egg = null;
        entity.getNavigation().resetMaxVisitedNodesMultiplier();
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        this.egg.getPersistentData().putLong("__carried__", Tracker.instance().getTick() + 100);
        AntTasks.setJob(entity, AntJob.NONE);
        final Brain<?> brain = entity.getBrain();
        final GlobalPos dropOff = brain.getMemory(MemoryModules.WORK_POS.get()).get();
        entity.getNavigation().setMaxVisitedNodesMultiplier(10);
        if (!entity.hasPassenger(this.egg))
        {
            final double d = entity.distanceToSqr(this.egg);
            if (d > 2)
            {
                this.setWalkTo(entity, this.egg, 1, 0);
            }
            else this.egg.startRiding(entity, true);
        }
        else
        {
            this.nest.hab.eggs.add(this.egg.getUUID());
            final BlockPos p = dropOff.pos();
            final double d = p.distSqr(entity.blockPosition());
            if (d > 3) this.setWalkTo(entity, p, 1, 0);
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
    protected boolean shouldWork(Mob entity)
    {
        this.egg = entity.getBrain().getMemory(MemoryModules.EGG.get()).orElse(null);
        return this.egg != null;
    }
}
