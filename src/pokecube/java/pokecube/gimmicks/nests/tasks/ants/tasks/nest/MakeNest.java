package pokecube.gimmicks.nests.tasks.ants.tasks.nest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.blocks.NestTile;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.nest.AntHabitat;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Map;

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

    public MakeNest()
    {
        super(MakeNest.mems);
    }

    private boolean placeNest(ServerLevel level, IPokemob pokemob, final NearBlock b)
    {
        final BlockPos pos = b.pos();
        var entity = pokemob.getEntity();
        if (!MoveEventsHandler.canAffectBlock(pokemob, new Vector3(pos), "nest_building")) return false;
        final PoiManager pois = level.getPoiManager();
        final long num = pois.getCountInRange(NestTasks.NEST_POI, pos, NestTasks.config.nestSpacing,
                PoiManager.Occupancy.ANY);
        if (num > 0) return false;
        final Brain<?> brain = entity.getBrain();
        level.setBlockAndUpdate(pos, NestTasks.NEST.get().defaultBlockState());
        final BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof NestTile nest)) return false;
        nest.setWrappedHab(new AntHabitat(nest));
        nest.addResident(pokemob);
        brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
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
        if (blocks == null) return;

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
            if (this.placeNest(level, pokemob, block)) return;
        }

        final Brain<?> brain = entity.getBrain();
        // partially Reset this if we failed
        brain.setMemory(MemoryModules.NO_NEST_TIMER.get(), 0);

    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        final boolean tameCheck = pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING);
        // Could be disabled by owner at runtime
        if (!AntTasks.isValid(entity)) return false;
        if (!tameCheck) return false;
        final Brain<?> brain = entity.getBrain();
        int timer = 0;
        if (brain.hasMemoryValue(MemoryModules.NO_NEST_TIMER.get()))
            timer = brain.getMemory(MemoryModules.NO_NEST_TIMER.get()).get();
        return timer > 60;
    }

}
