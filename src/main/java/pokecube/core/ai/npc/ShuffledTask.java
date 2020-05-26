package pokecube.core.ai.npc;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;

public class ShuffledTask<E extends LivingEntity> extends MultiTask<E>
{
    public ShuffledTask(final List<Pair<Task<? super E>, Integer>> tasks)
    {
        this(ImmutableMap.of(), tasks);
    }

    public ShuffledTask(final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems,
            final List<Pair<Task<? super E>, Integer>> tasks)
    {
        super(neededMems, ImmutableSet.of(), MultiTask.Ordering.SHUFFLED, MultiTask.RunType.RUN_ONE, tasks);
    }
}
