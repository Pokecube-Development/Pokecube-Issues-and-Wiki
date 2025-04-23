package pokecube.adventures.ai.tasks.battle.agro;

import java.util.function.Predicate;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.trainers.IHasPokemobs;

public class AgroTargets extends BaseAgroTask
{
    // Predicated to return true for valid targets
    final Predicate<LivingEntity> validTargets;

    // This is whether the ai should run for the current task holder
    private Predicate<LivingEntity> shouldRun = e -> true;

    public AgroTargets(final LivingEntity trainer, final float agressionProbability, final int battleTime,
            final Predicate<LivingEntity> validTargets)
    {
        super(trainer, agressionProbability, battleTime);
        this.validTargets = validTargets;
    }

    /**
     * The argument passed into this predicate is the current mob, so it should
     * only consider it for checks!
     */
    public AgroTargets setRunCondition(final Predicate<LivingEntity> shouldRun)
    {
        this.shouldRun = shouldRun;
        return this;
    }

    @Override
    public boolean isValidTarget(IHasPokemobs trainer, final LivingEntity target)
    {
        if (!this.validTargets.test(target)) return false;
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) return false;
        if (!this.shouldRun.test(this.entity)) return false;
        return trainer.canBattle(target, false).test();
    }
}
