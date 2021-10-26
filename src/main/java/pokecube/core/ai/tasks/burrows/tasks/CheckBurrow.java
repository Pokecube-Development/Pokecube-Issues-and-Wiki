package pokecube.core.ai.tasks.burrows.tasks;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
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
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.ai.IAIRunnable;

public class CheckBurrow extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();

    static
    {
        // We use this memory to decide where to put the hive
        CheckBurrow.mems.put(MemoryModules.VISIBLE_BLOCKS, MemoryModuleStatus.VALUE_PRESENT);
    }

    public static int BURROWSPACING = 64;

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
            this.entity.getBrain().eraseMemory(BurrowTasks.BURROW);
            this.entity.getBrain().eraseMemory(BurrowTasks.GOING_HOME);
            this.entity.getBrain().eraseMemory(BurrowTasks.JOB_INFO);

            // We need to do the following:
            //
            // 1. Determine if we are somewhere nice to make a hive
            // 2. Decide on where to place the hive
            // 3. Place the new hive block down

            // Lets see if we can find any leaves to place a hive under
            final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);

            final PointOfInterestManager pois = this.world.getPoiManager();
            final long num = pois.getCountInRange(p -> p == PointsOfInterest.NEST.get(), this.entity.blockPosition(),
                    CheckBurrow.BURROWSPACING, PointOfInterestManager.Status.ANY);

            if (blocks == null || num != 0) return;

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
        }
        else
        {
            // Here we might want to check if the burrow is still valid?
            StoreTask storage = null;
            for (final IAIRunnable run : this.pokemob.getTasks())
                if (run instanceof StoreTask)
                {
                    storage = (StoreTask) run;
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
        this.world.setBlockAndUpdate(pos, PokecubeItems.NESTBLOCK.get().defaultBlockState());
        final TileEntity tile = this.world.getBlockEntity(pos);
        if (!(tile instanceof NestTile)) return false;
        final NestTile nest = (NestTile) tile;
        nest.setWrappedHab(hab);
        nest.addResident(this.pokemob);
        brain.setMemory(BurrowTasks.BURROW, GlobalPos.of(this.world.dimension(), pos));
        brain.eraseMemory(BurrowTasks.NO_HOME_TIMER);
        return true;
    }

    @Override
    public boolean shouldRun()
    {
        // Check this incase the AI is disabled at runtime, say be the owner
        return BurrowTasks.isValid(this.entity);
    }

}
