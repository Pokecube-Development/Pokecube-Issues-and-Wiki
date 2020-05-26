package pokecube.adventures.ai.tasks;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.ai.brain.RootTask;

public abstract class BaseTask extends RootTask<LivingEntity>
{
    protected ServerWorld world;
    // The trainer Entity
    protected final IHasPokemobs    trainer;
    protected final IHasNPCAIStates aiTracker;
    protected final IHasMessages    messages;
    protected final boolean         valid;

    public BaseTask(final LivingEntity trainer,
            final Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn)
    {
        super(trainer, requiredMemoryStateIn);
        this.world = (ServerWorld) trainer.getEntityWorld();
        this.aiTracker = TrainerCaps.getNPCAIStates(trainer);
        this.trainer = TrainerCaps.getHasPokemobs(trainer);
        this.messages = TrainerCaps.getMessages(trainer);
        this.valid = trainer != null && this.aiTracker != null && this.messages != null;
    }
}
