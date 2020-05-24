package pokecube.adventures.ai.tasks.battle.agro;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.EntityPredicates;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;

public class AgroTargets extends BaseAgroTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        AgroTargets.MEMS.put(MemoryModules.ATTACKTARGET, MemoryModuleStatus.VALUE_ABSENT);
    }

    // Predicated to return true for invalid targets
    final Predicate<LivingEntity> validTargets;

    // This is whether the ai should run for the current task holder
    private Predicate<LivingEntity> shouldRun = e -> true;

    public AgroTargets(final LivingEntity trainer, final float agressionProbability, final int battleTime,
            final Predicate<LivingEntity> validTargets)
    {
        super(trainer, agressionProbability, battleTime);
        this.validTargets = validTargets;
    }

    public AgroTargets setRunCondition(final Predicate<LivingEntity> shouldRun)
    {
        this.shouldRun = shouldRun;
        return this;
    }

    @Override
    public boolean isValidTarget(final LivingEntity target)
    {
        if (!this.shouldRun.test(this.entity)) return false;
        if (!EntityPredicates.CAN_AI_TARGET.test(target)) return false;
        if (!this.trainer.canBattle(target, false).test()) return false;
        final int dist = PokecubeAdv.config.trainer_crowding_radius;
        final int num = PokecubeAdv.config.trainer_crowding_number;
        if (TrainerTracker.countTrainers(this.world, Vector3.getNewVector().set(this.entity), dist) > num) return false;
        return this.validTargets.test(target);
    }
}
