package pokecube.core.ai.tasks.combat;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.IAICombat;

public abstract class FightTask extends TaskBase<MobEntity> implements IAICombat
{

    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        FightTask.MEMS.put(MemoryModuleType.HURT_BY_ENTITY, MemoryModuleStatus.VALUE_PRESENT);
    }

    public FightTask(final IPokemob pokemob)
    {
        super(pokemob, FightTask.MEMS);
    }

}
