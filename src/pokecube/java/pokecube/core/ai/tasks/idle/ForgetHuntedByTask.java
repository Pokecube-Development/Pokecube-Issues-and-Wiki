package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.PokemobBehaviour;

import java.util.Map;

public class ForgetHuntedByTask extends PokemobBehaviour
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.HUNTED_BY.get(), MemoryStatus.VALUE_PRESENT);
    }

    int fleeingTicks = 0;

    final int duration;

    public ForgetHuntedByTask(final int duration)
    {
        super(MEMS);
        this.duration = duration;
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.fleeingTicks = 0;
        entityIn.getBrain().eraseMemory(MemoryModules.HUNTED_BY.get());
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        this.fleeingTicks++;
    }

    @Override
    public boolean shouldRun(Mob entityIn)
    {
        return entityIn.getBrain().hasMemoryValue(MemoryModules.HUNTED_BY.get()) && this.fleeingTicks < this.duration;
    }

}
