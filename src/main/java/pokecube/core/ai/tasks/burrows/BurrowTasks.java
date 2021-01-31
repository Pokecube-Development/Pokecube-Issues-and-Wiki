package pokecube.core.ai.tasks.burrows;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.Tasks;
import pokecube.core.ai.tasks.burrows.burrow.BurrowHab;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.core.ai.tasks.burrows.tasks.CheckBurrow;
import pokecube.core.ai.tasks.burrows.tasks.DigBurrow;
import pokecube.core.ai.tasks.burrows.tasks.ReturnHome;
import pokecube.core.events.pokemob.InitAIEvent.Init.Type;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.ai.IAIRunnable;

public class BurrowTasks
{
    public static final MemoryModuleType<GlobalPos> BURROW        = MemoryModules.NEST_POS;
    public static final MemoryModuleType<Boolean>   GOING_HOME    = MemoryModules.GOING_HOME;
    public static final MemoryModuleType<Integer>   NO_HOME_TIMER = MemoryModules.NO_NEST_TIMER;

    public static final MemoryModuleType<CompoundNBT> JOB_INFO = MemoryModules.JOB_INFO;

    public static final SensorType<BurrowSensor> NEST_SENSOR = new SensorType<>(BurrowSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(BurrowTasks.BURROW,
            BurrowTasks.GOING_HOME, BurrowTasks.NO_HOME_TIMER);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(BurrowTasks.NEST_SENSOR,
            Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ResourceLocation BURROWLOC = new ResourceLocation(PokecubeCore.MODID, "burrow");

    public static void registerMems(final Register<MemoryModuleType<?>> event)
    {
        Tasks.register(Type.IDLE, BurrowTasks::addTasks);
        CapabilityInhabitable.Register(BurrowTasks.BURROWLOC, () -> new BurrowHab());
    }

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(BurrowTasks.NEST_SENSOR.setRegistryName(PokecubeCore.MODID, "burrow"));
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list)
    {
        if (!PokecubeCore.getConfig().pokemobsMakeNests) return;
        if (!BurrowTasks.isValid(pokemob.getEntity())) return;

        list.add(new CheckBurrow(pokemob));
        list.add(new DigBurrow(pokemob));
        list.add(new ReturnHome(pokemob));

        BrainUtils.addToBrain(pokemob.getEntity().getBrain(), BurrowTasks.MEMORY_TYPES, BurrowTasks.SENSOR_TYPES);
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.BURROWS);
    }

    public static boolean shouldBeInside(final ServerWorld world, final Burrow burrow)
    {

        return false;
    }
}
