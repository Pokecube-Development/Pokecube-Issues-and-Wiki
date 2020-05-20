package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.ai.tasks.IRunnable;
import thut.api.entity.ai.IAIRunnable;

public class AITrainerBase implements IAIRunnable
{
    ServerWorld world;
    // The trainer Entity
    final LivingEntity        entity;
    final IHasPokemobs        trainer;
    final IHasNPCAIStates     aiTracker;
    final IHasMessages        messages;
    final boolean             valid;
    int                       noSeeTicks = 0;
    protected List<IRunnable> toRun      = Lists.newArrayList();

    int priority = 0;
    int mutex    = 0;

    public AITrainerBase(final LivingEntity trainer)
    {
        this.entity = trainer;
        this.world = (ServerWorld) trainer.getEntityWorld();
        this.aiTracker = TrainerCaps.getNPCAIStates(trainer);
        this.trainer = TrainerCaps.getHasPokemobs(trainer);
        this.messages = TrainerCaps.getMessages(trainer);
        this.valid = trainer != null && this.aiTracker != null && this.messages != null;
    }

    @Override
    public void finish()
    {
        this.toRun.forEach(w -> w.run(this.world));
        this.toRun.clear();
    }

    @Override
    public int getMutex()
    {
        return this.mutex;
    }

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
    }

    @Override
    public IAIRunnable setMutex(final int mutex)
    {
        this.mutex = mutex;
        return this;
    }

    @Override
    public IAIRunnable setPriority(final int prior)
    {
        this.priority = prior;
        return this;
    }

    @Override
    public boolean shouldRun()
    {
        return true;
    }
}
