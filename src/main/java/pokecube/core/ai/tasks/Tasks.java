package pokecube.core.ai.tasks;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.item.ItemEntity;
import pokecube.core.ai.routes.GuardAI.ShouldRun;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.idle.AIGuardEgg;
import pokecube.core.ai.tasks.idle.AIHungry;
import pokecube.core.ai.tasks.idle.AIIdle;
import pokecube.core.ai.tasks.idle.AIMate;
import pokecube.core.ai.tasks.idle.AIRoutes;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.CapHolders;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.TaskWrapper;

public class Tasks
{
    //@formatter:off
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> idle(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for combat
        final List<IAIRunnable> aiList = Lists.newArrayList();
        final MobEntity entity = pokemob.getEntity();

        final IGuardAICapability guardCap = entity.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        // Idle tasks
        // Guard your egg
        aiList.add(new AIGuardEgg(pokemob).setPriority(250));
        // Mate with things
        aiList.add(new AIMate(pokemob).setPriority(300));
        // Eat things
        aiList.add(new AIHungry(pokemob, new ItemEntity(entity.getEntityWorld(), 0, 0, 0), 16).setPriority(300));
        // Wander around
        aiList.add(new AIIdle(pokemob).setPriority(500));

        // Task for following routes/maintaining home location
        final AIRoutes routes = new AIRoutes(pokemob.getEntity(), guardCap);
        routes.wrapped.shouldRun = new ShouldRun()
        {
            @Override
            public boolean shouldRun()
            {
                if (!pokemob.getGeneralState(GeneralStates.TAMED)) return true;
                return pokemob.getGeneralState(GeneralStates.STAYING);
            }
        };
        // Follow paths or stay near home
        aiList.add(routes.setPriority(275));

        // Owner related tasks
        if (!pokemob.getPokedexEntry().isStationary) // Follow owner around
            aiList.add(new AIFollowOwner(pokemob, 3 + entity.getWidth() + pokemob.getPokedexEntry().length, 8 + entity.getWidth()
                    + pokemob.getPokedexEntry().length).setPriority(400));

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        list.add(Tasks.lookAtMany());
        list.add(Tasks.lookAtPlayerOrVillager());
        for(final IAIRunnable run: aiList)
            if(run instanceof ITask)
            list.add(Pair.of(run.getPriority(), new TaskWrapper<>((ITask) run)));
        return ImmutableList.copyOf(list);
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtMany()
    {
        return Pair.of(5, new FirstShuffledTask<>(
                ImmutableList.of(
                Pair.of(new LookAtEntityTask(EntityType.CAT, 8.0F),8),
                Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2),
                Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new LookAtEntityTask(EntityClassification.CREATURE, 8.0F),1),
                Pair.of(new LookAtEntityTask(EntityClassification.WATER_CREATURE, 8.0F), 1),
                Pair.of(new LookAtEntityTask(EntityClassification.MONSTER, 8.0F), 1),
                Pair.of(new DummyTask(30, 60), 2)
                )));
    }

    private static Pair<Integer, Task<LivingEntity>> lookAtPlayerOrVillager()
    {
        return Pair.of(3, new FirstShuffledTask<>(
                ImmutableList.of(
                Pair.of(new LookAtEntityTask(EntityType.VILLAGER,8.0F), 2),
                Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new DummyTask(30, 60),8)
                )));
    }
    //@formatter:on

}
