package pokecube.core.ai.tasks.combat.management;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.ITargetFinder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.combat.SetAttackTargetEvent;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.database.tags.Tags;
import pokecube.core.utils.AITools;
import pokecube.core.utils.PokemobTracker;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class FindTargetsTask extends TaskBase implements IAICombat, ITargetFinder
{
    public static int DEAGROTIMER = 50;

    UUID targetId = null;

    LivingEntity target = null;
    LivingEntity targetOwner = null;

    int switchTargetTimer = 0;
    int forgetTimer = 0;

    public static boolean handleDamagedTargets = true;
    static
    {
        ThutCore.FORGE_BUS.addListener(FindTargetsTask::onBrainSetTarget);
        ThutCore.FORGE_BUS.addListener(FindTargetsTask::onLivingSetTarget);
        ThutCore.FORGE_BUS.addListener(FindTargetsTask::onLivingHurt);
    }

    private static LivingEntity divertTarget(LivingEntity aggressor, LivingEntity aggressed)
    {
        List<Entity> mobs = PokemobTracker.getMobs(aggressed,
                e -> PokemobCaps.getPokemobFor(e) != null && e.distanceToSqr(aggressed) < 4096);

        // Remove any "non agressive" mobs, as they won't be actively drawing
        // agro from the player.
        mobs.removeIf(c -> {
            final IPokemob poke = PokemobCaps.getPokemobFor(c);
            if (poke == null) return true;
            return !poke.isRoutineEnabled(AIRoutine.AGRESSIVE);
        });
        final boolean targetHasMobs = !mobs.isEmpty();
        LivingEntity target = aggressed;
        if (targetHasMobs)
        {
            mobs.sort((o1, o2) -> (int) (o1.distanceToSqr(aggressor) - o2.distanceToSqr(aggressor)));
            final Entity mob = mobs.get(0);
            mobs = PokemobTracker.getMobs(mob, e -> true);
            // No loop diverting
            if (!mobs.isEmpty() || !(mob instanceof LivingEntity entity)) return target;
            target = entity;
        }
        return target;
    }

    public static void onMobTick(final LivingEntity living)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;
        // Only run this every 10 ticks
        if (living.tickCount % 10 != 0) return;
        LivingEntity target = BrainUtils.getAttackTarget(living);
        if (target == null) return;
        LivingEntity diverted = divertTarget(living, target);
        if (diverted != target)
        {
            BrainUtils.setAttackTarget(living, diverted);
        }
    }

    private static void onBrainSetTarget(final SetAttackTargetEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;
        event.newTarget = divertTarget(event.mob, event.originalTarget);
    }

    private static void onLivingSetTarget(final LivingChangeTargetEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;

        LivingEntity newTarget = event.getNewTarget();
        LivingEntity rootMob = event.getEntityLiving();

        // Don't manage this.
        if (newTarget == null) return;
        LivingEntity target = divertTarget(rootMob, newTarget);
        // Make sure they are marked as in a battle with each other.
        Battle.createOrAddToBattle(rootMob, target);
    }

    private static void onLivingHurt(final LivingHurtEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;

        final DamageSource source = event.getSource();
        final LivingEntity hurt = event.getEntityLiving();
        final Entity user = source.getDirectEntity();

        // Make sure they are marked as in a battle with each other.

        // First start with the direct entity, as this will cover things like
        // pokemobs.
        if (user instanceof LivingEntity mob)
        {
            Battle.createOrAddToBattle(hurt, mob);
        }
        // Then check the root entity, for things like shooters of arrows.
        else if (source.getEntity() instanceof LivingEntity mob)
        {
            Battle.createOrAddToBattle(hurt, mob);
        }
    }

    /**
     * Checks the validTargts as well as team settings, will not allow
     * targetting things on the same team.
     */
    final Predicate<Entity> validGuardTarget;

    public FindTargetsTask(final IPokemob mob)
    {
        super(mob, ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.validGuardTarget = input -> AITools.shouldBeAbleToAgro(this.entity, input);
    }

    @Override
    public void clear()
    {
        this.targetId = null;
        this.target = null;
        this.forgetTimer = 0;
        this.switchTargetTimer = 0;
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
        if (rate <= 0 || this.entity.tickCount % rate != 0) return false;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = new Vector3();
        if (this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob.getOwner() == null)
            centre.set(this.pokemob.getHome());
        else centre.set(this.pokemob.getOwner());

        // Only allow valid guard targets.
        final Optional<LivingEntity> pokemobs = this.entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                .findClosest(e -> this.validGuardTarget.test(e)
                        && e.distanceTo(this.entity) <= PokecubeCore.getConfig().guardSearchDistance);
        if (pokemobs.isEmpty()) return false;

        // This is already sorted by distance!
        final LivingEntity newtarget = pokemobs.get();
        // Agro the target.
        if (newtarget != null)
        {
            this.initiateBattle(newtarget);
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting Guard Target.");
            return true;
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

        if (this.pokemob.getGeneralState(GeneralStates.STAYING)) return false;

        final int rate = PokecubeCore.getConfig().guardTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || this.entity.tickCount % rate != 0) return false;

        final Iterable<LivingEntity> pokemobs = this.entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                .findAll(e -> AITools.validAgroTarget.test(e)
                        && e.distanceTo(this.entity) <= PokecubeCore.getConfig().guardSearchDistance);
        if (!pokemobs.iterator().hasNext()) return false;

        final Entity old = BrainUtils.getAttackTarget(this.entity);
        final IOwnable oldOwnable = OwnableCaps.getOwnable(old);
        final Entity oldOwner = oldOwnable != null ? oldOwnable.getOwner(this.world) : null;

        for (final LivingEntity entity : pokemobs)
        {
            if (oldOwner != null && entity == oldOwner) continue;
            final LivingEntity targ = BrainUtils.getAttackTarget(entity);
            if (entity instanceof Mob && targ != null && targ.equals(owner) && this.validGuardTarget.test(entity))
            {
                this.initiateBattle(entity);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting target who hit owner.");
                return true;
            }
        }
        return false;
    }

    /**
     * If the pokemob is "not alive", but it didn't faint, then it is most
     * likely that the mob has been recalled, and a new one is sent out. In this
     * case, we will switch target to either the new pokemob, if it has been a
     * short time, or the owner of the old pokemob, if it has been a longer
     * time.
     *
     * @return if switched to a new target
     */
    protected void checkSwitchedMob()
    {
        final boolean switched = this.target != null && !this.target.isAlive();
        if (!switched) return;
        // This means it either fainted, or died.
        if (this.targetOwner != null)
        {
            // Give some time to look for a new pokemob
            if (this.switchTargetTimer++ < 2 * FindTargetsTask.DEAGROTIMER)
            {
                final Iterable<LivingEntity> pokemobs = this.entity.getBrain()
                        .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                        .findAll(e -> AITools.validAgroTarget.test(e)
                                && e.distanceTo(this.entity) <= PokecubeCore.getConfig().guardSearchDistance);

                for (final LivingEntity entity : pokemobs)
                {
                    final LivingEntity owner = OwnableCaps.getOwner(entity);
                    if (owner == this.targetOwner)
                    {
                        this.initiateBattle(entity);
                        this.clear();
                        return;
                    }
                }
            }
            // Otherwise agro the owner
            else
            {
                this.initiateBattle(this.targetOwner);
                this.clear();
                return;
            }
        }

        // Lets check if the target say was failed to capture. If this was the
        // case, then the UUID is still the same, but the entity itself has
        // changed.
        final Entity newMob = this.world.getEntity(this.targetId);
        if (newMob instanceof LivingEntity living)
        {
            this.initiateBattle(living);
            this.clear();
            return;
        }
    }

    @Override
    public void reset()
    {}

    @Override
    public void run()
    {
        // Check if pokemob can see the target, if yes start battle
        if (this.targetId != null)
        {
            final Entity mob = this.world.getEntity(this.targetId);
            if (!(mob instanceof LivingEntity entity)
                    || (!BrainUtils.canSee(this.entity, entity) && !this.initiateBattle(entity)))
                this.clear();

            // Reset target ID here, so we don't keep looking for it.
            if (this.forgetTimer-- <= 0) this.clear();
            return;
        }

        // If pokemob is hurt by someone, for example players
        final Optional<LivingEntity> hurtBy = this.entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtBy != null && hurtBy.isPresent())
        {
            final LivingEntity target = hurtBy.get();
            // This will ensure that the target isn't on our team
            if (!this.validGuardTarget.test(target)) return;

            if (BrainUtils.canSee(this.entity, target))
            {
                this.initiateBattle(target);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting Target who hit us.");
                return;
            }
        }

        // If guarding, look for mobs not on the same team as you, and if you
        // find them, try to agress them.
        if (this.pokemob.getCombatState(CombatStates.GUARDING)) if (this.checkGuard()) return;

        // Ensure the correct owner is tracked.
        this.pokemob.getOwner(this.world);

        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (!this.pokemob.getGeneralState(GeneralStates.STAYING)) if (this.checkOwner()) return;

        final boolean playerNear = this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        // If wild, randomly decided to agro a nearby player instead.
        if (playerNear && AITools.shouldAgroNearestPlayer.test(this.pokemob))
        {
            int aggroDistance = Tags.POKEMOB.isIn(AITools.HOSTILE, this.pokemob.getPokedexEntry().getTrimmedName())
                    ? PokecubeCore.getConfig().hostileAggroRadius
                    : PokecubeCore.getConfig().aggressiveAggroRadius;
            Player player = this.entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).get();
            if (player != null && player.distanceTo(this.entity) > aggroDistance) player = null;
            if (player != null && AITools.validAgroTarget.test(player))
            {
                this.initiateBattle(player);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Found player to be angry with, agressing.");
            }
        }
    }

    private boolean initiateBattle(final LivingEntity target)
    {
        if (!Battle.createOrAddToBattle(this.entity, target))
        {
            this.clear();
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;
        if (!this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return false;
        if (BrainUtils.hasAttackTarget(this.entity))
        {
            final LivingEntity target = BrainUtils.getAttackTarget(this.entity);
            this.forgetTimer = FindTargetsTask.DEAGROTIMER;
            if (!target.getUUID().equals(this.targetId))
            {
                this.target = target;
                this.targetOwner = OwnableCaps.getOwner(target);
                this.targetId = this.target.getUUID();
                if (PokecubeCore.getConfig().debug_ai)
                    PokecubeAPI.logInfo("Found Target {} {}", this.target, this.targetOwner);
            }
            this.checkSwitchedMob();
            return false;
        }
        return true;
    }

}