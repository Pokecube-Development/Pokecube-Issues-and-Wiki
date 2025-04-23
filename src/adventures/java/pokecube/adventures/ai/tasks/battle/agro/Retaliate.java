package pokecube.adventures.ai.tasks.battle.agro;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;

import java.util.Map;

public class Retaliate extends BaseAgroTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        Retaliate.MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_ABSENT);
    }

    public Retaliate(final LivingEntity trainer)
    {
        super(trainer, 1, -1);
        this.getTrainer(trainer).addTargetWatcher(this);
    }

    @Override
    public boolean ignoreHasBattled(IHasPokemobs trainer, LivingEntity target)
    {
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)) return false;
        return brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() == target;
    }

    @Override
    public boolean isValidTarget(IHasPokemobs trainer, LivingEntity target)
    {
        if (target == null) return false;
        final Brain<?> brain = this.entity.getBrain();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) return false;
        if (!brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)) return false;
        if (!(target.isAlive() && BrainUtils.canSee(this.entity, target))) return false;
        return brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() == target;
    }

}
