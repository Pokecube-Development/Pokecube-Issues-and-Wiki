package pokecube.core.ai.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import thut.api.entity.ai.RootTask;

public class DummySetTask extends RootTask<LivingEntity>
{
    public DummySetTask()
    {
        super(ImmutableMap.of(MemoryModules.DUMMY.get(), MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        brain.setMemory(MemoryModules.DUMMY.get(), true);
        return false;
    }
}