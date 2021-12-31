package pokecube.core.ai.tasks.ants.tasks;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.ants.sensors.NestSensor.AntNest;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.entity.ai.RootTask;

public abstract class AbstractAntTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractAntTask.mems.put(AntTasks.NEST_POS, MemoryStatus.VALUE_PRESENT);
    }

    protected AntNest nest;
    protected AntJob  job;

    private int check_timer = 0;

    public AbstractAntTask(final IPokemob pokemob)
    {
        super(pokemob, AbstractAntTask.mems);
    }

    public AbstractAntTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(pokemob, RootTask.merge(AbstractAntTask.mems, mems));
    }

    abstract protected boolean doTask();

    @Override
    public boolean shouldRun()
    {
        this.job = AntTasks.getJob(this.entity);
        if (this.nest == null || this.check_timer-- < 0)
        {
            this.nest = NestSensor.getNest(this.entity).orElse(null);
            this.check_timer = 1200;
        }
        if (this.nest == null) return false;
        this.pokemob.setRoutineState(AIRoutine.MATE, false);
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean aiEnabled = this.pokemob.isRoutineEnabled(AIRoutine.ANTAI);
        return tameCheck && aiEnabled && this.doTask();
    }
}
