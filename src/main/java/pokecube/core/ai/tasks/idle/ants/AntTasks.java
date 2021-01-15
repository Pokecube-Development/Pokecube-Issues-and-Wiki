package pokecube.core.ai.tasks.idle.ants;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.idle.ants.sensors.GatherSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.ThreatSensor;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.ai.IAIRunnable;

public class AntTasks
{
    public static final MemoryModuleType<GlobalPos> NEST_POS = MemoryModules.NEST_POS;
    public static final MemoryModuleType<GlobalPos> WORK_POS = MemoryModules.WORK_POS;

    public static final MemoryModuleType<Integer> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_HIVE_TIMER     = MemoryModules.NO_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_WORK_TIME      = MemoryModules.NO_WORK_TIMER;

    public static final SensorType<NestSensor>   NEST_SENSOR   = new SensorType<>(NestSensor::new);
    public static final SensorType<GatherSensor> WORK_SENSOR   = new SensorType<>(GatherSensor::new);
    public static final SensorType<ThreatSensor> THREAT_SENSOR = new SensorType<>(ThreatSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(AntTasks.NEST_POS,
            AntTasks.WORK_POS, AntTasks.OUT_OF_HIVE_TIMER, AntTasks.NO_WORK_TIME, AntTasks.NO_HIVE_TIMER);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(AntTasks.NEST_SENSOR, AntTasks.WORK_SENSOR,
            AntTasks.THREAT_SENSOR, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ResourceLocation NESTLOC = new ResourceLocation(PokecubeCore.MODID, "ant_nest");

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(AntTasks.NEST_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_nests"));
        event.getRegistry().register(AntTasks.WORK_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_work"));
        event.getRegistry().register(AntTasks.THREAT_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_threat"));

        CapabilityInhabitable.Register(AntTasks.NESTLOC, () -> new AntHabitat());
    }

    public static void addAntIdleTasks(final IPokemob pokemob, final List<IAIRunnable> list)
    {
        if (!AIRoutine.ANTAI.isAllowed(pokemob)) return;

        list.add(new CheckNest(pokemob).setPriority(200));
        list.add(new MakeNest(pokemob));
        list.add(new EnterNest(pokemob));
        list.add(new Harvest(pokemob));
        list.add(new Patrol(pokemob));

        BrainUtils.addToBrain(pokemob.getEntity().getBrain(), AntTasks.MEMORY_TYPES, AntTasks.SENSOR_TYPES);
    }

    public static boolean isValidAnt(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.ANTAI);
    }

    public static class AntInhabitor implements IInhabitor
    {
        final MobEntity ant;

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

    public static class AntHabitat implements IInhabitable, INBTSerializable<CompoundNBT>
    {

        final List<Ant> ants = Lists.newArrayList();

        public AntHabitat()
        {
        }

        @Override
        public void onTick(final ServerWorld world)
        {
            // Here we need to release ants every so often as needed
            this.ants.removeIf(ant -> this.tryReleaseAnt(ant, world));
        }

        private boolean tryReleaseAnt(final Ant ant, final ServerWorld world)
        {
            // Tick this anyway, all will leave at daybreak!
            ant.ticksInHive++;
            if (!world.isDaytime()) return false;
            final boolean release = ant.ticksInHive > ant.minOccupationTicks;
            if (release)
            {
                final CompoundNBT tag = ant.entityData;
                final Entity entity = EntityType.loadEntityAndExecute(tag, world, (mob) ->
                {
                    // Here we should do things like heal the ant,
                    // maybe update the inventory of the nest/ant
                    // etc

                    return mob;
                });
                if (entity != null) return world.addEntity(entity);
            }
            return false;
        }

        @Override
        public void onExitHabitat(final MobEntity mob)
        {

        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            if (!this.canEnterHabitat(mob)) return false;
            mob.stopRiding();
            mob.removePassengers();
            final CompoundNBT tag = new CompoundNBT();
            mob.writeUnlessPassenger(tag);
            tag.remove("Leash");
            this.ants.add(new Ant(tag, 0, 2400));

            if (this.ants.size() < 10)
            {
                // TODO decide on if we should breed ants here.

            }
            mob.remove();
            return true;
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            if (!AntTasks.isValidAnt(mob)) return false;
            return this.ants.size() < 20;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.put("Ants", this.getAnts());
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.ants.clear();
            final ListNBT listnbt = nbt.getList("Ants", 10);
            for (int i = 0; i < listnbt.size(); ++i)
            {
                final CompoundNBT compoundnbt = listnbt.getCompound(i);
                final Ant beehivetileentity$bee = new Ant(compoundnbt.getCompound("EntityData"), compoundnbt.getInt(
                        "TicksInHive"), compoundnbt.getInt("MinOccupationTicks"));
                this.ants.add(beehivetileentity$bee);
            }
        }

        public ListNBT getAnts()
        {
            final ListNBT listnbt = new ListNBT();
            for (final Ant ant : this.ants)
            {
                ant.entityData.remove("UUID");
                final CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.put("EntityData", ant.entityData);
                compoundnbt.putInt("TicksInHive", ant.ticksInHive);
                compoundnbt.putInt("MinOccupationTicks", ant.minOccupationTicks);
                listnbt.add(compoundnbt);
            }
            return listnbt;
        }

        @Override
        public ResourceLocation getKey()
        {
            return AntTasks.NESTLOC;
        }

        public static class Ant
        {
            public final CompoundNBT entityData;

            public int       ticksInHive;
            public final int minOccupationTicks;

            private Ant(final CompoundNBT nbt, final int ticksInHive, final int minOccupationTicks)
            {
                this.entityData = nbt;
                this.ticksInHive = ticksInHive;
                this.minOccupationTicks = minOccupationTicks;
            }
        }
    }
}
