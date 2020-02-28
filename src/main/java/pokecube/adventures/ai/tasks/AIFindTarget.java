package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.maths.Vector3;

public class AIFindTarget extends AITrainerBase implements ITargetWatcher
{
    // The entity (normally a player) that is the target of this trainer.
    final Class<? extends LivingEntity>[] targetClass;
    // Predicated to return true for invalid targets
    final Predicate<LivingEntity> validTargets;

    private float agroChance = 1f;

    @SafeVarargs
    public AIFindTarget(final LivingEntity entityIn, final Class<? extends LivingEntity>... targetClass)
    {
        this(entityIn, 1, targetClass);
    }

    @SafeVarargs
    public AIFindTarget(final LivingEntity entityIn, final float agressionProbability,
            final Class<? extends LivingEntity>... targetClass)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
        this.targetClass = targetClass;
        this.validTargets = new Predicate<LivingEntity>()
        {
            @Override
            public boolean apply(final LivingEntity input)
            {
                // If the input has attacked us recently, then return true
                // regardless of following checks.
                if (input.getLastAttackedEntity() == AIFindTarget.this.entity && input.ticksExisted - input
                        .getLastAttackedEntityTime() < 50) return true;
                // Only target valid classes.
                if (!this.validClass(input) || !input.attackable()) return false;
                final IOwnable ownable = OwnableCaps.getOwnable(input);
                // Don't target pets
                if (ownable != null && ownable.getOwner() == entityIn) return false;
                // Don't target invulnerable players (spectator/creative)
                if (input instanceof PlayerEntity && (((PlayerEntity) input).abilities.isCreativeMode
                        || ((PlayerEntity) input).isSpectator())) return false;
                // Return true if player can battle the input.
                return AIFindTarget.this.trainer.canBattle(input);
            }

            private boolean validClass(final LivingEntity input)
            {
                for (final Class<? extends LivingEntity> s : targetClass)
                    if (s.isInstance(input)) return true;
                return false;
            }
        };
        this.agroChance = agressionProbability;
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
        if (!this.entity.isAlive() || this.entity.ticksExisted % 20 != 0) return false;
        // Permfriendly trainers shouldn't fight.
        if (this.aiTracker != null && this.aiTracker.getAIState(IHasNPCAIStates.PERMFRIENDLY)) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (this.trainer.getCooldown() > this.entity.getEntityWorld().getGameTime() || !this.trainer.isAgressive())
            return false;
        return true;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.aiTracker != null && this.aiTracker.getAIState(IHasNPCAIStates.FIXEDDIRECTION) && this.trainer
                .getTarget() == null)
        {
            this.entity.setRotationYawHead(this.aiTracker.getDirection());
            this.entity.prevRotationYawHead = this.aiTracker.getDirection();
            this.entity.rotationYawHead = this.aiTracker.getDirection();
            this.entity.rotationYaw = this.aiTracker.getDirection();
            this.entity.prevRotationYaw = this.aiTracker.getDirection();
        }
        if (this.shouldExecute()) this.updateTask();
    }

    public void updateTask()
    {
        // If target is valid, return.
        if (this.trainer.getTarget() != null) return;

        // Check random chance of actually aquiring a target.
        if (Math.random() > this.agroChance) return;

        // Look for targets
        final Vector3 here = Vector3.getNewVector().set(this.entity);
        LivingEntity target = null;
        final int sight = this.trainer.getAgressDistance();
        targetTrack:
        {
            here.addTo(0, this.entity.getEyeHeight(), 0);
            final Vector3 look = Vector3.getNewVector().set(this.entity.getLook(1));
            here.addTo(look);
            look.scalarMultBy(sight);
            look.addTo(here);
            final List<LivingEntity> targets = MovesUtils.targetsHit(this.entity, look);

            if (!targets.isEmpty()) for (final Object o : targets)
            {
                final LivingEntity e = (LivingEntity) o;
                final double dist = e.getDistance(this.entity);
                // Only visible or valid targets.
                if (this.validTargetSet(e) && dist < sight)
                {
                    target = e;
                    break targetTrack;
                }
            }
        }

        // If no target, return false.
        if (target == null)
        {
            // If trainer was in battle (any of these 3) reset trainer before
            // returning.
            if (this.trainer.getOutMob() != null || this.aiTracker.getAIState(IHasNPCAIStates.THROWING)
                    || this.aiTracker.getAIState(IHasNPCAIStates.INBATTLE)) this.trainer.resetPokemob();
            return;
        }
        // Set trainers target
        this.trainer.setTarget(target);
    }

    @Override
    public boolean validTargetSet(final LivingEntity target)
    {
        return this.validTargets.apply(target);
    }
}
