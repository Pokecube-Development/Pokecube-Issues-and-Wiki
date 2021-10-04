package pokecube.core.ai.npc;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ShuffledTask<E extends LivingEntity> extends MultiTask<E>
{
    public ShuffledTask(final List<Pair<Behavior<? super E>, Integer>> tasks)
    {
        this(ImmutableMap.of(), tasks);
    }

    public ShuffledTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMems,
            final List<Pair<Behavior<? super E>, Integer>> tasks)
    {
        super(neededMems, ImmutableSet.of(), MultiTask.Ordering.SHUFFLED, MultiTask.RunType.RUN_ONE, tasks);
    }
}
