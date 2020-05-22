package pokecube.adventures.ai.tasks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityPredicates;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;

public class AIRetaliate extends AITrainerBase implements ITargetWatcher
{

    public AIRetaliate(final LivingEntity entityIn)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
    }

    @Override
    public boolean shouldRun()
    {
        if (this.trainer.getTarget() != null) return false;
        // Dead trainers can't fight.
        if (!this.entity.isAlive()) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (this.trainer.getCooldown() > this.entity.getEntityWorld().getGameTime() || !this.trainer.isAgressive())
            return false;
        final LivingEntity target = this.entity.getAttackingEntity();
        return this.isValidTarget(target);
    }

    @Override
    public void tick()
    {
        this.updateTask();
    }

    public void updateTask()
    {
        // If target is valid, return.
        if (this.trainer.getTarget() != null) return;
        final LivingEntity target = this.entity.getAttackingEntity();

        if (target != null)
        {
            // Set trainers target
            this.trainer.setTarget(target);
            // Ensure no cooldown
            this.trainer.setAttackCooldown(-1);
        }
    }

    @Override
    public boolean isValidTarget(final LivingEntity target)
    {
        if (target == null) return false;
        if (!(target.isAlive() && this.entity.canEntityBeSeen(target))) return false;
        if (!EntityPredicates.CAN_AI_TARGET.test(target)) return false;
        if (target != null && target.getLastAttackedEntity() == this.entity && target
                .getLastAttackedEntityTime() < target.ticksExisted + 20) return true;
        final int timer = this.entity.getRevengeTimer();
        final int age = this.entity.ticksExisted - 50;
        return this.entity.getAttackingEntity() == target && timer > age;
    }
}
