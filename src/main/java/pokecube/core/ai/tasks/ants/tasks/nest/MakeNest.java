package pokecube.core.ai.tasks.ants.tasks.nest;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.nest.AntHabitat;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class MakeNest extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we have a hive, we will make one if needed.
        MakeNest.mems.put(AntTasks.NEST_POS, MemoryModuleStatus.VALUE_ABSENT);
        // We use this memory to determine how long since we had a hive
        MakeNest.mems.put(AntTasks.NO_HIVE_TIMER, MemoryModuleStatus.VALUE_PRESENT);
        // We use this memory to decide where to put the hive
        MakeNest.mems.put(MemoryModules.VISIBLE_BLOCKS, MemoryModuleStatus.VALUE_PRESENT);
    }

    public MakeNest(final IPokemob pokemob)
    {
        super(pokemob, MakeNest.mems);
    }

    private boolean placeNest(final NearBlock b)
    {
        final BlockPos pos = b.getPos();
        final Brain<?> brain = this.entity.getBrain();
        this.world.setBlockAndUpdate(pos, PokecubeItems.NESTBLOCK.get().defaultBlockState());
        final TileEntity tile = this.world.getBlockEntity(pos);
        if (!(tile instanceof NestTile)) return false;
        final NestTile nest = (NestTile) tile;
        nest.setWrappedHab(new AntHabitat());
        nest.addResident(this.pokemob);
        brain.eraseMemory(AntTasks.NO_HIVE_TIMER);
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
        if (blocks == null) return;

        // Otherwise on the ground
        final List<NearBlock> surfaces = Lists.newArrayList();
        blocks.forEach(b ->
        {
            if (b == null) return;
            if (PokecubeTerrainChecker.isTerrain(b.getState())) surfaces.add(b);
        });
        // last we check the terrain
        if (!surfaces.isEmpty())
        {
            final NearBlock block = surfaces.get(0);
            if (this.placeNest(block)) return;
        }

        final Brain<?> brain = this.entity.getBrain();
        // partially Reset this if we failed
        brain.setMemory(AntTasks.NO_HIVE_TIMER, 200);

    }

    @Override
    public boolean shouldRun()
    {
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        // Could be disabled by owner at runtime
        if (!AntTasks.isValid(this.entity)) return false;
        if (!tameCheck) return false;
        final Brain<?> brain = this.entity.getBrain();
        int timer = 0;
        if (brain.hasMemoryValue(AntTasks.NO_HIVE_TIMER)) timer = brain.getMemory(AntTasks.NO_HIVE_TIMER).get();
        return timer > 60;
    }

}
