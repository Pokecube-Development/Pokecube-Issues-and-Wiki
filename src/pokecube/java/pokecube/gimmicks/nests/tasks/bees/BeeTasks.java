package pokecube.gimmicks.nests.tasks.bees;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BeeTasks
{
    public static final Predicate<IPokemob> isBee = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isBee = entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS);
        // Only care about bees
        if (!isBee) return false;
        // Only process stock pokemobs
        return pokemob.getPokedexEntry().stock;
    };

    public static AIRoutine BEEAI = new AIRoutine("BEEAI", true, isBee);

    public static final Supplier<MemoryModuleType<GlobalPos>> HIVE_POS = MemoryModules.NEST_POS;
    public static final Supplier<MemoryModuleType<GlobalPos>> FLOWER_POS = MemoryModules.WORK_POS;

    public static final Supplier<MemoryModuleType<Integer>> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final Supplier<MemoryModuleType<Integer>> NO_HIVE_TIMER = MemoryModules.NO_NEST_TIMER;
    public static final Supplier<MemoryModuleType<Integer>> NO_FLOWER_TIME = MemoryModules.NO_WORK_TIMER;

    public static final Supplier<MemoryModuleType<Boolean>> HAS_NECTAR;

    public static final Supplier<SensorType<HiveSensor>> HIVE_SENSOR;
    public static final Supplier<SensorType<FlowerSensor>> FLOWER_SENSOR;

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

    private static List<SensorType<?>> getSensors()
    {
        return List.of(BeeTasks.HIVE_SENSOR.get(), BeeTasks.FLOWER_SENSOR.get(), Sensors.VISIBLE_BLOCKS.get());
    }

    private static void addTasks(final IPokemob pokemob, final List<IAIRunnable> list,
            Map<String, IAIRunnable> namedTasks)
    {
        if (!pokemob.getEntity().getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) return;
        // Gather Nectar from found flower
        list.add(new GatherNectar());
        // Return to hive with the Nectar from the flower
        list.add(new EnterHive());
        // Locate and update our current hive, run this one last, as it clears
        // hive status
        list.add(new CheckHive().setPriority(200));
        // Try to make a hive if we don't have one for too long
        list.add(new MakeHive());

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
            return home.orElse(null);
        }

        @Override
        public void onExitHabitat()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemoryValue(BeeTasks.HAS_NECTAR.get())) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR.get());
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            final IPokemob pokemob = PokemobCaps.getPokemobFor(this.bee);
            if (pokemob != null && nectar) pokemob.eat(BeeTasks.class);
            brain.eraseMemory(BeeTasks.HAS_NECTAR.get());
        }

        @Override
        public GlobalPos getWorkSite()
        {
            Optional<GlobalPos> flower = BeeTasks.getFlower(this.bee);
            return flower.orElse(null);
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
            IGNORED_BEE_TAGS = ObfuscationReflectionHelper.getPrivateValue(BeehiveBlockEntity.class, null, "IGNORED_BEE_TAGS");
        }

        final BeehiveBlockEntity hive;

        public BeeHabitat(final BlockEntity tile)
        {
            this.hive = (BeehiveBlockEntity) tile;
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
                final Level world = mob.level();
                final BlockState state = world.getBlockState(this.hive.getBlockPos());
                if (state.is(BlockTags.BEEHIVES))
                {
                    final int i = BeehiveBlockEntity.getHoneyLevel(state);
                    if (i < 5)
                    {
                        int j = world.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) --j;
                        world.setBlockAndUpdate(this.hive.getBlockPos(),
                                state.setValue(BeehiveBlock.HONEY_LEVEL, i + j));
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
            this.addOccupant(mob, nectar);
            // If this changed, then we added correctly.

            return num < this.hive.stored.size();
        }

        private void addOccupant(Mob mob, boolean nectar)
        {
            if (this.hive.stored.size() < 3)
            {
                mob.stopRiding();
                mob.ejectPassengers();
                var occ = BeehiveBlockEntity.Occupant.of(mob);
                var data = occ.entityData();
                data = data.update(tag -> tag.putBoolean("HasNectar", nectar));
                occ = new BeehiveBlockEntity.Occupant(data, occ.ticksInHive(), occ.minTicksInHive());
                this.hive.storeBee(occ);
                if (this.hive.getLevel() != null)
                {
                    if (mob instanceof Bee bee && bee.hasSavedFlowerPos()
                            && (this.hive.savedFlowerPos == null || this.hive.getLevel().random.nextBoolean()))
                    {
                        this.hive.savedFlowerPos = bee.getSavedFlowerPos();
                    }
                    BlockPos blockpos = this.hive.getBlockPos();
                    this.hive.getLevel().playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.hive.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, blockpos,
                            GameEvent.Context.of(mob, this.hive.getBlockState()));
                }
                mob.discard();
                // maybe this should use the super.setchanged somehow via manual
                // defining?
                hive.setChanged();
            }
        }

        @Override
        public boolean canEnterHabitat(final Mob mob)
        {
            if (!BeeTasks.isValid(mob)) return false;
            return !this.hive.isFull();
        }
    }
}
