package pokecube.adventures.ai.tasks.battle.agro;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

public abstract class BaseAgroTask extends BaseTask implements ITargetWatcher
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseAgroTask.MEMS.put(MemoryTypes.BATTLETARGET, MemoryStatus.VALUE_ABSENT);
    }

    private int timer = 0;

    private final int maxTimer;

    private final float chance;

    LivingEntity target = null;

    public BaseAgroTask(final LivingEntity trainer, final float agressionProbability, final int battleTime)
    {
        super(trainer, BaseAgroTask.MEMS);
        this.trainer.addTargetWatcher(this);
        this.maxTimer = battleTime;
        this.chance = agressionProbability;
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET)) return false;
        final LivingEntity targ = brain.getMemory(MemoryTypes.BATTLETARGET).get();
        if (targ != this.target)
        {
            this.timer = 0;
            this.target = targ;
        }
        return this.maxTimer <= 0 || this.timer++ < this.maxTimer;
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        this.timer++;
    }

    @Override
    protected void start(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        if (this.trainer.getCooldown() > gameTimeIn) return;
        if (worldIn.getRandom().nextDouble() > this.chance) return;
        final NearestVisibleLivingEntities mobs = this.entity.getBrain().getMemory(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();

        // Count tame mobs as their owners, rather than seperately mobs
        final Predicate<LivingEntity> tameChecker = mob ->
        {
            final IOwnable owned = OwnableCaps.getOwnable(mob);
            LivingEntity owner;
            // If there is an owner, divert the check to it, rather than mob
            if (owned != null && (owner = owned.getOwner(worldIn)) != null) return this.isValidTarget(owner);
            // Otherwise, let this pass as valid
            return true;
        };
        final double s = this.trainer.getAgressDistance();
        final Vec3 start = entityIn.getEyePosition(1);
        Vec3 line = entityIn.getViewVector(1).multiply(s, s, s);
        Vec3 end = start.add(line);

        final int rep_base = PokecubeAdv.config.trainer_min_rep;
        final int rep_cap = PokecubeAdv.config.trainer_max_rep;
        final int drep = rep_cap - rep_base;

        for (LivingEntity mob : mobs.findAll(mob -> this.isValidTarget(mob) && tameChecker.test(mob)))
        {
            if (mob instanceof Player && this.entity instanceof Villager)
            {
                final Villager villager = (Villager) this.entity;
                final int rep = villager.getPlayerReputation((Player) mob) + rep_base;
                double s1 = s;
                if (rep > rep_cap) s1 = 0;
                else if (rep < rep_base) s1 *= 2;
                else s1 *= (rep_cap - rep) / drep;
                line = entityIn.getViewVector(1).multiply(s1, s1, s1);
                end = start.add(line);
            }

            final boolean lookingAt = mob.getBoundingBox().clip(start, end).isPresent();
            if (!lookingAt)
            {
                BehaviorUtils.lookAtEntity(this.entity, mob);
                return;
            }
            final IOwnable owned = OwnableCaps.getOwnable(mob);
            LivingEntity owner;
            // Agro the owner of the mob, instead of the mob itself in this
            // case.
            if (owned != null && (owner = owned.getOwner(worldIn)) != null) mob = owner;
            this.timer = 0;
            this.target = mob;
            this.trainer.onSetTarget(mob);
            return;
        }
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        if (brain.hasMemoryValue(MemoryTypes.BATTLETARGET)) return false;
        if (owner.tickCount % PokecubeAdv.config.trainerAgroRate != 0) return false;
        return this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void stop(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        this.timer = 0;
        this.target = null;
    }
}
