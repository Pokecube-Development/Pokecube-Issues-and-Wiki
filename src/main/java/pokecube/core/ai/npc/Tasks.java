package pokecube.core.ai.npc;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import pokecube.core.ai.npc.behaviours.InteractWith;
import pokecube.core.ai.npc.behaviours.NpcMateTask;
import pokecube.core.ai.tasks.misc.BlankTask;
import pokecube.core.ai.tasks.misc.LookAtMob;
import pokecube.core.ai.tasks.misc.LookAtTask;
import pokecube.core.ai.tasks.misc.SwimTask;
import pokecube.core.entity.npc.NpcMob;

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
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getIdlePackage(
            VillagerProfession p_24599_, float p_24600_)
    {
        Predicate<LivingEntity> isCat = (mob) -> mob.getType()==EntityType.CAT;
        Predicate<LivingEntity> isNpc = (mob) -> mob instanceof NpcMob;
        
        Predicate<AgeableMob> canBreed = (mob) -> mob.canBreed();
        
        
        return ImmutableList.of(Pair.of(2, new RunOne(ImmutableList.of(
                Pair.of(InteractWith.of(isNpc, 8, MemoryModuleType.INTERACTION_TARGET, p_24600_, 2), 2),
                Pair.of(new InteractWith(isNpc, 8, canBreed, canBreed,
                        MemoryModuleType.BREED_TARGET, p_24600_, 2), 1),
                Pair.of(InteractWith.of(isCat, 8, MemoryModuleType.INTERACTION_TARGET, p_24600_, 2), 1),
                Pair.of(new VillageBoundRandomStroll(p_24600_), 1),
                Pair.of(new SetWalkTargetFromLookTarget(p_24600_, 2), 1), Pair.of(new JumpOnBed(p_24600_), 1),
                Pair.of(new DoNothing(30, 60), 1)))), Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new SetLookAndInteract(EntityType.PLAYER, 4)), Pair.of(3, new ShowTradesToPlayer(400, 1600)),
                Pair.of(3,
                        new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                                GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                                ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))),
                Pair.of(3,
                        new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET),
                                GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                                ImmutableList.of(Pair.of(new NpcMateTask(), 1)))),
                lookAtMany(), Pair.of(99, new UpdateActivityFromSchedule()));
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
