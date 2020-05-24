package pokecube.core.ai.tasks.combat.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.ITargetFinder;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.AITools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class FindTargetsTask extends TaskBase<MobEntity> implements IAICombat, ITargetFinder
{

    public static boolean handleDamagedTargets = true;
    static
    {
        MinecraftForge.EVENT_BUS.register(FindTargetsTask.class);
    }

    public static int DEAGROTIMER = 50;

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

    public FindTargetsTask(final IPokemob mob)
    {
        super(mob, ImmutableMap.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT));
    }

    @Override
    public void clear()
    {
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

        final List<LivingEntity> ret = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        // Only allow valid guard targets.
        for (final LivingEntity o : pokemobs)
            if (this.validGuardTarget.test(o)) ret.add(o);
        ret.removeIf(e -> e.getDistance(this.entity) > PokecubeCore.getConfig().guardSearchDistance);
        if (ret.isEmpty()) return false;

        // This is already sorted by distance!
        final LivingEntity newtarget = ret.get(0);
        // Agro the target.
        if (newtarget != null)
        {
            this.setAttackTarget(this.entity, newtarget);
            return true;
        }
        return false;
    }

    protected void setAttackTarget(final MobEntity attacker, final LivingEntity target)
    {
        if (target == null || !AITools.validTargets.test(target))
        {
            BrainUtils.deagro(attacker);
            this.clear();
        }
        else BrainUtils.initiateCombat(attacker, target);
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
        if (rate <= 0 || this.entity.ticksExisted % rate != 0) return false;

        final List<LivingEntity> list = new ArrayList<>();
        final List<LivingEntity> pokemobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();
        list.addAll(pokemobs);
        list.removeIf(e -> e.getDistance(this.entity) > PokecubeCore.getConfig().guardSearchDistance
                && AITools.validTargets.test(e));
        if (list.isEmpty()) return false;

        final Entity old = BrainUtils.getAttackTarget(this.entity);
        final IOwnable oldOwnable = OwnableCaps.getOwnable(old);
        final Entity oldOwner = oldOwnable != null ? oldOwnable.getOwner(this.world) : null;

        if (!list.isEmpty()) for (final LivingEntity entity : list)
        {
            if (oldOwner != null && entity == oldOwner) return false;
            final LivingEntity targ = BrainUtils.getAttackTarget(entity);
            if (entity instanceof MobEntity && targ != null && targ.equals(owner))
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
        final Optional<LivingEntity> hurtBy = this.entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtBy != null && hurtBy.isPresent())
        {
            final LivingEntity target = hurtBy.get();
            if (BrainUtil.canSee(this.entity.getBrain(), target))
            {
                this.setAttackTarget(this.entity, target);
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

        final boolean playerNear = this.entity.getBrain().hasMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        // If wild, randomly decided to agro a nearby player instead.
        if (playerNear && AITools.shouldAgroNearestPlayer.test(this.pokemob))
        {
            PlayerEntity player = this.entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).get();
            if (player != null && player.getDistance(this.entity) > PokecubeCore.getConfig().mobAggroRadius)
                player = null;
            if (player != null && AITools.validTargets.test(player))
            {
                this.setAttackTarget(this.entity, player);
                PokecubeCore.LOGGER.debug("Found player to be angry with, agressing.");
                return;
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;
        if (!this.entity.getBrain().hasMemory(MemoryModuleType.VISIBLE_MOBS)) return false;
        if (BrainUtils.hasAttackTarget(this.entity)) return false;
        return true;
    }

}