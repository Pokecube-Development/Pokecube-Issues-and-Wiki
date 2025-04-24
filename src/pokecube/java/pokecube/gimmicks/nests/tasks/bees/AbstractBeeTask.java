package pokecube.gimmicks.nests.tasks.bees;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class AbstractBeeTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractBeeTask.mems.put(BeeTasks.HIVE_POS.get(), MemoryStatus.VALUE_PRESENT);
    }

    public AbstractBeeTask()
    {
        super();
    }

    public AbstractBeeTask(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(RootTask.merge(AbstractBeeTask.mems, mems));
    }

    public abstract boolean doTask(Mob entity);

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        final boolean tameCheck = pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean beeCheck = pokemob.isRoutineEnabled(BeeTasks.BEEAI);
        return tameCheck && beeCheck && this.doTask(entity);
    }

}
