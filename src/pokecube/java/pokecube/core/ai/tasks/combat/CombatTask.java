package pokecube.core.ai.tasks.combat;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.PokemobBehaviour;
import thut.api.entity.ai.IAICombat;
import thut.api.entity.ai.RootTask;

import java.util.Map;

public abstract class CombatTask extends PokemobBehaviour implements IAICombat
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        CombatTask.MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_PRESENT);
    }

    public CombatTask()
    {
        super(CombatTask.MEMS);
    }

    public CombatTask(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(RootTask.merge(CombatTask.MEMS, mems));
    }

    public final LivingEntity getAttackTarget(Mob entityIn)
    {
        var pokemob = PokemobCaps.getPokemobFor(entityIn);
        if (pokemob.getMoveStats().targetEnemy != null) return pokemob.getMoveStats().targetEnemy;
        return BrainUtils.getAttackTarget(entityIn);
    }
}
