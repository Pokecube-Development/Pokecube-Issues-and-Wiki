package pokecube.adventures.ai.tasks;

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
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.ai.tasks.battle.BaseBattleTask;
import pokecube.adventures.ai.tasks.battle.ChooseAttacks;
import pokecube.adventures.ai.tasks.battle.ManageOutMob;
import pokecube.adventures.ai.tasks.battle.ManagePokemobTarget;
import pokecube.adventures.ai.tasks.battle.agro.BaseAgroTask;
import pokecube.adventures.ai.tasks.battle.agro.DeAgro;
import pokecube.adventures.ai.tasks.battle.agro.Retaliate;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.npc.Activities;
import thut.api.entity.ai.BrainUtil;

import java.util.List;

public class Tasks
{
    public static final SensorType<DummySensor> DUMMY = new SensorType<>(DummySensor::new);

    public static void init()
    {
    }

    public static final List<SensorType<?>> REMOVE = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES);

    private static List<SensorType<?>> getSensors()
    {
        return List.of(SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS.get(),
                Sensors.INTERESTING_ENTITIES.get());
    }

    private static List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.ATTACKTARGET.get(), MemoryTypes.BATTLETARGET.get(),
                MemoryTypes.DE_AGRO_TIMER.get(), MemoryTypes.NO_SEEN_TARGET_TIMER.get());
    }

    public static BaseBattleTask DEAGRO = new DeAgro();
    public static BaseBattleTask CHOOSE_ATTACKS = new ChooseAttacks();
    public static BaseBattleTask MANAGE_SENT_POKEMOBS = new ManageOutMob();
    public static BaseBattleTask MANAGE_POKEMOB_TARGETS = new ManagePokemobTarget();
    public static BaseAgroTask RETALIATE = new Retaliate();

    @SuppressWarnings("deprecation")
    public static void addBattleTasks(final LivingEntity mob,
            final List<Pair<Integer, Behavior<? super LivingEntity>>> tasks)
    {
        final Brain<?> brain = mob.getBrain();
        var trainer = mob.getData(TrainerCaps.TRAINER);

        final List<SensorType<?>> senses = Lists.newArrayList(Tasks.DUMMY);
        for (final SensorType<?> type : Tasks.getSensors()) if (!brain.sensors.containsKey(type)) senses.add(type);

        BrainUtil.removeSensors(brain, Tasks.REMOVE);
        BrainUtil.addToBrain(brain, Tasks.getMemories(), senses);

        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> battle_list = Lists.newArrayList();
        final List<Pair<Integer, ? extends Behavior<? super LivingEntity>>> other_list = Lists.newArrayList();
        for (final Pair<Integer, ? extends Behavior<? super LivingEntity>> task_pair : tasks)
            if (task_pair.getSecond() instanceof BaseAgroTask agro)
            {
                trainer.addTargetWatcher(agro);
                other_list.add(task_pair);
            }
            else battle_list.add(task_pair);
        trainer.addTargetWatcher(RETALIATE);

        battle_list.add(Pair.of(1, DEAGRO));
        other_list.add(Pair.of(1, RETALIATE));
        battle_list.add(Pair.of(1, CHOOSE_ATTACKS));
        battle_list.add(Pair.of(1, MANAGE_SENT_POKEMOBS));
        battle_list.add(Pair.of(1, MANAGE_POKEMOB_TARGETS));

        brain.addActivityWithConditions(Activities.BATTLE.get(), ImmutableList.copyOf(battle_list),
                ImmutableSet.of(Pair.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT)));

        brain.getActiveActivities().forEach(a -> BrainUtil.addToActivity(brain, a, other_list));
    }
}
