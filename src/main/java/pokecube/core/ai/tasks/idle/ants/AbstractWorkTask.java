package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAIRunnable;

public abstract class AbstractWorkTask extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        AbstractWorkTask.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_PRESENT);
        AbstractWorkTask.mems.put(AntTasks.GOING_HOME, MemoryModuleStatus.VALUE_ABSENT);
    }
    protected StoreTask storage = null;

    private final Predicate<AntJob> validJob;

    public AbstractWorkTask(final IPokemob pokemob, final Predicate<AntJob> job)
    {
        super(pokemob);
        this.validJob = job;
    }

    public AbstractWorkTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems,
            final Predicate<AntJob> job)
    {
        super(pokemob, mems);
        this.validJob = job;
    }

    protected boolean shouldWork()
    {
        return true;
    }

    @Override
    public final boolean doTask()
    {
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getPos())) return false;
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemory(AntTasks.WORK_POS)) return false;

        if (this.storage == null) for (final IAIRunnable run : this.pokemob.getTasks())
            if (run instanceof StoreTask)
            {
                this.storage = (StoreTask) run;
                this.pokemob.setRoutineState(AIRoutine.STORE, true);
                this.storage.storageLoc = this.nest.nest.getPos();
                this.storage.berryLoc = this.nest.nest.getPos();
                break;
            }
        if (this.storage == null) return false;
        if (!this.validJob.test(this.job)) return false;
        return this.shouldWork();
    }
}
