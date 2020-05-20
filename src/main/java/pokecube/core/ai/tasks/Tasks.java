package pokecube.core.ai.tasks;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.item.ItemEntity;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardAI.ShouldRun;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.combat.AIAttack;
import pokecube.core.ai.tasks.combat.AICombatMovement;
import pokecube.core.ai.tasks.combat.AIDodge;
import pokecube.core.ai.tasks.combat.AIFindTarget;
import pokecube.core.ai.tasks.combat.AILeap;
import pokecube.core.ai.tasks.combat.AISelectMove;
import pokecube.core.ai.tasks.idle.AIGuardEgg;
import pokecube.core.ai.tasks.idle.AIHungry;
import pokecube.core.ai.tasks.idle.AIIdle;
import pokecube.core.ai.tasks.idle.AIMate;
import pokecube.core.ai.tasks.idle.AIRoutes;
import pokecube.core.ai.tasks.utility.AIGatherStuff;
import pokecube.core.ai.tasks.utility.AIStoreStuff;
import pokecube.core.ai.tasks.utility.AIUseMove;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.CapHolders;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.TaskWrapper;

public class Tasks
{
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModules.ATTACKTARGET,
            MemoryModules.HUNTTARGET, MemoryModules.PATH, MemoryModules.WALK_TARGET);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS, SensorType.INTERACTABLE_DOORS, SensorType.HURT_BY);

    public static void initBrain(final Brain<?> brain)
    {
        BrainUtils.addToBrain(brain, Tasks.MEMORY_TYPES, Tasks.SENSOR_TYPES);
    }

    //@formatter:off
    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> idle(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for idle
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

        final GuardAI guardai = new GuardAI(pokemob.getEntity(),guardCap);
        final Pair<Integer, GuardTask<?>> pair = Pair.of(0, new GuardTask<>(guardai));
        list.add(pair);
        pokemob.getTasks().addAll(aiList);

        list.add(Tasks.lookAtMany());
        list.add(Tasks.lookAtPlayerOrVillager());
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            else if (run instanceof ITask) toAdd = new TaskWrapper<>((ITask) run);
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> combat(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for combat
        final List<IAIRunnable> aiList = Lists.newArrayList();

        // combat tasks
        aiList.add(new AISelectMove(pokemob).setPriority(190));
        // Attack stuff
        aiList.add(new AIAttack(pokemob).setPriority(200));
        // Dodge attacks
        aiList.add(new AIDodge(pokemob).setPriority(225));
        // Leap at things
        aiList.add(new AILeap(pokemob).setPriority(225));
        // Move around in combat
        aiList.add(new AICombatMovement(pokemob).setPriority(250));
        // Look for targets to kill
        final AIFindTarget targetFind = new AIFindTarget(pokemob);
        aiList.add(targetFind.setPriority(400));
        pokemob.setTargetFinder(targetFind);

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        final IGuardAICapability guardCap = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final GuardAI guardai = new GuardAI(pokemob.getEntity(),guardCap);
        final Pair<Integer, GuardTask<?>> pair = Pair.of(0, new GuardTask<>(guardai));
        list.add(pair);
        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            else if (run instanceof ITask) toAdd = new TaskWrapper<>((ITask) run);
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> utility(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for utilitiy
        final List<IAIRunnable> aiList = Lists.newArrayList();

        // combat tasks
        final AIStoreStuff ai = new AIStoreStuff(pokemob);
        // Store things in chests
        aiList.add(ai.setPriority(350));
        // Gather things from ground
        aiList.add(new AIGatherStuff(pokemob, 32, ai).setPriority(400));
        // Execute moves when told to
        aiList.add(new AIUseMove(pokemob).setPriority(250));

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        final IGuardAICapability guardCap = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final GuardAI guardai = new GuardAI(pokemob.getEntity(),guardCap);
        final Pair<Integer, GuardTask<?>> pair = Pair.of(0, new GuardTask<>(guardai));
        list.add(pair);
        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            else if (run instanceof ITask) toAdd = new TaskWrapper<>((ITask) run);
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
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
