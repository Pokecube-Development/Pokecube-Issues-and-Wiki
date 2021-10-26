package pokecube.core.ai.tasks.bees.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class MakeHive extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we have a hive, we will make one if needed.
        MakeHive.mems.put(BeeTasks.HIVE_POS, MemoryModuleStatus.VALUE_ABSENT);
        // We use this memory to determine how long since we had a hive
        MakeHive.mems.put(BeeTasks.NO_HIVE_TIMER, MemoryModuleStatus.VALUE_PRESENT);
        // We use this memory to decide where to put the hive
        MakeHive.mems.put(MemoryModules.VISIBLE_BLOCKS, MemoryModuleStatus.VALUE_PRESENT);
    }

    public MakeHive(final IPokemob pokemob)
    {
        super(pokemob, MakeHive.mems);
    }

    private boolean canPlaceHive(final NearBlock b, final Direction... dirs)
    {
        final BlockState state = Blocks.BEE_NEST.defaultBlockState();
        // We can only place the hive if this would be a valid place to right
        // click with a hive item to place.
        for (final Direction dir : dirs)
        {
            final BlockPos pos = b.getPos().relative(dir);
            final BlockState old = this.world.getBlockState(pos);
            if (!state.canSurvive(this.world, pos)) continue;
            if (!old.getMaterial().isReplaceable()) continue;
            final FluidState fluid = this.world.getFluidState(pos);
            if (!fluid.isEmpty()) continue;
            return true;
        }
        return false;
    }

    private boolean canPlaceHive(final NearBlock b, final Stream<Direction> directionValues)
    {
        return directionValues.anyMatch(d -> this.canPlaceHive(b, d));
    }

    private boolean placeHive(final NearBlock b, final Direction dir)
    {
        if (!this.canPlaceHive(b, dir)) return false;
        final BlockPos pos = b.getPos();
        final Brain<?> brain = this.entity.getBrain();
        this.world.setBlockAndUpdate(pos.relative(dir), Blocks.BEE_NEST.defaultBlockState());
        brain.eraseMemory(BeeTasks.NO_HIVE_TIMER);
        return true;
    }

    @Override
    public void reset()
    {
        // NOOP
    }

    @Override
    public void run()
    {
        // We need to do the following:
        //
        // 1. Determine if we are somewhere nice to make a hive
        // 2. Decide on where to place the hive
        // 3. Place the new hive block down

        // Lets see if we can find any leaves to place a hive under
        final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);

        // We will prefer under leaves if possible
        final List<NearBlock> leaves = Lists.newArrayList();
        // Otherwise on the sides of logs
        final List<NearBlock> logs = Lists.newArrayList();
        // Otherwise on the ground
        final List<NearBlock> surfaces = Lists.newArrayList();
        blocks.forEach(b ->
        {
            if (PokecubeTerrainChecker.isLeaves(b.getState()) && this.canPlaceHive(b, Direction.DOWN)) leaves.add(b);
            if (PokecubeTerrainChecker.isWood(b.getState()) && this.canPlaceHive(b, Direction.Plane.HORIZONTAL
                    .stream())) logs.add(b);
            if (PokecubeTerrainChecker.isTerrain(b.getState()) && this.canPlaceHive(b, Direction.values())) surfaces
                    .add(b);
        });

        // First check the leaves
        if (!leaves.isEmpty())
        {
            final NearBlock validLeaf = leaves.get(0);
            this.placeHive(validLeaf, Direction.DOWN);
            return;
        }

        // Now we check the logs
        if (!logs.isEmpty())
        {
            final NearBlock validLeaf = logs.get(0);
            final Stream<Direction> dirs = Direction.Plane.HORIZONTAL.stream();
            final List<Direction> tmp = Lists.newArrayList(dirs.iterator());
            Collections.shuffle(tmp);
            for (final Direction dir : tmp)
                if (this.placeHive(validLeaf, dir)) return;
            return;
        }

        // last we check the terrain
        if (!surfaces.isEmpty())
        {
            final NearBlock validLeaf = surfaces.get(0);
            final List<Direction> tmp = Lists.newArrayList(Direction.values());
            Collections.shuffle(tmp);
            for (final Direction dir : tmp)
                if (this.placeHive(validLeaf, dir)) return;
            return;
        }

        final Brain<?> brain = this.entity.getBrain();
        // partially Reset this if we failed
        brain.setMemory(BeeTasks.NO_HIVE_TIMER, 200);

    }

    @Override
    public boolean shouldRun()
    {
        // Test this here incase we had AI added before, and disabled at
        // runtime.
        if (!BeeTasks.isValid(this.entity)) return false;

        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        if (!tameCheck) return false;
        final Brain<?> brain = this.entity.getBrain();
        int timer = 0;
        if (brain.hasMemoryValue(BeeTasks.NO_HIVE_TIMER)) timer = brain.getMemory(BeeTasks.NO_HIVE_TIMER).get();
        return timer > 600;
    }

}
