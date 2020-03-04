package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
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

    private float     agroChance = 1f;
    private int       timer      = 0;
    private final int maxTimer;

    @SafeVarargs
    public AIFindTarget(final LivingEntity entityIn, final Class<? extends LivingEntity>... targetClass)
    {
        this(entityIn, 1, -1, targetClass);
    }

    @SafeVarargs
    public AIFindTarget(final LivingEntity entityIn, final float agressionProbability,
            final Class<? extends LivingEntity>... targetClass)
    {
        this(entityIn, agressionProbability, -1, targetClass);
    }

    @SafeVarargs
    public AIFindTarget(final LivingEntity entityIn, final float agressionProbability, final int battleTime,
            final Class<? extends LivingEntity>... targetClass)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
        this.targetClass = targetClass;
        this.maxTimer = battleTime;
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
                if (!this.validClass(input) || !input.attackable() || !this.canBattle(input)) return false;
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

            private boolean canBattle(final LivingEntity input)
            {
                if (AIFindTarget.this.entity.getAttackingEntity() == input) return true;
                final IHasPokemobs other = CapabilityHasPokemobs.getHasPokemobs(input);
                if (other != null && other.getNextPokemob().isEmpty() && other.getOutID() == null)
                {
                    boolean found = false;
                    if (other.getOutID() == null)
                    {
                        final List<Entity> mobs = PCEventsHandler.getOutMobs(input, false);
                        if (!mobs.isEmpty()) for (final Entity mob : mobs)
                            if (mob.getDistanceSq(input) < 32 * 32)
                            {
                                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                                if (pokemob != null && !found)
                                {
                                    other.setOutMob(pokemob);
                                    found = true;
                                    break;
                                }
                            }
                    }
                    return found;
                }
                return true;
            }
        };
        this.agroChance = agressionProbability;
    }

    @Override
    public boolean shouldRun()
    {
        if (this.trainer.getTarget() != null)
        {
            final LivingEntity target = this.trainer.getTarget();
            // Check if timer has run out.
            if (this.maxTimer > 0 && this.timer++ >= this.maxTimer)
            {
                final IHasPokemobs other = CapabilityHasPokemobs.getHasPokemobs(target);
                // this is an ended battle, so we cancel both side.
                if (other != null) other.setTarget(null);

                // Lets reset revenge/battle targets as well.
                if (target.getRevengeTarget() == this.entity) target.setRevengeTarget(null);
                if (this.entity.getRevengeTarget() == target) this.entity.setRevengeTarget(null);
                // Reset attack targets as well.
                if (target instanceof MobEntity && ((MobEntity) target).getAttackTarget() == this.entity)
                    ((MobEntity) target).setAttackTarget(null);
                if (this.entity instanceof MobEntity && ((MobEntity) this.entity).getAttackTarget() == target)
                    ((MobEntity) this.entity).setAttackTarget(null);

                this.trainer.setTarget(null);
                this.trainer.resetPokemob();
                return false;
            }
            // Check if target is invalid.
            if (!target.isAlive())
            {
                this.trainer.setTarget(null);
                this.trainer.resetPokemob();
                return false;
            }
            // check if too far away
            if (this.entity.getDistance(target) > PokecubeCore.getConfig().chaseDistance)
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
        if (this.aiTracker != null && this.aiTracker.getAIState(IHasNPCAIStates.FIXEDDIRECTION) && this.trainer
                .getTarget() == null)
        {
            this.entity.setRotationYawHead(this.aiTracker.getDirection());
            this.entity.prevRotationYawHead = this.aiTracker.getDirection();
            this.entity.rotationYawHead = this.aiTracker.getDirection();
            this.entity.rotationYaw = this.aiTracker.getDirection();
            this.entity.prevRotationYaw = this.aiTracker.getDirection();
        }
        this.updateTask();
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
