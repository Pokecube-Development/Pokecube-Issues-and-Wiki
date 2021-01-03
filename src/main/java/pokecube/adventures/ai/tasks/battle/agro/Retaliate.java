package pokecube.adventures.ai.tasks.battle.agro;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.EntityPredicates;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;

public class Retaliate extends BaseAgroTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        Retaliate.MEMS.put(MemoryModules.ATTACKTARGET, MemoryModuleStatus.VALUE_ABSENT);
    }

    public Retaliate(final LivingEntity trainer)
    {
        super(trainer, 1, -1);
        this.trainer.addTargetWatcher(this);
    }

    @Override
    public boolean ignoreHasBattled(final LivingEntity target)
    {
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemory(MemoryModuleType.HURT_BY_ENTITY)) return false;
        return brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() == target;
    }

    @Override
    public boolean isValidTarget(final LivingEntity target)
    {
        if (target == null) return false;
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemory(MemoryModuleType.HURT_BY_ENTITY)) return false;
        if (!(target.isAlive() && BrainUtils.canSee(this.entity, target))) return false;
        if (!EntityPredicates.CAN_AI_TARGET.test(target)) return false;
        return brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() == target;
    }

}
