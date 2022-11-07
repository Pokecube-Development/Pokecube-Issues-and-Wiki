package pokecube.core.ai.tasks.combat;

import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import thut.api.entity.ai.IAICombat;
import thut.api.entity.ai.RootTask;

public abstract class CombatTask extends TaskBase implements IAICombat
{

    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        CombatTask.MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_PRESENT);
    }

    public CombatTask(final IPokemob pokemob)
    {
        super(pokemob, CombatTask.MEMS);
    }

    public CombatTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(pokemob, RootTask.merge(CombatTask.MEMS, mems));
    }

    @Override
    protected Comparator<MemoryModuleType<?>> getCheckOrder()
    {
        return (a, b) -> {
            int a1 = a == MemoryModules.ATTACKTARGET.get() ? 1 : 0;
            int b1 = b == MemoryModules.ATTACKTARGET.get() ? 1 : 0;
            return Integer.compare(a1, b1);
        };
    }
}
