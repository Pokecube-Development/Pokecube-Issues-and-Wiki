package pokecube.gimmicks.nests.tasks.bees.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MakeHive extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // Don't run if we have a hive, we will make one if needed.
        MakeHive.mems.put(BeeTasks.HIVE_POS.get(), MemoryStatus.VALUE_ABSENT);
        // We use this memory to determine how long since we had a hive
        MakeHive.mems.put(BeeTasks.NO_HIVE_TIMER.get(), MemoryStatus.VALUE_PRESENT);
        // We use this memory to decide where to put the hive
        MakeHive.mems.put(MemoryModules.VISIBLE_BLOCKS.get(), MemoryStatus.VALUE_PRESENT);
    }

    public MakeHive()
    {
        super(MakeHive.mems);
    }

    private boolean canPlaceHive(ServerLevel level, NearBlock b, Direction... dirs)
    {
        final BlockState state = Blocks.BEE_NEST.defaultBlockState();
        // We can only place the hive if this would be a valid place to right
        // click with a hive item to place.
        for (final Direction dir : dirs)
        {
            final BlockPos pos = b.getPos().relative(dir);
            final BlockState old = level.getBlockState(pos);
            if (!state.canSurvive(level, pos)) continue;
            if (!old.canBeReplaced()) continue;
            final FluidState fluid = level.getFluidState(pos);
            if (!fluid.isEmpty()) continue;
            return true;
        }
        return false;
    }

    private boolean canPlaceHive(ServerLevel level, NearBlock b, Stream<Direction> directionValues)
    {
        return directionValues.anyMatch(d -> this.canPlaceHive(level, b, d));
    }

    private boolean placeHive(ServerLevel level, Mob entity, NearBlock b, Direction dir)
    {
        if (!this.canPlaceHive(level, b, dir)) return false;
        final BlockPos pos = b.getPos();

        final PoiManager pois = level.getPoiManager();
        final long num = pois.getCountInRange(p -> p.is(PoiTypeTags.BEE_HOME), pos,
                NestTasks.config.nestSpacing, PoiManager.Occupancy.ANY);
        if (num > 0) return false;

        final Brain<?> brain = entity.getBrain();
        level.setBlockAndUpdate(pos.relative(dir), Blocks.BEE_NEST.defaultBlockState());
        brain.eraseMemory(BeeTasks.NO_HIVE_TIMER.get());
        return true;
    }

    @Override
    public void reset(Mob entityIn)
    {
        // NOOP
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        // We need to do the following:
        //
        // 1. Determine if we are somewhere nice to make a hive
        // 2. Decide on where to place the hive
        // 3. Place the new hive block down

        // Lets see if we can find any leaves to place a hive under
        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entity);

        // We will prefer under leaves if possible
        final List<NearBlock> leaves = Lists.newArrayList();
        // Otherwise on the sides of logs
        final List<NearBlock> logs = Lists.newArrayList();
        // Otherwise on the ground
        final List<NearBlock> surfaces = Lists.newArrayList();

        if (blocks != null) blocks.forEach(b -> {
            if (PokecubeTerrainChecker.isLeaves(b.getState()) && this.canPlaceHive(level, b, Direction.DOWN))
                leaves.add(b);
            if (PokecubeTerrainChecker.isWood(b.getState()) && this.canPlaceHive(level, b,
                    Direction.Plane.HORIZONTAL.stream())) logs.add(b);
            if (PokecubeTerrainChecker.isTerrain(b.getState()) && this.canPlaceHive(level, b, Direction.values()))
                surfaces.add(b);
        });

        // First check the leaves
        if (!leaves.isEmpty())
        {
            final NearBlock validLeaf = leaves.getFirst();
            if (!MoveEventsHandler.canAffectBlock(PokemobCaps.getPokemobFor(entity), new Vector3(validLeaf.getPos()),
                    "nest_building")) return;
            this.placeHive(level, entity, validLeaf, Direction.DOWN);
            return;
        }

        // Now we check the logs
        if (!logs.isEmpty())
        {
            final NearBlock validLeaf = logs.getFirst();
            final Stream<Direction> dirs = Direction.Plane.HORIZONTAL.stream();
            final List<Direction> tmp = Lists.newArrayList(dirs.iterator());
            Collections.shuffle(tmp);
            for (final Direction dir : tmp) if (this.placeHive(level, entity, validLeaf, dir)) return;
            return;
        }

        // last we check the terrain
        if (!surfaces.isEmpty())
        {
            final NearBlock validLeaf = surfaces.getFirst();
            final List<Direction> tmp = Lists.newArrayList(Direction.values());
            Collections.shuffle(tmp);
            for (final Direction dir : tmp) if (this.placeHive(level, entity, validLeaf, dir)) return;
            return;
        }

        final Brain<?> brain = entity.getBrain();
        // partially Reset this if we failed
        brain.setMemory(BeeTasks.NO_HIVE_TIMER.get(), 0);

    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        // Test this here incase we had AI added before, and disabled at
        // runtime.
        if (!BeeTasks.isValid(entity)) return false;

        var pokemob = PokemobCaps.getPokemobFor(entity);
        final boolean tameCheck = pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING);
        if (!tameCheck) return false;
        final Brain<?> brain = entity.getBrain();
        int timer = 0;
        if (brain.hasMemoryValue(BeeTasks.NO_HIVE_TIMER.get()))
            timer = brain.getMemory(BeeTasks.NO_HIVE_TIMER.get()).get();
        // This timer is in ticks of the HiveSensor, which is only once per
        // second or so!
        return timer > 60;
    }

}
