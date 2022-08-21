package pokecube.core.ai.tasks.burrows.tasks;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants.AIRoutine;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.ai.tasks.burrows.burrow.BurrowHab;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.ai.IAIRunnable;

public class CheckBurrow extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // We use this memory to decide where to put the hive
        CheckBurrow.mems.put(MemoryModules.VISIBLE_BLOCKS.get(), MemoryStatus.VALUE_PRESENT);
    }

    int burrowCheckTimer = -10;

    Burrow burrow = null;

    public CheckBurrow(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.burrowCheckTimer = -10;
    }

    @Override
    public void run()
    {
        if (this.burrowCheckTimer++ < 100) return;

        this.burrowCheckTimer = 0;
        if (this.burrow == null) this.burrow = BurrowSensor.getNest(this.entity).orElse(null);

        if (this.burrow == null)
        {
            // Ensure these are cleared.
            this.entity.getBrain().eraseMemory(MemoryModules.NEST_POS.get());
            this.entity.getBrain().eraseMemory(MemoryModules.GOING_HOME.get());
            this.entity.getBrain().eraseMemory(MemoryModules.JOB_INFO.get());

            // We need to do the following:
            //
            // 1. Determine if we are somewhere nice to make a hive
            // 2. Decide on where to place the hive
            // 3. Place the new hive block down

            // Lets see if we can find any leaves to place a hive under
            final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);

            final PoiManager pois = this.world.getPoiManager();
            final long num = pois.getCountInRange(p -> p == PointsOfInterest.NEST.get(), this.entity.blockPosition(),
                    PokecubeCore.getConfig().nestSpacing, PoiManager.Occupancy.ANY);

            if (blocks == null || num != 0) return;

            // Otherwise on the ground
            final List<NearBlock> surfaces = Lists.newArrayList();
            blocks.forEach(b -> {
                if (b == null) return;
                if (PokecubeTerrainChecker.isTerrain(b.getState())) surfaces.add(b);
            });

            // last we check the terrain
            if (!surfaces.isEmpty())
            {
                final NearBlock block = surfaces.get(0);
                if (this.placeNest(block)) return;
            }
        }
        else
        {
            // Here we might want to check if the burrow is still valid?
            for (final IAIRunnable run : this.pokemob.getTasks()) if (run instanceof StoreTask storage)
            {
                this.pokemob.setRoutineState(AIRoutine.STORE, true);
                storage.storageLoc = this.burrow.nest.getBlockPos();
                storage.berryLoc = this.burrow.nest.getBlockPos();
                break;
            }
        }
    }

    private boolean placeNest(final NearBlock block)
    {
        BlockPos pos = block.getPos();
        // Then pick and make a new burrow.
        final BurrowHab hab = BurrowHab.makeFor(this.pokemob, pos);
        if (hab == null) return false;
        pos = hab.burrow.getCenter();
        final Brain<?> brain = this.entity.getBrain();
        this.world.setBlockAndUpdate(pos, PokecubeItems.NEST.get().defaultBlockState());
        final BlockEntity tile = this.world.getBlockEntity(pos);
        if (!(tile instanceof NestTile nest)) return false;
        nest.setWrappedHab(hab);
        nest.addResident(this.pokemob);
        brain.setMemory(MemoryModules.NEST_POS.get(), GlobalPos.of(this.world.dimension(), pos));
        brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
        return true;
    }

    @Override
    public boolean shouldRun()
    {
        // Check this incase the AI is disabled at runtime, say be the owner
        return BurrowTasks.isValid(this.entity);
    }

}
