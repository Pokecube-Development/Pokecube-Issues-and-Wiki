package pokecube.adventures.ai.tasks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class BaseTask extends RootTask<LivingEntity>
{
    public BaseTask(final LivingEntity trainer, final Map<MemoryModuleType<?>, MemoryStatus> requiredMemoryStateIn)
    {
        super(trainer, requiredMemoryStateIn);
    }

    protected IHasPokemobs getTrainer(LivingEntity trainer)
    {
        return trainer.getData(TrainerCaps.TRAINER);
    }

    protected IHasNPCAIStates getAIStates(LivingEntity trainer)
    {
        return trainer.getData(TrainerCaps.AISTATES);
    }

    protected IHasMessages getMessages(LivingEntity trainer)
    {
        return trainer.getData(TrainerCaps.MESSAGES);
    }
}
