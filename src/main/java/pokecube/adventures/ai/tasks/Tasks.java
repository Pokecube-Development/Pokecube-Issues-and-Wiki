package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.DummySensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.battle.ChooseAttacks;
import pokecube.adventures.ai.tasks.battle.ManageOutMob;
import pokecube.adventures.ai.tasks.battle.ManagePokemobTarget;
import pokecube.adventures.ai.tasks.battle.agro.BaseAgroTask;
import pokecube.adventures.ai.tasks.battle.agro.DeAgro;
import pokecube.adventures.ai.tasks.battle.agro.Retaliate;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;

public class Tasks
{
    public static final SensorType<DummySensor> DUMMY = new SensorType<>(DummySensor::new);

    public static void init()
    {
        Tasks.DUMMY.setRegistryName(PokecubeAdv.MODID, "dummy_sensor");
    }

    public static final List<SensorType<?>> REMOVE = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModules.ATTACKTARGET,
            MemoryTypes.BATTLETARGET);

    @SuppressWarnings("unchecked")
    public static void addBattleTasks(final LivingEntity mob,
            final List<Pair<Integer, Task<? super LivingEntity>>> tasks)
    {
        final Brain<?> brain = mob.getBrain();

        final List<SensorType<?>> senses = Lists.newArrayList(Tasks.DUMMY);
        for (final SensorType<?> type : Tasks.SENSOR_TYPES)
            if (!brain.sensors.containsKey(type)) senses.add(type);

        BrainUtils.removeSensors(brain, Tasks.REMOVE);
        BrainUtils.addToBrain(brain, Tasks.MEMORY_TYPES, senses);

        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> battle_list = Lists.newArrayList();
        final List<Pair<Integer, ? extends Task<? super LivingEntity>>> other_list = Lists.newArrayList();
        for (final Pair<Integer, ? extends Task<? super LivingEntity>> task_pair : tasks)
            if (task_pair.getSecond() instanceof BaseAgroTask) other_list.add(task_pair);
            else battle_list.add(task_pair);

        Task<?> task = new DeAgro(mob);
        battle_list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new Retaliate(mob);
        other_list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        task = new ChooseAttacks(mob);
        battle_list.add(Pair.of(1, (Task<? super LivingEntity>) task));
        task = new ManageOutMob(mob);
        battle_list.add(Pair.of(1, (Task<? super LivingEntity>) task));
        task = new ManagePokemobTarget(mob);
        battle_list.add(Pair.of(1, (Task<? super LivingEntity>) task));

        brain.registerActivity(Activities.BATTLE, ImmutableList.copyOf(battle_list), ImmutableSet.of(Pair.of(
                MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT)));

        BrainUtils.addToActivity(brain, Activity.CORE, other_list);
        BrainUtils.addToActivity(brain, Activity.IDLE, other_list);
        BrainUtils.addToActivity(brain, Activities.STATIONARY, other_list);

    }
}
