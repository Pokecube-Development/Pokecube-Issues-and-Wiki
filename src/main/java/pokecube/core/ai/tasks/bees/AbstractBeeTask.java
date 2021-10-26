package pokecube.core.ai.tasks.bees;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

public abstract class AbstractBeeTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractBeeTask.mems.put(BeeTasks.HIVE_POS, MemoryStatus.VALUE_PRESENT);
    }

    public AbstractBeeTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    public AbstractBeeTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(pokemob, RootTask.merge(AbstractBeeTask.mems, mems));
    }

    public abstract boolean doTask();

    @Override
    public boolean shouldRun()
    {
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean beeCheck = this.pokemob.isRoutineEnabled(AIRoutine.BEEAI);
        return tameCheck && beeCheck && this.doTask();
    }

}
