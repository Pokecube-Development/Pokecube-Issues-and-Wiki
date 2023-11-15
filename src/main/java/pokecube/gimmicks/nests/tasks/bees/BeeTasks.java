package pokecube.gimmicks.nests.tasks.bees;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.ai.IInhabitor;
import pokecube.api.ai.TaskAdders;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent.Init.Type;
import pokecube.api.raids.RaidManager;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.gimmicks.nests.tasks.bees.sensors.FlowerSensor;
import pokecube.gimmicks.nests.tasks.bees.sensors.HiveSensor;
import pokecube.gimmicks.nests.tasks.bees.tasks.CheckHive;
import pokecube.gimmicks.nests.tasks.bees.tasks.EnterHive;
import pokecube.gimmicks.nests.tasks.bees.tasks.GatherNectar;
import pokecube.gimmicks.nests.tasks.bees.tasks.MakeHive;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;

public class BeeTasks
{
    public static final Predicate<IPokemob> isBee = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isBee = entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS);
        // Only care about bees
        if (!isBee) return false;
        // Only process stock pokemobs
        if (!pokemob.getPokedexEntry().stock) return false;
        return true;
    };

    public static AIRoutine BEEAI = AIRoutine.create("BEEAI", true, isBee);

    public static final RegistryObject<MemoryModuleType<GlobalPos>> HIVE_POS = MemoryModules.NEST_POS;
    public static final RegistryObject<MemoryModuleType<GlobalPos>> FLOWER_POS = MemoryModules.WORK_POS;

    public static final RegistryObject<MemoryModuleType<Integer>> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final RegistryObject<MemoryModuleType<Integer>> NO_HIVE_TIMER = MemoryModules.NO_NEST_TIMER;
    public static final RegistryObject<MemoryModuleType<Integer>> NO_FLOWER_TIME = MemoryModules.NO_WORK_TIMER;

    public static final RegistryObject<MemoryModuleType<Boolean>> HAS_NECTAR;

    public static final RegistryObject<SensorType<HiveSensor>> HIVE_SENSOR;
    public static final RegistryObject<SensorType<FlowerSensor>> FLOWER_SENSOR;

    static
    {
        // Sensors
        HIVE_SENSOR = PokecubeCore.SENSORS.register("bee_hives", () -> new SensorType<>(HiveSensor::new));
        FLOWER_SENSOR = PokecubeCore.SENSORS.register("bee_flowers", () -> new SensorType<>(FlowerSensor::new));

        // Memories
        HAS_NECTAR = PokecubeCore.MEMORIES.register("bee_has_nectar",
                () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
    }

    public static void init()
    {
        TaskAdders.register(Type.IDLE, BeeTasks::addTasks);
        BeeEventsHandler.init();
        
        RaidManager.BANNEDAI.add(BEEAI);
    }

    private static List<MemoryModuleType<?>> getMemories()
    {
        return List.of(BeeTasks.HIVE_POS.get(), BeeTasks.FLOWER_POS.get(), BeeTasks.OUT_OF_HIVE_TIMER.get(),
                BeeTasks.NO_FLOWER_TIME.get(), BeeTasks.HAS_NECTAR.get(), BeeTasks.NO_HIVE_TIMER.get());
    }

    private static final List<SensorType<?>> getSensors()
    {
        return List.of(BeeTasks.HIVE_SENSOR.get(), BeeTasks.FLOWER_SENSOR.get(), Sensors.VISIBLE_BLOCKS.get());
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list,
            Map<String, IAIRunnable> namedTasks)
    {
        if (!pokemob.getEntity().getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) return;
        // Gather Nectar from found flower
        list.add(new GatherNectar(pokemob));
        // Return to hive with the Nectar from the flower
        list.add(new EnterHive(pokemob));
        // Locate and update our current hive, run this one last, as it clears
        // hive status
        list.add(new CheckHive(pokemob).setPriority(200));
        // Try to make a hive if we don't have one for too long
        list.add(new MakeHive(pokemob));

        BrainUtil.addToBrain(pokemob.getEntity().getBrain(), BeeTasks.getMemories(), BeeTasks.getSensors());
    }

    public static boolean isValid(final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(BeeTasks.BEEAI);
    }

    public static Optional<GlobalPos> getFlower(LivingEntity bee)
    {
        final Brain<?> brain = bee.getBrain();
        if (!brain.hasMemoryValue(BeeTasks.FLOWER_POS.get())) return Optional.empty();
        return brain.getMemory(BeeTasks.FLOWER_POS.get());
    }

    public static Optional<GlobalPos> getHive(LivingEntity bee)
    {
        final Brain<?> brain = bee.getBrain();
        if (!brain.hasMemoryValue(BeeTasks.HIVE_POS.get())) return Optional.empty();
        return brain.getMemory(BeeTasks.HIVE_POS.get());
    }

    public static class BeeInhabitor implements IInhabitor
    {
        final Mob bee;

        public BeeInhabitor(final Mob bee)
        {
            this.bee = bee;
        }

        @Override
        public GlobalPos getHome()
        {
            Optional<GlobalPos> home = BeeTasks.getHive(this.bee);
            return home.isPresent() ? home.get() : null;
        }

        @Override
        public void onExitHabitat()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemoryValue(BeeTasks.HAS_NECTAR.get())) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR.get());
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            final IPokemob pokemob = PokemobCaps.getPokemobFor(this.bee);
            if (pokemob != null && nectar) pokemob.eat(ItemStack.EMPTY);
            brain.eraseMemory(BeeTasks.HAS_NECTAR.get());
        }

        @Override
        public GlobalPos getWorkSite()
        {
            Optional<GlobalPos> flower = BeeTasks.getFlower(this.bee);
            return flower.isPresent() ? flower.get() : null;
        }

        @Override
        public void setWorkSite(final GlobalPos site)
        {
            final Brain<?> brain = this.bee.getBrain();
            if (site == null) brain.eraseMemory(BeeTasks.FLOWER_POS.get());
            else brain.setMemory(BeeTasks.FLOWER_POS.get(), site);
        }
    }

    public static class BeeHabitat implements IInhabitable
    {
        private static final List<String> IGNORED_BEE_TAGS;
        static
        {
            // We copy IGNORED_BEE_TAGS from BeehiveBlockEntity
            IGNORED_BEE_TAGS = ObfuscationReflectionHelper.getPrivateValue(BeehiveBlockEntity.class, null, "f_155129_");
        }

        final BeehiveBlockEntity hive;

        public BeeHabitat(final BeehiveBlockEntity tile)
        {
            this.hive = tile;
        }

        @Override
        public void onExitHabitat(final Mob mob)
        {
            final Brain<?> brain = mob.getBrain();
            if (!brain.hasMemoryValue(BeeTasks.HAS_NECTAR.get())) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR.get());
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            if (nectar)
            {
                final Level world = mob.getLevel();
                final BlockState state = world.getBlockState(this.hive.getBlockPos());
                if (state.is(BlockTags.BEEHIVES))
                {
                    final int i = BeehiveBlockEntity.getHoneyLevel(state);
                    if (i < 5)
                    {
                        int j = world.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) --j;
                        world.setBlockAndUpdate(this.hive.getBlockPos(),
                                state.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
                    }
                }
            }
        }

        @Override
        public boolean onEnterHabitat(final Mob mob)
        {
            final int num = this.hive.stored.size();
            final Brain<?> brain = mob.getBrain();
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR.get());
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();

            // Fix the silly vanilla thing that deletes tags...

            CompoundTag tag = new CompoundTag();
            CompoundTag old = mob.saveWithoutId(new CompoundTag());

            for (String s : IGNORED_BEE_TAGS)
            {
                if (old.contains(s)) tag.put(s, old.get(s));
            }
            mob.getPersistentData().put("__bee_fix__", tag);

            // Try to enter the hive
            this.hive.addOccupant(mob, nectar);
            // If this changed, then we added correctly.
            final boolean added = num < this.hive.stored.size();
            // BeehiveTileEntity checks this boolean directly for if
            // there is nectar in the bee.
            if (added) this.hive.stored.get(num).entityData.putBoolean("HasNectar", nectar);
            return added;
        }

        @Override
        public boolean canEnterHabitat(final Mob mob)
        {
            if (!BeeTasks.isValid(mob)) return false;
            return !this.hive.isFull();
        }
    }
}
