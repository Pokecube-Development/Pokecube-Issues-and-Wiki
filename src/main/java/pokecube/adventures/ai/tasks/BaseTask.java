package pokecube.adventures.ai.tasks;

import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.ai.brain.RootTask;

public abstract class BaseTask extends RootTask<LivingEntity>
{
    protected ServerLevel world;
    // The trainer Entity
    protected final IHasPokemobs    trainer;
    protected final IHasNPCAIStates aiTracker;
    protected final IHasMessages    messages;
    protected final boolean         valid;

    public BaseTask(final LivingEntity trainer,
            final Map<MemoryModuleType<?>, MemoryStatus> requiredMemoryStateIn)
    {
        super(trainer, requiredMemoryStateIn);
        this.world = (ServerLevel) trainer.getCommandSenderWorld();
        this.aiTracker = TrainerCaps.getNPCAIStates(trainer);
        this.trainer = TrainerCaps.getHasPokemobs(trainer);
        this.messages = TrainerCaps.getMessages(trainer);
        this.valid = trainer != null && this.aiTracker != null && this.messages != null;
    }
}
