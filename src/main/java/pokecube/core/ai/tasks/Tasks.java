package pokecube.core.ai.tasks;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.ShuffledTask;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
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
import pokecube.core.ai.tasks.misc.BlankTask;
import pokecube.core.ai.tasks.misc.FollowOwnerTask;
import pokecube.core.ai.tasks.misc.LookAtMob;
import pokecube.core.ai.tasks.misc.LookAtTask;
import pokecube.core.ai.tasks.misc.RunAway;
import pokecube.core.ai.tasks.misc.SwimTask;
import pokecube.core.ai.tasks.misc.WalkToTask;
import pokecube.core.ai.tasks.utility.GatherTask;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UseMoveTask;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.pokemob.InitAIEvent.Init;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.CapHolders;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;

public class Tasks
{
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModules.ATTACKTARGET,
            MemoryModules.HUNTTARGET, MemoryModules.HUNTED_BY, MemoryModules.MOVE_TARGET, MemoryModules.LEAP_TARGET,
            MemoryModules.PATH, MemoryModules.MATE_TARGET, MemoryModules.WALK_TARGET, MemoryModules.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModules.NOT_FOUND_PATH, MemoryModuleType.DOORS_TO_CLOSE);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static void initBrain(final Brain<?> brain)
    {
        BrainUtil.addToBrain(brain, Tasks.MEMORY_TYPES, Tasks.SENSOR_TYPES);
    }

    private static Map<Init.Type, List<ITaskAdder>> taskAdders = Maps.newConcurrentMap();

    static
    {
        for (final Init.Type type : Init.Type.values())
            Tasks.taskAdders.put(type, Lists.newArrayList());
    }

    public static void register(final Init.Type type, final ITaskAdder adder)
    {
        Tasks.taskAdders.get(type).add(adder);
    }

    public static List<ITaskAdder> getAdders(final Init.Type type)
    {
        return Tasks.taskAdders.get(type);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super LivingEntity>>> idle(final IPokemob pokemob,
            final float speed)
    {
        final PokedexEntry entry = pokemob.getPokedexEntry();
        // Tasks for idle
        final List<IAIRunnable> aiList = Lists.newArrayList();
        final Mob entity = pokemob.getEntity();
        Behavior<?> task;

        final IGuardAICapability guardCap = entity.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        if (entry.stock)
        {
            // Idle tasks
            // Guard your egg
            aiList.add(new GuardEggTask(pokemob).setPriority(3));
            // Mate with things
            if (pokemob instanceof IBreedingMob) aiList.add(new MateTask(pokemob).setPriority(2));
            // Eat things
            aiList.add(new HungerTask(pokemob).setPriority(1));
            // Wander around
            aiList.add(new IdleWalkTask(pokemob).setPriority(10));
        }
        // Owner related tasks
        if (!pokemob.getPokedexEntry().isStationary) // Follow owner around
            aiList.add(new FollowOwnerTask(pokemob, 3 + entity.getBbWidth() + pokemob.getPokedexEntry().length, 8
                    + entity.getBbWidth() + pokemob.getPokedexEntry().length));

        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> list = Lists.newArrayList();

        final GuardAI guardai = new GuardAI(pokemob.getEntity(), guardCap);
        guardai.shouldRun = () ->
        {
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) return true;
            return pokemob.getGeneralState(GeneralStates.STAYING);
        };

        final Pair<Integer, ? extends Behavior<? super LivingEntity>> pair = Pair.of(0, new GuardTask<>(entity, guardai));
        list.add(pair);

        if (entry.stock)
        {
            task = new LookAtTask(45, 90);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            task = new SwimTask(pokemob, 0.8F);
            list.add(Pair.of(0, (Behavior<? super LivingEntity>) task));
            list.add(Tasks.lookAtMany());
            list.add(Tasks.lookAtPlayerOrVillager());
        }
        // This one is outside as most things don't get this task.
        task = new WalkToTask(200);
        list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
        if (pokemob.isRoutineEnabled(AIRoutine.USEDOORS)) list.add(Pair.of(0, new InteractWithDoor()));

        // Send the event to let anyone edit the tasks if needed.
        PokecubeCore.POKEMOB_BUS.post(new Init(pokemob, Init.Type.IDLE, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super LivingEntity>>> combat(final IPokemob pokemob,
            final float speed)
    {
        // Tasks for combat
        final List<IAIRunnable> aiList = Lists.newArrayList();

        final PokedexEntry entry = pokemob.getPokedexEntry();

        if (entry.stock)
        {
            // combat tasks
            aiList.add(new SelectMoveTask(pokemob));
            // Attack stuff
            aiList.add(new UseAttacksTask(pokemob));
            // Dodge attacks
            aiList.add(new DodgeTask(pokemob));
            // Leap at things
            aiList.add(new LeapTask(pokemob));
            // Move around in combat
            aiList.add(new CicleTask(pokemob));
        }
        // Attack stuff
        aiList.add(new ForgetTargetTask(pokemob));
        // Call for help task
        aiList.add(new CallForHelpTask(pokemob, (float) PokecubeCore.getConfig().hordeRateFactor));

        // Look for targets to kill
        final FindTargetsTask targetFind = new FindTargetsTask(pokemob);
        aiList.add(targetFind);
        pokemob.setTargetFinder(targetFind);

        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> list = Lists.newArrayList();
        if (entry.stock)
        {
            Behavior<?> task = new LookAtTask(45, 90);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

            task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

            task = new SwimTask(pokemob, 0.8F);
            list.add(Pair.of(0, (Behavior<? super LivingEntity>) task));
        }
        if (pokemob.isRoutineEnabled(AIRoutine.USEDOORS)) list.add(Pair.of(0, new InteractWithDoor()));
        // Send the event to let anyone edit the tasks if needed.
        PokecubeCore.POKEMOB_BUS.post(new Init(pokemob, Init.Type.COMBAT, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends Behavior<? super LivingEntity>>> utility(final IPokemob pokemob,
            final float speed)
    {
        // Tasks for utilitiy
        final List<IAIRunnable> aiList = Lists.newArrayList();
        final PokedexEntry entry = pokemob.getPokedexEntry();
        Behavior<?> task;

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

        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> list = Lists.newArrayList();

        final IGuardAICapability guardCap = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final GuardAI guardai = new GuardAI(pokemob.getEntity(), guardCap);
        guardai.shouldRun = () ->
        {
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) return true;
            return pokemob.getGeneralState(GeneralStates.STAYING);
        };
        final Pair<Integer, ? extends Behavior<? super LivingEntity>> pair = Pair.of(0, new GuardTask<>(pokemob.getEntity(),
                guardai));
        list.add(pair);
        if (entry.stock)
        {
            task = new LookAtTask(45, 90);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            task = new RunAway(MemoryModules.HUNTED_BY, 1.5f);
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            task = new SwimTask(pokemob, 0.8F);
            list.add(Pair.of(0, (Behavior<? super LivingEntity>) task));
        }
        // This one is outside as most things don't get this task.
        task = new WalkToTask(200);
        list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
        if (pokemob.isRoutineEnabled(AIRoutine.USEDOORS)) list.add(Pair.of(0, new InteractWithDoor()));
        // Send the event to let anyone edit the tasks if needed.
        PokecubeCore.POKEMOB_BUS.post(new Init(pokemob, Init.Type.UTILITY, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    //@formatter:off
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
