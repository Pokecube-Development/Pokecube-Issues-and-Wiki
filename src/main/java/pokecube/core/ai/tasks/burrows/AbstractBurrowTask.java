package pokecube.core.ai.tasks.burrows;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

public abstract class AbstractBurrowTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractBurrowTask.mems.put(BurrowTasks.BURROW, MemoryModuleStatus.VALUE_PRESENT);
    }

    protected Burrow burrow;

    private int check_timer = 0;

    public AbstractBurrowTask(final IPokemob pokemob)
    {
        super(pokemob, AbstractBurrowTask.mems);
    }

    public AbstractBurrowTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        super(pokemob, RootTask.merge(AbstractBurrowTask.mems, neededMems));
    }

    abstract protected boolean doTask();

    @Override
    public boolean shouldRun()
    {
        if (this.burrow == null || this.check_timer-- < 0)
        {
            this.burrow = BurrowSensor.getNest(this.entity).orElse(null);
            this.check_timer = 1200;
        }
        if (this.burrow == null) return false;
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean aiEnabled = this.pokemob.isRoutineEnabled(AIRoutine.BURROWS);
        return tameCheck && aiEnabled && this.doTask();
    }

}
