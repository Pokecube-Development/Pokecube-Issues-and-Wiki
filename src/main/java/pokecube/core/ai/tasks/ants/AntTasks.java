package pokecube.core.ai.tasks.ants;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.Tasks;
import pokecube.core.ai.tasks.ants.nest.AntHabitat;
import pokecube.core.ai.tasks.ants.sensors.EggSensor;
import pokecube.core.ai.tasks.ants.sensors.GatherSensor;
import pokecube.core.ai.tasks.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.ants.sensors.ThreatSensor;
import pokecube.core.ai.tasks.ants.tasks.nest.CheckNest;
import pokecube.core.ai.tasks.ants.tasks.nest.EnterNest;
import pokecube.core.ai.tasks.ants.tasks.nest.MakeNest;
import pokecube.core.ai.tasks.ants.tasks.work.Build;
import pokecube.core.ai.tasks.ants.tasks.work.CarryEgg;
import pokecube.core.ai.tasks.ants.tasks.work.Dig;
import pokecube.core.ai.tasks.ants.tasks.work.Gather;
import pokecube.core.ai.tasks.ants.tasks.work.Guard;
import pokecube.core.ai.tasks.ants.tasks.work.Idle;
import pokecube.core.events.pokemob.InitAIEvent.Init.Type;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.ai.IAIRunnable;

public class AntTasks
{
    public static enum AntJob
    {
        NONE, DIG, BUILD, GUARD, GATHER, FARM;
    }

    public static enum AntRoom
    {
        EGG, FOOD, NODE, ENTRANCE;
    }

    public static final MemoryModuleType<GlobalPos> NEST_POS = MemoryModules.NEST_POS;
    public static final MemoryModuleType<GlobalPos> WORK_POS = MemoryModules.WORK_POS;

    public static final MemoryModuleType<Integer> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_HIVE_TIMER     = MemoryModules.NO_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_WORK_TIME      = MemoryModules.NO_WORK_TIMER;

    public static final MemoryModuleType<EntityPokemobEgg> EGG = MemoryModules.EGG;

    public static final MemoryModuleType<Integer> JOB_TYPE = MemoryModules.JOB_TYPE;

    public static final MemoryModuleType<CompoundNBT> JOB_INFO = MemoryModules.JOB_INFO;

    public static final MemoryModuleType<Boolean> GOING_HOME = MemoryModules.GOING_HOME;

    public static final SensorType<NestSensor>   NEST_SENSOR   = new SensorType<>(NestSensor::new);
    public static final SensorType<GatherSensor> WORK_SENSOR   = new SensorType<>(GatherSensor::new);
    public static final SensorType<ThreatSensor> THREAT_SENSOR = new SensorType<>(ThreatSensor::new);
    public static final SensorType<EggSensor>    EGG_SENSOR    = new SensorType<>(EggSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(AntTasks.NEST_POS,
            AntTasks.WORK_POS, AntTasks.OUT_OF_HIVE_TIMER, AntTasks.NO_WORK_TIME, AntTasks.NO_HIVE_TIMER,
            AntTasks.JOB_TYPE, AntTasks.JOB_INFO, AntTasks.EGG, AntTasks.GOING_HOME);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(AntTasks.NEST_SENSOR, AntTasks.WORK_SENSOR,
            AntTasks.EGG_SENSOR, AntTasks.THREAT_SENSOR, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ResourceLocation NESTLOC = new ResourceLocation(PokecubeCore.MODID, "ant_nest");

    public static void registerMems(final Register<MemoryModuleType<?>> event)
    {
        Tasks.register(Type.IDLE, AntTasks::addTasks);
        CapabilityInhabitable.Register(AntTasks.NESTLOC, () -> new AntHabitat());
    }

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(AntTasks.NEST_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_nests"));
        event.getRegistry().register(AntTasks.WORK_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_work"));
        event.getRegistry().register(AntTasks.THREAT_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_threat"));
        event.getRegistry().register(AntTasks.EGG_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_eggs"));
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list)
    {
        if (!PokecubeCore.getConfig().pokemobsMakeNests) return;
        if (!AIRoutine.ANTAI.isAllowed(pokemob)) return;

        list.add(new CheckNest(pokemob).setPriority(200));
        list.add(new MakeNest(pokemob));
        list.add(new EnterNest(pokemob).setPriority(0));
        list.add(new CarryEgg(pokemob).setPriority(0));
        list.add(new Guard(pokemob).setPriority(1));
        list.add(new Gather(pokemob).setPriority(2));
        list.add(new Build(pokemob).setPriority(2));
        list.add(new Dig(pokemob).setPriority(3));
        list.add(new Idle(pokemob).setPriority(4));

        BrainUtils.addToBrain(pokemob.getEntity().getBrain(), AntTasks.MEMORY_TYPES, AntTasks.SENSOR_TYPES);
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.ANTAI);
    }

    public static AntJob getJob(final MobEntity ant)
    {
        int index = 0;
        if (ant.getBrain().hasMemory(AntTasks.JOB_TYPE)) index = ant.getBrain().getMemory(AntTasks.JOB_TYPE).get();
        final AntJob job = AntJob.values()[index];
        return job;
    }

    public static void setJob(final MobEntity ant, final AntJob job)
    {
        ant.getBrain().setMemory(AntTasks.JOB_TYPE, job.ordinal());
    }

    public static boolean shouldAntBeInNest(final ServerWorld world, final BlockPos pos)
    {
        return !world.isDaytime() || world.isRainingAt(pos);
    }

    public static class AntInhabitor implements IInhabitor
    {
        final MobEntity ant;

        public AntJob job = AntJob.NONE;

        public AntInhabitor(final MobEntity ant)
        {
            this.ant = ant;
        }

        @Override
        public GlobalPos getHome()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemory(AntTasks.NEST_POS)) return null;
            return brain.getMemory(AntTasks.NEST_POS).get();
        }

        @Override
        public void onExitHabitat()
        {

        }

        @Override
        public GlobalPos getWorkSite()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemory(AntTasks.WORK_POS)) return null;
            return brain.getMemory(AntTasks.WORK_POS).get();
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
            final Brain<?> brain = this.ant.getBrain();
            if (site == null) brain.removeMemory(AntTasks.WORK_POS);
            else brain.setMemory(AntTasks.WORK_POS, site);
        }
    }

}
