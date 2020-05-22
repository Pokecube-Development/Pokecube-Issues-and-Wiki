package pokecube.core.ai.tasks.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.ITargetFinder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.AITools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class FindTargetsTask extends TaskBase<MobEntity> implements IAICombat, ITargetFinder
{

    public static boolean handleDamagedTargets = true;
    static
    {
        MinecraftForge.EVENT_BUS.register(FindTargetsTask.class);
    }

    public static int DEAGROTIMER = 50;

    public static void initiateCombat(final MobEntity mob, final LivingEntity target)
    {
        // No target self
        if (mob == target) return;
        // No target null
        if (target == null) return;
        // No target dead
        if (!target.isAlive() || target.getHealth() <= 0) return;
        // No target already had target
        if (target == BrainUtils.getAttackTarget(mob)) return;

        final IPokemob aggressor = CapabilityPokemob.getPokemobFor(mob);
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) targetMob.setCombatState(CombatStates.ANGRY, true);
        if (aggressor != null) aggressor.setCombatState(CombatStates.ANGRY, true);

        BrainUtils.setAttackTarget(mob, target);
        BrainUtils.setAttackTarget(target, mob);
    }

    public static void deagro(final LivingEntity mob)
    {
        if (mob == null) return;
        final IPokemob aggressor = CapabilityPokemob.getPokemobFor(mob);

        if (aggressor != null)
        {
            aggressor.getTargetFinder().clear();
            aggressor.setCombatState(CombatStates.ANGRY, false);
            aggressor.setCombatState(CombatStates.MATEFIGHT, false);
        }
        if (mob instanceof MobEntity)
        {
            final LivingEntity oldTarget = BrainUtils.getAttackTarget(mob);
            BrainUtils.setAttackTarget(mob, null);
            FindTargetsTask.deagro(oldTarget);
        }
        mob.getBrain().removeMemory(MemoryModules.ATTACKTARGET);
        mob.getBrain().removeMemory(MemoryModules.MATE_TARGET);
    }

    @SubscribeEvent
    public static void livingSetTargetEvent(final LivingSetAttackTargetEvent evt)
    {
        if (!FindTargetsTask.handleDamagedTargets || evt.getEntity().getEntityWorld().isRemote) return;
        // Only handle attack target set, not revenge target set.
        if (evt.getTarget() == ((LivingEntity) evt.getEntity()).getRevengeTarget()) return;
        // Prevent mob from targetting self.
        if (evt.getTarget() == evt.getEntity())
        {
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.log(Level.WARN, evt.getTarget()
                    + " is targetting self again.", new IllegalArgumentException());
            return;
        }

        // Attempt to divert the target over to one of our mobs.
        final List<Entity> outmobs = PCEventsHandler.getOutMobs(evt.getTarget(), true);
        outmobs.removeIf(o -> o == evt.getEntityLiving() || !o.isAlive());
        if (!outmobs.isEmpty() && evt.getEntityLiving() instanceof MobEntity)
        {
            Collections.sort(outmobs, (o1, o2) ->
            {
                final double dist1 = o1.getDistanceSq(evt.getEntityLiving());
                final double dist2 = o2.getDistanceSq(evt.getEntityLiving());
                return (int) (dist1 - dist2);
            });
            final Entity nearest = outmobs.get(0);
            if (nearest.getDistanceSq(evt.getEntityLiving()) < 256 && nearest instanceof LivingEntity)
            {
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Diverting agro to owner!");
                FindTargetsTask.initiateCombat((MobEntity) evt.getEntityLiving(), (LivingEntity) nearest);
                return;
            }
        }

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (pokemob != null)
        {
            if (evt.getTarget() != null && pokemob.getTargetID() == evt.getTarget().getEntityId())
            {
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Already targetted!");
                return;
            }
            if (evt.getTarget() == null && pokemob.getTargetID() == -1) return;

            // Prevent pokemob from targetting its owner.
            if (evt.getTarget() != null && evt.getTarget().getUniqueID().equals(pokemob.getOwnerId()))
            {
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.log(Level.WARN, evt.getTarget()
                        + " is targetting owner.", new IllegalArgumentException());
                return;
            }
            final boolean force = evt.getTarget() != null && evt.getTarget().getLastAttackedEntity() == evt.getEntity();
            pokemob.onSetTarget(evt.getTarget(), force);
        }
    }

    /** Prevents the owner from attacking their own pokemob. */
    @SubscribeEvent
    public static void onAttacked(final LivingAttackEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

        final DamageSource source = event.getSource();
        final LivingEntity attacked = (LivingEntity) event.getEntity();
        final IPokemob pokemobCap = CapabilityPokemob.getPokemobFor(attacked);
        if (pokemobCap == null) return;

        final Entity attacker = source.getTrueSource();

        // Camcel the event if it is from owner.
        if (pokemobCap.getGeneralState(GeneralStates.TAMED) && attacker instanceof PlayerEntity && attacker
                .getUniqueID().equals(pokemobCap.getOwnerId()))
        {
            event.setCanceled(true);
            event.setResult(Result.DENY);
            return;
        }
    }

    /**
     * Prevents the owner from attacking their own pokemob, and takes care of
     * properly setting attack targets for whatever was hurt.
     */
    @SubscribeEvent
    public static void onDamaged(final LivingDamageEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

        final DamageSource source = event.getSource();
        final LivingEntity attacked = (LivingEntity) event.getEntity();
        final IPokemob pokemobCap = CapabilityPokemob.getPokemobFor(attacked);
        if (pokemobCap == null) return;

        Entity attacker = source.getTrueSource();

        // Cancel the event if it is from owner.
        if (pokemobCap.getGeneralState(GeneralStates.TAMED) && attacker instanceof PlayerEntity
                && (PlayerEntity) attacker == pokemobCap.getOwner())
        {
            event.setCanceled(true);
            event.setResult(Result.DENY);
            return;
        }
        pokemobCap.setLogicState(LogicStates.SITTING, false);

        if (attacked instanceof MobEntity)
        {
            LivingEntity oldTarget = BrainUtils.getAttackTarget(attacked);
            // Don't include dead old targets.
            if (oldTarget != null && !oldTarget.isAlive()) oldTarget = null;

            if (!(oldTarget == null && attacker != attacked && attacker instanceof LivingEntity
                    && oldTarget != attacker)) attacker = null;

            LivingEntity newTarget = oldTarget;
            // Either keep old target, or agress the attacker.
            if (oldTarget != null && BrainUtils.getAttackTarget(attacked) != oldTarget) newTarget = oldTarget;
            else if (attacker instanceof LivingEntity) newTarget = (LivingEntity) attacker;
            final MobEntity living = (MobEntity) attacked;
            FindTargetsTask.initiateCombat(living, newTarget);
        }

    }

    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();

    /**
     * Checks the validTargts as well as team settings, will not allow
     * targetting things on the same team.
     */
    final Predicate<Entity> validGuardTarget = input ->
    {
        if (input == FindTargetsTask.this.entity) return false;
        if (TeamManager.sameTeam(FindTargetsTask.this.entity, input)) return false;
        if (!AITools.validTargets.test(input)) return false;
        return input instanceof LivingEntity;
    };

    private int agroTimer = -1;

    private LivingEntity entityTarget = null;

    public FindTargetsTask(final IPokemob mob)
    {
        super(mob, ImmutableMap.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT));
    }

    @Override
    public void clear()
    {
        this.agroTimer = FindTargetsTask.DEAGROTIMER;
        this.entityTarget = null;
    }

    /**
     * Check if there are any mobs nearby that will help us. <br>
     * <br>
     * This is called from {@link FindTargetsTask#shouldRun()}
     *
     * @return someone needed help.
     */
    protected boolean checkForHelp(final LivingEntity from)
    {
        // No need to get help against null
        if (from == null) return false;

        // Not social. doesn't do this.
        if (!this.pokemob.getPokedexEntry().isSocial) return false;

        // Random factor for this ai to apply
        if (Math.random() > 0.01 * PokecubeCore.getConfig().hordeRateFactor) return false;

        final List<LivingEntity> ret = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = Vector3.getNewVector();
        if (this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob.getOwner() == null) centre.set(
                this.pokemob.getHome());
        else centre.set(this.pokemob.getOwner());

        if (!TerrainManager.isAreaLoaded(this.world, centre, 18)) return false;

        // pokemobs =
        // this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS);

        // We check for whether it is the same species and, has the same owner
        // (including null) or is on the team.
        final Predicate<LivingEntity> relationCheck = input ->
        {
            final IPokemob other = CapabilityPokemob.getPokemobFor(input);
            // No pokemob, no helps.
            if (other == null) return false;
            // Not related, no helps.
            if (!other.getPokedexEntry().areRelated(FindTargetsTask.this.pokemob.getPokedexEntry())) return false;
            // Same owner (owned or null), helps.
            if (other.getOwnerId() == null && FindTargetsTask.this.pokemob.getOwnerId() == null || other
                    .getOwnerId() != null && other.getOwnerId().equals(FindTargetsTask.this.pokemob.getOwnerId()))
                return true;
            // Same team, helps.
            if (TeamManager.sameTeam(input, FindTargetsTask.this.entity)) return true;
            return false;
        };

        // Only allow valid guard targets.
        for (final LivingEntity o : pokemobs)
            if (relationCheck.test(o)) ret.add(o);

        for (final LivingEntity mob : ret)
        {
            if (!(mob instanceof MobEntity)) continue;
            // Only agress mobs that can see you are really under attack.
            if (!mob.canEntityBeSeen(this.entity)) continue;
            // Only agress if not currently in combat.
            if (BrainUtils.hasAttackTarget(mob)) continue;
            // Make all valid ones agress the target.
            this.setAttackTarget((MobEntity) mob, from);
        }

        return false;
    }

    /**
     * Check for and agress any guard targets. <br>
     * <br>
     * This is called from {@link FindTargetsTask#run()}
     *
     * @return a guard target was found
     */
    protected boolean checkGuard()
    {
        // Disabled via the boolean config.
        if (!PokecubeCore.getConfig().guardModeEnabled) return false;

        final int rate = PokecubeCore.getConfig().guardTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || this.entity.ticksExisted % rate != 0) return false;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = Vector3.getNewVector();
        if (this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob.getOwner() == null) centre.set(
                this.pokemob.getHome());
        else centre.set(this.pokemob.getOwner());

        if (!TerrainManager.isAreaLoaded(this.world, centre, PokecubeCore.getConfig().guardSearchDistance + 2))
            return false;

        final List<LivingEntity> ret = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        // Only allow valid guard targets.
        for (final LivingEntity o : pokemobs)
            if (this.validGuardTarget.test(o)) ret.add(o);
        ret.removeIf(e -> e.getDistance(this.entity) > PokecubeCore.getConfig().guardSearchDistance);
        if (ret.isEmpty()) return false;

        LivingEntity newtarget = null;
        double closest = Integer.MAX_VALUE;
        final Vector3 here = this.v1.set(this.entity, true);

        // Select closest visible guard target.
        for (final LivingEntity e : ret)
        {
            final double dist = e.getDistanceSq(this.entity);
            this.v.set(e, true);
            if (dist < closest && here.isVisible(this.world, this.v))
            {
                closest = dist;
                newtarget = e;
            }
        }

        // Agro the target.
        if (newtarget != null && Vector3.isVisibleEntityFromEntity(this.entity, newtarget))
        {
            this.setAttackTarget(this.entity, newtarget);
            return true;
        }
        return false;
    }

    protected void setAttackTarget(final MobEntity attacker, final LivingEntity target)
    {
        if (target == null)
        {
            FindTargetsTask.deagro(attacker);
            this.clear();
        }
        else
        {
            FindTargetsTask.initiateCombat(attacker, target);
            this.entityTarget = target;
        }
    }

    /**
     * Check if there is a target to hunt, if so, sets it as target. <br>
     * <br>
     * This is called from {@link FindTargetsTask#run()}
     *
     * @return if a hunt target was found.
     */
    protected boolean checkHunt()
    {
        final int rate = PokecubeCore.getConfig().hungerTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || this.entity.ticksExisted % rate != 0) return false;

        if (!TerrainManager.isAreaLoaded(this.world, this.entity.getPosition(), PokecubeCore
                .getConfig().guardSearchDistance + 2)) return false;

        final List<LivingEntity> list = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        list.addAll(pokemobs);
        list.removeIf(e -> e.getDistance(this.entity) > PokecubeCore.getConfig().guardSearchDistance);
        if (list.isEmpty()) return false;

        if (!list.isEmpty()) for (final LivingEntity entity : list)
        {
            final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
            if (mob != null && this.pokemob.getPokedexEntry().isFood(mob.getPokedexEntry()) && this.pokemob
                    .getLevel() > mob.getLevel() && Vector3.isVisibleEntityFromEntity(entity, entity))
            {
                this.setAttackTarget(this.entity, entity);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if owner is under attack, if so, agress the attacker. <br>
     * <br>
     * This is called from {@link FindTargetsTask#run()}
     *
     * @return if target was found.
     */
    protected boolean checkOwner()
    {
        final Entity owner = this.pokemob.getOwner();

        // Only apply if has owner.
        if (owner == null) return false;
        // Only apply if owner is close.
        if (this.entity.getDistanceSq(owner) > 64) return false;

        final int rate = PokecubeCore.getConfig().guardTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || this.entity.ticksExisted % rate != 0) return false;

        if (!TerrainManager.isAreaLoaded(this.world, this.entity.getPosition(), PokecubeCore
                .getConfig().guardSearchDistance + 2)) return false;

        final List<LivingEntity> list = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        list.addAll(pokemobs);
        list.removeIf(e -> e.getDistance(this.entity) > PokecubeCore.getConfig().guardSearchDistance);
        if (list.isEmpty()) return false;

        final Entity old = BrainUtils.getAttackTarget(this.entity);
        final IOwnable oldOwnable = OwnableCaps.getOwnable(old);
        final Entity oldOwner = oldOwnable != null ? oldOwnable.getOwner(this.world) : null;

        if (!list.isEmpty()) for (final LivingEntity entity : list)
        {
            if (oldOwner != null && entity == oldOwner) return false;
            final LivingEntity targ = BrainUtils.getAttackTarget(entity);
            if (entity instanceof MobEntity && targ != null && targ.equals(owner) && Vector3.isVisibleEntityFromEntity(
                    entity, entity))
            {
                this.setAttackTarget(this.entity, entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        final LivingEntity targ = BrainUtils.getAttackTarget(this.entity);
        // No need to find a target if we have one.
        if (targ != null)
        {
            // If target is dead, lets forget about it.
            if (!targ.isAlive() || targ.getHealth() <= 0) this.clear();
            return;
        }

        // If hunting, look for valid prey, and if found, agress it.
        if (!this.pokemob.getLogicState(LogicStates.SITTING) && this.pokemob.isCarnivore() && this.pokemob
                .getCombatState(CombatStates.HUNTING)) if (this.checkHunt()) return;
        // If guarding, look for mobs not on the same team as you, and if you
        // find them, try to agress them.
        if (this.pokemob.getCombatState(CombatStates.GUARDING)) if (this.checkGuard()) return;
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;

        if (!this.entity.getBrain().hasMemory(MemoryModuleType.VISIBLE_MOBS)) return false;

        // Ensure the correct owner is tracked.
        this.pokemob.getOwner(this.world);

        LivingEntity target = BrainUtils.getAttackTarget(this.entity);

        // Don't look for targets if you are sitting.
        final boolean ret = target == null && !this.pokemob.getLogicState(LogicStates.SITTING);

        // Target is too far away, lets forget it.
        if (target != null && this.entity.getDistance(target) > PokecubeCore.getConfig().chaseDistance)
        {
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Forgetting Target due to distance. {} -> {}",
                    this.entity, target);
            this.setAttackTarget(this.entity, null);
            return false;
        }

        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (!this.pokemob.getGeneralState(GeneralStates.STAYING) && this.pokemob.getGeneralState(GeneralStates.TAMED))
            if (this.checkOwner()) return false;

        /*
         * Check for others to try to help you.
         */
        if (this.checkForHelp(target)) return false;

        if (target == null && this.entityTarget != null)
        {
            target = this.entityTarget;
            if (this.agroTimer == -1) this.agroTimer = FindTargetsTask.DEAGROTIMER;
            else
            {
                this.agroTimer--;
                if (this.agroTimer == -1 || !this.pokemob.getCombatState(CombatStates.ANGRY)) this.clear();
                else if (this.entity.getDistance(target) < PokecubeCore.getConfig().chaseDistance)
                {
                    if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(
                            "Somehow lost target? Well, found it back again! {} -> {}", this.entity, this.entityTarget);
                    this.setAttackTarget(this.entity, this.entityTarget);
                }
            }
        }

        final boolean playerNear = this.entity.getBrain().hasMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        // If wild, randomly decided to agro a nearby player instead.
        if (ret && playerNear && AITools.shouldAgroNearestPlayer.test(this.pokemob))
        {
            PlayerEntity player = this.entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).get();
            if (player != null && player.getDistance(this.entity) > PokecubeCore.getConfig().mobAggroRadius)
                player = null;
            if (player != null && Vector3.isVisibleEntityFromEntity(this.entity, player) && this.entity.getEntityWorld()
                    .getDifficulty().getId() > Difficulty.EASY.getId() && AITools.validTargets.test(player))
            {
                this.setAttackTarget(this.entity, player);
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(
                        "Found player to be angry with, agressing.");
                return false;
            }
        }
        return ret;
    }

}