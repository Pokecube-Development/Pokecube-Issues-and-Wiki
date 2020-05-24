package pokecube.adventures.ai.tasks.idle;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.adventures.ai.tasks.BaseTask;

public class Mate extends BaseTask
{

    public Mate(final LivingEntity trainer, final Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn)
    {
        super(trainer, requiredMemoryStateIn);
        // TODO Auto-generated constructor stub
    }

}
