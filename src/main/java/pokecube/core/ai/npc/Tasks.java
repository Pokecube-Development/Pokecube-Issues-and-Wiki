package pokecube.core.ai.npc;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.SwimTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;

public class Tasks
{
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> stationary(
            final VillagerProfession profession, final float p_220638_1_)
    {
        return ImmutableList.of(Pair.of(0, new SwimTask(0.4F, 0.8F)), Pair.of(0, new InteractWithDoorTask()), Pair.of(0,
                new LookTask(45, 90)), Tasks.lookAtMany(), Tasks.lookAtPlayerOrVillager());
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> battle(
            final VillagerProfession profession, final float p_220638_1_)
    {
        return ImmutableList.of(Pair.of(0, new SwimTask(0.4F, 0.8F)), Pair.of(0, new InteractWithDoorTask()), Pair.of(0,
                new LookTask(45, 90)), Tasks.lookAtMany(), Tasks.lookAtPlayerOrVillager());
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtMany()
    {
        return Pair.of(5, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.CAT, 8.0F),
                8), Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2), Pair.of(new LookAtEntityTask(
                        EntityType.PLAYER, 8.0F), 2), Pair.of(new LookAtEntityTask(EntityClassification.CREATURE, 8.0F),
                                1), Pair.of(new LookAtEntityTask(EntityClassification.WATER_CREATURE, 8.0F), 1), Pair
                                        .of(new LookAtEntityTask(EntityClassification.MONSTER, 8.0F), 1), Pair.of(
                                                new DummyTask(30, 60), 2))));
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtPlayerOrVillager()
    {
        return Pair.of(3, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.VILLAGER,
                8.0F), 2), Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2), Pair.of(new DummyTask(30, 60),
                        8))));
    }
}
