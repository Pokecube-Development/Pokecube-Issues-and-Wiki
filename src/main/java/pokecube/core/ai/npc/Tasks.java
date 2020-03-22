package pokecube.core.ai.npc;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
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
        return ImmutableList.of(Pair.of(0, new SwimTask(0.4F, 0.8F)), Pair.of(0, new InteractWithDoorTask()), Pair.of(1,
                new LookTask(45, 90)));
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> battle(
            final VillagerProfession profession, final float p_220638_1_)
    {
        return ImmutableList.of(Pair.of(0, new SwimTask(0.4F, 0.8F)), Pair.of(0, new InteractWithDoorTask()), Pair.of(1,
                new LookTask(45, 90)));
    }
}
