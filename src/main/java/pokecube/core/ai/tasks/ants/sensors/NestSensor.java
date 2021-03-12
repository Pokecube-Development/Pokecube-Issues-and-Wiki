package pokecube.core.ai.tasks.ants.sensors;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestManager.Status;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.nest.AntHabitat;
import pokecube.core.blocks.nests.NestTile;

public class NestSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.NEST_POS, AntTasks.NO_HIVE_TIMER);

    public static int NESTSPACING = 64;

    public static class AntNest
    {
        public final NestTile   nest;
        public final AntHabitat hab;

        public AntNest(final NestTile tile, final AntHabitat hab)
        {
            this.nest = tile;
            this.hab = hab;
        }
    }

    public static Optional<AntNest> getNest(final MobEntity mob)
    {
        final Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryValue(AntTasks.NEST_POS)) return Optional.empty();
        final Optional<GlobalPos> pos_opt = brain.getMemory(AntTasks.NEST_POS);
        if (pos_opt.isPresent())
        {
            final World world = mob.getCommandSenderWorld();
            final GlobalPos pos = pos_opt.get();
            final boolean notHere = pos.dimension() != world.dimension();
            if (notHere || !world.isAreaLoaded(pos.pos(), 0)) return Optional.empty();
            final TileEntity tile = world.getBlockEntity(pos.pos());
            if (tile instanceof NestTile)
            {
                final NestTile nest = (NestTile) tile;
                if (!nest.isType(AntTasks.NESTLOC)) return Optional.empty();
                if (nest.getWrappedHab() instanceof AntHabitat) return Optional.of(new AntNest(nest, (AntHabitat) nest
                        .getWrappedHab()));
            }
        }
        return Optional.empty();
    }

    @Override
    protected void doTick(final ServerWorld worldIn, final MobEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(AntTasks.NEST_POS)) return;

        final PointOfInterestManager pois = worldIn.getPoiManager();
        final BlockPos pos = entityIn.blockPosition();
        final Random rand = new Random();
        final Optional<BlockPos> opt = pois.getRandom(p -> p == PointsOfInterest.NEST.get(), p -> this.validNest(p,
                worldIn, entityIn), Status.ANY, pos, NestSensor.NESTSPACING, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(AntTasks.NO_HIVE_TIMER);
            brain.setMemory(AntTasks.NEST_POS, GlobalPos.of(entityIn.getCommandSenderWorld().dimension(), opt
                    .get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(AntTasks.NO_HIVE_TIMER)) timer = brain.getMemory(AntTasks.NO_HIVE_TIMER).get();
            brain.setMemory(AntTasks.NO_HIVE_TIMER, timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerWorld worldIn, final MobEntity entityIn)
    {
        final TileEntity tile = worldIn.getBlockEntity(p);
        if (!(tile instanceof NestTile)) return false;
        final NestTile nest = (NestTile) tile;
        return nest.isType(AntTasks.NESTLOC);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return NestSensor.MEMS;
    }

}
