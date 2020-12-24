package pokecube.core.ai.npc;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AssignProfessionTask;
import net.minecraft.entity.ai.brain.task.BeginRaidTask;
import net.minecraft.entity.ai.brain.task.CelebrateRaidVictoryTask;
import net.minecraft.entity.ai.brain.task.ChangeJobTask;
import net.minecraft.entity.ai.brain.task.ClearHurtTask;
import net.minecraft.entity.ai.brain.task.CongregateTask;
import net.minecraft.entity.ai.brain.task.CreateBabyVillagerTask;
import net.minecraft.entity.ai.brain.task.ExpireHidingTask;
import net.minecraft.entity.ai.brain.task.ExpirePOITask;
import net.minecraft.entity.ai.brain.task.FarmTask;
import net.minecraft.entity.ai.brain.task.FindHidingPlaceDuringRaidTask;
import net.minecraft.entity.ai.brain.task.FindHidingPlaceTask;
import net.minecraft.entity.ai.brain.task.FindInteractionAndLookTargetTask;
import net.minecraft.entity.ai.brain.task.FindWalkTargetAfterRaidVictoryTask;
import net.minecraft.entity.ai.brain.task.FindWalkTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.ForgetRaidTask;
import net.minecraft.entity.ai.brain.task.GatherPOITask;
import net.minecraft.entity.ai.brain.task.GiveHeroGiftsTask;
import net.minecraft.entity.ai.brain.task.GoOutsideAfterRaidTask;
import net.minecraft.entity.ai.brain.task.HideFromRaidOnBellRingTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.InteractWithEntityTask;
import net.minecraft.entity.ai.brain.task.JumpOnBedTask;
import net.minecraft.entity.ai.brain.task.MultiTask;
import net.minecraft.entity.ai.brain.task.PanicTask;
import net.minecraft.entity.ai.brain.task.RingBellTask;
import net.minecraft.entity.ai.brain.task.ShareItemsTask;
import net.minecraft.entity.ai.brain.task.ShowWaresTask;
import net.minecraft.entity.ai.brain.task.SleepAtHomeTask;
import net.minecraft.entity.ai.brain.task.SpawnGolemTask;
import net.minecraft.entity.ai.brain.task.StayNearPointTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TradeTask;
import net.minecraft.entity.ai.brain.task.UpdateActivityTask;
import net.minecraft.entity.ai.brain.task.WakeUpTask;
import net.minecraft.entity.ai.brain.task.WalkRandomlyTask;
import net.minecraft.entity.ai.brain.task.WalkToHouseTask;
import net.minecraft.entity.ai.brain.task.WalkToPOITask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkToVillagerBabiesTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsRandomSecondaryPosTask;
import net.minecraft.entity.ai.brain.task.WorkTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;
import pokecube.core.ai.tasks.BlankTask;
import pokecube.core.ai.tasks.LookAtMob;
import pokecube.core.ai.tasks.LookAtTask;
import pokecube.core.ai.tasks.RunAway;
import pokecube.core.ai.tasks.SwimTask;

