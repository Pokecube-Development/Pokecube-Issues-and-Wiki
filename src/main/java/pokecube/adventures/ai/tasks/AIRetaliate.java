package pokecube.adventures.ai.tasks;

import net.minecraft.entity.LivingEntity;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.PokecubeCore;

public class AIRetaliate extends AITrainerBase implements ITargetWatcher
{

    public AIRetaliate(final LivingEntity entityIn)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
    }

    public boolean shouldExecute()
    {
        if (this.trainer.getTarget() != null)
        { // Check if target is invalid.
            if (this.trainer.getTarget() != null && !this.trainer.getTarget().isAlive())
            {
                this.trainer.setTarget(null);
                this.trainer.resetPokemob();
                return false;
            }
            // check if too far away
            if (this.entity.getDistance(this.trainer.getTarget()) > PokecubeCore.getConfig().chaseDistance)
            {
                this.trainer.setTarget(null);
                this.trainer.resetPokemob();
                return false;
            }
            return false;
        }
        // Dead trainers can't fight.
        if (!this.entity.isAlive()) return false;
        // Permfriendly trainers shouldn't fight.
        if (this.aiTracker != null && this.aiTracker.getAIState(IHasNPCAIStates.PERMFRIENDLY)) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (this.trainer.getCooldown() > this.entity.getEntityWorld().getGameTime() || !this.trainer.isAgressive())
            return false;
        final LivingEntity target = this.entity.getAttackingEntity();
        return target != null && target.isAlive() && this.entity.canEntityBeSeen(target);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.shouldExecute()) this.updateTask();
    }

    public void updateTask()
    {
        // If target is valid, return.
        if (this.trainer.getTarget() != null) return;
        final LivingEntity target = this.entity.getAttackingEntity();

        if (target != null)
            // Set trainers target
            this.trainer.setTarget(target);
    }

    @Override
    public boolean validTargetSet(final LivingEntity target)
    {
        return this.entity.getAttackingEntity() == target;
    }
}
