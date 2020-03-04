package pokecube.adventures.ai.tasks;

import net.minecraft.entity.LivingEntity;
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
        return target != null && target.isAlive() && this.entity.canEntityBeSeen(target);
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
    public boolean validTargetSet(final LivingEntity target)
    {
        return this.entity.getAttackingEntity() == target;
    }
}
