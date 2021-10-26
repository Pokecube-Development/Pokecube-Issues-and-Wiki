package pokecube.core.ai.npc;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.GoOutsideToCelebrate;
import net.minecraft.world.entity.ai.behavior.GoToClosestVillage;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlaceDuringRaid;
import net.minecraft.world.entity.ai.behavior.LookAndFollowTradingPlayerSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PlayTagWithOtherKids;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.ReactToBell;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.behavior.ResetRaidStatus;
import net.minecraft.world.entity.ai.behavior.RingBell;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.SetHiddenState;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetRaidStatus;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.behavior.SocializeAtBell;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.VictoryStroll;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerCalmDown;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.ai.behavior.WakeUp;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import pokecube.core.ai.tasks.misc.BlankTask;
import pokecube.core.ai.tasks.misc.LookAtMob;
import pokecube.core.ai.tasks.misc.LookAtTask;
import pokecube.core.ai.tasks.misc.RunAway;
import pokecube.core.ai.tasks.misc.SwimTask;

public class Tasks
{
    //@formatter:off
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> stationary(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> battle(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> core(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTask(45, 90)),
                Pair.of(0, new VillagerPanicTrigger()),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new LookAndFollowTradingPlayerSink(speed)),
                Pair.of(10, new AcquirePoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))),
                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
                Pair.of(10, new AssignProfessionFromJobSite()), Pair.of(10, new ResetProfession())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> work(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(5, new ShuffledTask<>(ImmutableList.of(
                    Pair.of(new WorkAtPoi(), 7),
                    Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4F,  4), 2),
                    Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                    Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, 0.4F, 1, 6, MemoryModuleType.JOB_SITE), 5),
                    Pair.of(new HarvestFarmland(), profession == VillagerProfession.FARMER ? 2 : 5)
                    ))),
                Pair.of(10, new ShowTradesToPlayer(400, 1600)),
                Pair.of(10, new SetLookAndInteract(EntityType.PLAYER,4)),
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, speed,9 , 100, 1200)),
                Pair.of(3, new SetWalkTargetFromBlockMemory(MemoryModuleType.HOME, speed,9 , 100, 1200)),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new ValidateNearbyPoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> play(final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new MoveToTargetSink()),
                Tasks.lookAtMany(),
                Pair.of(5, new PlayTagWithOtherKids()),
                Pair.of(5, new ShuffledTask<>(
                    ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT),
                    ImmutableList.of(
                    Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2),
                    Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1),
                    Pair.of(new VillageBoundRandomStroll(speed), 1),
                    Pair.of(new SetWalkTargetFromLookTarget(speed, 2),1),
                    Pair.of(new JumpOnBed(speed), 2),
                    Pair.of(new BlankTask(20, 40), 2)
                    ))),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> rest(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.HOME, speed, 1, 150, 1200)),
                Pair.of(3, new ValidateNearbyPoi(PoiType.HOME, MemoryModuleType.HOME)),
                Pair.of(3, new SleepInBed()),
                Pair.of(5, new ShuffledTask<>(
                    ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT),
                    ImmutableList.of(
                    Pair.of(new SetClosestHomeAsWalkTarget(speed), 1),
                    Pair.of(new RandomStroll(speed), 4),
                    Pair.of(new GoToClosestVillage(speed, 4), 2),
                    Pair.of(new BlankTask(20, 40), 2)
                    ))),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> meet(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(2, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new StrollAroundPoi(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                    Pair.of(new SocializeAtBell(), 2)
                    ))),
                Pair.of(10, new ShowTradesToPlayer(400, 1600)),
                Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)),
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT,speed, 6, 100, 200)),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new ValidateNearbyPoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT)),
                Pair.of(3, new GateBehavior<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                    GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new TradeWithVillager(), 1)
                    ))),
                Tasks.lookAtMany(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> idle(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(2, new RunOne<>(
                    ImmutableList.of(
                    Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2),
                    Pair.of(new InteractWith<>(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, speed, 2), 1),
                    Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1),
                    Pair.of(new VillageBoundRandomStroll(speed), 1),
                    Pair.of(new SetWalkTargetFromLookTarget(speed, 2), 1),
                    Pair.of(new JumpOnBed(speed), 1),
                    Pair.of(new BlankTask(30, 60), 1)
                    ))),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new SetLookAndInteract(EntityType.PLAYER, 4)),
                Pair.of(3, new ShowTradesToPlayer(400, 1600)),
                Pair.of(3, new GateBehavior<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                    GateBehavior.OrderPolicy.ORDERED,GateBehavior.RunningPolicy.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new TradeWithVillager(), 1)
                    ))),
                Pair.of(3, new GateBehavior<>(
                    ImmutableMap.of(),
                    ImmutableSet.of(
                    MemoryModuleType.BREED_TARGET),
                    GateBehavior.OrderPolicy.ORDERED,
                    GateBehavior.RunningPolicy.RUN_ONE,
                    ImmutableList.of(
                    Pair.of(new VillagerMakeLove(), 1)
                    ))),
                Tasks.lookAtMany(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> panic(
            final VillagerProfession profession, final float p_220636_1_)
    {
        final float f = p_220636_1_ * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new VillagerCalmDown()),
                Pair.of(1, new RunAway(MemoryModuleType.NEAREST_HOSTILE, f)),
                Pair.of(1, new RunAway(MemoryModuleType.HURT_BY_ENTITY, f)),
                Pair.of(3, new VillageBoundRandomStroll(f, 2, 2)),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> preRaid(
            final VillagerProfession profession, final float p_220642_1_)
    {
        return ImmutableList.of(
                Pair.of(0, new RingBell()),
                Pair.of(0, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, p_220642_1_ * 1.5F, 2, 150, 200), 6),
                    Pair.of(new VillageBoundRandomStroll(p_220642_1_ * 1.5F), 2)
                    ))),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99,new ResetRaidStatus())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> raid(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new ShuffledTask<>(
                    ImmutableList.of(
                    Pair.of(new GoOutsideToCelebrate( speed), 5),
                    Pair.of(new VictoryStroll(speed * 1.1F), 2)
                    ))),
                Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)),
                Pair.of(2, new LocateHidingPlaceDuringRaid(24,speed * 1.4F)),
                Tasks.lookAtPlayerOrVillager(),
                Pair.of(99,new ResetRaidStatus())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> hide(
            final VillagerProfession profession, final float speed)
    {
        final int i = 2;
        return ImmutableList.of(
                Pair.of(0, new SetHiddenState(15, i)),
                Pair.of(1, new LocateHidingPlace(32,
                speed * 1.25F, i)),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    private static Pair<Integer, Behavior<LivingEntity>> lookAtMany()
    {
        return Pair.of(5, new ShuffledTask<>(
                ImmutableList.of(
                Pair.of(new LookAtMob(EntityType.CAT, 8.0F),8),
                Pair.of(new LookAtMob(EntityType.VILLAGER, 8.0F), 2),
                Pair.of(new LookAtMob(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new LookAtMob(MobCategory.CREATURE, 8.0F),1),
                Pair.of(new LookAtMob(MobCategory.WATER_CREATURE, 8.0F), 1),
                Pair.of(new LookAtMob(MobCategory.MONSTER, 8.0F), 1),
                Pair.of(new BlankTask(30, 60), 2)
                )));
    }

    private static Pair<Integer, Behavior<LivingEntity>> lookAtPlayerOrVillager()
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
