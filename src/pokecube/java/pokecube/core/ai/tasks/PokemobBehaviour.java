package pokecube.core.ai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class PokemobBehaviour extends RootTask<Mob> implements ITask
{
    public PokemobBehaviour(Map<MemoryModuleType<?>, MemoryStatus> entryCondition)
    {
        super(entryCondition, Integer.MAX_VALUE);
    }

    int priority = 0;

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public void reset(Mob entityIn)
    {

    }

    @Override
    protected void stop(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        this.reset(entityIn);
    }

    @Override
    protected boolean canStillUse(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        return this.shouldRun(entityIn);
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel level, final Mob owner)
    {
        return this.shouldRun(owner);
    }

    @Override
    public IAIRunnable setPriority(final int prior)
    {
        this.priority = prior;
        return this;
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        return true;
    }
}
