package pokecube.core.ai.npc;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import pokecube.core.ai.npc.behaviours.InteractWith;
import pokecube.core.ai.tasks.misc.BlankTask;
import pokecube.core.ai.tasks.misc.LookAtMob;
import pokecube.core.ai.tasks.misc.LookAtTask;
import pokecube.core.ai.tasks.misc.SwimTask;
import pokecube.core.entity.npc.NpcMob;

public class Tasks
{

    //@formatter:off
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> stationary(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, InteractWithDoor.create()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> battle(
            final VillagerProfession profession, final float speed)
    {
        return ImmutableList.of(
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, InteractWithDoor.create()),
                Pair.of(0, new LookAtTask(45, 90)),
                Tasks.lookAtMany(),
                Tasks.lookAtPlayerOrVillager()
        );
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getIdlePackage(
            VillagerProfession profession, float speed)
    {
        Predicate<LivingEntity> isCat = (mob) -> mob.getType()==EntityType.CAT;
        Predicate<LivingEntity> isNpc = (mob) -> mob instanceof NpcMob;
        Predicate<LivingEntity> isNpc2 = (mob) -> mob instanceof NpcMob;
        Predicate<LivingEntity> canBreed = (mob) -> mob instanceof NpcMob npc &&  npc.canBreed();
        
        var interact_npc = InteractWith.of(isNpc, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2);
        var interact_cat =  InteractWith.of(isCat, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2);
        var interact_breedable = InteractWith.of(isNpc2, 8, canBreed, canBreed, MemoryModuleType.BREED_TARGET, speed, 2);

        return ImmutableList.of(
                Pair.of(2, new RunOne(ImmutableList.of(
                        Pair.of(interact_npc, 2), 
                        Pair.of(interact_breedable, 1), Pair.of(interact_cat, 1), 
                        Pair.of(VillageBoundRandomStroll.create(speed), 1),
                        Pair.of(SetWalkTargetFromLookTarget.create(speed, 2), 1), 
                        Pair.of(new JumpOnBed(speed), 1), Pair.of(new DoNothing(30, 60), 1)))), 
                Pair.of(3, new GiveGiftToHero(100)), 
                Pair.of(3, SetLookAndInteract.create(EntityType.PLAYER, 4)), 
                Pair.of(3, new ShowTradesToPlayer(400, 1600)), 
                Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, 
                        ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))), 
                Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, 
                        ImmutableList.of(Pair.of(new VillagerMakeLove(), 1)))), 
                getFullLookBehavior(), 
                Pair.of(99, UpdateActivityFromSchedule.create()));
    }
    private static Pair<Integer, BehaviorControl<LivingEntity>> getFullLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.CAT, 8.0F), 8), Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(MobCategory.CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.WATER_CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.AXOLOTLS, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.WATER_AMBIENT, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.MONSTER, 8.0F), 1), Pair.of(new DoNothing(30, 60), 2))));
     }

    private static Pair<Integer, BehaviorControl<LivingEntity>> lookAtMany()
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

    private static Pair<Integer, BehaviorControl<LivingEntity>> lookAtPlayerOrVillager()
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
