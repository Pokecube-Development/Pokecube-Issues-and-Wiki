package pokecube.core.ai.tasks.bees;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.Tasks;
import pokecube.core.ai.tasks.bees.sensors.FlowerSensor;
import pokecube.core.ai.tasks.bees.sensors.HiveSensor;
import pokecube.core.ai.tasks.bees.tasks.CheckHive;
import pokecube.core.ai.tasks.bees.tasks.EnterHive;
import pokecube.core.ai.tasks.bees.tasks.GatherNectar;
import pokecube.core.ai.tasks.bees.tasks.MakeHive;
import pokecube.core.events.pokemob.InitAIEvent.Init.Type;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
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
        Tasks.register(Type.IDLE, BeeTasks::addTasks);
    }

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(BeeTasks.HIVE_SENSOR.setRegistryName(PokecubeCore.MODID, "bee_hives"));
        event.getRegistry().register(BeeTasks.FLOWER_SENSOR.setRegistryName(PokecubeCore.MODID, "bee_flowers"));
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list)
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

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.BEEAI);
    }

    public static class BeeInhabitor implements IInhabitor
    {
        final MobEntity bee;

        public BeeInhabitor(final MobEntity bee)
        {
            this.bee = bee;
        }

        @Override
        public GlobalPos getHome()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.HIVE_POS)) return null;
            return brain.getMemory(BeeTasks.HIVE_POS).get();
        }

        @Override
        public void onExitHabitat()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.HAS_NECTAR)) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(this.bee);
            if (pokemob != null && nectar) pokemob.eat(ItemStack.EMPTY);
            brain.removeMemory(BeeTasks.HAS_NECTAR);
        }

        @Override
        public GlobalPos getWorkSite()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.FLOWER_POS)) return null;
            return brain.getMemory(BeeTasks.FLOWER_POS).get();
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
            final Brain<?> brain = this.bee.getBrain();
            if (site == null) brain.removeMemory(BeeTasks.FLOWER_POS);
            else brain.setMemory(BeeTasks.FLOWER_POS, site);
        }
    }

    public static class BeeHabitat implements IInhabitable
    {

        final BeehiveTileEntity hive;

        public BeeHabitat(final BeehiveTileEntity tile)
        {
            this.hive = tile;
        }

        @Override
        public void onExitHabitat(final MobEntity mob)
        {
            final Brain<?> brain = mob.getBrain();
            if (!brain.hasMemory(BeeTasks.HAS_NECTAR)) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            if (nectar)
            {
                final World world = mob.getEntityWorld();
                final BlockState state = world.getBlockState(this.hive.getPos());
                if (state.getBlock().isIn(BlockTags.BEEHIVES))
                {
                    final int i = BeehiveTileEntity.getHoneyLevel(state);
                    if (i < 5)
                    {
                        int j = world.rand.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) --j;
                        world.setBlockState(this.hive.getPos(), state.with(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i
                                + j)));
                    }
                }
            }
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            final int num = this.hive.bees.size();
            final Brain<?> brain = mob.getBrain();
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            // Try to enter the hive
            this.hive.tryEnterHive(mob, nectar);
            // If this changed, then we added correctly.
            final boolean added = num < this.hive.bees.size();
            // BeehiveTileEntity checks this boolean directly for if
            // there is nectar in the bee.
            if (added) this.hive.bees.get(num).entityData.putBoolean("HasNectar", nectar);
            return added;
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            if (!BeeTasks.isValid(mob)) return false;
            return !this.hive.isFullOfBees();
        }
    }
}
