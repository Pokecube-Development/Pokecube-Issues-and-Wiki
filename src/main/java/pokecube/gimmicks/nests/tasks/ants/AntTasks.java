package pokecube.gimmicks.nests.tasks.ants;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.IInhabitor;
import pokecube.api.ai.TaskAdders;
import pokecube.api.entity.CapabilityInhabitable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent.Init.Type;
import pokecube.api.raids.RaidManager;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.gimmicks.nests.tasks.ants.nest.AntHabitat;
import pokecube.gimmicks.nests.tasks.ants.sensors.EggSensor;
import pokecube.gimmicks.nests.tasks.ants.sensors.GatherSensor;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor;
import pokecube.gimmicks.nests.tasks.ants.sensors.ThreatSensor;
import pokecube.gimmicks.nests.tasks.ants.tasks.nest.CheckNest;
import pokecube.gimmicks.nests.tasks.ants.tasks.nest.EnterNest;
import pokecube.gimmicks.nests.tasks.ants.tasks.nest.MakeNest;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.Build;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.CarryEgg;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.Dig;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.Gather;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.Guard;
import pokecube.gimmicks.nests.tasks.ants.tasks.work.Idle;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;
import thut.api.item.ItemList;

public class AntTasks
{
    public static ResourceLocation ANTS = new ResourceLocation(PokecubeAPI.MODID, "ants");

    public static final Predicate<IPokemob> isAnt = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isAnt = ItemList.is(AntTasks.ANTS, entity);
        // Only care about bees
        if (!isAnt) return false;
        // Only process stock pokemobs
        if (!pokemob.getPokedexEntry().stock) return false;
        return true;
    };

    public static AIRoutine ANTAI = AIRoutine.create("ANTAI", true, isAnt);

    public static enum AntJob
    {
        NONE, DIG, BUILD, GUARD, GATHER, FARM;
    }

    public static enum AntRoom
    {
        EGG, FOOD, NODE, ENTRANCE;
    }

    public static final RegistryObject<SensorType<NestSensor>> NEST_SENSOR;
    public static final RegistryObject<SensorType<GatherSensor>> WORK_SENSOR;
    public static final RegistryObject<SensorType<ThreatSensor>> THREAT_SENSOR;
    public static final RegistryObject<SensorType<EggSensor>> EGG_SENSOR;

    static
    {
        // Sensors
        NEST_SENSOR = PokecubeCore.SENSORS.register("ant_nests", () -> new SensorType<>(NestSensor::new));
        WORK_SENSOR = PokecubeCore.SENSORS.register("ant_work", () -> new SensorType<>(GatherSensor::new));
        THREAT_SENSOR = PokecubeCore.SENSORS.register("ant_threat", () -> new SensorType<>(ThreatSensor::new));
        EGG_SENSOR = PokecubeCore.SENSORS.register("ant_eggs", () -> new SensorType<>(EggSensor::new));
    }

    public static final ResourceLocation NESTLOC = new ResourceLocation(PokecubeCore.MODID, "ant_nest");

    public static void init()
    {
        CapabilityInhabitable.Register(AntTasks.NESTLOC, () -> new AntHabitat());
        TaskAdders.register(Type.IDLE, AntTasks::addTasks);
        
        RaidManager.BANNEDAI.add(ANTAI);
    }

    private static final List<SensorType<?>> getSensors()
    {
        return List.of(AntTasks.NEST_SENSOR.get(), AntTasks.WORK_SENSOR.get(), AntTasks.EGG_SENSOR.get(),
                AntTasks.THREAT_SENSOR.get(), Sensors.VISIBLE_BLOCKS.get(), Sensors.INTERESTING_ENTITIES.get());
    }

    private static final List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.NEST_POS.get(), MemoryModules.WORK_POS.get(),
                MemoryModules.OUT_OF_NEST_TIMER.get(), MemoryModules.NO_WORK_TIMER.get(),
                MemoryModules.NO_NEST_TIMER.get(), MemoryModules.JOB_TYPE.get(), MemoryModules.JOB_INFO.get(),
                MemoryModules.EGG.get(), MemoryModules.GOING_HOME.get());
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list,
            Map<String, IAIRunnable> namedTasks)
    {
        if (!PokecubeCore.getConfig().pokemobsMakeNests) return;
        if (!AntTasks.ANTAI.isAllowed(pokemob)) return;

        list.add(new CheckNest(pokemob).setPriority(200));
        list.add(new MakeNest(pokemob));
        list.add(new EnterNest(pokemob).setPriority(0));
        list.add(new CarryEgg(pokemob).setPriority(0));
        list.add(new Guard(pokemob).setPriority(1));
        list.add(new Gather(pokemob).setPriority(2));
        list.add(new Build(pokemob).setPriority(2));
        list.add(new Dig(pokemob).setPriority(3));
        list.add(new Idle(pokemob).setPriority(4));

        BrainUtil.addToBrain(pokemob.getEntity().getBrain(), AntTasks.getMemories(), AntTasks.getSensors());
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AntTasks.ANTAI);
    }

    public static AntJob getJob(final Mob ant)
    {
        int index = 0;
        if (ant.getBrain().hasMemoryValue(MemoryModules.JOB_TYPE.get()))
            index = ant.getBrain().getMemory(MemoryModules.JOB_TYPE.get()).get();
        final AntJob job = AntJob.values()[index];
        return job;
    }

    public static void setJob(final Mob ant, final AntJob job)
    {
        ant.getBrain().setMemory(MemoryModules.JOB_TYPE.get(), job.ordinal());
    }

    public static boolean shouldAntBeInNest(final ServerLevel world, final BlockPos pos)
    {
        return !world.isDay() || world.isRainingAt(pos);
    }

    public static class AntInhabitor implements IInhabitor
    {
        final Mob ant;

        public AntJob job = AntJob.NONE;

        public AntInhabitor(final Mob ant)
        {
            this.ant = ant;
        }

        @Override
        public GlobalPos getHome()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemoryValue(MemoryModules.NEST_POS.get())) return null;
            return brain.getMemory(MemoryModules.NEST_POS.get()).get();
        }

        @Override
        public void onExitHabitat()
        {

        }

        @Override
        public GlobalPos getWorkSite()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemoryValue(MemoryModules.WORK_POS.get())) return null;
            return brain.getMemory(MemoryModules.WORK_POS.get()).get();
        }

        @Override
        public void setWorkSite(final GlobalPos site)
        {
            final Brain<?> brain = this.ant.getBrain();
            if (site == null) brain.eraseMemory(MemoryModules.WORK_POS.get());
            else brain.setMemory(MemoryModules.WORK_POS.get(), site);
        }
    }

}
