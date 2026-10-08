package pokecube.gimmicks.nests.tasks.burrows.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.ai.tasks.utility.StoreItems;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.blocks.NestTile;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import pokecube.gimmicks.nests.tasks.burrows.burrow.BurrowHab;
import pokecube.gimmicks.nests.tasks.burrows.sensors.BurrowSensor;
import pokecube.gimmicks.nests.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Map;

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

    public CheckBurrow()
    {
        super(mems);
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.burrowCheckTimer = -10;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        if (this.burrowCheckTimer++ < 100) return;

        this.burrowCheckTimer = 0;
        if (this.burrow == null) this.burrow = BurrowSensor.getNest(entity).orElse(null);

        if (this.burrow == null)
        {
            // Ensure these are cleared.
            entity.getBrain().eraseMemory(MemoryModules.NEST_POS.get());
            entity.getBrain().eraseMemory(MemoryModules.GOING_HOME.get());
            entity.getBrain().eraseMemory(MemoryModules.JOB_INFO.get());

            // We need to do the following:
            //
            // 1. Determine if we are somewhere nice to make a hive
            // 2. Decide on where to place the hive
            // 3. Place the new hive block down

            // Lets see if we can find any leaves to place a hive under
            final List<NearBlock> blocks = BrainUtils.getNearBlocks(entity);

            final PoiManager pois = level.getPoiManager();
            final long num = pois.getCountInRange(NestTasks.NEST_POI, entity.blockPosition(),
                    NestTasks.config.nestSpacing, PoiManager.Occupancy.ANY);

            if (blocks == null || num != 0) return;

            // Otherwise on the ground
            final List<NearBlock> surfaces = Lists.newArrayList();
            blocks.forEach(b -> {
                if (b == null) return;
                if (PokecubeTerrainChecker.isTerrain(b.state())) surfaces.add(b);
            });

            // last we check the terrain
            if (!surfaces.isEmpty())
            {
                var pokemob = PokemobCaps.getPokemobFor(entity);
                final NearBlock block = surfaces.getFirst();
                this.placeNest(level, pokemob, block);
            }
        }
        else
        {
            var pokemob = PokemobCaps.getPokemobFor(entity);
            var storage = entity.getData(StoreItems.StoreBehaviour.TYPE);
            // Here we might want to check if the burrow is still valid?
            pokemob.setRoutineState(AIRoutine.STORE, true);
            storage.storageLoc = this.burrow.nest.getBlockPos();
            storage.berryLoc = this.burrow.nest.getBlockPos();
        }
    }

    private void placeNest(ServerLevel level, IPokemob pokemob, NearBlock block)
    {
        BlockPos pos = block.pos();
        if (!MoveEventsHandler.canAffectBlock(pokemob, new Vector3(pos), "nest_building")) return;
        // Then pick and make a new burrow.
        final BurrowHab hab = BurrowHab.makeFor(pokemob, pos);
        if (hab == null) return;
        pos = hab.burrow.getCenter();
        final Brain<?> brain = pokemob.getEntity().getBrain();
        level.setBlockAndUpdate(pos, NestTasks.NEST.get().defaultBlockState());
        final BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof NestTile nest)) return;
        nest.setWrappedHab(hab);
        nest.addResident(pokemob);
        brain.setMemory(MemoryModules.NEST_POS.get(), GlobalPos.of(level.dimension(), pos));
        brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        // Check this incase the AI is disabled at runtime, say be the owner
        return BurrowTasks.isValid(entity);
    }

}
