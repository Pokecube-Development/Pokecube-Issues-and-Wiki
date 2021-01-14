package pokecube.adventures.ai.tasks.battle.agro;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

public abstract class BaseAgroTask extends BaseTask implements ITargetWatcher
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> MEMS = Maps.newHashMap();

    static
    {
        BaseAgroTask.MEMS.put(MemoryTypes.BATTLETARGET, MemoryModuleStatus.VALUE_ABSENT);
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
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemory(MemoryTypes.BATTLETARGET)) return false;
        final LivingEntity targ = brain.getMemory(MemoryTypes.BATTLETARGET).get();
        if (targ != this.target)
        {
            this.timer = 0;
            this.target = targ;
        }
        return this.maxTimer <= 0 || this.timer++ < this.maxTimer;
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final LivingEntity owner, final long gameTime)
    {
        this.timer++;
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        if (this.trainer.getCooldown() > gameTimeIn) return;
        if (worldIn.getRandom().nextDouble() > this.chance) return;
        final List<LivingEntity> mobs = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).get();

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
        final Vector3d start = entityIn.getEyePosition(1);
        Vector3d line = entityIn.getLook(1).mul(s, s, s);
        Vector3d end = start.add(line);

        final int rep_base = PokecubeAdv.config.trainer_min_rep;
        final int rep_cap = PokecubeAdv.config.trainer_max_rep;
        final int drep = rep_cap - rep_base;

        for (LivingEntity mob : mobs)
            if (this.isValidTarget(mob) && tameChecker.test(mob))
            {
                if (mob instanceof PlayerEntity && this.entity instanceof VillagerEntity)
                {
                    final VillagerEntity villager = (VillagerEntity) this.entity;
                    final int rep = villager.getPlayerReputation((PlayerEntity) mob);
                    double s1 = s;
                    if (rep > rep_cap) s1 = 0;
                    else if (rep < rep_base) s1 *= 2;
                    else s1 *= (rep_cap - rep) / drep;
                    line = entityIn.getLook(1).mul(s1, s1, s1);
                    end = start.add(line);
                }

                final boolean lookingAt = mob.getBoundingBox().rayTrace(start, end).isPresent();
                if (!lookingAt)
                {
                    BrainUtil.lookAt(this.entity, mob);
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
    protected boolean shouldExecute(final ServerWorld worldIn, final LivingEntity owner)
    {
        final Brain<?> brain = owner.getBrain();
        if (brain.hasMemory(MemoryTypes.BATTLETARGET)) return false;
        if (owner.ticksExisted % PokecubeAdv.config.trainerAgroRate != 0) return false;
        return this.entity.getBrain().hasMemory(MemoryModuleType.VISIBLE_MOBS);
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        this.timer = 0;
        this.target = null;
    }
}
