package pokecube.adventures.ai.tasks.idle;

import java.util.Map;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.adventures.ai.tasks.BaseTask;

public class Mate extends BaseTask
{

    public Mate(final LivingEntity trainer, final Map<MemoryModuleType<?>, MemoryStatus> requiredMemoryStateIn)
    {
        super(trainer, requiredMemoryStateIn);
        // TODO Auto-generated constructor stub
    }

}
