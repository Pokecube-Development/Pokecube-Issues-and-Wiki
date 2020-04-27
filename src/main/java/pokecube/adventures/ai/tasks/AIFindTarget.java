package pokecube.adventures.ai.tasks;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.EntityPredicates;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.AITools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.maths.Vector3;

public class AIFindTarget extends AITrainerBase implements ITargetWatcher
{
    public static boolean canBattle(final LivingEntity input, final LivingEntity mobIn)
    {

        if (input != null && input.getLastAttackedEntity() == mobIn) return true;
        if (mobIn.getRevengeTarget() != null && mobIn.getRevengeTarget() == input) return true;
        final IHasPokemobs other = TrainerCaps.getHasPokemobs(input);
        if (other == null) return true;
        if (other.getTarget() != null && other.getTarget() != mobIn) return false;
        if (other.getNextPokemob().isEmpty() && other.getOutID() == null)
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

    @SafeVarargs
    public static Predicate<LivingEntity> match(final LivingEntity entityIn, final boolean allowTamed,
            final Class<? extends LivingEntity>... targetClass)
    {
        return new Predicate<LivingEntity>()
        {
            IHasPokemobs trainer = TrainerCaps.getHasPokemobs(entityIn);

            @Override
            public boolean test(final LivingEntity input)
            {
                if (!AITools.validTargets.test(input)) return false;

                // If the input has attacked us recently, then return true
                // regardless of following checks.
                if (input.getLastAttackedEntity() == entityIn && input.ticksExisted - input
                        .getLastAttackedEntityTime() < 50) return true;

                final boolean validClass = this.validClass(input);
                final boolean validAgro = input.attackable();
                final boolean canBattle = AIFindTarget.canBattle(input, entityIn);

                // Only target valid classes.
                if (!validClass || !validAgro || !canBattle) return false;
                final IOwnable ownable = OwnableCaps.getOwnable(input);
                // Don't target pets
                if (ownable != null && ownable.getOwner() == entityIn) return false;
                // Maybe not target other's pets as well
                if (!this.tameCheck(input)) return false;
                // Don't target invulnerable players (spectator/creative)
                if (!EntityPredicates.CAN_AI_TARGET.test(input)) return false;
                // Return true if player can battle the input.
                return this.trainer.canBattle(input);
            }

            private boolean tameCheck(final LivingEntity input)
            {
                if (allowTamed) return true;
                final IOwnable mob = OwnableCaps.getOwnable(input);
                if (mob == null) return true;
                return mob.getOwnerId() == null;
            }

            private boolean validClass(final LivingEntity input)
            {
                for (final Class<? extends LivingEntity> s : targetClass)
                    if (s.isInstance(input)) return true;
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<LivingEntity> match(final LivingEntity entityIn,
            final Class<? extends LivingEntity>... targetClass)
    {
        return AIFindTarget.match(entityIn, false, targetClass);
    }

    // Predicated to return true for invalid targets
    final Predicate<LivingEntity> validTargets;

    private float     agroChance = 1f;
    private int       timer      = 0;
    private final int maxTimer;

    // This is whether the ai should run for the current task holder
    private Predicate<LivingEntity> shouldRun = e -> true;

    public AIFindTarget(final LivingEntity entityIn, final float agressionProbability, final int battleTime,
            final Predicate<LivingEntity> validTargets)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
        this.maxTimer = battleTime;
        this.agroChance = agressionProbability;
        this.validTargets = validTargets;
    }

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
        this(entityIn, agressionProbability, battleTime, AIFindTarget.match(entityIn, targetClass));
    }

    public AIFindTarget setRunCondition(final Predicate<LivingEntity> shouldRun)
    {
        this.shouldRun = shouldRun;
        return this;
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.shouldRun.test(this.entity)) return false;
        if (this.trainer.getTarget() != null)
        {
            final LivingEntity target = this.trainer.getTarget();
            // Check if timer has run out.
            if (this.maxTimer > 0 && this.timer++ >= this.maxTimer)
            {
                this.timer = 0;
                final IHasPokemobs other = TrainerCaps.getHasPokemobs(target);
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
        final Vector3 here = Vector3.getNewVector().set(this.entity, true);
        LivingEntity target = null;
        final int sight = this.trainer.getAgressDistance();

        if (!this.world.isAreaLoaded(here.getPos(), sight + 3)) return;

        final Predicate<Entity> matcher = e -> e instanceof LivingEntity && this.validTargetSet((LivingEntity) e);
        final Entity match = here.firstEntityExcluding(sight, this.entity.getLook(0), this.world, this.entity, matcher);
        if (match instanceof LivingEntity) target = (LivingEntity) match;

        // If no target, return false.
        if (target == null)
        {
            // If trainer was in battle (any of these 3) reset trainer before
            // returning.
            if (this.trainer.getOutMob() != null || this.aiTracker.getAIState(IHasNPCAIStates.THROWING)
                    || this.aiTracker.getAIState(IHasNPCAIStates.INBATTLE)) this.trainer.resetPokemob();
            return;
        }
        final IHasPokemobs other = TrainerCaps.getHasPokemobs(target);
        // Set trainers target
        this.trainer.setTarget(target);
        if (other != null) other.setTarget(this.entity);
    }

    @Override
    public boolean validTargetSet(final LivingEntity target)
    {
        return this.validTargets.test(target);
    }
}
