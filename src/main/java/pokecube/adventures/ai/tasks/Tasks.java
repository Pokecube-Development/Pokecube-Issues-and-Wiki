package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.DummySensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.battle.ChooseAttacks;
import pokecube.adventures.ai.tasks.battle.ManageOutMob;
import pokecube.adventures.ai.tasks.battle.ManagePokemobTarget;
import pokecube.adventures.ai.tasks.battle.agro.BaseAgroTask;
import pokecube.adventures.ai.tasks.battle.agro.DeAgro;
import pokecube.adventures.ai.tasks.battle.agro.Retaliate;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import thut.api.entity.ai.BrainUtil;

public class Tasks
{
    public static final SensorType<DummySensor> DUMMY = new SensorType<>(DummySensor::new);

    public static void init()
    {
        Tasks.DUMMY.setRegistryName(PokecubeAdv.MODID, "dummy_sensor");
    }

    public static final List<SensorType<?>> REMOVE = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES);

    private static final List<SensorType<?>> getSensors()
    {
        return List.of(SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS.get(),
                Sensors.INTERESTING_ENTITIES.get());
    }

    private static final List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.ATTACKTARGET.get(), MemoryTypes.BATTLETARGET.get());
    }

    @SuppressWarnings("unchecked")
    public static void addBattleTasks(final LivingEntity mob,
            final List<Pair<Integer, Behavior<? super LivingEntity>>> tasks)
    {
        final Brain<?> brain = mob.getBrain();

        final List<SensorType<?>> senses = Lists.newArrayList(Tasks.DUMMY);
        for (final SensorType<?> type : Tasks.getSensors()) if (!brain.sensors.containsKey(type)) senses.add(type);

        BrainUtil.removeSensors(brain, Tasks.REMOVE);
        BrainUtil.addToBrain(brain, Tasks.getMemories(), senses);

        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> battle_list = Lists.newArrayList();
        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> other_list = Lists.newArrayList();
        for (final Pair<Integer, ? extends Behavior<? super LivingEntity>> task_pair : tasks)
            if (task_pair.getSecond() instanceof BaseAgroTask) other_list.add(task_pair);
            else battle_list.add(task_pair);

        Behavior<?> task = new DeAgro(mob);
        battle_list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

        task = new Retaliate(mob);
        other_list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

        task = new ChooseAttacks(mob);
        battle_list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
        task = new ManageOutMob(mob);
        battle_list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
        task = new ManagePokemobTarget(mob);
        battle_list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

        brain.addActivityWithConditions(Activities.BATTLE.get(), ImmutableList.copyOf(battle_list),
                ImmutableSet.of(Pair.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT)));

        brain.activeActivities.forEach(a -> {
            BrainUtil.addToActivity(brain, a, other_list);
        });
    }
}
