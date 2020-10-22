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
import net.minecraft.entity.ai.brain.task.Task;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.ShuffledTask;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.WalkToTask;
import pokecube.core.ai.tasks.combat.attacks.SelectMoveTask;
import pokecube.core.ai.tasks.combat.attacks.UseAttacksTask;
import pokecube.core.ai.tasks.combat.management.CallForHelpTask;
import pokecube.core.ai.tasks.combat.management.FindTargetsTask;
import pokecube.core.ai.tasks.combat.management.ForgetTargetTask;
import pokecube.core.ai.tasks.combat.movement.CicleTask;
import pokecube.core.ai.tasks.combat.movement.DodgeTask;
import pokecube.core.ai.tasks.combat.movement.LeapTask;
import pokecube.core.ai.tasks.idle.ForgetHuntedByTask;
import pokecube.core.ai.tasks.idle.GuardEggTask;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.ai.tasks.idle.IdleWalkTask;
import pokecube.core.ai.tasks.idle.MateTask;
import pokecube.core.ai.tasks.utility.GatherTask;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UseMoveTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.CapHolders;
import thut.api.entity.ai.IAIRunnable;

public class Tasks
{
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModules.ATTACKTARGET,
            MemoryModules.HUNTTARGET, MemoryModules.HUNTED_BY, MemoryModules.MOVE_TARGET, MemoryModules.PATH,
            MemoryModules.MATE_TARGET, MemoryModules.WALK_TARGET, MemoryModules.LOOK_TARGET,
            MemoryModules.NOT_FOUND_PATH);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS,
             SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static void initBrain(final Brain<?> brain)
    {
        BrainUtils.addToBrain(brain, Tasks.MEMORY_TYPES, Tasks.SENSOR_TYPES);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> idle(final IPokemob pokemob,
            final float speed)
    {
        // Tasks for idle
        final List<IAIRunnable> aiList = Lists.newArrayList();
        final MobEntity entity = pokemob.getEntity();

        final IGuardAICapability guardCap = entity.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        // Idle tasks
        // Guard your egg
        aiList.add(new GuardEggTask(pokemob));
        // Mate with things
        aiList.add(new MateTask(pokemob));
        // Eat things
        aiList.add(new HungerTask(pokemob));
        // Wander around
        aiList.add(new IdleWalkTask(pokemob));

        // Owner related tasks
        if (!pokemob.getPokedexEntry().isStationary) // Follow owner around
            aiList.add(new FollowOwnerTask(pokemob, 3 + entity.getWidth() + pokemob.getPokedexEntry().length, 8 + entity
                    .getWidth() + pokemob.getPokedexEntry().length));

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        final GuardAI guardai = new GuardAI(pokemob.getEntity(), guardCap);
        guardai.shouldRun = () ->
        {
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) return true;
            return pokemob.getGeneralState(GeneralStates.STAYING);
        };

        final Pair<Integer, ? extends Task<? super LivingEntity>> pair = Pair.of(0, new GuardTask<>(entity, guardai));
        list.add(pair);

        Task<?> task = new LookAtTask(45, 90);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new WalkToTask(200);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new SwimTask(pokemob, 0.4F, 0.8F);
        list.add(Pair.of(0, (Task<? super LivingEntity>) task));

        list.add(Tasks.lookAtMany());
        list.add(Tasks.lookAtPlayerOrVillager());

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> combat(final IPokemob pokemob,
            final float speed)
    {
        // Tasks for combat
        final List<IAIRunnable> aiList = Lists.newArrayList();

        // combat tasks
        aiList.add(new SelectMoveTask(pokemob));
        // Attack stuff
        aiList.add(new UseAttacksTask(pokemob));
        // Attack stuff
        aiList.add(new ForgetTargetTask(pokemob));
        // Dodge attacks
        aiList.add(new DodgeTask(pokemob));
        // Leap at things
        aiList.add(new LeapTask(pokemob));
        // Move around in combat
        aiList.add(new CicleTask(pokemob));
        // Call for help task
        aiList.add(new CallForHelpTask(pokemob, (float) PokecubeCore.getConfig().hordeRateFactor));

        // Look for targets to kill
        final FindTargetsTask targetFind = new FindTargetsTask(pokemob);
        aiList.add(targetFind);
        pokemob.setTargetFinder(targetFind);

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        Task<?> task = new LookAtTask(45, 90);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new SwimTask(pokemob, 0.4F, 0.8F);
        list.add(Pair.of(0, (Task<? super LivingEntity>) task));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Task<? super LivingEntity>>> utility(final IPokemob pokemob,
            final float speed)
    {
        // Tasks for utilitiy
        final List<IAIRunnable> aiList = Lists.newArrayList();

        // combat tasks
        final StoreTask ai = new StoreTask(pokemob);
        // Store things in chests
        aiList.add(ai);
        // Gather things from ground
        aiList.add(new GatherTask(pokemob, 32, ai));
        // Execute moves when told to
        aiList.add(new UseMoveTask(pokemob));
        // forget we were being hunted
        aiList.add(new ForgetHuntedByTask(pokemob, 100));

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> list = Lists.newArrayList();

        final IGuardAICapability guardCap = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final GuardAI guardai = new GuardAI(pokemob.getEntity(), guardCap);
        guardai.shouldRun = () ->
        {
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) return true;
            return pokemob.getGeneralState(GeneralStates.STAYING);
        };
        final Pair<Integer, ? extends Task<? super LivingEntity>> pair = Pair.of(0, new GuardTask<>(pokemob.getEntity(),
                guardai));
        list.add(pair);

        Task<?> task = new LookAtTask(45, 90);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new WalkToTask(200);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
        list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new SwimTask(pokemob, 0.4F, 0.8F);
        list.add(Pair.of(0, (Task<? super LivingEntity>) task));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Task<LivingEntity> toAdd = null;
            if (run instanceof Task<?>) toAdd = (Task<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    //@formatter:off
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
