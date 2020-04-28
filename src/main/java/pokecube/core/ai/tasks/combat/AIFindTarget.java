package pokecube.core.ai.tasks.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
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
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
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
public class AIFindTarget extends AIBase implements IAICombat
{

    public static boolean handleDamagedTargets = true;
    static
    {
        MinecraftForge.EVENT_BUS.register(AIFindTarget.class);
    }

    public static int DEAGROTIMER = 50;

    @SubscribeEvent
    public static void livingSetTargetEvent(final LivingSetAttackTargetEvent evt)
    {
        if (!AIFindTarget.handleDamagedTargets || evt.getEntity().getEntityWorld().isRemote) return;
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
                ((MobEntity) evt.getEntityLiving()).setAttackTarget((LivingEntity) nearest);
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
        if (!AIFindTarget.handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

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
        if (!AIFindTarget.handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

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
            final MobEntity living = (MobEntity) attacked;
            LivingEntity oldTarget = living.getAttackTarget();

            // Don't include dead old targets.
            if (oldTarget != null && !oldTarget.isAlive()) oldTarget = null;

            if (!(oldTarget == null && attacker != living && attacker instanceof LivingEntity && living
                    .getAttackTarget() != attacker)) attacker = null;

            final IPokemob agres = CapabilityPokemob.getPokemobFor(attacker);
            if (agres != null)
            {
                if (agres.getPokedexEntry().isFood(pokemobCap.getPokedexEntry()) && agres.getCombatState(
                        CombatStates.HUNTING))
                {
                    // track running away.
                }
                if (agres.getLover() == living && attacker != null) agres.setLover(attacker);

            }

            // Either keep old target, or agress the attacker.
            if (oldTarget != null && living.getAttackTarget() != oldTarget) living.setAttackTarget(oldTarget);
            else if (attacker instanceof LivingEntity) living.setAttackTarget((LivingEntity) attacker);
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
                                                 if (input == AIFindTarget.this.entity) return false;
                                                 if (TeamManager.sameTeam(AIFindTarget.this.entity, input))
                                                     return false;
                                                 if (!AITools.validTargets.test(input)) return false;
                                                 return input instanceof LivingEntity;
                                             };
    private int             agroTimer        = -1;
    private LivingEntity    entityTarget     = null;

    public AIFindTarget(final IPokemob mob)
    {
        super(mob);
    }

    public void clear()
    {
        this.agroTimer = AIFindTarget.DEAGROTIMER;
        this.entityTarget = null;
    }

    /**
     * Check if there are any mobs nearby that will help us. <br>
     * <br>
     * This is called from {@link AIFindTarget#shouldRun()}
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

        final List<MobEntity> ret = new ArrayList<>();
        List<MobEntity> pokemobs;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = Vector3.getNewVector();
        if (this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob.getOwner() == null) centre.set(
                this.pokemob.getHome());
        else centre.set(this.pokemob.getOwner());

        if (!TerrainManager.isAreaLoaded(this.world, centre, 18)) return false;

        pokemobs = this.getEntitiesWithinDistance(this.world, centre.getPos(), 16, MobEntity.class);

        // We check for whether it is the same species and, has the same owner
        // (including null) or is on the team.
        final Predicate<MobEntity> relationCheck = input ->
        {
            final IPokemob other = CapabilityPokemob.getPokemobFor(input);
            // No pokemob, no helps.
            if (other == null) return false;
            // Not related, no helps.
            if (!other.getPokedexEntry().areRelated(AIFindTarget.this.pokemob.getPokedexEntry())) return false;
            // Same owner (owned or null), helps.
            if (other.getOwnerId() == null && AIFindTarget.this.pokemob.getOwnerId() == null || other
                    .getOwnerId() != null && other.getOwnerId().equals(AIFindTarget.this.pokemob.getOwnerId()))
                return true;
            // Same team, helps.
            if (TeamManager.sameTeam(input, AIFindTarget.this.entity)) return true;
            return false;
        };

        // Only allow valid guard targets.
        for (final Object o : pokemobs)
            if (relationCheck.test((MobEntity) o)) ret.add((MobEntity) o);

        for (final MobEntity mob : ret)
        {
            // Only agress mobs that can see you are really under attack.
            if (!mob.canEntityBeSeen(this.entity)) continue;
            // Only agress if not currently in combat.
            if (mob.getAttackTarget() != null) continue;
            // Make all valid ones agress the target.
            final IPokemob other = CapabilityPokemob.getPokemobFor(mob);
            this.setAttackTarget(mob, from);
            this.setCombatState(other, CombatStates.ANGRY, false);
        }

        return false;
    }

    /**
     * Check for and agress any guard targets. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
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

        final List<LivingEntity> ret = new ArrayList<>();
        List<LivingEntity> pokemobs;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = Vector3.getNewVector();
        if (this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob.getOwner() == null) centre.set(
                this.pokemob.getHome());
        else centre.set(this.pokemob.getOwner());

        if (!TerrainManager.isAreaLoaded(this.world, centre, PokecubeCore.getConfig().guardSearchDistance + 2))
            return false;

        pokemobs = this.getEntitiesWithinDistance(this.world, centre.getPos(), PokecubeCore
                .getConfig().guardSearchDistance, LivingEntity.class);

        // Only allow valid guard targets.
        for (final Object o : pokemobs)
            if (this.validGuardTarget.test((Entity) o)) ret.add((LivingEntity) o);

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
            this.setCombatState(this.pokemob, CombatStates.ANGRY, true);
            this.entityTarget = newtarget;
            return true;
        }
        return false;
    }

    /**
     * Check if there is a target to hunt, if so, sets it as target. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
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
        final List<LivingEntity> list = this.getEntitiesWithinDistance(this.entity, PokecubeCore
                .getConfig().guardSearchDistance, LivingEntity.class);
        if (!list.isEmpty()) for (final LivingEntity entity : list)
        {
            final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
            if (mob != null && this.pokemob.getPokedexEntry().isFood(mob.getPokedexEntry()) && this.pokemob
                    .getLevel() > mob.getLevel() && Vector3.isVisibleEntityFromEntity(entity, entity))
            {
                this.setAttackTarget(this.entity, entity);
                this.entityTarget = entity;
                this.setCombatState(this.pokemob, CombatStates.ANGRY, true);
                this.setLogicState(this.pokemob, LogicStates.SITTING, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if owner is under attack, if so, agress the attacker. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
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
        final List<LivingEntity> list = this.getEntitiesWithinDistance(this.pokemob.getOwner(), PokecubeCore
                .getConfig().guardSearchDistance, LivingEntity.class);

        final Entity old = this.entity.getAttackTarget();
        final IOwnable oldOwnable = OwnableCaps.getOwnable(old);
        final Entity oldOwner = oldOwnable != null ? oldOwnable.getOwner(this.world) : null;

        if (!list.isEmpty()) for (final LivingEntity entity : list)
        {
            if (oldOwner != null && entity == oldOwner) return false;

            if (entity instanceof MobEntity && ((MobEntity) entity).getAttackTarget() != null && ((MobEntity) entity)
                    .getAttackTarget().equals(owner) && Vector3.isVisibleEntityFromEntity(entity, entity))
            {
                this.setAttackTarget(this.entity, entity);
                this.setAttackTarget((MobEntity) entity, this.entity);
                this.entityTarget = entity;
                this.setCombatState(this.pokemob, CombatStates.ANGRY, true);
                this.setLogicState(this.pokemob, LogicStates.SITTING, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the closest vulnerable player within the given radius, or null
     * if none is found.
     */
    PlayerEntity getClosestVulnerablePlayer(final double x, final double y, final double z, final double distance)
    {
        double d4 = -1.0D;
        PlayerEntity PlayerEntity = null;

        final List<? extends PlayerEntity> list = this.world.getPlayers();
        if (list.isEmpty()) return null;

        for (int i = 0; i < list.size(); ++i)
        {
            if (!(list.get(i) instanceof PlayerEntity)) continue;
            final PlayerEntity PlayerEntity1 = list.get(i);
            if (PlayerEntity1.isCreative()) continue;
            if (PlayerEntity1.isSpectator()) continue;
            if (!PlayerEntity1.abilities.disableDamage && PlayerEntity1.isAlive())
            {
                final double d5 = PlayerEntity1.getDistanceSq(x, y, z);
                double d6 = distance;
                if (PlayerEntity1.isSneaking()) d6 = distance * 0.800000011920929D;
                if ((distance < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    PlayerEntity = PlayerEntity1;
                }
            }
        }

        return PlayerEntity;
    }

    /**
     * Returns the closest vulnerable player to this entity within the given
     * radius, or null if none is found
     */
    PlayerEntity getClosestVulnerablePlayerToEntity(final Entity entity, final double distance)
    {
        return this.getClosestVulnerablePlayer(entity.posX, entity.posY, entity.posZ, distance);
    }

    @Override
    public void reset()
    {

    }

    @Override
    public void run()
    {
        // No need to find a target if we have one.
        if (this.entity.getAttackTarget() != null)
        {
            // If target is dead, lets forget about it.
            if (!this.entity.getAttackTarget().isAlive() || this.entity.getAttackTarget().getHealth() <= 0) this
                    .clear();
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

        // Ensure the correct owner is tracked.
        this.pokemob.getOwner(this.world);

        LivingEntity target = this.entity.getAttackTarget();

        // Don't look for targets if you are sitting.
        final boolean ret = target == null && !this.pokemob.getLogicState(LogicStates.SITTING);
        final boolean tame = this.pokemob.getGeneralState(GeneralStates.TAMED);

        // Target is too far away, lets forget it.
        if (target != null && this.entity.getDistance(target) > PokecubeCore.getConfig().chaseDistance)
        {
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Forgetting Target due to distance. {} -> {}",
                    this.entity, target);
            this.setAttackTarget(this.entity, null);
            this.clear();
            return false;
        }

        // If we have a target, we don't need to look for another.
        if (target != null)
        {
            final IOwnable targetOwnable = OwnableCaps.getOwnable(this.entityTarget);

            // Prevents swapping to owner as target if we are owned and we just
            // defeated someone, only applies to tame mobs, wild mobs will still
            // try to kill the owner if they run away.
            if (this.entityTarget != null && this.entityTarget != target && targetOwnable != null && targetOwnable
                    .getOwner(this.world) == target && this.pokemob.getGeneralState(GeneralStates.TAMED)
                    && this.entityTarget.getHealth() <= 0)
            {
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Battle is over.");
                this.setAttackTarget(this.entity, null);
                this.setCombatState(this.pokemob, CombatStates.ANGRY, false);
                this.clear();
                return false;
            }

            this.entityTarget = target;
            // If our target is dead, we can forget it, so long as it isn't
            // owned
            if (!target.isAlive() || target.getHealth() <= 0)
            {
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Target is dead!");
                this.setAttackTarget(this.entity, null);
                this.clear();
                return false;
            }

            // If our target is us, we should forget it.
            if (target == this.entity)
            {
                this.setAttackTarget(this.entity, null);
                this.clear();
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Cannot target self.");
                return false;
            }

            // If we are not angry, we should forget target.
            if (!this.pokemob.getCombatState(CombatStates.ANGRY))
            {
                this.setAttackTarget(this.entity, null);
                this.clear();
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Not Angry. losing target now.");
                return false;
            }

            // If our target is owner, we should forget it.
            if (target.getUniqueID().equals(this.pokemob.getOwnerId()))
            {
                this.setAttackTarget(this.entity, null);
                this.clear();
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Cannot target owner.");
                return false;
            }

            // If your owner is too far away, shouldn't have a target, should be
            // going back to the owner.
            if (tame)
            {
                final Entity owner = this.pokemob.getOwner();
                final boolean stayOrGuard = this.pokemob.getCombatState(CombatStates.GUARDING) || this.pokemob
                        .getGeneralState(GeneralStates.STAYING);
                if (owner != null && !stayOrGuard && owner.getDistance(this.entity) > PokecubeCore
                        .getConfig().chaseDistance)
                {
                    this.setAttackTarget(this.entity, null);
                    this.entityTarget = null;
                    if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(
                            "Cannot target mob that far while guarding.");
                    return false;
                }

                // If the target is a pokemob, on same team, we shouldn't target
                // it either
                if (TeamManager.sameTeam(target, this.entity))
                {
                    this.setAttackTarget(this.entity, null);
                    this.clear();
                    if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Cannot target team mates.");
                    return false;
                }

            }
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
            if (this.agroTimer == -1) this.agroTimer = AIFindTarget.DEAGROTIMER;
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

        // If wild, randomly decided to agro a nearby player instead.
        if (ret && AITools.shouldAgroNearestPlayer.test(this.pokemob))
        {
            final PlayerEntity player = this.getClosestVulnerablePlayerToEntity(this.entity, PokecubeCore
                    .getConfig().mobAggroRadius);

            if (player != null && Vector3.isVisibleEntityFromEntity(this.entity, player) && this.entity.getEntityWorld()
                    .getDifficulty().getId() > Difficulty.EASY.getId() && AITools.validTargets.test(player))
            {
                this.setCombatState(this.pokemob, CombatStates.ANGRY, true);
                this.setAttackTarget(this.entity, player);
                this.entityTarget = player;
                if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(
                        "Found player to be angry with, agressing.");
                return false;
            }
        }
        return ret;
    }

}