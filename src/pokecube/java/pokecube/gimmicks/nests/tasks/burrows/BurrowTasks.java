package pokecube.gimmicks.nests.tasks.burrows;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.TaskAdders;
import pokecube.api.entity.CapabilityInhabitable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent.Init.Type;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.gimmicks.nests.tasks.burrows.burrow.BurrowHab;
import pokecube.gimmicks.nests.tasks.burrows.sensors.BurrowSensor;
import pokecube.gimmicks.nests.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.gimmicks.nests.tasks.burrows.tasks.CheckBurrow;
import pokecube.gimmicks.nests.tasks.burrows.tasks.DigBurrow;
import pokecube.gimmicks.nests.tasks.burrows.tasks.ReturnHome;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;
import thut.api.item.ItemList;

public class BurrowTasks
{
    public static ResourceLocation _BURROWS = ResourceLocation.fromNamespaceAndPath(PokecubeAPI.MODID, "burrowers");

    public static final Predicate<IPokemob> burrows = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isAnt = ItemList.is(BurrowTasks._BURROWS, entity);
        // Only care about bees
        if (!isAnt) return false;
        // Only process stock pokemobs
        return pokemob.getPokedexEntry().stock;
    };

    public static AIRoutine BURROWS = new AIRoutine("BURROWS", true, burrows);

    public static final Supplier<SensorType<BurrowSensor>> NEST_SENSOR;

    static
    {
        NEST_SENSOR = PokecubeCore.SENSORS.register("burrow", () -> new SensorType<>(BurrowSensor::new));
    }

    public static final ResourceLocation BURROWLOC = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID, "burrow");

    public static void init()
    {
        TaskAdders.register(Type.IDLE, BurrowTasks::addTasks);
        CapabilityInhabitable.Register(BurrowTasks.BURROWLOC, BurrowHab::new);
    }

    private static List<SensorType<?>> getSensors()
    {
        return List.of(BurrowTasks.NEST_SENSOR.get(), Sensors.VISIBLE_BLOCKS.get(), Sensors.INTERESTING_ENTITIES.get());
    }

    private static List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.NEST_POS.get(), MemoryModules.JOB_INFO.get(), MemoryModules.GOING_HOME.get(),
                MemoryModules.NO_NEST_TIMER.get());
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list,
            Map<String, IAIRunnable> namedTasks)
    {
        if (!PokecubeCore.getConfig().pokemobsMakeNests) return;
        if (!BurrowTasks.isValid(pokemob.getEntity())) return;

        list.add(new CheckBurrow(pokemob));
        list.add(new DigBurrow(pokemob));
        list.add(new ReturnHome(pokemob));

        BrainUtil.addToBrain(pokemob.getEntity().getBrain(), BurrowTasks.getMemories(), BurrowTasks.getSensors());
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(BurrowTasks.BURROWS);
    }

    public static boolean shouldBeInside(final ServerLevel world, final Burrow burrow)
    {

        return false;
    }
}
