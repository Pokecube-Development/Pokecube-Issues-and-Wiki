package pokecube.core.ai.tasks.burrows.sensors;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.ai.tasks.burrows.burrow.BurrowHab;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IInhabitable;
import thut.core.common.ThutCore;

public class BurrowSensor extends Sensor<Mob>
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

    public static Optional<Burrow> getNest(final Mob mob)
    {
        final Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryValue(BurrowTasks.BURROW)) return Optional.empty();
        final Optional<GlobalPos> pos_opt = brain.getMemory(BurrowTasks.BURROW);
        if (pos_opt.isPresent())
        {
            final Level world = mob.getLevel();
            final GlobalPos pos = pos_opt.get();
            final boolean notHere = pos.dimension() != world.dimension();
            if (notHere) return Optional.empty();
            final BlockEntity tile = world.getBlockEntity(pos.pos());
            if (tile instanceof NestTile)
            {
                final NestTile nest = (NestTile) tile;
                if (!nest.isType(BurrowTasks.BURROWLOC)) return Optional.empty();
                if (nest.getWrappedHab() instanceof BurrowHab)
                    return Optional.of(new Burrow(nest, (BurrowHab) nest.getWrappedHab()));
            }
        }
        return Optional.empty();
    }

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(BurrowTasks.BURROW)) return;

        final PoiManager pois = worldIn.getPoiManager();
        final BlockPos pos = entityIn.blockPosition();
        final Random rand = ThutCore.newRandom();
        final Optional<BlockPos> opt = pois.getRandom(p -> p == PointsOfInterest.NEST.get(),
                p -> this.validNest(p, worldIn, entityIn), Occupancy.ANY, pos, 64, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(BurrowTasks.NO_HOME_TIMER);
            brain.setMemory(BurrowTasks.BURROW, GlobalPos.of(entityIn.getLevel().dimension(), opt.get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(BurrowTasks.NO_HOME_TIMER))
                timer = brain.getMemory(BurrowTasks.NO_HOME_TIMER).get();
            brain.setMemory(BurrowTasks.NO_HOME_TIMER, timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerLevel worldIn, final Mob entityIn)
    {
        final BlockEntity tile = worldIn.getBlockEntity(p);
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
