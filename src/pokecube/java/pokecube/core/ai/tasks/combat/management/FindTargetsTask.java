package pokecube.core.ai.tasks.combat.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.ITargetFinder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.PokemobBehaviour;
import pokecube.core.database.tags.Tags;
import pokecube.core.utils.AITools;
import pokecube.core.utils.PokemobTracker;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Ownable;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class FindTargetsTask extends PokemobBehaviour implements IAICombat, ITargetFinder
{
    public static LivingChangeTargetEvent.ILivingTargetType AGROREDIRECT = new LivingChangeTargetEvent.ILivingTargetType() {};

    public static int DEAGROTIMER = 50;

    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.ATTACKTARGETID.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.TIMER_SWAPTARGET.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.TIMER_FORGETTARGET.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.TARGETOWNER.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModules.TRACKEDTARGET.get(), MemoryStatus.REGISTERED);
        MEMS.put(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT);
    }

    public static boolean handleDamagedTargets = true;

    static
    {
        ThutCore.FORGE_BUS.addListener(FindTargetsTask::onLivingSetTarget);
        ThutCore.FORGE_BUS.addListener(FindTargetsTask::onLivingHurt);
    }

    private static LivingEntity divertTarget(LivingEntity aggressor, LivingEntity aggressed)
    {
        // We don't handle diverting self agression here.
        if (aggressed == aggressor) return aggressor;

        List<Entity> mobs = PokemobTracker.getMobs(aggressed,
                e -> PokemobCaps.getPokemobFor(e) != null && e.distanceToSqr(aggressed) < 4096);

        // Remove any "non agressive" mobs, as they won't be actively drawing
        // agro from the player.
        mobs.removeIf(c -> {
            final IPokemob poke = PokemobCaps.getPokemobFor(c);
            if (poke == null) return true;
            // Also prevent staying ones
            if (poke.getGeneralState(GeneralStates.STAYING)) return true;
            return !poke.isRoutineEnabled(AIRoutine.AGRESSIVE);
        });
        final boolean targetHasMobs = !mobs.isEmpty();
        LivingEntity target = aggressed;
        if (targetHasMobs)
        {
            mobs.sort((o1, o2) -> (int) (o1.distanceToSqr(aggressor) - o2.distanceToSqr(aggressor)));
            final Entity mob = mobs.getFirst();
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
        // Only run this every 20 ticks
        if (living.tickCount % 20 != 0) return;
        LivingEntity target = BrainUtils.getAttackTarget(living);
        if (target == null) return;
        LivingEntity diverted = divertTarget(living, target);
        if (diverted != target)
        {
            Battle battle = Battle.getBattle(living);
            var mob = PokemobCaps.getPokemobFor(living);
            BrainUtils.setAttackTarget(living, diverted);
            if (battle != null && mob != null)
            {
                List<LivingEntity> mobs = Lists.newArrayList(battle.getEnemies(living));
                for (int i = 0; i < mobs.size(); i++)
                {
                    var enemy = mobs.get(i);
                    if (enemy != diverted) continue;
                    mob.getMoveStats().enemyIndex = i;
                    mob.updateBattleInfo();
                    mob.onSetTarget(diverted, true);
                    break;
                }
            }
        }
    }

    private static void onLivingSetTarget(final LivingChangeTargetEvent event)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        LivingEntity rootMob = event.getEntity();

        // We handle it inside the re-direct.
        if (event.getTargetType() == AGROREDIRECT)
        {
            // Make sure they are marked as in a battle with each other.
            Battle.createOrAddToBattle(rootMob, newTarget);
            return;
        }

        var oldTarget = BrainUtils.getAttackTarget(rootMob);
        if (newTarget == oldTarget) return;

        // Don't manage this.
        if (newTarget == null) return;
        LivingEntity target = divertTarget(rootMob, newTarget);
        // Now fire our re-direct event
        LivingChangeTargetEvent event2 = new LivingChangeTargetEvent(rootMob, target, AGROREDIRECT);
        ThutCore.FORGE_BUS.post(event2);
    }

    private static void onLivingHurt(final LivingDamageEvent.Post event)
    {
        if (!FindTargetsTask.handleDamagedTargets) return;

        final DamageSource source = event.getSource();
        final LivingEntity hurt = event.getEntity();
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
     * Checks the validTargts as well as team settings, will not allow targetting things on the same team.
     */
    final BiFunction<Mob, Entity, Boolean> validGuardTarget;

    public FindTargetsTask()
    {
        super(MEMS);
        this.validGuardTarget = AITools::shouldBeAbleToAgro;
    }

    @Override
    public void clear(Mob entityIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.ATTACKTARGETID.get());
        entityIn.getBrain().eraseMemory(MemoryModules.TIMER_SWAPTARGET.get());
        entityIn.getBrain().eraseMemory(MemoryModules.TIMER_FORGETTARGET.get());
        entityIn.getBrain().eraseMemory(MemoryModules.TARGETOWNER.get());
        entityIn.getBrain().eraseMemory(MemoryModules.TRACKEDTARGET.get());
    }

    /**
     * Check for and agress any guard targets. <br>
     * <br>
     *
     * @return a guard target was found
     */
    protected boolean checkGuard(IPokemob pokemob)
    {
        // Disabled via the boolean config.
        if (!PokecubeCore.getConfig().guardModeEnabled) return false;

        var entity = pokemob.getEntity();
        final int rate = PokecubeCore.getConfig().guardTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || entity.tickCount % rate != 0) return false;

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        final Vector3 centre = new Vector3();
        if (pokemob.getGeneralState(GeneralStates.STAYING) || pokemob.getOwner() == null) centre.set(pokemob.getHome());
        else centre.set(pokemob.getOwner());

        // Only allow valid guard targets.
        final Optional<LivingEntity> pokemobs = entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest(
                        e -> this.validGuardTarget.apply(entity, e)
                                && e.distanceTo(entity) <= PokecubeCore.getConfig().guardSearchDistance);
        if (pokemobs.isEmpty()) return false;

        // This is already sorted by distance!
        final LivingEntity newtarget = pokemobs.get();
        // Agro the target.
        if (newtarget != null)
        {
            this.initiateBattle(newtarget, entity);
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting Guard Target.");
            return true;
        }
        return false;
    }

    /**
     * Check if owner is under attack, if so, agress the attacker. <br>
     * <br>
     *
     * @return if target was found.
     */
    protected boolean checkOwner(IPokemob pokemob)
    {
        final Entity owner = pokemob.getOwner();

        // Only apply if has owner.
        if (owner == null) return false;

        if (pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        if (!pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;

        var entity = pokemob.getEntity();
        final int rate = PokecubeCore.getConfig().guardTickRate;
        // Disable via rate out of bounds, or not correct time in the rate.
        if (rate <= 0 || entity.tickCount % rate != 0) return false;

        final Iterable<LivingEntity> pokemobs = entity.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(
                        e -> AITools.validAgroTarget.test(e)
                                && e.distanceTo(entity) <= PokecubeCore.getConfig().guardSearchDistance);
        if (!pokemobs.iterator().hasNext()) return false;

        final Entity old = BrainUtils.getAttackTarget(entity);
        final IOwnable oldOwnable = ThutCaps.getOwnable(old);
        final Entity oldOwner = oldOwnable != null ? oldOwnable.getOwner((ServerLevel) entity.level()) : null;

        for (final LivingEntity _entity : pokemobs)
        {
            if (oldOwner != null && _entity == oldOwner) continue;
            final LivingEntity targ = BrainUtils.getAttackTarget(_entity);
            if (_entity instanceof Mob && targ != null && targ.equals(owner) && this.validGuardTarget.apply(entity,
                    _entity))
            {
                this.initiateBattle(_entity, entity);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting target who hit owner.");
                return true;
            }
        }
        return false;
    }

    /**
     * If the pokemob is "not alive", but it didn't faint, then it is most likely that the mob has been recalled, and a
     * new one is sent out. In this case, we will switch target to either the new pokemob, if it has been a short time,
     * or the owner of the old pokemob, if it has been a longer time.
     */
    protected void checkSwitchedMob(IPokemob pokemob, LivingEntity target, LivingEntity targetOwner)
    {
        final boolean switched = target != null && !target.isAlive();
        var entity = pokemob.getEntity();
        if (!switched || !(entity.level() instanceof ServerLevel level)) return;
        var brain = entity.getBrain();
        // This means it either fainted, or died.
        if (targetOwner != null)
        {
            int switchTargetTimer = brain.getMemory(MemoryModules.TIMER_SWAPTARGET.get()).orElse(0);
            // Give some time to look for a new pokemob
            if (switchTargetTimer++ < 2 * FindTargetsTask.DEAGROTIMER)
            {
                final Iterable<LivingEntity> pokemobs = brain.getMemory(
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(
                        e -> AITools.validAgroTarget.test(e)
                                && e.distanceTo(entity) <= PokecubeCore.getConfig().guardSearchDistance);

                for (final LivingEntity _entity : pokemobs)
                {
                    final LivingEntity owner = Ownable.getOwner(_entity);
                    if (owner == targetOwner)
                    {
                        this.initiateBattle(_entity, entity);
                        this.clear(entity);
                        return;
                    }
                }
                brain.setMemory(MemoryModules.TIMER_SWAPTARGET.get(), switchTargetTimer);
            }
            // Otherwise agro the owner
            else
            {
                this.initiateBattle(targetOwner, entity);
                this.clear(entity);
                return;
            }
        }
        if (brain.hasMemoryValue(MemoryModules.ATTACKTARGETID.get()))
        {
            // Lets check if the target say was failed to capture. If this was the
            // case, then the UUID is still the same, but the entity itself has
            // changed.
            final Entity newMob = level.getEntity(brain.getMemory(MemoryModules.ATTACKTARGETID.get()).get());
            if (newMob instanceof LivingEntity living)
            {
                this.initiateBattle(living, entity);
                this.clear(entity);
            }
        }
        else
        {
            this.clear(entity);
        }

    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var brain = entity.getBrain();
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Check if pokemob can see the target, if yes start battle
        if (brain.hasMemoryValue(MemoryModules.ATTACKTARGETID.get()))
        {
            var targetId = brain.getMemory(MemoryModules.ATTACKTARGETID.get()).get();
            final Entity mob = level.getEntity(targetId);
            if (!(mob instanceof LivingEntity living) || (!BrainUtils.canSee(entity, living) && !this.initiateBattle(
                    living, entity))) this.clear(entity);
            int forgetTimer = brain.getMemory(MemoryModules.TIMER_FORGETTARGET.get())
                    .orElse(FindTargetsTask.DEAGROTIMER);
            // Reset target ID here, so we don't keep looking for it.
            if (forgetTimer-- <= 0) this.clear(entity);
            else brain.setMemory(MemoryModules.TIMER_FORGETTARGET.get(), forgetTimer);
            return;
        }

        // If pokemob is hurt by someone, for example players
        final Optional<LivingEntity> hurtBy = entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtBy != null && hurtBy.isPresent())
        {
            final LivingEntity target = hurtBy.get();
            // This will ensure that the target isn't on our team
            if (!this.validGuardTarget.apply(entity, target)) return;

            if (BrainUtils.canSee(entity, target))
            {
                this.initiateBattle(target, entity);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Selecting Target who hit us.");
                return;
            }
        }

        // If guarding, look for mobs not on the same team as you, and if you
        // find them, try to agress them.
        if (pokemob.getCombatState(CombatStates.GUARDING)) if (this.checkGuard(pokemob)) return;

        // Ensure the correct owner is tracked.
        pokemob.getOwner(level);

        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (!pokemob.getGeneralState(GeneralStates.STAYING)) if (this.checkOwner(pokemob)) return;

        final boolean playerNear = entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        // If wild, randomly decided to agro a nearby player instead.
        if (playerNear && AITools.shouldAgroNearestPlayer.test(pokemob))
        {
            int aggroDistance = Tags.POKEMOB.isIn(AITools.HOSTILE, pokemob.getPokedexEntry().getTrimmedName())
                    ? PokecubeCore.getConfig().hostileAggroRadius
                    : PokecubeCore.getConfig().aggressiveAggroRadius;
            Player player = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).get();
            if (player != null && player.distanceTo(entity) > aggroDistance) player = null;
            if (player != null && AITools.validAgroTarget.test(player))
            {
                this.initiateBattle(player, entity);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Found player to be angry with, agressing.");
            }
        }
    }

    private boolean initiateBattle(final LivingEntity target, Mob entity)
    {
        if (!Battle.createOrAddToBattle(entity, target))
        {
            this.clear(entity);
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (!pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;
        if (!entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return false;
        if (BrainUtils.hasAttackTarget(entity))
        {
            var brain = entity.getBrain();
            final LivingEntity target = BrainUtils.getAttackTarget(entity);
            var targetOwner = Ownable.getOwner(target);

            if (!brain.hasMemoryValue(MemoryModules.ATTACKTARGETID.get()))
            {
                brain.setMemory(MemoryModules.ATTACKTARGETID.get(), target.getUUID());
            }
            else
            {
                var targetId = brain.getMemory(MemoryModules.ATTACKTARGETID.get()).get();
                if (!target.getUUID().equals(targetId))
                {
                    brain.setMemory(MemoryModules.ATTACKTARGETID.get(), target.getUUID());
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Found Target {} {}", target, targetOwner);
                }
            }
            this.checkSwitchedMob(pokemob, target, targetOwner);
            return false;
        }
        return true;
    }

}