package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor.AntNest;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

public abstract class AbstractAntTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractAntTask.mems.put(AntTasks.NEST_POS, MemoryModuleStatus.VALUE_PRESENT);
    }

    protected AntNest nest;
    protected AntJob  job;

    public AbstractAntTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    public AbstractAntTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems)
    {
        super(pokemob, mems);
    }

    abstract protected boolean doTask();

    @Override
    public boolean shouldRun()
    {
        this.job = AntTasks.getJob(this.entity);
        this.nest = NestSensor.getNest(this.entity).orElse(null);
        if (this.nest == null) return false;
        this.pokemob.setRoutineState(AIRoutine.MATE, false);
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean beeCheck = this.pokemob.isRoutineEnabled(AIRoutine.ANTAI);
        return tameCheck && beeCheck && this.doTask();
    }
}
