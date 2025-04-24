package pokecube.gimmicks.nests.tasks.ants.tasks;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor.AntNest;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class AbstractAntTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractAntTask.mems.put(MemoryModules.NEST_POS.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected AntNest nest;
    protected AntJob job;

    private int check_timer = 0;

    public AbstractAntTask()
    {
        super(AbstractAntTask.mems);
    }

    public AbstractAntTask(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(RootTask.merge(AbstractAntTask.mems, mems));
    }

    abstract protected boolean doTask(Mob entity);

    @Override
    public boolean shouldRun(Mob entity)
    {
        this.job = AntTasks.getJob(entity);
        if (this.nest == null || this.check_timer-- < 0)
        {
            this.nest = NestSensor.getNest(entity).orElse(null);
            this.check_timer = 1200;
        }
        if (this.nest == null) return false;
        var pokemob = PokemobCaps.getPokemobFor(entity);
        pokemob.setRoutineState(AIRoutine.MATE, false);
        final boolean tameCheck = pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean aiEnabled = pokemob.isRoutineEnabled(AntTasks.ANTAI);
        return tameCheck && aiEnabled && this.doTask(entity);
    }
}
