package pokecube.core.ai.tasks.ants.tasks.nest;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.nest.AntHabitat;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class MakeNest extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we have a hive, we will make one if needed.
        MakeNest.mems.put(MemoryModules.NEST_POS.get(), MemoryStatus.VALUE_ABSENT);
        // We use this memory to determine how long since we had a hive
        MakeNest.mems.put(MemoryModules.NO_NEST_TIMER.get(), MemoryStatus.VALUE_PRESENT);
        // We use this memory to decide where to put the hive
        MakeNest.mems.put(MemoryModules.VISIBLE_BLOCKS.get(), MemoryStatus.VALUE_PRESENT);
    }

    public MakeNest(final IPokemob pokemob)
    {
        super(pokemob, MakeNest.mems);
    }

    private boolean placeNest(final NearBlock b)
    {
        final BlockPos pos = b.getPos();
        if (!MoveEventsHandler.canAffectBlock(pokemob, new Vector3(pos), "nest_building")) return false;
        final PoiManager pois = this.world.getPoiManager();
        final long num = pois.getCountInRange(p -> p == PointsOfInterest.NEST.get(), pos,
                PokecubeCore.getConfig().nestSpacing, PoiManager.Occupancy.ANY);
        if (num > 0) return false;
        final Brain<?> brain = this.entity.getBrain();
        this.world.setBlockAndUpdate(pos, PokecubeItems.NEST.get().defaultBlockState());
        final BlockEntity tile = this.world.getBlockEntity(pos);
        if (!(tile instanceof NestTile nest)) return false;
        nest.setWrappedHab(new AntHabitat());
        nest.addResident(this.pokemob);
        brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
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

        final Brain<?> brain = this.entity.getBrain();
        // partially Reset this if we failed
        brain.setMemory(MemoryModules.NO_NEST_TIMER.get(), 0);

    }

    @Override
    public boolean shouldRun()
    {
        final boolean tameCheck = this.pokemob.getOwnerId() == null
                || this.pokemob.getGeneralState(GeneralStates.STAYING);
        // Could be disabled by owner at runtime
        if (!AntTasks.isValid(this.entity)) return false;
        if (!tameCheck) return false;
        final Brain<?> brain = this.entity.getBrain();
        int timer = 0;
        if (brain.hasMemoryValue(MemoryModules.NO_NEST_TIMER.get()))
            timer = brain.getMemory(MemoryModules.NO_NEST_TIMER.get()).get();
        return timer > 60;
    }

}
