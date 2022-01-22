package pokecube.core.ai.npc.behaviours;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.entity.npc.NpcMob;

public class NpcMateTask extends VillagerMakeLove
{
    private static final int INTERACT_DIST_SQR = 5;
    private static final float SPEED_MODIFIER = 0.5F;
    private long birthTimestamp;

    public NpcMateTask()
    {
        super();
    }

    protected boolean checkExtraStartConditions(ServerLevel level, Villager mob)
    {
        return this.isBreedingPossible(mob);
    }

    protected boolean canStillUse(ServerLevel level, Villager mob, long time)
    {
        return time <= this.birthTimestamp && this.isBreedingPossible(mob);
    }

    protected void start(ServerLevel level, Villager mob, long time)
    {
        AgeableMob ageablemob = BrainUtils.getMateTarget(mob);
        BehaviorUtils.lockGazeAndWalkToEachOther(mob, ageablemob, SPEED_MODIFIER);
        level.broadcastEntityEvent(ageablemob, (byte) 18);
        level.broadcastEntityEvent(mob, (byte) 18);
        int i = 275 + mob.getRandom().nextInt(50);
        this.birthTimestamp = time + (long) i;
    }

    protected void tick(ServerLevel level, Villager mob, long time)
    {
        Villager villager = (Villager) BrainUtils.getMateTarget(mob);
        if (!(mob.distanceToSqr(villager) > INTERACT_DIST_SQR))
        {
            BehaviorUtils.lockGazeAndWalkToEachOther(mob, villager, SPEED_MODIFIER);
            if (time >= this.birthTimestamp)
            {
                mob.eatAndDigestFood();
                villager.eatAndDigestFood();
                this.tryToGiveBirth(level, mob, villager);
            }
            else if (mob.getRandom().nextInt(35) == 0)
            {
                level.broadcastEntityEvent(villager, (byte) 12);
                level.broadcastEntityEvent(mob, (byte) 12);
            }

        }
    }

    private void tryToGiveBirth(ServerLevel level, Villager mob, Villager target)
    {
        Optional<BlockPos> optional = this.takeVacantBed(level, mob);
        if (!optional.isPresent())
        {
            mob.setAge(3000);
            target.setAge(3000);
            level.broadcastEntityEvent(target, (byte) 13);
            level.broadcastEntityEvent(mob, (byte) 13);
        }
        else
        {
            Optional<Villager> optional1 = this.breed(level, mob, target);
            if (optional1.isPresent())
            {
                this.giveBedToChild(level, optional1.get(), optional.get());
            }
            else
            {
                level.getPoiManager().release(optional.get());
                DebugPackets.sendPoiTicketCountPacket(level, optional.get());
            }
        }

    }

    protected void stop(ServerLevel level, Villager mob, long time)
    {
        mob.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        mob.getPersistentData().remove("__mate__");
    }

    private boolean isBreedingPossible(Villager mob)
    {
        if (!(mob instanceof NpcMob npc && mob.getLevel() instanceof ServerLevel level)) return false;

        if (!mob.canBreed()) return false;

        Predicate<LivingEntity> valid = (target_) -> {
            if (target_ instanceof NpcMob npc2)
            {
                return npc2.isMale() != npc.isMale();
            }
            return false;
        };

        if (mob.getPersistentData().hasUUID("__mate__"))
        {
            Entity e = level.getEntity(mob.getPersistentData().getUUID("__mate__"));
            if ((e instanceof AgeableMob mate_target) && valid.test(mate_target))
            {
                BrainUtils.setMateTarget(mate_target, mob);
                BrainUtils.setMateTarget(mob, mate_target);
                return true;
            }
        }

        AgeableMob mate_target = BrainUtils.getMateTarget(mob);

        if (!valid.test(mate_target)) mate_target = null;

        if (mate_target == null)
        {
            return false;
        }
        else
        {
            Brain<Villager> brain = mob.getBrain();
            boolean validMate = BrainUtils.targetIsValid(brain, MemoryModuleType.BREED_TARGET, valid) && mob.canBreed()
                    && mate_target.canBreed();
            if (validMate)
            {
                BrainUtils.setMateTarget(mate_target, mob);
                mob.getPersistentData().putUUID("__mate__", mate_target.getUUID());
            }
            return validMate;
        }
    }

    private Optional<BlockPos> takeVacantBed(ServerLevel level, Villager mob)
    {
        return level.getPoiManager().take(PoiType.HOME.getPredicate(), (target) -> {
            return this.canReach(mob, target);
        }, mob.blockPosition(), 48);
    }

    private boolean canReach(Villager mob, BlockPos target)
    {
        Path path = mob.getNavigation().createPath(target.above(), PoiType.HOME.getValidRange());
        return path != null && path.canReach();
    }

    private Optional<Villager> breed(ServerLevel level, Villager mob, Villager target)
    {
        Villager villager = mob.getBreedOffspring(level, target);
        if (villager == null)
        {
            return Optional.empty();
        }
        else
        {
            mob.setAge(6000);
            target.setAge(6000);
            villager.setAge(-24000);
            villager.moveTo(mob.getX(), mob.getY(), mob.getZ(), 0.0F, 0.0F);
            level.addFreshEntityWithPassengers(villager);
            level.broadcastEntityEvent(villager, (byte) 12);
            return Optional.of(villager);
        }
    }

    private void giveBedToChild(ServerLevel level, Villager mob, BlockPos pos)
    {
        GlobalPos globalpos = GlobalPos.of(level.dimension(), pos);
        mob.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
    }
}