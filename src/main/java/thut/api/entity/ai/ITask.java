package thut.api.entity.ai;

import java.util.Map;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

public interface ITask extends IAIRunnable
{
    Map<MemoryModuleType<?>, MemoryModuleStatus> getNeededMemories();
}
