package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.interfaces.IPokemob;

public abstract class AbstractWorkTask extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only run this if we have an egg to carry
        AbstractWorkTask.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_PRESENT);
    }

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

    abstract protected boolean shouldWork();

    @Override
    public final boolean doTask()
    {
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getPos())) return false;
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Integer> hiveTimer = brain.getMemory(AntTasks.OUT_OF_HIVE_TIMER);
        final int timer = hiveTimer.orElseGet(() -> 0);
        if (timer < -2400) return false;
        if (!this.validJob.test(this.job)) return false;
        this.pokemob.setPokemonNickname("" + this.job);
        return this.shouldWork();
    }
}
