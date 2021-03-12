package pokecube.core.ai.tasks.burrows.sensors;

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
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.ai.tasks.burrows.burrow.BurrowHab;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IInhabitable;

public class BurrowSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(BurrowTasks.BURROW, BurrowTasks.NO_HOME_TIMER);

    public static class Burrow
    {
        public final NestTile nest;

        public final BurrowHab hab;

        public Burrow(final NestTile tile, final BurrowHab hab)
        {
            this.nest = tile;
            this.hab = hab;
        }
    }

    public static Optional<Burrow> getNest(final MobEntity mob)
    {
        final Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryValue(BurrowTasks.BURROW)) return Optional.empty();
        final Optional<GlobalPos> pos_opt = brain.getMemory(BurrowTasks.BURROW);
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
                if (!nest.isType(BurrowTasks.BURROWLOC)) return Optional.empty();
                if (nest.getWrappedHab() instanceof BurrowHab) return Optional.of(new Burrow(nest, (BurrowHab) nest
                        .getWrappedHab()));
            }
        }
        return Optional.empty();
    }

    @Override
    protected void doTick(final ServerWorld worldIn, final MobEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(BurrowTasks.BURROW)) return;

        final PointOfInterestManager pois = worldIn.getPoiManager();
        final BlockPos pos = entityIn.blockPosition();
        final Random rand = new Random();
        final Optional<BlockPos> opt = pois.getRandom(p -> p == PointsOfInterest.NEST.get(), p -> this.validNest(p,
                worldIn, entityIn), Status.ANY, pos, 64, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(BurrowTasks.NO_HOME_TIMER);
            brain.setMemory(BurrowTasks.BURROW, GlobalPos.of(entityIn.getCommandSenderWorld().dimension(), opt
                    .get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(BurrowTasks.NO_HOME_TIMER)) timer = brain.getMemory(BurrowTasks.NO_HOME_TIMER).get();
            brain.setMemory(BurrowTasks.NO_HOME_TIMER, timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerWorld worldIn, final MobEntity entityIn)
    {
        final TileEntity tile = worldIn.getBlockEntity(p);
        if (!(tile instanceof NestTile)) return false;
        final NestTile nest = (NestTile) tile;
        if (!nest.isType(BurrowTasks.BURROWLOC)) return false;
        final IInhabitable habitat = nest.getWrappedHab();
        return habitat.canEnterHabitat(entityIn);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return BurrowSensor.MEMS;
    }

}
