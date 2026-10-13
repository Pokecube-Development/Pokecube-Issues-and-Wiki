package pokecube.gimmicks.nests.tasks.birds;

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
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.tasks.birds.sensors.NestSensor;
import pokecube.gimmicks.nests.tasks.birds.tasks.CheckNest;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import pokecube.gimmicks.nests.tasks.burrows.burrow.BurrowHab;
import pokecube.gimmicks.nests.tasks.burrows.sensors.BurrowSensor;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;
import thut.api.item.ItemList;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BirdTasks
{
    public static ResourceLocation _BIRD_NESTS = ResourceLocation.fromNamespaceAndPath(PokecubeAPI.MODID, "nest_birds");

    public static final Predicate<IPokemob> nest_birds = pokemob -> {
        final Mob entity = pokemob.getEntity();
        // Only care about ones in our tag
        if (!ItemList.is(BirdTasks._BIRD_NESTS, entity)) return false;
        // Only process stock pokemobs
        return pokemob.getPokedexEntry().stock;
    };

    public static AIRoutine BIRD_NESTS = new AIRoutine("BIRD_NESTS", true, nest_birds);

    public static final Supplier<SensorType<NestSensor>> NEST_SENSOR;

    static
    {
        NEST_SENSOR = PokecubeCore.SENSORS.register("bird_nest", () -> new SensorType<>(NestSensor::new));
    }

    public static final ResourceLocation NESTLOC = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "bird_nest");

    public static void init()
    {
        TaskAdders.register(InitAIEvent.Init.Type.IDLE, BirdTasks::addTasks);
        CapabilityInhabitable.Register(NESTLOC, BurrowHab::new);
    }

    private static List<SensorType<?>> getSensors()
    {
        return List.of(NEST_SENSOR.get(), Sensors.VISIBLE_BLOCKS.get(), Sensors.INTERESTING_ENTITIES.get());
    }

    private static List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.NEST_POS.get(), MemoryModules.JOB_INFO.get(), MemoryModules.GOING_HOME.get(),
                MemoryModules.NO_NEST_TIMER.get());
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list,
            Map<String, IAIRunnable> namedTasks)
    {
        if (!NestTasks.config.pokemobsMakeNests) return;
        if (!isValid(pokemob.getEntity())) return;

        list.add(new CheckNest());

        BrainUtil.addToBrain(pokemob.getEntity().getBrain(), getMemories(), getSensors());
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(BurrowTasks.BURROWS);
    }

    public static boolean shouldBeInside(final ServerLevel world, final BurrowSensor.Burrow burrow)
    {
        return false;
    }
}