public class Tasks
{
    //@formatter:off
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> stationary(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoorTask()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> battle(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoorTask()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> core(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoorTask()),
                Pair.of(0, new LookAtTask(45, 90)),
                Pair.of(0, new PanicTask()),
                Pair.of(0, new WakeUpTask()),
                Pair.of(0, new HideFromRaidOnBellRingTask()),
                Pair.of(0, new BeginRaidTask()),
                Pair.of(1, new WalkToTargetTask()),
                Pair.of(2, new TradeTask(speed)),
                Pair.of(10, new GatherPOITask(profession.getPointOfInterest(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(10, new GatherPOITask(PointOfInterestType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))),
                Pair.of(10, new GatherPOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
                Pair.of(10, new AssignProfessionTask()), Pair.of(10, new ChangeJobTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> work(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(5, new ShuffledTask<>(ImmutableList.of(
                    Pair.of(new SpawnGolemTask(), 7),
                    Pair.of(new WorkTask(MemoryModuleType.JOB_SITE, 0.4F,  4), 2),
                    Pair.of(new WalkTowardsPosTask(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                    Pair.of(new WalkTowardsRandomSecondaryPosTask(MemoryModuleType.SECONDARY_JOB_SITE, 0.4F, 1, 6, MemoryModuleType.JOB_SITE), 5),
                    Pair.of(new FarmTask(), profession == VillagerProfession.FARMER ? 2 : 5)
                    ))),
                Pair.of(10, new ShowWaresTask(400, 1600)),
                Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER,4)),
                Pair.of(2, new StayNearPointTask(MemoryModuleType.JOB_SITE, speed,9 , 100, 1200)),
                Pair.of(3, new StayNearPointTask(MemoryModuleType.HOME, speed,9 , 100, 1200)),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(3, new ExpirePOITask(profession.getPointOfInterest(), MemoryModuleType.JOB_SITE)),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> play(final float walkingSpeed)
    {
        return ImmutableList.of(
                Pair.of(0, new WalkToTargetTask()),
                Tasks.lookAtMany(),
                Pair.of(5, new WalkToVillagerBabiesTask()),
                Pair.of(5, new ShuffledTask<>(
                    ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleStatus.VALUE_ABSENT),
                    ImmutableList.of(
                    Pair.of(InteractWithEntityTask.func_220445_a(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, walkingSpeed, 2), 2),
                    Pair.of(InteractWithEntityTask.func_220445_a(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, walkingSpeed, 2), 1),
                    Pair.of(new FindWalkTargetTask(walkingSpeed), 1),
                    Pair.of(new WalkTowardsLookTargetTask(walkingSpeed, 2),1),
                    Pair.of(new JumpOnBedTask(walkingSpeed), 2),
                    Pair.of(new BlankTask(20, 40), 2)
                    ))),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> rest(
            final VillagerProfession profession, final float walkingSpeed)
    {
        return ImmutableList.of(
                Pair.of(2, new StayNearPointTask(MemoryModuleType.HOME, walkingSpeed, 1, 150, 1200)),
                Pair.of(3, new ExpirePOITask(PointOfInterestType.HOME, MemoryModuleType.HOME)),
                Pair.of(3, new SleepAtHomeTask()),
                Pair.of(5, new ShuffledTask<>(
                    ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleStatus.VALUE_ABSENT),
                    ImmutableList.of(
                    Pair.of(new WalkToHouseTask(walkingSpeed), 1),
                    Pair.of(new WalkRandomlyTask(walkingSpeed), 4),
                    Pair.of(new WalkToPOITask(walkingSpeed, 4), 2),
                    Pair.of(new BlankTask(20, 40), 2)
                    ))),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> meet(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(2, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new WorkTask(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                    Pair.of(new CongregateTask(), 2)
                    ))),
                Pair.of(10, new ShowWaresTask(400, 1600)),
                Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
                Pair.of(2, new StayNearPointTask(MemoryModuleType.MEETING_POINT,speed, 6, 100, 200)),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(3, new ExpirePOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT)),
                Pair.of(3, new MultiTask<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                    MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new ShareItemsTask(), 1)
                    ))),
                Tasks.lookAtMany(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> idle(
            final VillagerProfession profession, final float p_220641_1_)
    {
        return ImmutableList.of(
                Pair.of(2, new FirstShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(InteractWithEntityTask.func_220445_a(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, p_220641_1_, 2), 2),
                    Pair.of(new InteractWithEntityTask<>(EntityType.VILLAGER, 8, AgeableEntity::canBreed, AgeableEntity::canBreed, MemoryModuleType.BREED_TARGET, p_220641_1_, 2), 1),
                    Pair.of(InteractWithEntityTask.func_220445_a(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, p_220641_1_, 2), 1),
                    Pair.of(new FindWalkTargetTask(p_220641_1_), 1),
                    Pair.of(new WalkTowardsLookTargetTask(p_220641_1_, 2), 1),
                    Pair.of(new JumpOnBedTask(p_220641_1_), 1),
                    Pair.of(new BlankTask(30, 60), 1)
                    ))),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(3, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
                Pair.of(3, new ShowWaresTask(400, 1600)),
                Pair.of(3, new MultiTask<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                    MultiTask.Ordering.ORDERED,MultiTask.RunType.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new ShareItemsTask(), 1)
                    ))),
                Pair.of(3, new MultiTask<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(
                    MemoryModuleType.BREED_TARGET),
                    MultiTask.Ordering.ORDERED,
                    MultiTask.RunType.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new CreateBabyVillagerTask(), 1)
                    ))),
                Tasks.lookAtMany(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> panic(
            final VillagerProfession profession, final float p_220636_1_)
    {
        final float f = p_220636_1_ * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new ClearHurtTask()),
                Pair.of(1, new RunAway(MemoryModuleType.NEAREST_HOSTILE, f)),
                Pair.of(1, new RunAway(MemoryModuleType.HURT_BY_ENTITY, f)),
                Pair.of(3, new FindWalkTargetTask(f, 2, 2)),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> preRaid(
            final VillagerProfession profession, final float p_220642_1_)
    {
        return ImmutableList.of(
                Pair.of(0, new RingBellTask()),
                Pair.of(0, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new StayNearPointTask(MemoryModuleType.MEETING_POINT, p_220642_1_ * 1.5F, 2, 150, 200), 6),
                    Pair.of(new FindWalkTargetTask(p_220642_1_ * 1.5F), 2)
                    ))),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99,new ForgetRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> raid(
            final VillagerProfession profession, final float p_220640_1_)
    {
        return ImmutableList.of(
                Pair.of(0, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new GoOutsideAfterRaidTask( p_220640_1_), 5),
                    Pair.of(new FindWalkTargetAfterRaidVictoryTask(p_220640_1_ * 1.1F), 2)
                    ))),
                Pair.of(0, new CelebrateRaidVictoryTask(600, 600)),
                Pair.of(2, new FindHidingPlaceDuringRaidTask(24,p_220640_1_ * 1.4F)),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99,new ForgetRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> hide(
            final VillagerProfession profession, final float p_220644_1_)
    {
        final int i = 2;
        return ImmutableList.of(
                Pair.of(0, new ExpireHidingTask(15, i)),
                Pair.of(1, new FindHidingPlaceTask(32,
                p_220644_1_ * 1.25F, i)),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtMany()
    {
        return Pair.of(5, new ShuffledTask<>(
                ImmutableList.of(
                Pair.of(new LookAtMob(EntityType.CAT, 8.0F),8),
                Pair.of(new LookAtMob(EntityType.VILLAGER, 8.0F), 2),
                Pair.of(new LookAtMob(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new LookAtMob(EntityClassification.CREATURE, 8.0F),1),
                Pair.of(new LookAtMob(EntityClassification.WATER_CREATURE, 8.0F), 1),
                Pair.of(new LookAtMob(EntityClassification.MONSTER, 8.0F), 1),
                Pair.of(new BlankTask(30, 60), 2)
                )));
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtPlayerOrVillager()
    {
        return Pair.of(3, new ShuffledTask<>(
                ImmutableList.of(
                Pair.of(new LookAtMob(EntityType.VILLAGER,8.0F), 2),
                Pair.of(new LookAtMob(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new BlankTask(30, 60),8)
                )));
    }
    //@formatter:on
}
