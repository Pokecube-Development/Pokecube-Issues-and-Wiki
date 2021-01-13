package pokecube.core.ai.tasks.idle.bees;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.idle.bees.sensors.FlowerSensor;
import pokecube.core.ai.tasks.idle.bees.sensors.HiveSensor;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.ai.IAIRunnable;

public class BeeTasks
{
    public static final MemoryModuleType<GlobalPos> HIVE_POS   = MemoryModules.NEST_POS;
    public static final MemoryModuleType<GlobalPos> FLOWER_POS = MemoryModules.WORK_POS;

    public static final MemoryModuleType<Integer> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_HIVE_TIMER     = MemoryModules.NO_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_FLOWER_TIME    = MemoryModules.NO_WORK_TIMER;

    public static final MemoryModuleType<Boolean> HAS_NECTAR = new MemoryModuleType<>(Optional.of(Codec.BOOL));

    public static final SensorType<HiveSensor>   HIVE_SENSOR   = new SensorType<>(HiveSensor::new);
    public static final SensorType<FlowerSensor> FLOWER_SENSOR = new SensorType<>(FlowerSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(BeeTasks.HIVE_POS,
            BeeTasks.FLOWER_POS, BeeTasks.OUT_OF_HIVE_TIMER, BeeTasks.NO_FLOWER_TIME, BeeTasks.HAS_NECTAR,
            BeeTasks.NO_HIVE_TIMER);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(BeeTasks.HIVE_SENSOR,
            BeeTasks.FLOWER_SENSOR, Sensors.VISIBLE_BLOCKS);

    public static void registerMems(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(BeeTasks.HAS_NECTAR.setRegistryName(PokecubeCore.MODID, "bee_has_nectar"));

        BeeEventsHandler.init();
    }

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(BeeTasks.HIVE_SENSOR.setRegistryName(PokecubeCore.MODID, "bee_hives"));
        event.getRegistry().register(BeeTasks.FLOWER_SENSOR.setRegistryName(PokecubeCore.MODID, "bee_flowers"));
    }

    public static void addBeeIdleTasks(final IPokemob pokemob, final List<IAIRunnable> list)
    {
        if (!EntityTypeTags.BEEHIVE_INHABITORS.contains(pokemob.getEntity().getType())) return;
        // Gather Nectar from found flower
        list.add(new GatherNectar(pokemob));
        // Return to hive with the Nectar from the flower
        list.add(new EnterHive(pokemob));
        // Locate and update our current hive, run this one last, as it clears
        // hive status
        list.add(new CheckHive(pokemob).setPriority(200));
        // Try to make a hive if we don't have one for too long
        list.add(new MakeHive(pokemob));

        BrainUtils.addToBrain(pokemob.getEntity().getBrain(), BeeTasks.MEMORY_TYPES, BeeTasks.SENSOR_TYPES);
    }

    public static boolean isValidBee(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.BEEAI);
    }
}
